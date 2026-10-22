package com.pactole.common;

import com.pactole.enums.SplitMode;
import com.pactole.exceptions.Errors;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class SplitCalculatorTest {

    @Test
    void equalSplit_evenAmount_eachMemberGetsExactShare() {
        UUID a = UUID.randomUUID(), b = UUID.randomUUID(), c = UUID.randomUUID();
        Map<UUID, Long> result = SplitCalculator.equal(300, List.of(a, b, c));
        assertThat(result).containsEntry(a, 100L).containsEntry(b, 100L).containsEntry(c, 100L);
    }

    @Test
    void equalSplit_oddAmount_sumEqualsTotal() {
        UUID a = UUID.randomUUID(), b = UUID.randomUUID(), c = UUID.randomUUID();
        Map<UUID, Long> result = SplitCalculator.equal(100, List.of(a, b, c));
        long sum = result.values().stream().mapToLong(Long::longValue).sum();
        assertThat(sum).isEqualTo(100L);
    }

    @Test
    void exactSplit_validInputs_correctAmounts() {
        UUID a = UUID.randomUUID(), b = UUID.randomUUID();
        List<SplitCalculator.Input> inputs = List.of(
            new SplitCalculator.Input(a, new BigDecimal("0.75")),
            new SplitCalculator.Input(b, new BigDecimal("0.25"))
        );
        Map<UUID, Long> result = SplitCalculator.exact(100, inputs);
        assertThat(result).containsEntry(a, 75L).containsEntry(b, 25L);
    }

    @Test
    void exactSplit_sumMismatch_throwsValidation() {
        UUID a = UUID.randomUUID();
        List<SplitCalculator.Input> inputs = List.of(new SplitCalculator.Input(a, new BigDecimal("1.50")));
        assertThatThrownBy(() -> SplitCalculator.exact(100, inputs))
            .isInstanceOf(Errors.Validation.class)
            .extracting(e -> ((Errors.Validation) e).getMessageKey())
            .isEqualTo("split.exact.sum.mismatch");
    }

    @Test
    void percentSplit_sums100Percent_distributesByWeight() {
        UUID a = UUID.randomUUID(), b = UUID.randomUUID();
        List<SplitCalculator.Input> inputs = List.of(
            new SplitCalculator.Input(a, new BigDecimal("70")),
            new SplitCalculator.Input(b, new BigDecimal("30"))
        );
        Map<UUID, Long> result = SplitCalculator.percent(1000, inputs);
        assertThat(result.get(a)).isEqualTo(700L);
        assertThat(result.get(b)).isEqualTo(300L);
    }

    @Test
    void percentSplit_doesNotSum100_throwsValidation() {
        UUID a = UUID.randomUUID(), b = UUID.randomUUID();
        List<SplitCalculator.Input> inputs = List.of(
            new SplitCalculator.Input(a, new BigDecimal("50")),
            new SplitCalculator.Input(b, new BigDecimal("40"))
        );
        assertThatThrownBy(() -> SplitCalculator.percent(1000, inputs))
            .isInstanceOf(Errors.Validation.class)
            .extracting(e -> ((Errors.Validation) e).getMessageKey())
            .isEqualTo("split.percent.total");
    }

    @Test
    void sharesSplit_weightedDistribution_correct() {
        UUID a = UUID.randomUUID(), b = UUID.randomUUID();
        List<SplitCalculator.Input> inputs = List.of(
            new SplitCalculator.Input(a, new BigDecimal("2")),
            new SplitCalculator.Input(b, new BigDecimal("1"))
        );
        Map<UUID, Long> result = SplitCalculator.shares(300, inputs);
        assertThat(result.get(a)).isEqualTo(200L);
        assertThat(result.get(b)).isEqualTo(100L);
    }

    @Test
    void sharesSplit_allZeroWeights_throwsValidation() {
        UUID a = UUID.randomUUID();
        List<SplitCalculator.Input> inputs = List.of(new SplitCalculator.Input(a, BigDecimal.ZERO));
        assertThatThrownBy(() -> SplitCalculator.shares(100, inputs))
            .isInstanceOf(Errors.Validation.class)
            .extracting(e -> ((Errors.Validation) e).getMessageKey())
            .isEqualTo("split.shares.positive");
    }

    @Test
    void itemizedSplit_emptyItems_throwsValidation() {
        assertThatThrownBy(() -> SplitCalculator.itemized(100, List.of()))
            .isInstanceOf(Errors.Validation.class)
            .extracting(e -> ((Errors.Validation) e).getMessageKey())
            .isEqualTo("split.itemized.empty");
    }

    @Test
    void compute_negativeAmount_throwsValidation() {
        UUID a = UUID.randomUUID();
        assertThatThrownBy(() -> SplitCalculator.compute(-100, SplitMode.EQUAL,
                List.of(new SplitCalculator.Input(a, BigDecimal.ONE)), List.of()))
            .isInstanceOf(Errors.Validation.class)
            .extracting(e -> ((Errors.Validation) e).getMessageKey())
            .isEqualTo("split.amount.positive");
    }

    @Test
    void equalSplit_noParticipants_throwsValidation() {
        assertThatThrownBy(() -> SplitCalculator.equal(100, List.of()))
            .isInstanceOf(Errors.Validation.class)
            .extracting(e -> ((Errors.Validation) e).getMessageKey())
            .isEqualTo("split.no.participants");
    }

    @Test
    void equalSplit_duplicateParticipants_throwsValidation() {
        UUID a = UUID.randomUUID();
        assertThatThrownBy(() -> SplitCalculator.equal(100, List.of(a, a)))
            .isInstanceOf(Errors.Validation.class)
            .extracting(e -> ((Errors.Validation) e).getMessageKey())
            .isEqualTo("split.duplicate.participant");
    }

    @Test
    void toCents_correctConversion() {
        assertThat(SplitCalculator.toCents(new BigDecimal("12.50"))).isEqualTo(1250L);
        assertThat(SplitCalculator.toCents(BigDecimal.ONE)).isEqualTo(100L);
        assertThat(SplitCalculator.toCents(BigDecimal.ZERO)).isEqualTo(0L);
    }

    @Test
    void allocate_remainderDistributed_sumEqualsTotal() {
        UUID a = UUID.randomUUID(), b = UUID.randomUUID(), c = UUID.randomUUID();
        List<UUID> ids = List.of(a, b, c);
        List<BigDecimal> weights = List.of(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE);
        Map<UUID, Long> result = SplitCalculator.allocate(10, ids, weights);
        long sum = result.values().stream().mapToLong(Long::longValue).sum();
        assertThat(sum).isEqualTo(10L);
    }
}
