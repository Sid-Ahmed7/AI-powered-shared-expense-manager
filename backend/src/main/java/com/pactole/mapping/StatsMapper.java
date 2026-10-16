package com.pactole.mapping;

import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.pactole.common.SplitCalculator;
import com.pactole.dto.response.stats.MonthComparison;

@Component
public class StatsMapper {

    public Map<String, Object> toFacts(MonthComparison data) {
        String month = data.month().getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH) + " " + data.month().getYear();
        Map<String, Object> facts = new LinkedHashMap<>();
        facts.put("mois", month);
        facts.put("total", euros(data.current().totalCents()));
        facts.put("nombre_de_depenses", data.current().expenseCount());
        facts.put("evolution_vs_mois_precedent", data.deltaPercent() == null ? "pas de données le mois précédent" : data.deltaPercent() + " %");
        facts.put("par_categorie", data.current().byCategory().stream() .map(category -> category.label() + " : " + euros(category.cents()) + " (" + category.count() + ")").toList());
        facts.put("par_membre", data.current().byMember().stream().map(member -> member.name() + " a payé " + euros(member.paidCents()) + " et consommé " + euros(member.consumedCents())).toList());
        facts.put("plus_grosse_depense", data.current().biggestExpense() == null ? "aucune" : data.current().biggestExpense() + " (" + euros(data.current().biggestExpenseCents()) + ")");
        return facts;
    }

    private static String euros(long cents) {
        return SplitCalculator.format(cents);
    }
}
