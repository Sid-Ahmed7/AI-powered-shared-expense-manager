package com.pactole.services.settlement;

import java.util.List;
import java.util.UUID;

import com.pactole.common.SettlementCalculator;
import com.pactole.dto.request.settlement.SettlementRequest;
import com.pactole.dto.response.settlement.SettlementResponse;

public interface ISettlementService {
    List<SettlementResponse> list(UUID groupId, UUID accountId);
    SettlementResponse create(UUID groupId, UUID accountId, SettlementRequest request);
    SettlementResponse createFromSuggestion(UUID groupId, UUID accountId, SettlementCalculator.Transfer transfer);
    void delete(UUID groupId, UUID settlementId, UUID accountId);
}
