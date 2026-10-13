package com.pactole.dto.response.token;

public record AccessTokenResponse(String value, long expiresInSeconds) {}
