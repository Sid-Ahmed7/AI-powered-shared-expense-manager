package com.pactole.services.ai;

import com.pactole.dto.response.ai.Recap;
import com.pactole.dto.response.ai.ReminderResponse;
import com.pactole.dto.response.stats.MonthComparison;
import com.pactole.enums.Tone;
import com.pactole.services.expensesGroup.IGroupAccess;

public interface IAiWriter {

    Recap recap(IGroupAccess.Context ctx, MonthComparison data);
    ReminderResponse reminder(String debtor, String creditor, long amountCents, Tone tone, String groupName);
}
