package com.pactole.mapping;

import java.util.Map;

import com.pactole.enums.Category;

public final class CategoryMappings {

    private CategoryMappings() {}

    public static final Map<String, Category> KEYWORDS = Map.ofEntries(
        Map.entry("course", Category.COURSES),
        Map.entry("supermarche", Category.COURSES),
        Map.entry("alim", Category.COURSES),
        Map.entry("epicerie", Category.COURSES),

        Map.entry("resto", Category.RESTAURANT),
        Map.entry("cafe", Category.RESTAURANT),
        Map.entry("brasserie", Category.RESTAURANT),

        Map.entry("loyer", Category.LOGEMENT),
        Map.entry("locat", Category.LOGEMENT),

        Map.entry("essence", Category.TRANSPORT),
        Map.entry("train", Category.TRANSPORT),
        Map.entry("taxi", Category.TRANSPORT),
        Map.entry("metro", Category.TRANSPORT),
        Map.entry("bus", Category.TRANSPORT),

        Map.entry("hotel", Category.VOYAGE),
        Map.entry("voyage", Category.VOYAGE),
        Map.entry("avion", Category.VOYAGE),
        Map.entry("airbnb", Category.VOYAGE),

        Map.entry("facture", Category.FACTURES),
        Map.entry("abonnement", Category.FACTURES),
        Map.entry("electricite", Category.FACTURES),
        Map.entry("internet", Category.FACTURES),

        Map.entry("medecin", Category.SANTE),
        Map.entry("pharmacie", Category.SANTE),

        Map.entry("cadeau", Category.CADEAUX),

        Map.entry("loisir", Category.LOISIRS),
        Map.entry("sortie", Category.LOISIRS),
        Map.entry("cinema", Category.LOISIRS),
        Map.entry("sport", Category.LOISIRS),

        // ── EN ────────────────────────────────────────────────────────────────
        Map.entry("grocer", Category.COURSES),
        Map.entry("supermarket", Category.COURSES),

        Map.entry("dining", Category.RESTAURANT),

        Map.entry("hous", Category.LOGEMENT),
        Map.entry("rent", Category.LOGEMENT),
        Map.entry("mortgage", Category.LOGEMENT),

        Map.entry("fuel", Category.TRANSPORT),
        Map.entry("uber", Category.TRANSPORT),
        Map.entry("flight", Category.TRANSPORT),

        Map.entry("travel", Category.VOYAGE),
        Map.entry("accomod", Category.VOYAGE),

        Map.entry("bill", Category.FACTURES),
        Map.entry("subscript", Category.FACTURES),
        Map.entry("utility", Category.FACTURES),

        Map.entry("health", Category.SANTE),
        Map.entry("doctor", Category.SANTE),
        Map.entry("pharmacy", Category.SANTE),
        Map.entry("medical", Category.SANTE),

        Map.entry("gift", Category.CADEAUX),

        Map.entry("entertain", Category.LOISIRS),
        Map.entry("leisure", Category.LOISIRS),
        Map.entry("movie", Category.LOISIRS)
    );
}