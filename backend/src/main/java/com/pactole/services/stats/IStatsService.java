package com.pactole.services.stats;

import java.time.LocalDate;
import java.time.YearMonth;

import com.pactole.dto.response.stats.MonthComparison;
import com.pactole.dto.response.stats.Summary;
import com.pactole.services.expensesGroup.IGroupAccess;

public interface IStatsService {

    Summary summary(IGroupAccess.Context ctx, LocalDate from, LocalDate to);
    MonthComparison month(IGroupAccess.Context ctx, YearMonth month);
}
