package com.pactole.dto.response.token;

import com.pactole.dto.response.account.AccountResponse;

import lombok.Builder;

@Builder 
public record TokenResponse(String accessToken, long expiresIn, AccountResponse account) {}
