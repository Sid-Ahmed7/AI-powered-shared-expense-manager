package com.pactole.dto.response.group;

import java.util.List;

public record JoinPreview(String groupName, List<MemberResponse> unclaimedMembers) {}
