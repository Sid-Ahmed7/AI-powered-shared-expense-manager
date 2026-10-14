package com.pactole.services.expensesGroup;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.pactole.entities.Account;
import com.pactole.entities.ExpenseGroup;
import com.pactole.entities.Member;
import com.pactole.exceptions.Errors;
import com.pactole.repository.AccountRepository;
import com.pactole.repository.ExpenseGroupRepository;
import com.pactole.repository.MemberRepository;

@Component 
public class GroupAccess implements IGroupAccess {

    private final ExpenseGroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final AccountRepository accountRepository;

    public GroupAccess(ExpenseGroupRepository groupRepository, MemberRepository memberRepository, AccountRepository accountRepository) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public Member requireMember(UUID groupId, UUID accountId) {
        return memberRepository.findByGroupIdAndAccountId(groupId, accountId).orElseThrow(() -> new Errors.NotFound("Group introuvable"));
    }

    @Override
    public Context requireOwner(UUID groupId, UUID accountId) {
        Context ctx = context(groupId, accountId);
        UUID ownerId = ctx.group().getCreatedBy();
        boolean ownerGone = accountRepository.findById(ownerId).map(Account::isDeleted).orElse(true);
        if (!ownerId.equals(accountId) && !ownerGone) {
            throw new Errors.Forbidden("Seul le créateur du groupe peut faire cette action");
        }
        return ctx;
    }

    @Override
    public boolean isOwner(Context ctx, UUID accountId) {
        return accountId.equals(ctx.group().getCreatedBy());
    }

    @Override
    public Context context(UUID groupId, UUID accountId) {
        Member me = requireMember(groupId, accountId);
        ExpenseGroup group = groupRepository.findById(groupId).orElseThrow(() -> new Errors.NotFound("Groupe introuvable"));
        return new Context(group, me, memberRepository.findByGroupIdOrderByIdAsc(groupId));
    }

    
    

    
}
