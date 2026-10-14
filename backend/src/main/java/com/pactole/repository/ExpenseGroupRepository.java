package com.pactole.repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.pactole.entities.ExpenseGroup;

public interface ExpenseGroupRepository extends JpaRepository<ExpenseGroup, UUID> {
    Optional<ExpenseGroup> findByInviteCode(String inviteCode);
    boolean existsByInviteCode(String inviteCode);
    @Query("SELECT g FROM ExpenseGroup g WHERE g.id in (SELECT m.groupId FROM Member m WHERE m.accountId = :accountId) ORDER BY g.createdAt desc")
    List<ExpenseGroup> findAllForUser(UUID accountId);
}
