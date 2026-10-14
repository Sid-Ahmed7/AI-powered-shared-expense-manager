package com.pactole.controller.settlement;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.pactole.common.CurrentUser;
import com.pactole.common.SettlementCalculator;
import com.pactole.dto.request.settlement.SettlementRequest;
import com.pactole.dto.response.settlement.SettlementResponse;
import com.pactole.services.balance.BalanceService;
import com.pactole.services.balance.IBalanceService.Balances;
import com.pactole.services.expensesGroup.GroupAccess;
import com.pactole.services.settlement.SettlementService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/groups/{groupId}")
public class SettlementController {

    private final GroupAccess groupAccess;
    private final BalanceService balanceService;
    private final SettlementService settlementService;

    public SettlementController(GroupAccess groupAccess, BalanceService balanceService, SettlementService settlementService) {
        this.groupAccess = groupAccess;
        this.balanceService = balanceService;
        this.settlementService = settlementService;
    }

    @GetMapping("/balances")
    public Balances balances(@PathVariable UUID groupId, @AuthenticationPrincipal Jwt jwt) {
        return balanceService.compute(groupAccess.context(groupId, CurrentUser.id(jwt)));
    }

    @GetMapping("/settlements")
    public List<SettlementResponse> list(@PathVariable UUID groupId, @AuthenticationPrincipal Jwt jwt) {
        return settlementService.list(groupId, CurrentUser.id(jwt));
    }

    @PostMapping("/settlements")
    @ResponseStatus(HttpStatus.CREATED)
    public SettlementResponse create(@PathVariable UUID groupId, @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody SettlementRequest request) {
        return settlementService.create(groupId, CurrentUser.id(jwt), request);
    }

    @PostMapping("/settlements/from-suggestion")
    @ResponseStatus(HttpStatus.CREATED)
    public SettlementResponse settleSuggestion(@PathVariable UUID groupId, @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody SettlementCalculator.Transfer transfer) {
        return settlementService.createFromSuggestion(groupId, CurrentUser.id(jwt), transfer);
    }

    @DeleteMapping("/settlements/{settlementId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID groupId, @PathVariable UUID settlementId, @AuthenticationPrincipal Jwt jwt) {
        settlementService.delete(groupId, settlementId, CurrentUser.id(jwt));
    }
}
