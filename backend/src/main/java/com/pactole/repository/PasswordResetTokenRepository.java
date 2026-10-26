package com.pactole.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;
import java.util.Optional;

import com.pactole.entities.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.accountId = :accountId")
    void deleteByAccountId(UUID accountId);
}
