package com.pactole.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pactole.entities.Account;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    
    Optional<Account> findByEmail(String email);

    boolean existsByEmail(String email);
}
