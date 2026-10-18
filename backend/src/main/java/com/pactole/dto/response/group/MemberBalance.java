package com.pactole.dto.response.group;

import java.util.UUID;

public record MemberBalance(
    UUID memberId,
    String name,
    long paidCents,
    long consumedCents,
    long netCents
) {}
