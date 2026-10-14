package com.pactole.enums;

import java.text.Normalizer;
import java.util.Map;
import com.pactole.mapping.CategoryMappings;


public enum Category {
    COURSES("Courses"),
    RESTAURANT("Restaurant"),
    LOGEMENT("Logement"),
    TRANSPORT("Transport"),
    LOISIRS("Loisirs"),
    VOYAGE("Voyage"),
    FACTURES("Factures"),
    SANTE("Santé"),
    CADEAUX("Cadeaux"),
    AUTRE("Autre");

    private final String label;

    Category(String label) { this.label = label; }

    public String label() { return label; }

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
