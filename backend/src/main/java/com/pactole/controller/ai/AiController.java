package com.pactole.controller.ai;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.pactole.ai.AiClient;
import com.pactole.common.CurrentUser;
import com.pactole.constants.CommonConstants;
import com.pactole.dto.request.ai.AskRequest;
import com.pactole.dto.request.ai.ParseRequest;
import com.pactole.dto.request.ai.ReminderRequest;
import com.pactole.dto.response.ai.AiStatus;
import com.pactole.dto.response.stats.MonthComparison;
import com.pactole.dto.response.ai.AskResponse;
import com.pactole.dto.response.ai.ExpenseDraft;
import com.pactole.dto.response.ai.Recap;
import com.pactole.dto.response.ai.ReceiptDraft;
import com.pactole.dto.response.ai.ReminderResponse;
import com.pactole.common.Messages;
import com.pactole.exceptions.Errors;
import com.pactole.security.AiRateLimiter;
import com.pactole.security.ImageSignature;
import com.pactole.services.ai.AiFeatures;
import com.pactole.services.ai.AiWriter;
import com.pactole.services.ai.GroupAssistant;
import com.pactole.services.expensesGroup.GroupAccess;
import com.pactole.services.stats.StatsService;

import jakarta.validation.Valid;

@RestController 
@RequestMapping("/api")
public class AiController {

    private final AiClient clientAi;
    private final AiRateLimiter rateLimiter;
    private final GroupAccess groupAccess;
    private final AiFeatures expenseFeatures;
    private final GroupAssistant assistant;
    private final AiWriter writer;
    private final StatsService statsService;
    private final Messages messages;

    public AiController(AiClient clientAi, AiRateLimiter rateLimiter, GroupAccess groupAccess, AiFeatures expenseFeatures, GroupAssistant assistant, AiWriter writer, StatsService statsService, Messages messages) {
        this.clientAi = clientAi;
        this.rateLimiter = rateLimiter;
        this.groupAccess = groupAccess;
        this.expenseFeatures = expenseFeatures;
        this.assistant = assistant;
        this.writer = writer;
        this.statsService = statsService;
        this.messages = messages;
    }

    public record RecapResponse(MonthComparison stats, Recap recap, String aiError) {}

    @GetMapping("/ai/status")
    public AiStatus status() {
        return new AiStatus(clientAi.enabled(), clientAi.textModel(), clientAi.visionModel());
    }

    @PostMapping("/groups/{groupId}/ai/parse-expense")
    public ExpenseDraft parse(@PathVariable UUID groupId, @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ParseRequest request) {
        GroupAccess.Context ctx = guard(groupId, jwt);
        return expenseFeatures.parse(ctx, request.getText(), today());
    }

    @PostMapping(value = "/groups/{groupId}/ai/scan-receipt", consumes = "multipart/form-data")
    public ReceiptDraft scan(@PathVariable UUID groupId, @AuthenticationPrincipal Jwt jwt, @RequestPart("file") MultipartFile file) throws IOException {
        UUID userId = CurrentUser.id(jwt);
        groupAccess.requireMember(groupId, userId);
        if (!clientAi.enabled()) {
            throw new Errors.AiUnavailable("ai.disabled");
        }
        if (file.isEmpty()) {
            throw new Errors.Validation("ai.image.empty");
        }
        rateLimiter.acquire(userId);
        byte[] bytes = file.getBytes();
        String realType = ImageSignature.detect(bytes).orElseThrow(() -> new Errors.Validation("ai.image.type.invalid"));
        return expenseFeatures.scanReceipt(bytes, realType, today());
    }

    @PostMapping("/groups/{groupId}/ai/ask")
    public AskResponse ask(@PathVariable UUID groupId, @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AskRequest request) {
        GroupAccess.Context ctx = guard(groupId, jwt);
        return assistant.ask(ctx, request.getQuestion(), request.getHistory(), today());
    }

    
    @GetMapping("/groups/{groupId}/ai/recap")
    public RecapResponse recap(@PathVariable UUID groupId, @AuthenticationPrincipal Jwt jwt, @RequestParam(required = false) String month) {
        UUID userId = CurrentUser.id(jwt);
        GroupAccess.Context ctx = groupAccess.context(groupId, userId);
        YearMonth ym;
        try {
            ym = month == null || month.isBlank() ? YearMonth.now(CommonConstants.ZONE) : YearMonth.parse(month);
        } catch (java.time.format.DateTimeParseException e) {
            throw new Errors.Validation("ai.recap.month.invalid");
        }
        MonthComparison data = statsService.month(ctx, ym);
        if (!clientAi.enabled() || data.current().expenseCount() == 0) {
            return new RecapResponse(data, null, null);
        }
        try {
            rateLimiter.acquire(userId);
            return new RecapResponse(data, writer.recap(ctx, data), null);
        } catch (Errors.AiUnavailable e) {
            return new RecapResponse(data, null, messages.get(e.getMessageKey(), e.getArgs()));
        } catch (Errors.TooManyRequests e) {
            return new RecapResponse(data, null, messages.get(e.getMessageKey(), e.getArgs())); 
        }
    }

    @PostMapping("/groups/{groupId}/ai/reminder")
    public ReminderResponse reminder(@PathVariable UUID groupId, @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ReminderRequest request) {
        UUID userId = CurrentUser.id(jwt);
        GroupAccess.Context ctx = groupAccess.context(groupId, userId);
        String debtor = ctx.member(request.getFromMemberId()).getName();
        String creditor = ctx.member(request.getToMemberId()).getName();
        if (clientAi.enabled()) {
            rateLimiter.acquire(userId);
        }
        return writer.reminder(debtor, creditor, request.getAmountCents(), request.getTone(), ctx.group().getName());
    }

    private GroupAccess.Context guard(UUID groupId, Jwt jwt) {
        UUID userId = CurrentUser.id(jwt);
        GroupAccess.Context ctx = groupAccess.context(groupId, userId);
        if (!clientAi.enabled()) {
            throw new Errors.AiUnavailable("ai.disabled");
        }
        rateLimiter.acquire(userId);
        return ctx;
    }

    private static LocalDate today() {
        return LocalDate.now(CommonConstants.ZONE);
    }
    
}
