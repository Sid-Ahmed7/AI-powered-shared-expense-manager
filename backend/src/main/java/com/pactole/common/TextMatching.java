package com.pactole.common;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.pactole.constants.CommonConstants;

public final class TextMatching {

    private TextMatching() {
    }

    public static String normalize(String text) {
        if (text == null) return "";
        String normalized = Normalizer.normalize(text.toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        return normalized.replaceAll(CommonConstants.NORMALIZE_FORM, "");
    }

    public static Optional<UUID> matchMember(String name, Map<UUID, String> members) {
        if (name == null || name.isBlank()) return Optional.empty();
        String n = normalize(name);
        List<UUID> matches = members.entrySet().stream()
                .filter(e -> normalize(e.getValue()).contains(n) || n.contains(normalize(e.getValue())))
                .map(Map.Entry::getKey)
                .toList();
        return matches.size() == 1 ? Optional.of(matches.get(0)) : Optional.empty();
    }

    public static boolean likelyDuplicate(String firstDesc, long firstCents, LocalDate firstDate, String secondDesc, long secondCents, LocalDate secondDate) {
        if (firstCents != secondCents || !firstDate.equals(secondDate)) return false;
        String first = normalize(firstDesc);
        String second = normalize(secondDesc);
        if (first.equals(second)) return true;
        Set<String> firstWords = new HashSet<>(Arrays.asList(first.split("\\s+")));
        Set<String> secondWords = new HashSet<>(Arrays.asList(second.split("\\s+")));
        Set<String> union = new HashSet<>(firstWords);
        union.addAll(secondWords);
        if (union.isEmpty()) return true;
        Set<String> intersection = new HashSet<>(firstWords);
        intersection.retainAll(secondWords);
        return (double) intersection.size() / union.size() >= 0.6;
    }
}
