package com.pactole.enums;

import java.text.Normalizer;
import java.util.Map;
import com.pactole.mapping.CategoryMappings;


public enum Category {
    COURSES,
    RESTAURANT,
    LOGEMENT,
    TRANSPORT,
    LOISIRS,
    VOYAGE,
    FACTURES,
    SANTE,
    CADEAUX,
    AUTRE;

    public static Category parse(String value) {
        if (value == null || value.isBlank()) return AUTRE;
        String v = normalize(value);

        for (Category c : values()) {
            if (c.name().equalsIgnoreCase(v)) return c;
        }

        return CategoryMappings.KEYWORDS.entrySet().stream()
                .filter(e -> v.startsWith(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(AUTRE);
    }

    private static String normalize(String s) {
        String nfd = Normalizer.normalize(s.trim().toLowerCase(), Normalizer.Form.NFD);
        return nfd.replaceAll("\\p{M}", "").replaceAll("[^a-z0-9]", "");
    }
}
