package com.pactole.builder.expenseGroup;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.pactole.entities.ExpenseGroup;

@Component
public class ExpenseGroupBuilder {

    public ExpenseGroup build(String name, String currency, String inviteCode, UUID createdBy) {
        return ExpenseGroup.builder()
                .name(name)
                .currency(currency)
                .inviteCode(inviteCode)
                .createdBy(createdBy)
                .build();
    }
}
