package com.pactole.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.pactole.common.MemberAmount;
import com.pactole.entities.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, UUID>, JpaSpecificationExecutor<Expense> {

    List<Expense> findByGroupIdAndAmountCentsAndDateBetween(UUID groupId, long amountCents, LocalDate from, LocalDate to);

    List<Expense> findByGroupIdAndDateBetweenOrderByDateDesc(UUID groupId, LocalDate from, LocalDate to);

    long countByGroupId(UUID groupId);

    List<Expense> findByGroupIdAndCreatedByOrderByDateDesc(UUID groupId, UUID createdBy);

    @Query("SELECT e.payerMemberId AS memberId, SUM(e.amountCents) AS cents FROM Expense e WHERE e.groupId = :groupId GROUP BY e.payerMemberId")
    List<MemberAmount> paidByMember(UUID groupId);

    @Query("SELECT s.memberId AS memberId, SUM(s.amountCents) AS cents FROM Expense e JOIN e.shares s WHERE e.groupId = :groupId GROUP BY s.memberId")
    List<MemberAmount> owedByMember(UUID groupId);
}
