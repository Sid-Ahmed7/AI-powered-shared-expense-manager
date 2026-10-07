package com.pactole.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.pactole.entities.RefreshToken;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM RefreshToken t WHERE t.tokenHash = :hash")
    Optional<RefreshToken> findByHashForUpdate(String hash);

    List<RefreshToken> findByFamilyId(UUID familyId);

    @Query("SELECT t FROM RefreshToken t WHERE t.userId = :userId and t.revokedAt is null and t.expiresAt > :now order by t.createdAt desc")
    List<RefreshToken> findActiveByUser(UUID userId, Instant now);

    @Modifying
    @Query("DELETE FROM RefreshToken t WHERE t.expiresAt < :before")
    int deleteExpiredBefore(Instant before);
}
