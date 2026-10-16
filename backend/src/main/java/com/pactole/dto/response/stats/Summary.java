package com.pactole.dto.response.stats;

import java.time.LocalDate;
import java.util.List;

public record Summary(
        LocalDate from,
        LocalDate to,
        long totalCents,
        int expenseCount,
        List<CategoryStat> byCategory,
        List<MemberStat> byMember,
        String biggestExpense,
        long biggestExpenseCents) {}
