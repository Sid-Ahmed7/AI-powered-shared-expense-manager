package com.pactole.services.balance;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.pactole.common.MemberAmount;
import com.pactole.common.SettlementCalculator;

import org.springframework.transaction.annotation.Transactional;

import com.pactole.dto.response.group.MemberBalance;
import com.pactole.entities.Member;
import com.pactole.entities.Settlement;
import com.pactole.repository.ExpenseRepository;
import com.pactole.repository.SettlementRepository;
import com.pactole.services.expensesGroup.GroupAccess;

public class BalanceService implements IBalanceService {

    private final ExpenseRepository expenseRepository;
    private final SettlementRepository settlementRepository;

    public BalanceService(ExpenseRepository expenseRepository, SettlementRepository settlementRepository) {
        this.expenseRepository = expenseRepository;
        this.settlementRepository = settlementRepository;
    }

    @Transactional(readOnly = true)
    public Balances compute(GroupAccess.Context ctx) {
        UUID groupId = ctx.group().getId();
        Map<UUID, Long> paidMember = toMap(expenseRepository.paidByMember(groupId));
        Map<UUID, Long> owedMember = toMap(expenseRepository.owedByMember(groupId));
        Map<UUID, Long> settled = new LinkedHashMap<>();
        for (Settlement settlement : settlementRepository.findByGroupIdOrderBySettledOnDescIdDesc(groupId)) {
            settled.merge(settlement.getFromMemberId(), settlement.getAmountCents(), Long::sum);
            settled.merge(settlement.getToMemberId(), -settlement.getAmountCents(), Long::sum);
        }

        List<MemberBalance> list = ctx.members().stream().map(member -> {
            long paid = paidMember.getOrDefault(member.getId(), 0L);
            long owed = owedMember.getOrDefault(member.getId(), 0L);
            long net = paid - owed + settled.getOrDefault(member.getId(), 0L);
            return new MemberBalance(member.getId(), member.getName(), paid, owed, net);
        }).toList();

        Map<UUID, Long> net = new LinkedHashMap<>();
        list.forEach(balance -> net.put(balance.memberId(), balance.netCents()));
        List<SettlementCalculator.Transfer> transfers = SettlementCalculator.simplify(net);

        MemberBalance next = list.stream().min(Comparator.comparingLong(MemberBalance::netCents).thenComparing(MemberBalance::memberId)).orElse(null);
        String reason = next == null ? null : next.netCents() < 0 ? next.name() + " a le solde le plus bas du groupe"
                : "Les comptes sont équilibrés, à vous de voir !";
        return new Balances(list, transfers, next == null ? null : next.memberId(), reason);
    }

    public long netOf(GroupAccess.Context ctx, Member member) {
        return compute(ctx).net().getOrDefault(member.getId(), 0L);
    }

    private static Map<UUID, Long> toMap(List<MemberAmount> rows) {
        Map<UUID, Long> map = new LinkedHashMap<>();
        rows.forEach(r -> map.put(r.getMemberId(), r.getCents()));
        return map;
    }
    
}
