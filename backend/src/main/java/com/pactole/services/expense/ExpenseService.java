package com.pactole.services.expense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.pactole.common.InputSanitizer;
import com.pactole.common.SplitCalculator;
import com.pactole.common.TextMatching;
import com.pactole.dto.request.expense.ExpenseRequest;
import com.pactole.dto.request.expense.ItemRequest;
import com.pactole.dto.request.expense.SplitValueRequest;
import com.pactole.entities.Expense;
import com.pactole.enums.Category;
import com.pactole.enums.Source;
import com.pactole.enums.SplitMode;
import com.pactole.exceptions.DuplicateExpenseException;
import com.pactole.exceptions.Errors;
import com.pactole.repository.ExpenseRepository;
import com.pactole.services.expensesGroup.GroupAccess;
import com.pactole.services.expensesGroup.IGroupAccess;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpenseService implements IExpenseService {

    private final ExpenseRepository expenseRepository;
    private final GroupAccess groupAccess;

    public ExpenseService(ExpenseRepository expenseRepository, GroupAccess groupAccess) {
        this.expenseRepository = expenseRepository;
        this.groupAccess = groupAccess;
    }

    @Override
    @Transactional
    public Expense createExpense(UUID groupId, UUID accountId, ExpenseRequest request) {
        IGroupAccess.Context ctx = groupAccess.context(groupId, accountId);
        Expense expense = new Expense(groupId, accountId);
        applyRequest(ctx, expense, request, accountId);
        if (!request.isForce()) {
            checkDuplicate(expense, null);
        }
        return expenseRepository.save(expense);
    }

    @Override
    @Transactional
    public Expense updateExpense(UUID groupId, UUID expenseId, UUID accountId, ExpenseRequest request) {
        IGroupAccess.Context ctx = groupAccess.context(groupId, accountId);
        Expense expense = loadExpense(groupId, expenseId);
        if (request.getVersion() != null && request.getVersion() != expense.getVersion()) {
            throw new Errors.Conflict("Cette dépense a été modifiée entre-temps. Rechargez avant de la modifier.");
        }
        applyRequest(ctx, expense, request, accountId);
        return expense;    }

    @Override
    @Transactional
    public void deleteExpense(UUID groupId, UUID expenseId, UUID accountId) {
        groupAccess.requireMember(groupId, accountId);
        expenseRepository.delete(loadExpense(groupId, expenseId));
    }

    @Override
    @Transactional(readOnly = true)
    public Expense getExpense(UUID groupId, UUID expenseId, UUID accountId) {
        groupAccess.requireMember(groupId, accountId);
        return loadExpense(groupId, expenseId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Expense> search(UUID groupId, UUID accountId, Category category, UUID memberId, String query, LocalDate from, LocalDate to, int page, int size) {
        groupAccess.requireMember(groupId, accountId);
        return expenseRepository.findAll(specification(groupId, category, memberId, query, from, to),
            PageRequest.of(Math.max(0, page), Math.clamp(size, 1, 100),
            Sort.by(Sort.Order.desc("date"), Sort.Order.desc("id"))));
    }

    @Override
    public Specification<Expense> specification(UUID groupId, Category category, UUID memberId, String q, LocalDate from, LocalDate to) {

    Specification<Expense> spec = (root, query, cb) -> cb.equal(root.get("groupId"), groupId);
        if (category != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category"), category));
        }
        if (from != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.<LocalDate>get("date"), from));
        }
        if (to != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.<LocalDate>get("date"), to));
        }
        if (q != null && !q.isBlank()) {
            String like = "%" + escapeLike(q.toLowerCase().strip()) + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.<String>get("description")), like, '\\'));
        }
        if (memberId != null) {
            spec = spec.and((root, query, cb) -> {
                Join<Expense, Expense.Share> shares = root.join("shares", JoinType.LEFT);
                query.distinct(true);
                return cb.or(cb.equal(root.get("payerMemberId"), memberId), cb.equal(shares.get("memberId"), memberId));
            });
        }
        return spec;
    }

    private Expense loadExpense(UUID groupId, UUID expenseId) {
        return expenseRepository.findById(expenseId)
                .filter(e -> groupId.equals(e.getGroupId()))
                .orElseThrow(() -> new Errors.NotFound("Dépense introuvable"));
    }

    static String escapeLike(String value) {
        String inputValue = value.length() > 100 ? value.substring(0, 100) : value;
        return inputValue.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private void applyRequest(IGroupAccess.Context ctx, Expense expense, ExpenseRequest request, UUID editorId) {
        LocalDate today = LocalDate.now();
        if (request.getDate() != null && (request.getDate().isAfter(today.plusYears(1)) || request.getDate().isBefore(today.minusYears(10)))) {
            throw new Errors.Validation("Date de dépense invalide");
        }
        long amountCents = SplitCalculator.toCents(request.getAmount());
        ctx.member(request.getPayerMemberId());

        List<SplitCalculator.Input> inputs = new ArrayList<>();
        List<SplitCalculator.Item> calcItems = null;
        List<Expense.Item> storedItems = null;
        Map<UUID, BigDecimal> weights = new HashMap<>();

        if (request.getSplitMode() == SplitMode.ITEMIZED) {
            if (request.getItems() == null || request.getItems().isEmpty()) {
                throw new Errors.Validation("Le mode « par article » nécessite au moins un article");
            }
            calcItems = new ArrayList<>();
            storedItems = new ArrayList<>();
            for (ItemRequest item : request.getItems()) {
                item.getMemberIds().forEach(ctx::member);
                long cents = SplitCalculator.toCents(item.getAmount());
                calcItems.add(new SplitCalculator.Item(item.getLabel(), cents, item.getMemberIds()));
                storedItems.add(new Expense.Item(InputSanitizer.text(item.getLabel(), "Libellé d'article", 120), cents, List.copyOf(item.getMemberIds())));
            }
        } else {
            List<SplitValueRequest> splits = request.getSplits() == null || request.getSplits().isEmpty()
                    ? ctx.members().stream().map(m -> new SplitValueRequest(m.getId(), null)).toList()
                    : request.getSplits();
            for (SplitValueRequest s : splits) {
                ctx.member(s.getMemberId());
                inputs.add(new SplitCalculator.Input(s.getMemberId(), s.getValue()));
                if (s.getValue() != null && request.getSplitMode() != SplitMode.EQUAL) {
                    weights.put(s.getMemberId(), s.getValue());
                }
            }
        }

        Map<UUID, Long> computed = SplitCalculator.compute(amountCents, request.getSplitMode(), inputs, calcItems);
        List<Expense.Share> shares = computed.entrySet().stream()
                .map(e -> new Expense.Share(e.getKey(), e.getValue(), weights.get(e.getKey())))
                .toList();

        expense.apply(
                InputSanitizer.text(request.getDescription(), "Description", 200),
                amountCents,
                request.getPayerMemberId(),
                request.getCategory() == null ? Category.AUTRE : request.getCategory(),
                request.getSplitMode(),
                request.getDate() == null ? today : request.getDate(),
                storedItems,
                request.getSource() == null ? Source.MANUAL : request.getSource(),
                shares,
                editorId);
    }

    private void checkDuplicate(Expense candidate, UUID ignoreId) {
        LocalDate d = candidate.getDate();
        for (Expense existing : expenseRepository.findByGroupIdAndAmountCentsAndDateBetween(
                candidate.getGroupId(), candidate.getAmountCents(), d.minusDays(1), d.plusDays(1))) {
            if (existing.getId().equals(ignoreId)) continue;
            if (TextMatching.likelyDuplicate(candidate.getDescription(), candidate.getAmountCents(), candidate.getDate(),
                    existing.getDescription(), existing.getAmountCents(), existing.getDate())) {
                throw new DuplicateExpenseException("Une dépense très proche existe déjà : « " + existing.getDescription()
                        + " » (" + SplitCalculator.format(existing.getAmountCents()) + ", le " + existing.getDate()
                        + "). Enregistrer quand même ?", existing.getId());
            }
        }
    }
}
