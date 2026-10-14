package com.pactole.services.balance;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.pactole.common.SettlementCalculator;
import com.pactole.dto.response.group.MemberBalance;
import com.pactole.entities.Member;
import com.pactole.services.expensesGroup.GroupAccess;

public interface IBalanceService {

      public record Balances(List<MemberBalance> members, List<SettlementCalculator.Transfer> transfers, UUID suggestedNextPayerId, String suggestedNextPayerReason) {
        public Map<UUID, Long> net() {
            Map<UUID, Long> map = new LinkedHashMap<>();
            members.forEach(member -> map.put(member.getMemberId(), member.getNetCents()));
            return map;
        }
    }
     Balances compute(GroupAccess.Context ctx);
    
     long netOf(GroupAccess.Context ctx, Member member);
}
