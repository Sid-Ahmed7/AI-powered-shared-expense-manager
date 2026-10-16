package com.pactole.dto.response.ai;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.pactole.enums.Category;

public record ReceiptDraft(
        String merchant,
        LocalDate date,
        BigDecimal total,
        Category category,
        List<ReceiptItem> items,
        List<String> warnings) {}
