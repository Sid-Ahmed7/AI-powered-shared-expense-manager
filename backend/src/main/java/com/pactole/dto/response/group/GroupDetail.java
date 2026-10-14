package com.pactole.dto.response.group;

import java.util.List;
import java.util.UUID;

public record GroupDetail(UUID id, String name, String currency, String inviteCode,
                          UUID myMemberId, boolean owner, List<MemberResponse> members) {}
