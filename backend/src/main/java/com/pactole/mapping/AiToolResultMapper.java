package com.pactole.mapping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.pactole.common.SettlementCalculator;
import com.pactole.common.SplitCalculator;
import com.pactole.dto.response.stats.Summary;
import com.pactole.entities.Expense;
import com.pactole.services.balance.IBalanceService;

@Component
public class AiToolResultMapper {

    public Map<String, Object> toBalanceResult(IBalanceService.Balances balances, Map<UUID, String> names) {
        List<Map<String, Object>> members = balances.members().stream().map(member -> Map.<String, Object>of(
                "membre", member.getName(),
                "a_paye", euros(member.getPaidCents()),
                "a_consomme", euros(member.getConsumedCents()),
                "solde", euros(member.getNetCents()))).toList();
        List<Map<String, Object>> transfers = new ArrayList<>();
        for (SettlementCalculator.Transfer transfer : balances.transfers()) {
            transfers.add(Map.of(
                "de", names.getOrDefault(transfer.fromMemberId(), "?"),
                "a", names.getOrDefault(transfer.toMemberId(), "?"),
                "montant", euros(transfer.amountCents())));
        }
        return Map.of("soldes", members, "virements_suggeres", transfers);
    }

    public Map<String, Object> toExpenseRow(Expense expense, Map<UUID, String> names) {
        return Map.<String, Object>of(
                "date", expense.getDate().toString(),
                "description", expense.getDescription(),
                "montant", euros(expense.getAmountCents()),
                "categorie", expense.getCategory().label(),
                "paye_par", names.getOrDefault(expense.getPayerMemberId(), "?"),
                "parts", toShares(expense, names));
    }

    public Map<String, Object> toSummaryResult(Summary summary) {
        return Map.of(
                "total", euros(summary.totalCents()),
                "nombre_de_depenses", summary.expenseCount(),
                "par_categorie", summary.byCategory().stream().map(category -> Map.of("categorie", category.label(), "total", euros(category.cents()), "nombre", category.count())).toList(),
                "par_membre", summary.byMember().stream().map(member -> Map.of("membre", member.name(), "a_paye", euros(member.paidCents()), "a_consomme", euros(member.consumedCents()))).toList(),
                "plus_grosse_depense", summary.biggestExpense() == null ? "aucune" : summary.biggestExpense() + " (" + euros(summary.biggestExpenseCents()) + ")");
    }

    private Map<String, Object> toShares(Expense expense, Map<UUID, String> names) {
        Map<String, Object> map = new LinkedHashMap<>();
        expense.getShares().forEach(share -> map.put(names.getOrDefault(share.getMemberId(), "?"), euros(share.getAmountCents())));
        return map;
    }

    private static String euros(long cents) {
        return SplitCalculator.format(cents);
    }
}