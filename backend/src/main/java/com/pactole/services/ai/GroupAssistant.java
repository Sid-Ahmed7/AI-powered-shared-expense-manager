package com.pactole.services.ai;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pactole.ai.MistralClient;
import com.pactole.common.AICommon;
import com.pactole.common.Messages;
import com.pactole.common.TextMatching;
import com.pactole.constants.CommonConstants;
import com.pactole.dto.request.ai.ChatTurn;
import com.pactole.dto.response.ai.AskResponse;
import com.pactole.dto.response.stats.Summary;
import com.pactole.enums.Category;
import com.pactole.exceptions.Errors;
import com.pactole.mapping.AiToolResultMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import com.pactole.entities.Expense;
import com.pactole.repository.ExpenseRepository;
import com.pactole.security.PromptGuard;
import com.pactole.services.balance.BalanceService;
import com.pactole.services.balance.IBalanceService;
import com.pactole.services.expense.ExpenseService;
import com.pactole.services.expensesGroup.IGroupAccess;
import com.pactole.services.stats.StatsService;

@Service
public class GroupAssistant implements IGroupAssistant {

    private final MistralClient clientAi;
    private final ObjectMapper mapper;
    private final BalanceService balanceService;
    private final StatsService statsService;
    private final ExpenseRepository expenseRepository;
    private final ExpenseService expenseService;
    private final AiToolResultMapper toolResultMapper;
    private final Messages messages;

    public GroupAssistant(MistralClient clientAi, ObjectMapper mapper, BalanceService balanceService, StatsService statsService, ExpenseRepository expenseRepository, ExpenseService expenseService, AiToolResultMapper toolResultMapper, Messages messages) {
        this.clientAi = clientAi;
        this.mapper = mapper;
        this.balanceService = balanceService;
        this.statsService = statsService;
        this.expenseRepository = expenseRepository;
        this.expenseService = expenseService;
        this.toolResultMapper = toolResultMapper;
        this.messages = messages;
    }

    @Override
    @Transactional(readOnly = true)
    public AskResponse ask(IGroupAccess.Context ctx, String question, List<ChatTurn> history, LocalDate today) {
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt(ctx, today)));
        if (history != null) {
            history.stream().filter(turn -> "user".equals(turn.getRole()) || "assistant".equals(turn.getRole())).skip(Math.max(0, history.size() - 6)).forEach(turn -> messages.add(Map.of("role", turn.getRole(), "content", PromptGuard.clean(turn.getContent()))));
        }
        messages.add(Map.of("role", "user", "content", PromptGuard.clean(question)));

        List<String> toolsUsed = new ArrayList<>();
        for (int step = 0; step < CommonConstants.MAX_STEPS; step++) {
            JsonNode reply = clientAi.chat(clientAi.textModel(), messages, tools(), false, 0.2);
            JsonNode toolCalls = reply.path("tool_calls");
            if (!toolCalls.isArray() || toolCalls.isEmpty()) {
                return new AskResponse(reply.path("content").asText(msg("ai.ask.no.answer")), toolsUsed);
            }
            Map<String, Object> assistantMsg = new LinkedHashMap<>();
            assistantMsg.put("role", "assistant");
            assistantMsg.put("content", reply.path("content").asText(""));
            assistantMsg.put("tool_calls", mapper.convertValue(toolCalls, List.class));
            messages.add(assistantMsg);

            for (JsonNode call : toolCalls) {
                String name = call.path("function").path("name").asText();
                JsonNode args = parseArguments(call.path("function").path("arguments"));
                Object result;
                try {
                    result = execute(ctx, name, args, today);
                } catch (RuntimeException e) {
                    result = Map.of("error", e.getMessage());
                }
                toolsUsed.add(name);
                messages.add(Map.of("role", "tool", "name", name, "tool_call_id", call.path("id").asText(), "content", toJson(result)));
            }
        }
        return new AskResponse(msg("ai.ask.failed"), toolsUsed);
    }

    private String systemPrompt(IGroupAccess.Context ctx, LocalDate today) {
        return """
            Tu es l'assistant du groupe « %s » dans l'application Pactole.
            Membres : %s. Tu parles à %s. Devise : %s.
            Aujourd'hui : %s %s.

            Règles :
                - Réponds uniquement à partir des données renvoyées par les outils. N'invente JAMAIS un montant.
                - Appelle les outils dont tu as besoin avant de répondre.
                - Les montants sont en centimes dans les outils, affiche-les en euros (12,50 €).
                - Un solde positif = le groupe doit de l'argent à la personne ; négatif = elle en doit.
                - Réponds en français, court et concret (2 à 5 phrases ou une liste).
                - Si la question n'a rien à voir avec les dépenses du groupe, dis-le gentiment.
            """.formatted(ctx.group().getName(), String.join(", ", ctx.names().values()),
            ctx.me().getName(), ctx.group().getCurrency(),
            today.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRENCH), today);
    }

    private static List<Map<String, Object>> tools() {
        return List.of(
            tool("get_balances", "Soldes actuels et virements suggérés pour tout équilibrer.", Map.of()),
            tool("list_expenses", "Liste des dépenses avec filtres optionnels.", Map.of(
                "category", Map.of("type", "string", "description", "Catégorie : " + categoryNames()),
                "member", Map.of("type", "string", "description", "Prénom du membre"),
                "from", Map.of("type", "string", "description", "Date début AAAA-MM-JJ"),
                "to", Map.of("type", "string", "description", "Date fin AAAA-MM-JJ"),
                "search", Map.of("type", "string", "description", "Mot-clé dans la description"),
                "limit", Map.of("type", "integer", "description", "Nombre max (défaut 20, max 50)"))),
            tool("spending_summary", "Totaux sur une période : par catégorie, par membre, plus grosse dépense.", Map.of(
                "from", Map.of("type", "string", "description", "Date début AAAA-MM-JJ"),
                "to", Map.of("type", "string", "description", "Date fin AAAA-MM-JJ"))));
    }

    private static Map<String, Object> tool(String name, String description, Map<String, Object> properties) {
        return Map.of("type", "function", "function", Map.of("name", name, "description", description,"parameters", Map.of("type", "object", "properties", properties)));
    }

    Object execute(IGroupAccess.Context ctx, String name, JsonNode args, LocalDate today) {
        return switch (name) {
            case "get_balances" -> balancesTool(ctx);
            case "list_expenses" -> listTool(ctx, args);
            case "spending_summary" -> summaryTool(ctx, args, today);
            default -> Map.of("error", "Outil inconnu : " + name);
        };
    }

    private Object balancesTool(IGroupAccess.Context ctx) {
        IBalanceService.Balances balances = balanceService.compute(ctx);
        return toolResultMapper.toBalanceResult(balances, ctx.names());
    }

    private Object listTool(IGroupAccess.Context ctx, JsonNode args) {
        Category category = args.hasNonNull("category") ? Category.parse(args.get("category").asText()) : null;
        UUID memberId = null;
        if (args.hasNonNull("member")) {
            memberId = TextMatching.matchMember(args.get("member").asText(), ctx.names()).orElseThrow(() -> new Errors.Validation("group.member.not.in.group", args.get("member").asText()));
        }
        int limit = Math.clamp(args.path("limit").asInt(20), 1, 50);
        Specification<Expense> spec = expenseService.specification(ctx.group().getId(), category, memberId,
                AICommon.text(args.path("search")), AICommon.date(args.path("from")), AICommon.date(args.path("to")));
        Page<Expense> page = expenseRepository.findAll(spec, PageRequest.of(0, limit, Sort.by(Sort.Order.desc("date"))));
        List<Map<String, Object>> list = page.getContent().stream().map(e -> toolResultMapper.toExpenseRow(e, ctx.names())).toList();
        return Map.of("total_correspondant", page.getTotalElements(), "depenses", list);
    }

    private Object summaryTool(IGroupAccess.Context ctx, JsonNode args, LocalDate today) {
        LocalDate from = AICommon.date(args.path("from"));
        LocalDate to = AICommon.date(args.path("to"));
        Summary s = statsService.summary(ctx, from == null ? today.minusYears(10) : from, to == null ? today : to);
        return toolResultMapper.toSummaryResult(s);
    }

    private static String categoryNames() {
        return String.join(", ", Arrays.stream(Category.values()).map(Enum::name).toList());
    }

    private JsonNode parseArguments(JsonNode raw) {
        if (raw.isObject()) return raw;
        try {
            return mapper.readTree(raw.asText("{}"));
        } catch (Exception e) {
            return mapper.createObjectNode();
        }
    }

    private String toJson(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (Exception e) {
            return "{\"error\":\"serialisation impossible\"}";
        }
    }

    private String msg(String key, Object... args) {
        return messages.get(key, args);
    }
}