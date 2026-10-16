package com.pactole.dto.response.stats;

import java.time.YearMonth;

public record MonthComparison(YearMonth month, Summary current, Summary previous, Integer deltaPercent) {}
