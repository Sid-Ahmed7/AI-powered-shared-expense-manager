package com.pactole.builder.settlement;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.pactole.entities.Settlement;

@Component
public class SettlementBuilder {

    public Settlement build(UUID groupId, UUID fromMemberId, UUID toMemberId, long amountCents, LocalDate settledOn, String note, UUID createdBy) {
        return Settlement.builder()
            .groupId(groupId)
            .fromMemberId(fromMemberId)
            .toMemberId(toMemberId)
            .amountCents(amountCents)
            .settledOn(settledOn)
            .note(note)
            .createdBy(createdBy)
            .build();
    }
}
