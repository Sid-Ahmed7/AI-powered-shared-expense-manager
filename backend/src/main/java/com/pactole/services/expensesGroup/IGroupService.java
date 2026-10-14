package com.pactole.services.expensesGroup;

import java.util.List;
import java.util.UUID;

import com.pactole.entities.ExpenseGroup;
import com.pactole.entities.Member;

public interface IGroupService {
    
    ExpenseGroup create(UUID accountId, String name, String currency, List<String> otherMembers);
    Member addMember(UUID groupId, UUID accountId, String name);
    ExpenseGroup update(UUID groupId, UUID accountId, String name);
    ExpenseGroup regenerateInvite(UUID groupId, UUID accountId);
    void delete(UUID groupId, UUID accountId);
    ExpenseGroup byInviteCode(String code);
    ExpenseGroup join(String code, UUID accountId, UUID claimMemberId);






}
