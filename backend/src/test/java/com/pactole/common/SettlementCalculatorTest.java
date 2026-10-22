package com.pactole.common;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class SettlementCalculatorTest {

    @Test
    void simplify_twoPersonDebt_producesOneTransfer() {
        UUID a = UUID.randomUUID(), b = UUID.randomUUID();
        List<SettlementCalculator.Transfer> result = SettlementCalculator.simplify(Map.of(a, -500L, b, 500L));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).fromMemberId()).isEqualTo(a);
        assertThat(result.get(0).toMemberId()).isEqualTo(b);
        assertThat(result.get(0).amountCents()).isEqualTo(500L);
    }

    @Test
    void simplify_alreadyBalanced_producesNoTransfers() {
        UUID a = UUID.randomUUID();
        List<SettlementCalculator.Transfer> result = SettlementCalculator.simplify(Map.of(a, 0L));
        assertThat(result).isEmpty();
    }

    @Test
    void simplify_threePeople_totalAmountConserved() {
        UUID a = UUID.randomUUID(), b = UUID.randomUUID(), c = UUID.randomUUID();
        List<SettlementCalculator.Transfer> result = SettlementCalculator.simplify(Map.of(a, -300L, b, -100L, c, 400L));
        long totalPaid = result.stream().mapToLong(SettlementCalculator.Transfer::amountCents).sum();
        assertThat(totalPaid).isEqualTo(400L);
    }

    @Test
    void simplify_multipleCreditors_eachDebtorPaysCorrectly() {
        UUID a = UUID.randomUUID(), b = UUID.randomUUID(), c = UUID.randomUUID(), d = UUID.randomUUID();
        List<SettlementCalculator.Transfer> result = SettlementCalculator.simplify(Map.of(a, -250L, b, -250L, c, 250L, d, 250L));
        long totalPaid = result.stream().mapToLong(SettlementCalculator.Transfer::amountCents).sum();
        assertThat(totalPaid).isEqualTo(500L);
        result.forEach(t -> assertThat(t.amountCents()).isPositive());
    }

    @Test
    void simplify_inconsistentSum_throwsIllegalState() {
        UUID a = UUID.randomUUID(), b = UUID.randomUUID();
        assertThatThrownBy(() -> SettlementCalculator.simplify(Map.of(a, -500L, b, 400L))).isInstanceOf(IllegalStateException.class);
    }
}
