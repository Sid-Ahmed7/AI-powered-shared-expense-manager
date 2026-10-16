package com.pactole.dto.response.ai;

import java.util.List;

public record Recap(String title, String summary, List<String> insights, String tip) {}
