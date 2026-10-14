package com.pactole.services.expense;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;

import com.pactole.dto.request.expense.ExpenseRequest;
import com.pactole.entities.Expense;
import com.pactole.enums.Category;

public interface IExpenseService {
    Expense createExpense(UUID groupId, UUID accountId, ExpenseRequest request);
    Expense updateExpense(UUID groupId, UUID expenseId, UUID accountId, ExpenseRequest request);
    void deleteExpense(UUID groupId, UUID expenseId, UUID accountId);
    Expense getExpense(UUID groupId, UUID expenseId, UUID accountId);
    Page<Expense> search(UUID groupId, UUID accountId, Category category, UUID memberId, String query, LocalDate from, LocalDate to, int page, int size);
    Specification<Expense> specification(UUID groupId, Category category, UUID memberId, String query, LocalDate from, LocalDate to); 
}
