package com.pactole.controller.auth;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pactole.common.ClientInfo;
import com.pactole.common.CurrentUser;
import com.pactole.dto.request.login.LoginRequest;
import com.pactole.dto.request.register.RegisterRequest;
import com.pactole.dto.response.account.AccountResponse;
import com.pactole.dto.response.auth.AuthResponse;
import com.pactole.dto.response.token.AccessTokenResponse;
import com.pactole.dto.response.token.TokenResponse;
import com.pactole.entities.Account;
import com.pactole.exceptions.Errors;
import com.pactole.security.AccessTokenResponseService;
import com.pactole.security.RefreshCookies;
import com.pactole.services.auth.AuthService;
import com.pactole.services.refreshToken.SessionService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController 
@RequestMapping("/api/auth")
public class AuthController {


    private final AuthService authService;
    private final SessionService sessionService;
    private final AccessTokenResponseService accessTokenResponseService;
    private final RefreshCookies refreshCookies;

    public AuthController(AuthService authService, SessionService sessionService, AccessTokenResponseService accessTokenResponseService, RefreshCookies refreshCookies) {
        this.authService = authService;
        this.sessionService = sessionService;
        this.accessTokenResponseService = accessTokenResponseService;
        this.refreshCookies = refreshCookies;
    }

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest http) {
        AuthResponse response = authService.register(request.getEmail(), request.getPassword(), request.getFullName(), ClientInfo.of(http));
        return respond(HttpStatus.CREATED, response.account(), response.session());
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        AuthResponse response = authService.login(request.getEmail(), request.getPassword(), ClientInfo.of(http));
        return respond(HttpStatus.OK, response.account(), response.session());
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(name = "${pactole.security.refresh.cookie-name}", required = false) String token, HttpServletRequest http) {
        try {
            SessionService.IssuedSession session = sessionService.rotate(token, ClientInfo.of(http));
            return respond(HttpStatus.OK, authService.activeAccount(session.sessionRequest().getUserId()), session);
        } catch (Errors.Unauthorized e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.SET_COOKIE, refreshCookies.clear())
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = "${pactole.security.refresh.cookie-name}", required = false) String token, HttpServletRequest http) {
        sessionService.logout(token);
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, refreshCookies.clear()).build();
    }

    @GetMapping("/me")
        public AccountResponse me(@AuthenticationPrincipal Jwt jwt) {
            return  AccountResponse.from(authService.activeAccount(CurrentUser.id(jwt)));
    }
    
    private ResponseEntity<TokenResponse> respond(HttpStatus status, Account account, SessionService.IssuedSession session) {
        AccessTokenResponse access = accessTokenResponseService.issue(account, session.sessionRequest().getSessionId());
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, refreshCookies.create(session.sessionRequest().getRawRefreshToken(), session.sessionRequest().getExpiresAt()))
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(new TokenResponse(access.value(), access.expiresInSeconds(), AccountResponse.from(account)));
    }
    
}
