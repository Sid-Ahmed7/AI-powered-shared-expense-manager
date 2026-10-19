package com.pactole.controller.account;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pactole.common.ClientInfo;
import com.pactole.common.CurrentUser;
import com.pactole.dto.request.account.DeleteAccountRequest;
import com.pactole.dto.request.password.ChangePasswordRequest;
import com.pactole.security.RefreshCookies;
import com.pactole.services.account.AccountService;
import com.pactole.services.refreshToken.ISessionService;
import com.pactole.services.refreshToken.SessionService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController 
@RequestMapping("api/account")
public class AccountController {
    
    private final AccountService accountService;
    private final SessionService sessionService;
    private final RefreshCookies refreshCookies;
    private final CurrentUser currentUser;


    public AccountController(AccountService accountService, SessionService sessionService, RefreshCookies refreshCookies, CurrentUser currentUser) {
        this.accountService = accountService;
        this.sessionService = sessionService;
        this.refreshCookies = refreshCookies;
        this.currentUser = currentUser;
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
}