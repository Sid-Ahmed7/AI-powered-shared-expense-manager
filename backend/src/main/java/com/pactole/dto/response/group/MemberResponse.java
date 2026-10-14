package com.pactole.dto.response.group;

import java.util.UUID;

public record MemberResponse(UUID id, String name, boolean hasAccount, boolean me) {}
