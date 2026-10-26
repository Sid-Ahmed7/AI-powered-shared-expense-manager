package com.pactole.services.passwordReset;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.pactole.common.EmailNormalizer;
import com.pactole.common.HashToken;
import com.pactole.constants.CommonConstants;
import com.pactole.entities.Account;
import com.pactole.entities.PasswordResetToken;
import com.pactole.enums.RefreshReason;
import com.pactole.exceptions.Errors;
import com.pactole.repository.AccountRepository;
import com.pactole.repository.PasswordResetTokenRepository;
import com.pactole.services.email.EmailService;
import com.pactole.services.password.PasswordService;
import com.pactole.services.refreshToken.SessionService;

import jakarta.transaction.Transactional;

@Service 
public class PasswordResetService implements IPasswordResetService {
        private static final SecureRandom RANDOM = new SecureRandom();

    private final PasswordResetTokenRepository tokenRepository;
    private final AccountRepository accountRepository;
    private final PasswordService passwordService;
    private final SessionService sessionService;
    private final EmailService emailService;
    private final String appUrl;

    public PasswordResetService(PasswordResetTokenRepository tokenRepository, AccountRepository accountRepository, PasswordService passwordService, SessionService sessionService, EmailService emailService, @Value("${pactole.app-url}") String appUrl) {
        this.tokenRepository = tokenRepository;
        this.accountRepository = accountRepository;
        this.passwordService = passwordService;
        this.sessionService = sessionService;
        this.emailService = emailService;
        this.appUrl = appUrl;
    }

    @Override
    @Transactional 
    public void requestReset(String oldEmail) {
        String email = EmailNormalizer.normalizeEmail(oldEmail);

        accountRepository.findByEmail(email).filter(account -> !account.isDeleted()).ifPresent(acc -> {
            tokenRepository.deleteByAccountId(acc.getId());
            byte[] bytes = new byte[32];
            RANDOM.nextBytes(bytes);
            String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            PasswordResetToken token = new PasswordResetToken(acc.getId(), HashToken.hash(rawToken), Instant.now().plus(CommonConstants.TTL_HOURS, ChronoUnit.HOURS));
            tokenRepository.save(token);
            String resetLink = appUrl + "/reset-password?token=" + rawToken;
            emailService.sendPasswordReset(acc.getEmail(), acc.getFullName(), resetLink);
        });

    }

    @Override
    @Transactional 
    public void resetPassword(String token, String newPassword) {
        String hash = HashToken.hash(token);
        PasswordResetToken  passwordToken = tokenRepository.findByTokenHash(hash).orElseThrow(() -> new Errors.Validation("auth.reset.token.invalid"));

        if (passwordToken.isExpired() || passwordToken.isUsed()) {
            throw new Errors.Validation("auth.reset.token.expired");
        }

        Account account = accountRepository.findById(passwordToken.getAccountId()).filter(acc -> !acc.isDeleted()).orElseThrow(() -> new Errors.Validation("auth.reset.token.invalid"));

        passwordService.validateNewPassword(newPassword, account);
        account.changePassword(passwordService.encodePassword(newPassword));
        passwordToken.markUsed();

        sessionService.revokeAll(account.getId(), RefreshReason.PASSWORD_RESET, null);
    }


    
}
