package com.rag.aichat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rag.aichat.common.BusinessException;
import com.rag.aichat.config.RagProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service
public class OpenAiCompatibleClient {

    private final RagProperties ragProperties;
    private final ObjectMapper objectMapper;

    public OpenAiCompatibleClient(RagProperties ragProperties, ObjectMapper objectMapper) {
        this.ragProperties = ragProperties;
        this.objectMapper = objectMapper;
    }

    public List<float[]> embed(List<String> texts) {
        RagProperties.Embedding config = ragProperties.getEmbedding();
        requireConfig(config.getBaseUrl(), "embedding API base URL");
        requireConfig(config.getApiKey(), "embedding API key");

        RestTemplate client = new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(ragProperties.getRag().getTimeoutSeconds()))
                .build();
        String url = config.getBaseUrl().replaceAll("/+$", "") + "/embeddings";
        Map<String, Object> payload = Map.of("model", config.getModel(), "input", texts);

        try {
            JsonNode response = client.postForObject(url, request(payload, config.getApiKey()), JsonNode.class);
            List<float[]> vectors = new ArrayList<>();
            for (JsonNode item : response.get("data")) {
                JsonNode data = item.get("embedding");
                float[] vector = new float[data.size()];
                for (int i = 0; i < data.size(); i++) {
                    vector[i] = (float) data.get(i).asDouble();
                }
                vectors.add(vector);
            }
            return vectors;
        } catch (RestClientException | NullPointerException | ArithmeticException e) {
            throw new BusinessException(502, 502, "embedding api failed: " + e.getMessage());
        }
    }

    public String chat(List<Map<String, String>> messages) {
        RagProperties.Llm config = ragProperties.getLlm();
        requireConfig(config.getBaseUrl(), "LLM API base URL");
        requireConfig(config.getApiKey(), "LLM API key");

        RestTemplate client = new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(ragProperties.getRag().getTimeoutSeconds()))
                .build();
        String url = config.getBaseUrl().replaceAll("/+$", "") + "/chat/completions";
        Map<String, Object> payload = Map.of("model", config.getModel(), "messages", messages);

        try {
            JsonNode response = client.postForObject(url, request(payload, config.getApiKey()), JsonNode.class);
            return response.get("choices").get(0).get("message").get("content").asText();
        } catch (RestClientException | NullPointerException | ArithmeticException e) {
            throw new BusinessException(502, 502, "LLM api failed: " + e.getMessage());
        }
    }

    public void streamChat(List<Map<String, String>> messages, Consumer<String> onChunk) {
        RagProperties.Llm config = ragProperties.getLlm();
        requireConfig(config.getBaseUrl(), "LLM API base URL");
        requireConfig(config.getApiKey(), "LLM API key");

        String url = config.getBaseUrl().replaceAll("/+$", "") + "/chat/completions";
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", config.getModel());
        payload.put("messages", messages);
        payload.put("stream", true);

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + config.getApiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) {
                String body = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                throw new BusinessException(502, 502, "LLM api failed: " + body);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("data:")) {
                        continue;
                    }
                    String data = line.substring(5).trim();
                    if (data.isEmpty() || "[DONE]".equals(data)) {
                        continue;
                    }
                    JsonNode node = objectMapper.readTree(data);
                    JsonNode choices = node.get("choices");
                    if (choices == null || choices.isEmpty()) {
                        continue;
                    }
                    JsonNode delta = choices.get(0).path("delta").path("content");
                    if (delta == null || delta.isMissingNode() || delta.isNull()) {
                        continue;
                    }
                    String chunk = delta.asText();
                    if (!chunk.isEmpty()) {
                        onChunk.accept(chunk);
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(502, 502, "LLM api interrupted");
        } catch (BusinessException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
            throw new BusinessException(502, 502, "LLM api failed: " + e.getMessage());
        }
    }

    private HttpEntity<Map<String, Object>> request(Map<String, Object> payload, String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(payload, headers);
    }

    private void requireConfig(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(503, 503, name + " is not configured");
        }
    }
}
