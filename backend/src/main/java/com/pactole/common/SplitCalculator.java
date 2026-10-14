package com.pactole.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.pactole.enums.SplitMode;
import com.pactole.exceptions.Errors;

public final class SplitCalculator {

    private SplitCalculator() {}

    public record Input(UUID memberId, BigDecimal value) {}

    public record Item(String label, long amountCents, List<UUID> memberIds) {}

    private record Part(int index, long floor, BigDecimal remainder) {}

    public static Map<UUID, Long> compute(long amountCents, SplitMode mode, List<Input> inputs, List<Item> items) {
        if (amountCents <= 0) {
            throw new Errors.Validation("split.amount.positive");
        }
        return switch (mode) {
            case EQUAL -> equal(amountCents, ids(inputs));
            case EXACT -> exact(amountCents, inputs);
            case PERCENT -> percent(amountCents, inputs);
            case SHARES -> shares(amountCents, inputs);
            case ITEMIZED -> itemized(amountCents, items);
        };
    }

    static Map<UUID, Long> equal(long amountCents, List<UUID> ids) {
        requireParticipants(ids);
        List<BigDecimal> weights = ids.stream().map(id -> BigDecimal.ONE).toList();
        return allocate(amountCents, ids, weights);
    }

    static Map<UUID, Long> exact(long amountCents, List<Input> inputs) {
        requireParticipants(ids(inputs));
        Map<UUID, Long> result = new LinkedHashMap<>();
        long sum = 0;
        for (Input in : inputs) {
            if (in.value() == null || in.value().signum() < 0) {
                throw new Errors.Validation("split.amount.invalid.member", in.memberId());
            }
            long cents = toCents(in.value());
            result.merge(in.memberId(), cents, Long::sum);
            sum += cents;
        }
        if (sum != amountCents) {
            throw new Errors.Validation("split.exact.sum.mismatch", format(sum), format(amountCents));
        }
        return result;
    }

    static Map<UUID, Long> percent(long amountCents, List<Input> inputs) {
        requireParticipants(ids(inputs));
        BigDecimal total = inputs.stream().map(i -> nonNegative(i.value())).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.subtract(BigDecimal.valueOf(100)).abs().compareTo(new BigDecimal("0.01")) > 0) {
            throw new Errors.Validation("split.percent.total", total);
        }
        return allocate(amountCents, ids(inputs), inputs.stream().map(Input::value).toList());
    }

    static Map<UUID, Long> shares(long amountCents, List<Input> inputs) {
        requireParticipants(ids(inputs));
        List<BigDecimal> weights = inputs.stream().map(i -> nonNegative(i.value())).toList();
        if (weights.stream().allMatch(w -> w.signum() == 0)) {
            throw new Errors.Validation("split.shares.positive");
        }
        return allocate(amountCents, ids(inputs), weights);
    }

    static Map<UUID, Long> itemized(long amountCents, List<Item> items) {
        if (items == null || items.isEmpty()) {
            throw new Errors.Validation("split.itemized.empty");
        }
        Map<UUID, Long> subtotal = new LinkedHashMap<>();
        for (Item item : items) {
            if (item.memberIds() == null || item.memberIds().isEmpty()) {
                throw new Errors.Validation("split.item.no.member", item.label());
            }
            if (item.amountCents() < 0) {
                throw new Errors.Validation("split.item.negative.amount", item.label());
            }
            List<UUID> unique = List.copyOf(new LinkedHashSet<>(item.memberIds()));
            equalOrZero(item.amountCents(), unique).forEach((id, cents) -> subtotal.merge(id, cents, Long::sum));
        }
        long itemsTotal = subtotal.values().stream().mapToLong(Long::longValue).sum();
        if (itemsTotal == amountCents) return subtotal;
        if (itemsTotal == 0) return equal(amountCents, List.copyOf(subtotal.keySet()));
        List<UUID> ids = List.copyOf(subtotal.keySet());
        return allocate(amountCents, ids, ids.stream().map(id -> BigDecimal.valueOf(subtotal.get(id))).toList());
    }

    static Map<UUID, Long> allocate(long total, List<UUID> ids, List<BigDecimal> weights) {
        BigDecimal weightSum = weights.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (weightSum.signum() <= 0) {
            throw new Errors.Validation("split.weights.positive");
        }
        BigDecimal bigTotal = BigDecimal.valueOf(total);
        List<Part> parts = new ArrayList<>();
        long allocated = 0;
        for (int i = 0; i < ids.size(); i++) {
            BigDecimal exactShare = bigTotal.multiply(weights.get(i)).divide(weightSum, 10, RoundingMode.HALF_EVEN);
            long floor = exactShare.setScale(0, RoundingMode.FLOOR).longValueExact();
            parts.add(new Part(i, floor, exactShare.subtract(BigDecimal.valueOf(floor))));
            allocated += floor;
        }
        long leftover = total - allocated;
        List<Part> byRemainder = parts.stream().sorted(Comparator.comparing(Part::remainder).reversed().thenComparingInt(Part::index)).toList();
        long[] cents = parts.stream().mapToLong(Part::floor).toArray();
        for (int k = 0; k < leftover; k++) {
            cents[byRemainder.get(k % byRemainder.size()).index()]++;
        }
        Map<UUID, Long> result = new LinkedHashMap<>();
        for (int i = 0; i < ids.size(); i++) {
            result.merge(ids.get(i), cents[i], Long::sum);
        }
        return result;
    }

    private static Map<UUID, Long> equalOrZero(long amount, List<UUID> ids) {
        if (amount == 0) {
            Map<UUID, Long> zero = new LinkedHashMap<>();
            ids.forEach(id -> zero.put(id, 0L));
            return zero;
        }
        return equal(amount, ids);
    }

    public static long toCents(BigDecimal euros) {
        return euros.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact();
    }

    public static String format(long cents, Locale locale) {
        return String.format(locale, "%.2f €", BigDecimal.valueOf(cents).movePointLeft(2));
    }

    public static String format(long cents) {
        return format(cents, Locale.FRENCH);
    }

    private static List<UUID> ids(List<Input> inputs) {
        return inputs == null ? List.of() : inputs.stream().map(Input::memberId).toList();
    }

    private static void requireParticipants(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new Errors.Validation("split.no.participants");
        }
        Set<UUID> unique = new LinkedHashSet<>(ids);
        if (unique.size() != ids.size()) {
            throw new Errors.Validation("split.duplicate.participant");
        }
    }

    private static BigDecimal nonNegative(BigDecimal value) {
        if (value == null || value.signum() < 0) {
            throw new Errors.Validation("split.value.negative");
        }
        return value;
    }
}
