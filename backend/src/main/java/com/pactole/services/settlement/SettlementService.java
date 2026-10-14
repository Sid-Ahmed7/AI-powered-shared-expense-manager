package com.pactole.services.settlement;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pactole.builder.settlement.SettlementBuilder;
import com.pactole.common.InputSanitizer;
import com.pactole.common.SettlementCalculator;
import com.pactole.common.SplitCalculator;
import com.pactole.dto.request.settlement.SettlementRequest;
import com.pactole.dto.response.settlement.SettlementResponse;
import com.pactole.entities.Settlement;
import com.pactole.exceptions.Errors;
import com.pactole.repository.SettlementRepository;
import com.pactole.services.balance.BalanceService;
import com.pactole.services.expensesGroup.GroupAccess;
import com.pactole.services.expensesGroup.IGroupAccess;

@Service
public class SettlementService implements ISettlementService {

    private final SettlementRepository settlementRepository;
    private final GroupAccess groupAccess;
    private final BalanceService balanceService;
    private final SettlementBuilder settlementBuilder;

    public SettlementService(SettlementRepository settlementRepository, GroupAccess groupAccess,BalanceService balanceService, SettlementBuilder settlementBuilder) {
        this.settlementRepository = settlementRepository;
        this.groupAccess = groupAccess;
        this.balanceService = balanceService;
        this.settlementBuilder = settlementBuilder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettlementResponse> list(UUID groupId, UUID accountId) {
        groupAccess.requireMember(groupId, accountId);
        return settlementRepository.findByGroupIdOrderBySettledOnDescIdDesc(groupId).stream().map(SettlementResponse::from).toList();
    }

    @Override
    @Transactional
    public SettlementResponse create(UUID groupId, UUID accountId, SettlementRequest request) {
        IGroupAccess.Context ctx = groupAccess.context(groupId, accountId);
        ctx.member(request.getFromMemberId());
        ctx.member(request.getToMemberId());
        if (request.getFromMemberId().equals(request.getToMemberId())) {
            throw new Errors.Validation("settlement.self.payment");
        }
        String note = request.getNote() == null || request.getNote().isBlank() ? null : InputSanitizer.text(request.getNote(), "Note", 200);
        Settlement settlement = settlementRepository.save(settlementBuilder.build(
                groupId,
                request.getFromMemberId(),
                request.getToMemberId(),
                SplitCalculator.toCents(request.getAmount()),
                request.getDate() == null ? LocalDate.now() : request.getDate(),
                note,
                accountId));
        return SettlementResponse.from(settlement);
    }

    @Override
    @Transactional
    public SettlementResponse createFromSuggestion(UUID groupId, UUID accountId, SettlementCalculator.Transfer transfer) {
        IGroupAccess.Context ctx = groupAccess.context(groupId, accountId);
        if (!balanceService.compute(ctx).transfers().contains(transfer)) {
            throw new Errors.Conflict("settlement.outdated");
        }
        Settlement settlement = settlementRepository.save(settlementBuilder.build(
                groupId,
                transfer.fromMemberId(),
                transfer.toMemberId(),
                transfer.amountCents(),
                LocalDate.now(),
                "settlement.suggested.note",
                accountId));
        return SettlementResponse.from(settlement);
    }

    @Override
    @Transactional
    public void delete(UUID groupId, UUID settlementId, UUID accountId) {
        groupAccess.requireMember(groupId, accountId);
        Settlement settlement = settlementRepository.findById(settlementId).filter(s -> groupId.equals(s.getGroupId())).orElseThrow(() -> new Errors.NotFound("settlement.not.found"));
        settlementRepository.delete(settlement);
    }
}
