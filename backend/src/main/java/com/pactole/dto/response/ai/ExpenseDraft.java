package com.pactole.dto.response.ai;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.pactole.dto.request.expense.SplitValueRequest;
import com.pactole.enums.Category;
import com.pactole.enums.SplitMode;

public record ExpenseDraft(
        String description,
        BigDecimal amount,
        UUID payerMemberId,
        Category category,
        LocalDate date,
        SplitMode splitMode,
        List<SplitValueRequest> splits,
        List<String> warnings,
        List<String> questions) {}
