package com.pactole.dto.response.stats;

import java.util.UUID;

public record MemberStat(UUID memberId, String name, long paidCents, long consumedCents) {}
