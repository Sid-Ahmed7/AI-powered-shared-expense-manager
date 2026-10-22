package com.pactole.ai;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pactole.builder.ai.AiHttpClientBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.pactole.config.PactoleProperties;
import com.pactole.exceptions.Errors;

@Component
public class AiClient implements IAiClient {

    private static final Logger log = LoggerFactory.getLogger(AiClient.class);

    private final PactoleProperties.Ai aiConfiguration;
    private final RestClient http;
    private final ObjectMapper objectMapper;

    public AiClient(PactoleProperties properties, RestClient.Builder builder, ObjectMapper objectMapper, AiHttpClientBuilder httpBuilder) {
        this.aiConfiguration = properties.ai();
        this.objectMapper = objectMapper;
        this.http = httpBuilder.build(properties.ai(), builder);
    }

    @Override
    public boolean enabled() {
        return aiConfiguration.enabled();
    }

    @Override
    public String textModel() {
        return aiConfiguration.model();
    }

    @Override
    public String visionModel() {
        return aiConfiguration.visionModel();
    }

    @Override
    public JsonNode chat(String model, List<Map<String, Object>> messages, List<Map<String, Object>> tools, boolean jsonMode, double temperature) {
        if (!enabled()) {
            throw new Errors.AiUnavailable("ai.disabled");
        }
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                return attemptChat(model, messages, tools, jsonMode, temperature);
            } catch (ServiceOverloadedException e) {
                if (attempt == 3) throw new Errors.AiUnavailable("ai.client.error.status", 503);
                int delaySec = attempt * 2;
                log.warn("Modèle surchargé (503), tentative {}/3 dans {} s...", attempt + 1, delaySec);
                try {
                    Thread.sleep(delaySec * 1000L);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new Errors.AiUnavailable("ai.client.error.status", 503);
                }
            }
        }
        throw new Errors.AiUnavailable("ai.client.error.status", 503);
    }

    private JsonNode attemptChat(String model, List<Map<String, Object>> messages, List<Map<String, Object>> tools, boolean jsonMode, double temperature) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", temperature);
        if (tools != null && !tools.isEmpty()) {
            body.put("tools", tools);
            body.put("tool_choice", "auto");
        }
        if (jsonMode) {
            body.put("response_format", Map.of("type", "json_object"));
        }

        long start = System.nanoTime();
        try {
            String raw = http.post()
                .uri(aiConfiguration.chatPath())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    int status = res.getStatusCode().value();
                    String errorBody = new String(res.getBody().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                    log.error("AIHTTP {} — URL: {} — body: {}", status, req.getURI(), errorBody);
                    if (status == 401) throw new Errors.AiUnavailable("ai.client.key.invalid");
                    if (status == 429) throw new Errors.TooManyRequests("ai.client.quota", 20);
                    if (status == 503) throw new ServiceOverloadedException();
                    throw new Errors.AiUnavailable("ai.client.error.status", status);
                })
                .body(String.class);
            JsonNode response = objectMapper.readTree(raw);
            JsonNode usage = response.path("usage");
            log.info("AI{} : {} ms, {} jetons en entrée, {} en sortie", model, (System.nanoTime() - start) / 1_000_000, usage.path("prompt_tokens").asInt(), usage.path("completion_tokens").asInt());
            return response.path("choices").path(0).path("message");
        } catch (ResourceAccessException e) {
            throw new Errors.AiUnavailable("ai.client.timeout");
        } catch (Errors.AiUnavailable | Errors.TooManyRequests | ServiceOverloadedException e) {
            throw e;
        } catch (Exception e) {
            throw new Errors.AiUnavailable("ai.client.parse.error", e.getMessage());
        }
    }

    private static final class ServiceOverloadedException extends RuntimeException {
        ServiceOverloadedException() { super("503 - service overloaded"); }
    }
}
