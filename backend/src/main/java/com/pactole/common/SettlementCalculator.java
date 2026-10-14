package com.pactole.common;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;

public final class SettlementCalculator {

    private SettlementCalculator() {}

    public record Transfer(UUID fromMemberId, UUID toMemberId, long amountCents) {}

    private record Entry(UUID memberId, long amount) {}

    public static List<Transfer> simplify(Map<UUID, Long> netBalances) {
        long sum = netBalances.values().stream().mapToLong(Long::longValue).sum();
        if (sum != 0) {
            throw new IllegalStateException("Soldes incohérents : la somme vaut " + sum + " centimes au lieu de 0");
        }

        Comparator<Entry> biggestFirst = Comparator.comparingLong(Entry::amount).reversed()
                .thenComparing(Entry::memberId);

        PriorityQueue<Entry> creditors = new PriorityQueue<>(biggestFirst);
        PriorityQueue<Entry> debtors = new PriorityQueue<>(biggestFirst);

        netBalances.forEach((id, balance) -> {
            if (balance > 0) creditors.add(new Entry(id, balance));
            else if (balance < 0) debtors.add(new Entry(id, -balance));
        });

        List<Transfer> transfers = new ArrayList<>();
        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            Entry creditor = creditors.poll();
            Entry debtor = debtors.poll();
            long amount = Math.min(creditor.amount(), debtor.amount());
            transfers.add(new Transfer(debtor.memberId(), creditor.memberId(), amount));
            if (creditor.amount() > amount) creditors.add(new Entry(creditor.memberId(), creditor.amount() - amount));
            if (debtor.amount() > amount) debtors.add(new Entry(debtor.memberId(), debtor.amount() - amount));
        }
        return transfers;
    }
}
