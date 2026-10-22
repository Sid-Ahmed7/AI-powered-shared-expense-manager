package com.pactole.builder.ai;

import java.time.Duration;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.pactole.config.PactoleProperties;

@Component
public class AiHttpClientBuilder {

    public RestClient build(PactoleProperties.Ai config, RestClient.Builder builder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(config.timeoutSeconds()));
        return builder
                .baseUrl(config.baseUrl())
                .requestFactory(factory)
                .defaultHeader("Authorization", "Bearer " + (config.apiKey() == null ? "" : config.apiKey()))
                .build();
    }
}
