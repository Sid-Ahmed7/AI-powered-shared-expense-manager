package com.pactole.services.ai;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pactole.ai.MistralClient;
import com.pactole.common.AICommon;
import com.pactole.common.Messages;
import com.pactole.common.SplitCalculator;
import com.pactole.mapping.MistralResponseMapper;
import com.pactole.mapping.StatsMapper;
import com.pactole.dto.response.ai.Recap;
import com.pactole.dto.response.ai.ReminderResponse;
import com.pactole.dto.response.stats.MonthComparison;
import com.pactole.enums.Tone;
import com.pactole.security.PromptGuard;
import com.pactole.services.expensesGroup.IGroupAccess;

@Service
public class AiWriter implements IAiWriter {

    private final MistralClient clientAi;
    private final ObjectMapper mapper;
    private final MistralResponseMapper responseMapper;
    private final StatsMapper statsMapper;
    private final Messages messages;

    public AiWriter(MistralClient clientAi, ObjectMapper mapper, MistralResponseMapper responseMapper, StatsMapper statsMapper, Messages messages) {
        this.clientAi = clientAi;
        this.mapper = mapper;
        this.responseMapper = responseMapper;
        this.statsMapper = statsMapper;
        this.messages = messages;
    }

    @Override
    public Recap recap(IGroupAccess.Context ctx, MonthComparison data) {
        Map<String, Object> facts = statsMapper.toFacts(data);
        String system = """
                Tu rédiges le bilan mensuel d'un groupe de dépenses partagées.
                Ton : chaleureux, un peu d'humour, jamais moralisateur. Français. Tutoiement du groupe.
                Utilise UNIQUEMENT les chiffres fournis, ne calcule rien de nouveau et n'en invente aucun.
                Réponds UNIQUEMENT avec ce JSON :
                {"title": "titre accrocheur (max 8 mots)",
                 "summary": "2 phrases qui résument le mois",
                 "insights": ["3 observations concrètes tirées des chiffres"],
                 "tip": "1 conseil pratique pour le mois prochain"}
                """;
        JsonNode reply = clientAi.chat(clientAi.textModel(), List.of(Map.of("role", "system", "content", system), Map.of("role", "user", "content", "Groupe « " + ctx.group().getName() + " ». Données :\n" + toJson(facts))), List.of(), true, 0.7);
        JsonNode json = AICommon.parse(mapper, reply.path("content").asText());
        Recap response = responseMapper.toRecap(json);
        return new Recap(response.title(), response.summary(), response.insights().stream().limit(4).toList(), response.tip());
    }

    @Override
    public ReminderResponse reminder(String debtor, String creditor, long amountCents, Tone tone, String groupName) {
        Tone ton = tone == null ? Tone.SYMPA : tone;
        String amount = euros(amountCents);
        if (!clientAi.enabled()) {
            return new ReminderResponse(fallback(debtor, creditor, amount, groupName, ton));
        }
        String style = switch (ton) {
            case SYMPA -> "amical et léger";
            case DROLE -> "drôle, avec un jeu de mots ou une petite exagération, jamais vexant";
            case FORMEL -> "poli et neutre";
        };
        String prompt = """
                Écris un court message (2 phrases max) pour rappeler à %s qu'il/elle doit %s à %s
                dans le groupe « %s ». Ton : %s. Le message est envoyé par %s.
                Garde le montant exactement tel quel : %s. Réponds en JSON : {"message": "..."}
                """.formatted(debtor, amount, creditor, groupName, style, creditor, amount);
        try {
            JsonNode reply = clientAi.chat(clientAi.textModel(), List.of(Map.of("role", "user", "content", PromptGuard.clean(prompt))), List.of(), true, 0.9);
            ReminderResponse result = responseMapper.toReminderResponse(AICommon.parse(mapper, reply.path("content").asText()));
            if (result.message() == null || !result.message().contains(amount.replace(" €", ""))) {
                return new ReminderResponse(fallback(debtor, creditor, amount, groupName, ton));
            }
            return result;
        } catch (Exception e) {
            return new ReminderResponse(fallback(debtor, creditor, amount, groupName, ton));
        }
    }

    private String fallback(String debtor, String creditor, String amount, String groupName, Tone tone) {
        return switch (tone) {
            case DROLE -> messages.get("ai.reminder.fallback.drole", debtor, groupName, amount, creditor);
            case FORMEL -> messages.get("ai.reminder.fallback.formel", debtor, groupName, amount, creditor);
            default -> messages.get("ai.reminder.fallback.sympa", debtor, amount, creditor, groupName);
        };
    }

    private static String euros(long cents) {
        return SplitCalculator.format(cents);
    }

    private String toJson(Object value) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (Exception e) {
            return value.toString();
        }
    }
}
