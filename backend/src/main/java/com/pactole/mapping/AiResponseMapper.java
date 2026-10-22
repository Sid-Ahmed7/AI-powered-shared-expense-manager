package com.pactole.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.pactole.common.AICommon;
import com.pactole.dto.request.expense.SplitValueRequest;
import com.pactole.dto.response.ai.AskResponse;
import com.pactole.dto.response.ai.ExpenseDraft;
import com.pactole.dto.response.ai.Recap;
import com.pactole.dto.response.ai.ReceiptDraft;
import com.pactole.dto.response.ai.ReceiptItem;
import com.pactole.dto.response.ai.ReminderResponse;
import com.pactole.enums.Category;
import com.pactole.enums.SplitMode;

@Component
public class AiResponseMapper {

    public ExpenseDraft toExpenseDraft(JsonNode node) {
        return new ExpenseDraft(
            AICommon.text(node.get("description")),
            AICommon.money(node.get("amount")),
            null,
            toCategory(node.get("category")),
            AICommon.date(node.get("date")),
            toSplitMode(node.get("splitMode")),
            toSplits(node.get("splits")),
            AICommon.strings(node.get("warnings")),
            AICommon.strings(node.get("questions")));
    }

    public ReceiptDraft toReceiptDraft(JsonNode node) {
        return new ReceiptDraft(
            AICommon.text(node.get("merchant")),
            AICommon.date(node.get("date")),
            AICommon.money(node.get("total")),
            toCategory(node.get("category")),
            toReceiptItems(node.get("items")),
            AICommon.strings(node.get("warnings")));
    }

    public Recap toRecap(JsonNode node) {
        return new Recap(
                AICommon.text(node.get("title")),
                AICommon.text(node.get("summary")),
                AICommon.strings(node.get("insights")),
                AICommon.text(node.get("tip")));
    }

    public ReminderResponse toReminderResponse(JsonNode node) {
        return new ReminderResponse(AICommon.text(node.get("message")));
    }

    public AskResponse toAskResponse(JsonNode node) {
        return new AskResponse(
                AICommon.text(node.get("content")),
                AICommon.strings(node.get("toolsUsed")));
    }

    private Category toCategory(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) return null;
        try {
            return Category.valueOf(node.asText().strip().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private SplitMode toSplitMode(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) return SplitMode.EQUAL;
        try {
            return SplitMode.valueOf(node.asText().strip().toUpperCase());
        } catch (IllegalArgumentException e) {
            return SplitMode.EQUAL;
        }
    }

    private List<SplitValueRequest> toSplits(JsonNode node) {
        List<SplitValueRequest> list = new ArrayList<>();
        if (node == null || !node.isArray()) return list;
        node.forEach(item -> {
            String memberId = AICommon.text(item.get("memberId"));
            if (memberId != null) {
                try {
                    list.add(new SplitValueRequest(UUID.fromString(memberId), AICommon.money(item.get("value"))));
                } catch (IllegalArgumentException ignored) {}
            }
        });
        return list;
    }

    private List<ReceiptItem> toReceiptItems(JsonNode node) {
        List<ReceiptItem> list = new ArrayList<>();
        if (node == null || !node.isArray()) return list;
        node.forEach(item -> list.add(new ReceiptItem(
                AICommon.text(item.get("label")),
                AICommon.money(item.get("amount")))));
        return list;
    }
}
