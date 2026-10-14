package com.pactole.services.expensesGroup;

import java.security.SecureRandom;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pactole.builder.expenseGroup.ExpenseGroupBuilder;
import com.pactole.builder.member.MemberBuilder;
import com.pactole.constants.CommonConstants;
import com.pactole.entities.Account;
import com.pactole.entities.ExpenseGroup;
import com.pactole.entities.Member;
import com.pactole.exceptions.Errors;
import com.pactole.repository.AccountRepository;
import com.pactole.repository.ExpenseGroupRepository;
import com.pactole.repository.MemberRepository;

@Service
public class GroupService implements IGroupService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final ExpenseGroupRepository expenseGroupRepository;
    private final MemberRepository memberRepository;
    private final AccountRepository accountRepository;
    private final GroupAccess access;
    private final ExpenseGroupBuilder expenseGroupBuilder;
    private final MemberBuilder memberBuilder;

    public GroupService(ExpenseGroupRepository expenseGroupRepository, MemberRepository memberRepository, AccountRepository accountRepository, GroupAccess access, ExpenseGroupBuilder expenseGroupBuilder,MemberBuilder memberBuilder) {
        this.expenseGroupRepository = expenseGroupRepository;
        this.memberRepository = memberRepository;
        this.accountRepository = accountRepository;
        this.access = access;
        this.expenseGroupBuilder = expenseGroupBuilder;
        this.memberBuilder = memberBuilder;
    }

    @Override
    @Transactional
    public ExpenseGroup create(UUID accountId, String name, String currency, List<String> otherMembers) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new Errors.NotFound("Utilisateur introuvable"));
        if (otherMembers != null && otherMembers.size() > CommonConstants.MAX_MEMBERS - 1) {
            throw new Errors.Validation("Un groupe est limité à " + CommonConstants.MAX_MEMBERS + " membres");
        }
        ExpenseGroup group = expenseGroupRepository.save(expenseGroupBuilder.build(
                validName(name),
                currency == null || currency.isBlank() ? "EUR" : currency.toUpperCase(),
                newInviteCode(), accountId));

        memberRepository.save(memberOf(group.getId(), account.getFullName(), accountId));

        LinkedHashSet<String> seen = new LinkedHashSet<>();
        seen.add(account.getFullName().toLowerCase());
        for (String other : otherMembers == null ? List.<String>of() : otherMembers) {
            if (other == null || other.isBlank()) continue;
            String clean = cleanName(other);
            if (seen.add(clean.toLowerCase())) {
                memberRepository.save(memberOf(group.getId(), clean, null));
            }
        }
        return group;
    }

    @Override
    @Transactional
    public Member addMember(UUID groupId, UUID accountId, String name) {
        access.requireMember(groupId, accountId);
        if (memberRepository.countByGroupId(groupId) >= CommonConstants.MAX_MEMBERS) {
            throw new Errors.Validation("Un groupe est limité à " + CommonConstants.MAX_MEMBERS + " membres");
        }
        String clean = cleanName(name);
        if (memberRepository.existsByGroupIdAndNameIgnoreCase(groupId, clean)) {
            throw new Errors.Conflict("Il y a déjà un membre « " + clean + " » dans ce groupe");
        }
        return memberRepository.save(memberOf(groupId, clean, null));
    }

    @Override
    @Transactional
    public ExpenseGroup update(UUID groupId, UUID accountId, String name) {
        IGroupAccess.Context ctx = access.context(groupId, accountId);
        if (name != null && !name.isBlank()) ctx.group().setName(validName(name));
        return ctx.group();
    }

    @Override
    @Transactional
    public ExpenseGroup regenerateInvite(UUID groupId, UUID accountId) {
        IGroupAccess.Context ctx = access.requireOwner(groupId, accountId);
        ctx.group().setInviteCode(newInviteCode());
        return ctx.group();
    }

    @Override
    @Transactional
    public void delete(UUID groupId, UUID accountId) {
        IGroupAccess.Context ctx = access.requireOwner(groupId, accountId);
        expenseGroupRepository.delete(ctx.group());
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseGroup byInviteCode(String code) {
        String normalized = code == null ? "" : code.strip().toUpperCase();
        if (!CommonConstants.INVITE_FORMAT.matcher(normalized).matches()) {
            throw new Errors.NotFound("Code d'invitation invalide");
        }
        return expenseGroupRepository.findByInviteCode(normalized).orElseThrow(() -> new Errors.NotFound("Code d'invitation invalide"));
    }

    @Override
    @Transactional
    public ExpenseGroup join(String code, UUID accountId, UUID claimMemberId) {
        ExpenseGroup group = byInviteCode(code);
        if (memberRepository.findByGroupIdAndAccountId(group.getId(), accountId).isPresent()) {
            return group;
        }
        if (claimMemberId != null) {
            if (memberRepository.claimIfFree(claimMemberId, group.getId(), accountId) == 0) {
                throw new Errors.Conflict("Ce membre n'existe pas ou est déjà associé à un compte");
            }
        } else {
            Account account = accountRepository.findById(accountId).orElseThrow(() -> new Errors.NotFound("Utilisateur introuvable"));
            String name = account.getFullName();
            int suffix = 2;
            while (memberRepository.existsByGroupIdAndNameIgnoreCase(group.getId(), name)) {
                name = account.getFullName() + " " + suffix++;
            }
            memberRepository.save(memberOf(group.getId(), name, accountId));
        }
        return group;
    }

    private Member memberOf(UUID groupId, String name, UUID accountId) {
        return memberBuilder.build(groupId, name, accountId);
    }

    private static String validName(String name) {
        if (name == null || name.isBlank()) throw new Errors.Validation("Nom du groupe requis");
        String trimmed = name.strip();
        if (trimmed.length() > 80) throw new Errors.Validation("Nom du groupe trop long (80 caractères max)");
        return trimmed;
    }

    private static String cleanName(String name) {
        if (name == null || name.isBlank()) throw new Errors.Validation("Prénom requis");
        return name.strip();
    }

    private String newInviteCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 8; i++) {
                sb.append(CommonConstants.CODE_ALPHABET.charAt(RANDOM.nextInt(CommonConstants.CODE_ALPHABET.length())));
            }
            code = sb.toString();
        } while (expenseGroupRepository.existsByInviteCode(code));
        return code;
    }
}
