package com.pactole.services.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pactole.common.ClientInfo;
import com.pactole.common.EmailNormalizer;
import com.pactole.common.InputSanitizer;
import com.pactole.config.PactoleProperties;
import com.pactole.constants.CommonConstants;
import com.pactole.dto.response.AuthResponse;
import com.pactole.entities.Account;
import com.pactole.exceptions.Errors;
import com.pactole.repository.AccountRepository;
import com.pactole.security.RateLimiter;
import com.pactole.security.Limits;
import com.pactole.security.PasswordPolicy;
import com.pactole.services.password.PasswordService;
import com.pactole.services.refreshToken.SessionService;

import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService implements IAuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicy passwordPolicy;
    private final SessionService sessionService;
    private final RateLimiter rateLimiter;
    private final Limits limits;
    private final PasswordService passwordService;
    private final String randomHash;

    public AuthService(AccountRepository accountRepository, PasswordEncoder passwordEncoder, PasswordPolicy passwordPolicy, SessionService sessionService, RateLimiter rateLimiter, Limits limits, PactoleProperties properties,PasswordService passwordService) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicy = passwordPolicy;
        this.sessionService = sessionService;
        this.rateLimiter = rateLimiter;
        this.limits = limits;
        this.passwordService = passwordService;
        this.randomHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Transactional
    public AuthResponse register(String email, String password, String fullName, ClientInfo client) {
        String accountEmail = EmailNormalizer.normalizeEmail(email);
        String name = InputSanitizer.clean(fullName, CommonConstants.FIELD_CLEAN);
        passwordPolicy.validate(password, accountEmail, name);

        if(accountRepository.existsByEmail(accountEmail)) {
            throw new Errors.Conflict("Un compte existe déjà avec cet email");
        }
        Account account = accountRepository.save(new Account(accountEmail, passwordService.encodePassword(password), name));
        account.recordSuccessfulLogin(Instant.now());
        SessionService.IssuedSession session = sessionService.start(account.getId(), client);
        return new AuthResponse(account, session);
    }

    @Transactional(dontRollbackOn = Errors.Unauthorized.class)
    public AuthResponse login(String email, String password, ClientInfo client) {
        String accountEmail = EmailNormalizer.normalizeEmail(email);
        long wait = rateLimiter.tryAcquire(limits.loginPerEmail, accountEmail);

        if(wait > 0) {
            throw new Errors.TooManyRequests("Trop de tentatives pour ce compte, réessayez dans " + wait + " s", wait);
        }

        String userPassword = password == null ? "" : password;
        Optional<Account> accountFound = accountRepository.findByEmail(accountEmail).filter(account -> !account.isDeleted());

        if(accountFound.isEmpty()) {
            passwordEncoder.matches(userPassword, randomHash);;
            throw new Errors.TooManyRequests("Trop de tentatives pour ce compte, réessayez dans " + wait + " s", wait);
        }

        Account account = accountFound.get();
        Instant now = Instant.now();
        if (account.isLocked(now)) {
            passwordEncoder.matches(userPassword, randomHash);
            throw new Errors.Unauthorized("Trop de tentatives pour ce compte, réessayez dans " + wait + " s", wait);
        }

        if (!passwordEncoder.matches(userPassword, account.getPassword())) {
        
            throw new Errors.Unauthorized("Trop de tentatives pour ce compte, réessayez dans " + wait + " s", wait);
        }

        account.recordSuccessfulLogin(now);
        if (passwordEncoder.upgradeEncoding(account.getPassword())) {
            account.changeHash(passwordService.encodePassword(userPassword));
        }
        rateLimiter.reset(limits.loginPerEmail, email);
        SessionService.IssuedSession session = sessionService.start(account.getId(), client);
        return new AuthResponse(account, session);
    }

    @Transactional
    public Account activeAccount(UUID accountId) {
        return accountRepository.findById(accountId).filter(account -> !account.isDeleted()).orElseThrow(() -> new Errors.Unauthorized("Session expirée, reconnectez-vous"));
    }
}
