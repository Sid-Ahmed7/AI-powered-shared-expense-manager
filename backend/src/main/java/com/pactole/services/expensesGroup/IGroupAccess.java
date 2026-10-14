package com.pactole.services.expensesGroup;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.pactole.entities.ExpenseGroup;
import com.pactole.entities.Member;
import com.pactole.exceptions.Errors;

public interface IGroupAccess {

      public record Context(ExpenseGroup group, Member me, List<Member> members) {
        public Map<UUID, String> names() {
            return members.stream().collect(Collectors.toMap(Member::getId, Member::getName,(a, b) -> a, LinkedHashMap::new));
        }

        public Member member(UUID memberId) {
            return members.stream().filter(m -> memberId.equals(m.getId())).findFirst().orElseThrow(() -> new Errors.Validation("group.member.not.in.group", memberId));
        }
    }

    Member requireMember(UUID groupId, UUID accountId);
    Context requireOwner(UUID groupId, UUID accountId);
    boolean isOwner(Context ctx, UUID userId);
    Context context(UUID groupId, UUID userId);
}
