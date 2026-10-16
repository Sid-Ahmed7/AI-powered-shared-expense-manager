package com.pactole.dto.response.ai;

import java.util.List;

public record AskResponse(String answer, List<String> toolsUsed) {}
