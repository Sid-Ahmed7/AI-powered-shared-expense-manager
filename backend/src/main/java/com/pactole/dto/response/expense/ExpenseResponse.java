package com.pactole.dto.response.expense;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.pactole.entities.Expense;
import com.pactole.enums.Category;
import com.pactole.enums.Source;
import com.pactole.enums.SplitMode;

public record ExpenseResponse(
        UUID id,
        String description,
        long amountCents,
        UUID payerMemberId,
        Category category,
        String categoryLabel,
        SplitMode splitMode,
        LocalDate date,
        List<ShareResponse> shares,
        List<Expense.Item> items,
        Source source,
        Instant createdAt,
        long version) {

    public static ExpenseResponse from(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getDescription(),
                expense.getAmountCents(),
                expense.getPayerMemberId(),
                expense.getCategory(),
                expense.getCategory().label(),
                expense.getSplitMode(),
                expense.getDate(),
                expense.getShares().stream().map(share -> new ShareResponse(share.getMemberId(), share.getAmountCents(), share.getWeight())).toList(),
                expense.getItems(),
                expense.getSource(),
                expense.getCreatedAt(),
                expense.getVersion());
    }
}
