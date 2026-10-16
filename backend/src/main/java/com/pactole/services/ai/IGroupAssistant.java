package com.pactole.services.ai;

import java.time.LocalDate;
import java.util.List;

import com.pactole.dto.request.ai.ChatTurn;
import com.pactole.dto.response.ai.AskResponse;
import com.pactole.services.expensesGroup.IGroupAccess;

public interface IGroupAssistant {

    AskResponse ask(IGroupAccess.Context ctx, String question, List<ChatTurn> history, LocalDate today);
}
