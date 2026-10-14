package com.pactole.dto.response.expense;

import java.math.BigDecimal;
import java.util.UUID;

public record ShareResponse(UUID memberId, long amountCents, BigDecimal weight) {}
