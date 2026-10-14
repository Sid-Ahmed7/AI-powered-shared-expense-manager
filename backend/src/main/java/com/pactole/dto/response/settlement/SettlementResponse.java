package com.pactole.dto.response.settlement;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.pactole.entities.Settlement;

public record SettlementResponse(
        UUID id,
        UUID fromMemberId,
        UUID toMemberId,
        long amountCents,
        LocalDate settledOn,
        String note,
        Instant createdAt) {

    public static SettlementResponse from(Settlement s) {
        return new SettlementResponse(
                s.getId(),
                s.getFromMemberId(),
                s.getToMemberId(),
                s.getAmountCents(),
                s.getSettledOn(),
                s.getNote(),
                s.getCreatedAt());
    }
}
