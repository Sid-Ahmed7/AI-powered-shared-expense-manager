package com.pactole.dto.response.group;

import java.util.UUID;

public record GroupSummary(UUID id, String name, String currency, long memberCount) {}
