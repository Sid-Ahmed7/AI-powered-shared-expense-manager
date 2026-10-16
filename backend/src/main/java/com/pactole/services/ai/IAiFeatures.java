package com.pactole.services.ai;

import java.time.LocalDate;

import com.pactole.dto.response.ai.ExpenseDraft;
import com.pactole.dto.response.ai.ReceiptDraft;
import com.pactole.services.expensesGroup.IGroupAccess;

public interface IAiFeatures {

    ExpenseDraft parse(IGroupAccess.Context ctx, String text, LocalDate today);
    ReceiptDraft scanReceipt(byte[] image, String contentType, LocalDate today);

}
