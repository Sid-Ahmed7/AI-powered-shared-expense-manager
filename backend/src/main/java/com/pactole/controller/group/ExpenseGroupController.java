package com.pactole.controller.group;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.pactole.common.CurrentUser;
import com.pactole.dto.request.group.AddMemberRequest;
import com.pactole.dto.request.group.CreateGroupRequest;
import com.pactole.dto.request.group.JoinRequest;
import com.pactole.dto.request.group.UpdateGroupRequest;
import com.pactole.dto.response.group.GroupDetail;
import com.pactole.dto.response.group.GroupSummary;
import com.pactole.dto.response.group.JoinPreview;
import com.pactole.dto.response.group.MemberResponse;
import com.pactole.entities.ExpenseGroup;
import com.pactole.repository.ExpenseGroupRepository;
import com.pactole.repository.ExpenseRepository;
import com.pactole.repository.MemberRepository;
import com.pactole.services.balance.BalanceService;
import com.pactole.services.expensesGroup.GroupAccess;
import com.pactole.services.expensesGroup.GroupService;
import com.pactole.services.expensesGroup.IGroupAccess;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class ExpenseGroupController {

    private final GroupService service;
    private final GroupAccess access;
    private final ExpenseGroupRepository expenseGroupRepository;
    private final MemberRepository memberRepository;
    private final ExpenseRepository expenseRepository;
    private final BalanceService balanceService;

    public ExpenseGroupController(GroupService service, GroupAccess access, ExpenseGroupRepository expenseGroupRepository, MemberRepository memberRepository, ExpenseRepository expenseRepository, BalanceService balanceService) {
        this.service = service;
        this.access = access;
        this.expenseGroupRepository = expenseGroupRepository;
        this.memberRepository = memberRepository;
        this.expenseRepository = expenseRepository;
        this.balanceService = balanceService;
    }

    @GetMapping("/groups")
    @Transactional(readOnly = true)
    public List<GroupSummary> list(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = CurrentUser.id(jwt);
        return expenseGroupRepository.findAllForUser(userId).stream().map(g -> {
                    IGroupAccess.Context ctx = access.context(g.getId(), userId);
                    long expenseCount = expenseRepository.countByGroupId(g.getId());
                    long myBalance = balanceService.netOf(ctx, ctx.me());
                    long totalSpent = expenseRepository.totalSpentByGroup(g.getId());
                    return new GroupSummary(g.getId(), g.getName(), g.getCurrency(), ctx.members().size(), expenseCount, myBalance, totalSpent);
                }).toList();
    }

    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupDetail create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateGroupRequest request) {
        UUID userId = CurrentUser.id(jwt);
        ExpenseGroup group = service.create(userId, request.getName(), request.getCurrency(), request.getMembers());
        return detail(group.getId(), userId);
    }

    @GetMapping("/groups/{groupId}")
    public GroupDetail get(@PathVariable UUID groupId, @AuthenticationPrincipal Jwt jwt) {
        return detail(groupId, CurrentUser.id(jwt));
    }

    @PatchMapping("/groups/{groupId}")
    public GroupDetail update(@PathVariable UUID groupId, @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateGroupRequest request) {
        UUID userId = CurrentUser.id(jwt);
        service.update(groupId, userId, request.getName());
        return detail(groupId, userId);
    }

    @DeleteMapping("/groups/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID groupId, @AuthenticationPrincipal Jwt jwt) {
        service.delete(groupId, CurrentUser.id(jwt));
    }

    @PostMapping("/groups/{groupId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupDetail addMember(@PathVariable UUID groupId, @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AddMemberRequest request) {
        UUID userId = CurrentUser.id(jwt);
        service.addMember(groupId, userId, request.getName());
        return detail(groupId, userId);
    }

    @PostMapping("/groups/{groupId}/invite/regenerate")
    public GroupDetail regenerateInvite(@PathVariable UUID groupId, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = CurrentUser.id(jwt);
        service.regenerateInvite(groupId, userId);
        return detail(groupId, userId);
    }

    @GetMapping("/join/{code}")
    @Transactional(readOnly = true)
    public JoinPreview joinPreview(@PathVariable String code) {
        ExpenseGroup group = service.byInviteCode(code);
        List<MemberResponse> unclaimed = memberRepository.findByGroupIdOrderByIdAsc(group.getId()).stream()
                .filter(m -> m.getAccountId() == null)
                .map(m -> new MemberResponse(m.getId(), m.getName(), false, false))
                .toList();
        return new JoinPreview(group.getName(), unclaimed);
    }

    @PostMapping("/join/{code}")
    public GroupDetail join(@PathVariable String code, @AuthenticationPrincipal Jwt jwt,
                            @RequestBody(required = false) JoinRequest request) {
        UUID userId = CurrentUser.id(jwt);
        ExpenseGroup group = service.join(code, userId, request == null ? null : request.getMemberId());
        return detail(group.getId(), userId);
    }

    @Transactional(readOnly = true)
    GroupDetail detail(UUID groupId, UUID userId) {
        IGroupAccess.Context ctx = access.context(groupId, userId);
        List<MemberResponse> list = ctx.members().stream()
                .map(m -> new MemberResponse(m.getId(), m.getName(), m.getAccountId() != null,
                        m.getId().equals(ctx.me().getId())))
                .toList();
        ExpenseGroup g = ctx.group();
        return new GroupDetail(g.getId(), g.getName(), g.getCurrency(), g.getInviteCode(),
                ctx.me().getId(), access.isOwner(ctx, userId), list);
    }
}
