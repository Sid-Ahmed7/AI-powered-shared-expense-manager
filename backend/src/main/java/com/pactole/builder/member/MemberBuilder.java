package com.pactole.builder.member;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.pactole.entities.Member;

@Component
public class MemberBuilder {

    public Member build(UUID groupId, String name, UUID accountId) {
        return Member.builder()
                .groupId(groupId)
                .name(name)
                .accountId(accountId)
                .build();
    }
}
