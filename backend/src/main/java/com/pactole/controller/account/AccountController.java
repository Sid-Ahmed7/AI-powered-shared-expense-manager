package com.pactole.controller.account;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pactole.common.ClientInfo;
import com.pactole.common.CurrentUser;
import com.pactole.entities.Expense;
import com.pactole.entities.Member;
import com.pactole.dto.request.account.DeleteAccountRequest;
import com.pactole.dto.request.password.ChangePasswordRequest;
import com.pactole.repository.ExpenseGroupRepository;
import com.pactole.repository.ExpenseRepository;
import com.pactole.repository.MemberRepository;
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

    public AccountController(AccountService accountService, SessionService sessionService, RefreshCookies refreshCookies,
                             CurrentUser currentUser, ExpenseGroupRepository groups, MemberRepository members, ExpenseRepository expenses) {
        this.accountService = accountService;
        this.sessionService = sessionService;
        this.refreshCookies = refreshCookies;
        this.currentUser = currentUser;
        this.groups = groups;
        this.members = members;
        this.expenses = expenses;
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
    public List<Object> securityEvents(@AuthenticationPrincipal Jwt jwt) {
        return List.of();
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