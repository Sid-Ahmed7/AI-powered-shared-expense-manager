package com.pactole.controller.account;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pactole.common.ClientInfo;
import com.pactole.common.CurrentUser;
import com.pactole.entities.Account;
import com.pactole.entities.Expense;
import com.pactole.entities.Member;
import com.pactole.entities.RefreshToken;
import com.pactole.dto.request.account.DeleteAccountRequest;
import com.pactole.dto.request.password.ChangePasswordRequest;
import com.pactole.repository.AccountRepository;
import com.pactole.repository.ExpenseGroupRepository;
import com.pactole.repository.ExpenseRepository;
import com.pactole.repository.MemberRepository;
import com.pactole.repository.RefreshTokenRepository;
import com.pactole.security.RefreshCookies;
import com.pactole.services.account.AccountService;
import com.pactole.services.refreshToken.ISessionService;
import com.pactole.services.refreshToken.SessionService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;
    private final SessionService sessionService;
    private final RefreshCookies refreshCookies;
    private final CurrentUser currentUser;
    private final ExpenseGroupRepository groups;
    private final MemberRepository members;
    private final ExpenseRepository expenses;
    private final AccountRepository accounts;
    private final RefreshTokenRepository tokens;

    public AccountController(AccountService accountService, SessionService sessionService, RefreshCookies refreshCookies,
                             CurrentUser currentUser, ExpenseGroupRepository groups, MemberRepository members,
                             ExpenseRepository expenses, AccountRepository accounts, RefreshTokenRepository tokens) {
        this.accountService = accountService;
        this.sessionService = sessionService;
        this.refreshCookies = refreshCookies;
        this.currentUser = currentUser;
        this.groups = groups;
        this.members = members;
        this.expenses = expenses;
        this.accounts = accounts;
        this.tokens = tokens;
    }

    @GetMapping("/sessions")
    public List<ISessionService.SessionView> sessions(@AuthenticationPrincipal Jwt jwt) {
        return sessionService.list(CurrentUser.id(jwt), currentUser.sessionId(jwt));
    }

    @PostMapping("/sessions/revoke-others")
    public Map<String, Integer> revokeOthers(@AuthenticationPrincipal Jwt jwt, HttpServletRequest http) {
        return Map.of("revoked", accountService.revokeOtherSession(CurrentUser.id(jwt), currentUser.sessionId(jwt), ClientInfo.of(http)));
    }

    @PostMapping("/password")
    public Map<String, Integer> changePassword(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ChangePasswordRequest request, HttpServletRequest http) {
        int revoked = accountService.changePassword(CurrentUser.id(jwt), currentUser.sessionId(jwt), request.getCurrentPassword(), request.getNewPassword(), ClientInfo.of(http));
        return Map.of("revokedSessions", revoked);
    }

    @PostMapping("/delete")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody DeleteAccountRequest request, HttpServletRequest http) {
        accountService.deleteAccount(CurrentUser.id(jwt), request.getPassword(), ClientInfo.of(http));
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, refreshCookies.clear()).build();
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> revoke(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID sessionId, HttpServletRequest http) {
        accountService.revokeSession(CurrentUser.id(jwt), sessionId, ClientInfo.of(http));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/security-events")
    @Transactional(readOnly = true)
    public List<ISessionService.SecurityEvent> securityEvents(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = CurrentUser.id(jwt);
        Account account = accounts.findById(userId).orElseThrow();
        Instant since = Instant.now().minus(90, ChronoUnit.DAYS);

        List<RefreshToken> recent = tokens.findRecentByUser(userId, since);

        List<ISessionService.SecurityEvent> events = new ArrayList<>();

        // Account creation
        events.add(new ISessionService.SecurityEvent("REGISTER", account.getCreatedAt(), null));

        // One LOGIN_SUCCESS per session family + revocation events
        Set<UUID> seenFamilies = new HashSet<>();
        for (RefreshToken token : recent) {
            if (seenFamilies.add(token.getFamilyId())) {
                events.add(new ISessionService.SecurityEvent("LOGIN_SUCCESS", token.getFamilyStartedAt(), token.getUserAgent()));
            }
            if (token.getRevokedAt() != null && token.getRevokeReason() != null) {
                String type = switch (token.getRevokeReason()) {
                    case "LOGOUT"           -> "LOGOUT";
                    case "LOGOUT_ALL"       -> "LOGOUT_ALL";
                    case "REUSE_DETECTED"   -> "REFRESH_REUSE_DETECTED";
                    case "PASSWORD_CHANGED" -> "PASSWORD_CHANGED";
                    case "SESSION_REVOKED"  -> "SESSION_REVOKED";
                    case "ACCOUNT_DELETED"  -> null;
                    default                 -> null;
                };
                if (type != null && seenFamilies.contains(token.getFamilyId())) {
                    events.add(new ISessionService.SecurityEvent(type, token.getRevokedAt(), token.getUserAgent()));
                }
            }
        }

        return events.stream()
                .sorted(Comparator.comparing(ISessionService.SecurityEvent::date).reversed())
                .limit(30)
                .toList();
    }

    @GetMapping(value = "/export", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> export(@AuthenticationPrincipal Jwt jwt) {
        UUID accountId = CurrentUser.id(jwt);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("exportedAt", Instant.now());

        data.put("groups", groups.findAllForUser(accountId).stream().map(g -> {
            Member me = members.findByGroupIdAndAccountId(g.getId(), accountId).orElse(null);
            List<Expense> myExpenses = expenses.findByGroupIdAndCreatedByOrderByDateDesc(g.getId(), accountId);
            return Map.of(
                "group", g.getName(),
                "memberName", me != null ? me.getName() : "",
                "expensesCreated", myExpenses.stream().map(e -> Map.of(
                    "date", e.getDate().toString(),
                    "description", e.getDescription(),
                    "amountCents", e.getAmountCents(),
                    "category", e.getCategory().name())).toList());
        }).toList());

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"pactole-mes-donnees.json\"")
            .header(HttpHeaders.CACHE_CONTROL, "no-store")
            .body(data);
    }
}
