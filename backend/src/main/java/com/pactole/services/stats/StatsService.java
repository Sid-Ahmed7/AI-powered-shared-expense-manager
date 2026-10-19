package com.pactole.services.stats;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pactole.dto.response.stats.CategoryStat;
import com.pactole.dto.response.stats.MemberStat;
import com.pactole.dto.response.stats.MonthComparison;
import com.pactole.dto.response.stats.Summary;
import com.pactole.entities.Expense;
import com.pactole.enums.Category;
import com.pactole.repository.ExpenseRepository;
import com.pactole.services.expensesGroup.IGroupAccess;

@Service
public class StatsService implements IStatsService {

    private final ExpenseRepository expenseRepository;

    public StatsService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Summary summary(IGroupAccess.Context ctx, LocalDate from, LocalDate to) {
        List<Expense> list = expenseRepository.findByGroupIdAndDateBetweenOrderByDateDesc(ctx.group().getId(), from, to);

        Map<Category, long[]> byCategory = new EnumMap<>(Category.class);
        Map<UUID, long[]> byMember = new LinkedHashMap<>();
        ctx.members().forEach(member -> byMember.put(member.getId(), new long[2]));

        long total = 0;
        Expense biggest = null;

        for (Expense e : list) {
            total += e.getAmountCents();
            long[] c = byCategory.computeIfAbsent(e.getCategory(), k -> new long[2]);
            c[0] += e.getAmountCents();
            c[1]++;
            byMember.computeIfAbsent(e.getPayerMemberId(), k -> new long[2])[0] += e.getAmountCents();
            for (Expense.Share s : e.getShares()) {
                byMember.computeIfAbsent(s.getMemberId(), k -> new long[2])[1] += s.getAmountCents();
            }
            if (biggest == null || e.getAmountCents() > biggest.getAmountCents()) biggest = e;
        }

        Map<UUID, String> names = ctx.names();
        List<CategoryStat> categories = byCategory.entrySet().stream()
                .map(entry -> new CategoryStat(entry.getKey().name(), entry.getKey().label(), entry.getValue()[0], (int) entry.getValue()[1]))
                .sorted(Comparator.comparingLong(CategoryStat::cents).reversed())
                .toList();
        List<MemberStat> members = byMember.entrySet().stream().map(entry -> new MemberStat(entry.getKey(), names.getOrDefault(entry.getKey(), "?"), entry.getValue()[0], entry.getValue()[1])).toList();

        return new Summary(from, to, total, list.size(), categories, members, biggest == null ? null : biggest.getDescription(), biggest == null ? 0 : biggest.getAmountCents());
    }

    @Override
    public MonthComparison month(IGroupAccess.Context ctx, YearMonth month) {
        Summary current = summary(ctx, month.atDay(1), month.atEndOfMonth());
        YearMonth prev = month.minusMonths(1);
        Summary previous = summary(ctx, prev.atDay(1), prev.atEndOfMonth());
        Integer delta = previous.totalCents() == 0 ? null : (int) Math.round((current.totalCents() - previous.totalCents()) * 100.0 / previous.totalCents());
        return new MonthComparison(month, current, previous, delta);
    }
}
