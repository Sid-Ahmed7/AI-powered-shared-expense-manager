package com.pactole.ai;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

public interface IMistralClient {

    boolean enabled();
    JsonNode chat(String model, List<Map<String, Object>> messages, List<Map<String, Object>> tools, boolean jsonMode, double temperature);
    String textModel();
    String visionModel();
}
