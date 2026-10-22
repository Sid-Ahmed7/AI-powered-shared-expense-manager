package com.pactole.services.ai;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pactole.ai.AiClient;
import com.pactole.common.AICommon;
import com.pactole.common.Messages;
import com.pactole.mapping.AiResponseMapper;
import com.pactole.common.TextMatching;
import com.pactole.constants.CommonConstants;
import com.pactole.dto.request.expense.SplitValueRequest;
import com.pactole.dto.response.ai.ExpenseDraft;
import com.pactole.dto.response.ai.ReceiptDraft;
import com.pactole.dto.response.ai.ReceiptItem;
import com.pactole.enums.Category;
import com.pactole.enums.SplitMode;
import com.pactole.exceptions.Errors;
import com.pactole.security.PromptGuard;
import com.pactole.services.expensesGroup.IGroupAccess;

@Service
public class AiFeatures implements IAiFeatures {

    private final AiClient clientAI;
    private final ObjectMapper mapper;
    private final AiResponseMapper responseMapper;
    private final Messages messages;

    public AiFeatures(AiClient clientAI, ObjectMapper mapper, AiResponseMapper responseMapper, Messages messages) {
        this.clientAI = clientAI;
        this.mapper = mapper;
        this.responseMapper = responseMapper;
        this.messages = messages;
    }

    @Override
    public ExpenseDraft parse(IGroupAccess.Context ctx, String text, LocalDate today) {
        String system = """
                Tu transformes une phrase décrivant une dépense partagée en JSON.
                Membres du groupe : %s.
                La personne qui écrit s'appelle %s : « je », « j'ai », « moi » la désignent.
                Aujourd'hui : %s %s. Devise : %s.
                Catégories possibles : %s.

                Réponds UNIQUEMENT avec un objet JSON de cette forme :
                {
                  "description": "texte court",
                  "amount": nombre décimal en euros ou null,
                  "payer": "prénom de celui qui a payé, ou MOI",
                  "participants": ["prénoms"] ou ["TOUS"],
                  "excluded": ["prénoms exclus explicitement"],
                  "splitMode": "EQUAL" | "EXACT" | "SHARES",
                  "splits": [{"name": "prénom", "value": nombre}],
                  "category": "une des catégories",
                  "date": "AAAA-MM-JJ",
                  "questions": ["question si une information indispensable manque"]
                }

                Règles :
                - Par défaut, tout le monde participe à parts égales (participants = ["TOUS"], splitMode = EQUAL).
                - "sauf X" ou "X n'a pas participé" -> X dans excluded.
                - Montants différents par personne -> splitMode EXACT, splits en euros.
                - "X compte double" -> splitMode SHARES, splits = nombre de parts.
                - Dates relatives (« hier », « samedi dernier ») -> date exacte. Sans date : aujourd'hui.
                - Écris les prénoms comme dans la phrase, n'invente aucun membre.
                - Si le montant est absent, amount = null et pose la question.
                """.formatted(
                String.join(", ", ctx.names().values()),
                ctx.me().getName(),
                today.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRENCH), today,
                ctx.group().getCurrency(),
                Arrays.stream(Category.values()).map(Enum::name).collect(Collectors.joining(", ")));

        JsonNode message = clientAI.chat(clientAI.textModel(),
            List.of(Map.of("role", "system", "content", system),Map.of("role", "user", "content", PromptGuard.clean(text))),
            List.of(), true, 0.1);

        JsonNode json = AICommon.parse(mapper, message.path("content").asText());
        return resolveDraft(json, ctx.names(), ctx.me().getId(), today);
    }

    @Override
    public ReceiptDraft scanReceipt(byte[] image, String contentType, LocalDate today) {
        if (contentType == null || !CommonConstants.IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new Errors.Validation("ai.image.type.invalid");
        }
        String dataUrl = "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(image);
        String prompt = """
                Voici la photo d'un ticket de caisse. Extrais-en le contenu.
                Réponds UNIQUEMENT avec ce JSON :
                {
                  "merchant": "nom du commerce ou null",
                  "date": "AAAA-MM-JJ ou null",
                  "total": nombre (montant total TTC),
                  "category": "COURSES | RESTAURANT | LOISIRS | TRANSPORT | AUTRE ...",
                  "items": [{"label": "libellé", "amount": nombre}]
                }
                Règles :
                - Une ligne "2 x Coca 3,50" -> {"label": "2 x Coca", "amount": 7.00}.
                - Les remises sont des lignes à montant négatif.
                - N'inclus pas les lignes de sous-total, TVA ou moyen de paiement.
                """;
        List<Map<String, Object>> content = List.of(Map.of("type", "text", "text", prompt), Map.of("type", "image_url", "image_url", Map.of("url", dataUrl)));
        JsonNode message = clientAI.chat(clientAI.visionModel(), List.of(Map.of("role", "user", "content", content)), List.of(), true, 0.0);
        return responseMapper.toReceiptDraft(AICommon.parse(mapper, message.path("content").asText()));
    }

    ExpenseDraft resolveDraft(JsonNode json, Map<UUID, String> members, UUID meId, LocalDate today) {
        List<String> warnings = new ArrayList<>();
        List<String> questions = new ArrayList<>(AICommon.strings(json.path("questions")));

        BigDecimal amount = AICommon.money(json.path("amount"));
        if (amount != null && amount.signum() <= 0) {
            warnings.add(messages.get("ai.parse.amount.incoherent", amount));
            amount = null;
        }
        if (amount == null && questions.isEmpty()) {
            questions.add(messages.get("ai.parse.question.amount"));
        }

        UUID payer = resolveName(AICommon.text(json.path("payer")), members, meId, warnings).orElse(meId);

        List<String> rawParticipants = AICommon.strings(json.path("participants"));
        Set<UUID> participants = new LinkedHashSet<>();
        boolean everyone = rawParticipants.isEmpty() || rawParticipants.stream().anyMatch(participant -> CommonConstants.ALL.contains(TextMatching.normalize(participant)));
        if (everyone) {
            participants.addAll(members.keySet());
        } else {
            for (String name : rawParticipants) {
                resolveName(name, members, meId, warnings).ifPresent(participants::add);
            }
        }
        for (String name : AICommon.strings(json.path("excluded"))) {
            resolveName(name, members, meId, warnings).ifPresent(participants::remove);
        }

        SplitMode mode = switch (Optional.ofNullable(AICommon.text(json.path("splitMode"))).orElse("EQUAL").toUpperCase()) {
            case "EXACT" -> SplitMode.EXACT;
            case "SHARES" -> SplitMode.SHARES;
            default -> SplitMode.EQUAL;
        };
        List<SplitValueRequest> splits = new ArrayList<>();
        if (mode != SplitMode.EQUAL) {
            for (JsonNode split : json.path("splits")) {
                Optional<UUID> id = resolveName(AICommon.text(split.path("name")), members, meId, warnings);
                BigDecimal value = AICommon.money(split.path("value"));
                if (id.isPresent() && value != null && value.signum() >= 0) {
                    splits.add(new SplitValueRequest(id.get(), value));
                }
            }
            if (mode == SplitMode.EXACT && amount != null) {
                BigDecimal sum = splits.stream().map(SplitValueRequest::getValue).reduce(BigDecimal.ZERO, BigDecimal::add);
                if (sum.compareTo(amount) != 0) {
                    warnings.add(messages.get("ai.parse.split.mismatch", sum, amount));
                    mode = SplitMode.EQUAL;
                }
            }
            if (splits.isEmpty()) mode = SplitMode.EQUAL;
        }
        if (mode == SplitMode.EQUAL) {
            if (participants.isEmpty()) {
                warnings.add(messages.get("ai.parse.no.participant"));
                participants.addAll(members.keySet());
            }
            splits = participants.stream().map(id -> new SplitValueRequest(id, null)).toList();
        }

        LocalDate date = AICommon.date(json.path("date"));
        if (date == null || date.isAfter(today.plusDays(1)) || date.isBefore(today.minusYears(2))) {
            date = today;
        }
        String description = Optional.ofNullable(AICommon.text(json.path("description"))).orElse(messages.get("ai.parse.default.description"));

        return new ExpenseDraft(truncate(description, 200), amount, payer, Category.parse(AICommon.text(json.path("category"))), date, mode, splits, warnings, questions);
    }

    ReceiptDraft resolveReceipt(JsonNode json, LocalDate today) {
        List<String> warnings = new ArrayList<>();
        List<ReceiptItem> items = new ArrayList<>();
        BigDecimal discounts = BigDecimal.ZERO;

        for (JsonNode node : json.path("items")) {
            String label = AICommon.text(node.path("label"));
            BigDecimal amount = AICommon.money(node.path("amount"));
            if (label == null || amount == null) continue;
            if (amount.signum() < 0) {
                discounts = discounts.add(amount);
                continue;
            }
            items.add(new ReceiptItem(truncate(label, 120), amount));
        }
        if (items.isEmpty()) {
            warnings.add(messages.get("ai.receipt.no.items"));
        }

        BigDecimal itemsSum = items.stream().map(ReceiptItem::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total = AICommon.money(json.path("total"));
        if (total == null || total.signum() <= 0) {
            total = itemsSum.add(discounts);
            warnings.add(messages.get("ai.receipt.total.missing"));
        }
        if (discounts.signum() != 0) {
            warnings.add(messages.get("ai.receipt.discount", discounts.abs()));
        }
        BigDecimal gap = total.subtract(itemsSum.add(discounts));
        if (gap.abs().compareTo(new BigDecimal("0.01")) > 0) {
            warnings.add(messages.get("ai.receipt.sum.mismatch", itemsSum.add(discounts), total));
        }

        LocalDate date = AICommon.date(json.path("date"));
        if (date == null || date.isAfter(today.plusDays(1))) date = today;

        return new ReceiptDraft(AICommon.text(json.path("merchant")), date, total,Category.parse(AICommon.text(json.path("category"))),items, warnings);
    }

    private Optional<UUID> resolveName(String name, Map<UUID, String> members, UUID meId, List<String> warnings) {
        if (name == null) return Optional.empty();
        if (CommonConstants.ME.contains(TextMatching.normalize(name))) return Optional.of(meId);
        Optional<UUID> match = TextMatching.matchMember(name, members);
        if (match.isEmpty()) {
            warnings.add(messages.get("ai.parse.member.unknown", name));
        }
        return match;
    }

    private static String truncate(String input, int max) {
        return input == null ? "" : input.length() <= max ? input : input.substring(0, max);
    }
}
