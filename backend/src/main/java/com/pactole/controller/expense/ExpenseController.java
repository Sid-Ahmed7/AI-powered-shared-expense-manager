package com.pactole.controller.expense;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.pactole.common.CurrentUser;
import com.pactole.dto.request.expense.ExpenseRequest;
import com.pactole.dto.response.expense.ExpenseResponse;
import com.pactole.dto.response.expense.PageResponse;
import com.pactole.entities.Expense;
import com.pactole.enums.Category;
import com.pactole.services.expense.ExpenseService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/groups/{groupId}/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public PageResponse<ExpenseResponse> list(@PathVariable UUID groupId, @AuthenticationPrincipal Jwt jwt,
                                              @RequestParam(required = false) Category category,
                                              @RequestParam(required = false) UUID memberId,
                                              @RequestParam(required = false) String q,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "30") int size) {
        Page<Expense> result = expenseService.search(groupId, CurrentUser.id(jwt), category, memberId, q, from, to, page, size);
        return new PageResponse<>(result.getContent().stream().map(ExpenseResponse::from).toList(), result.getTotalElements(), result.getNumber(), result.getSize());
    }

    @GetMapping("/{expenseId}")
    public ExpenseResponse get(@PathVariable UUID groupId, @PathVariable UUID expenseId, @AuthenticationPrincipal Jwt jwt) {
        return ExpenseResponse.from(expenseService.getExpense(groupId, expenseId, CurrentUser.id(jwt)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse create(@PathVariable UUID groupId, @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ExpenseRequest request) {
        return ExpenseResponse.from(expenseService.createExpense(groupId, CurrentUser.id(jwt), request));
    }

    @PutMapping("/{expenseId}")
    public ExpenseResponse update(@PathVariable UUID groupId, @PathVariable UUID expenseId, @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ExpenseRequest request) {
        return ExpenseResponse.from(expenseService.updateExpense(groupId, expenseId, CurrentUser.id(jwt), request));
    }

    @DeleteMapping("/{expenseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID groupId, @PathVariable UUID expenseId, @AuthenticationPrincipal Jwt jwt) {
        expenseService.deleteExpense(groupId, expenseId, CurrentUser.id(jwt));
    }
}
