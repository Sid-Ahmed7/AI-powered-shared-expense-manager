package com.pactole.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor 
@Entity
@Table(name = "accounts")
public class Account {

    @Id 
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name="password", nullable = false)
    private String password;

    @Column(name="full_name", nullable = false)
    private String fullName;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "failed_login_count", nullable = false)
    private int failedLoginCount;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "password_changed_at", nullable = false)
    private Instant passwordChangedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    
    public Account(String email, String password, String fullName) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.createdAt = Instant.now();
        this.passwordChangedAt = Instant.now();
    }

    public boolean isLocked(Instant now) {
        return lockedUntil != null && lockedUntil.isAfter(now);
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void recordSuccessfulLogin(Instant now) {
        failedLoginCount = 0;
        lockedUntil = null;
        lastLoginAt = now;
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
        this.passwordChangedAt = Instant.now();
    }

    public void changeHash(String newPassword) {
        this.password = newPassword;
    }

    public void anonymizeAccount(String anonymizeHash) {
        this.email = "anonymous-" + id + "@anonymous.com";
        this.password = anonymizeHash;
        this.fullName = "Anonymous";
        this.lastLoginAt = null;
        this.lockedUntil = null;
        this.failedLoginCount = 0;
        this.deletedAt = Instant.now();
    }
}
