package com.pactole.ai;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pactole.builder.ai.MistralHttpClientBuilder;

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
public class MistralClient implements IMistralClient {
    
    private static final Logger log = LoggerFactory.getLogger(MistralClient.class);

    private final PactoleProperties.Ai aiConfiguration;
    private final RestClient http;
    private final ObjectMapper objectMapper;

    public MistralClient(PactoleProperties properties, RestClient.Builder builder, ObjectMapper objectMapper, MistralHttpClientBuilder httpBuilder) {
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
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    int status = res.getStatusCode().value();
                    String errorBody = new String(res.getBody().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                    log.error("Mistral HTTP {} — URL: {} — body: {}", status, req.getURI(), errorBody);
                    if (status == 401) throw new Errors.AiUnavailable("ai.mistral.key.invalid");
                    if (status == 429) throw new Errors.TooManyRequests("ai.mistral.quota", 20);
                    throw new Errors.AiUnavailable("ai.mistral.error.status", status);
                })
                .body(String.class);
            JsonNode response = objectMapper.readTree(raw);
            JsonNode usage = response.path("usage");
            log.info("Mistral {} : {} ms, {} jetons en entrée, {} en sortie", model, (System.nanoTime() - start) / 1_000_000, usage.path("prompt_tokens").asInt(), usage.path("completion_tokens").asInt());
            return response.path("choices").path(0).path("message");
        } catch (ResourceAccessException e) {
            throw new Errors.AiUnavailable("ai.mistral.timeout");
        } catch (Errors.AiUnavailable | Errors.TooManyRequests e) {
            throw e;
        } catch (Exception e) {
            throw new Errors.AiUnavailable("ai.mistral.parse.error", e.getMessage());
        }
    }
}
