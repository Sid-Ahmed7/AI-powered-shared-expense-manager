package com.pactole.services;

import com.pactole.common.HashToken;
import com.pactole.entities.Account;
import com.pactole.entities.PasswordResetToken;
import com.pactole.exceptions.Errors;
import com.pactole.repository.AccountRepository;
import com.pactole.repository.PasswordResetTokenRepository;
import com.pactole.services.email.EmailService;
import com.pactole.services.password.PasswordService;
import com.pactole.services.passwordReset.PasswordResetService;
import com.pactole.services.refreshToken.SessionService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock AccountRepository          accountRepository;
    @Mock PasswordResetTokenRepository tokenRepository;
    @Mock PasswordService            passwordService;
    @Mock SessionService             sessionService;
    @Mock EmailService               emailService;

    PasswordResetService service;

    @BeforeEach
    void setUp() {
        service = new PasswordResetService(
            tokenRepository, accountRepository, passwordService,
            sessionService, emailService, "http://localhost:5173"
        );
    }

    // ── requestReset ──────────────────────────────────────────────────────────

    @Test
    void requestReset_unknownEmail_silentlyIgnored() {
        when(accountRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThatNoException().isThrownBy(() -> service.requestReset("unknown@test.com"));
        verifyNoInteractions(emailService, tokenRepository);
    }

    @Test
    void requestReset_deletedAccount_silentlyIgnored() {
        Account deleted = mock(Account.class);
        when(deleted.isDeleted()).thenReturn(true);
        when(accountRepository.findByEmail(any())).thenReturn(Optional.of(deleted));

        assertThatNoException().isThrownBy(() -> service.requestReset("deleted@test.com"));
        verifyNoInteractions(emailService);
    }

    @Test
    void requestReset_activeAccount_deletesOldTokenSavesNewAndSendsEmail() {
        UUID accountId = UUID.randomUUID();
        Account account = mock(Account.class);
        when(account.isDeleted()).thenReturn(false);
        when(account.getId()).thenReturn(accountId);
        when(account.getEmail()).thenReturn("user@test.com");
        when(account.getFullName()).thenReturn("Alice");
        when(accountRepository.findByEmail(any())).thenReturn(Optional.of(account));

        service.requestReset("user@test.com");

        verify(tokenRepository).deleteByAccountId(accountId);
        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(captor.capture());
        assertThat(captor.getValue().getAccountId()).isEqualTo(accountId);
        assertThat(captor.getValue().getTokenHash()).hasSize(64); // SHA-256 hex
        verify(emailService).sendPasswordReset(eq("user@test.com"), eq("Alice"), anyString());
    }

    @Test
    void requestReset_resetLinkContainsRawToken() {
        Account account = mock(Account.class);
        when(account.isDeleted()).thenReturn(false);
        when(account.getId()).thenReturn(UUID.randomUUID());
        when(account.getEmail()).thenReturn("user@test.com");
        when(account.getFullName()).thenReturn("Bob");
        when(accountRepository.findByEmail(any())).thenReturn(Optional.of(account));

        service.requestReset("user@test.com");

        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendPasswordReset(any(), any(), linkCaptor.capture());
        assertThat(linkCaptor.getValue()).startsWith("http://localhost:5173/reset-password?token=");
    }

    // ── resetPassword ─────────────────────────────────────────────────────────

    @Test
    void resetPassword_invalidToken_throwsValidation() {
        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resetPassword("bad-token", "NewPassword1234!"))
            .isInstanceOf(Errors.Validation.class);
    }

    @Test
    void resetPassword_expiredToken_throwsValidation() {
        PasswordResetToken expired = mock(PasswordResetToken.class);
        when(expired.isExpired()).thenReturn(true);
        when(expired.isUsed()).thenReturn(false);
        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> service.resetPassword("expired-token", "NewPassword1234!"))
            .isInstanceOf(Errors.Validation.class);
    }

    @Test
    void resetPassword_usedToken_throwsValidation() {
        PasswordResetToken used = mock(PasswordResetToken.class);
        when(used.isExpired()).thenReturn(false);
        when(used.isUsed()).thenReturn(true);
        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.of(used));

        assertThatThrownBy(() -> service.resetPassword("used-token", "NewPassword1234!"))
            .isInstanceOf(Errors.Validation.class);
    }

    @Test
    void resetPassword_deletedAccount_throwsValidation() {
        PasswordResetToken token = mock(PasswordResetToken.class);
        when(token.isExpired()).thenReturn(false);
        when(token.isUsed()).thenReturn(false);
        when(token.getAccountId()).thenReturn(UUID.randomUUID());
        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token));

        Account deleted = mock(Account.class);
        when(deleted.isDeleted()).thenReturn(true);
        when(accountRepository.findById(any())).thenReturn(Optional.of(deleted));

        assertThatThrownBy(() -> service.resetPassword("valid-token", "NewPassword1234!"))
            .isInstanceOf(Errors.Validation.class);
    }

    @Test
    void resetPassword_validToken_changesPasswordRevokesSessionsMarksUsed() {
        UUID accountId = UUID.randomUUID();
        PasswordResetToken token = mock(PasswordResetToken.class);
        when(token.isExpired()).thenReturn(false);
        when(token.isUsed()).thenReturn(false);
        when(token.getAccountId()).thenReturn(accountId);
        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.of(token));

        Account account = mock(Account.class);
        when(account.isDeleted()).thenReturn(false);
        when(account.getId()).thenReturn(accountId);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(passwordService.encodePassword(any())).thenReturn("hashed");

        service.resetPassword("valid-token", "NewPassword1234!");

        verify(passwordService).validateNewPassword("NewPassword1234!", account);
        verify(account).changePassword("hashed");
        verify(token).markUsed();
        verify(sessionService).revokeAll(eq(accountId), any(), isNull());
    }

    @Test
    void resetPassword_storedHashMatchesRawToken() {
        String rawToken = "my-test-token";
        String expectedHash = HashToken.hash(rawToken);

        when(tokenRepository.findByTokenHash(expectedHash)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resetPassword(rawToken, "NewPassword1234!"))
            .isInstanceOf(Errors.Validation.class);

        verify(tokenRepository).findByTokenHash(expectedHash);
    }
}
