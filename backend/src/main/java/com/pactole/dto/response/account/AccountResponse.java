package com.pactole.dto.response.account;

import java.util.UUID;

import com.pactole.entities.Account;

public record AccountResponse(UUID id, String email, String displayName) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(account.getId(), account.getEmail(), account.getFullName());
    }
}
