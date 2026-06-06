package com.mycma.infrastructure.ai.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycma.core.domain.AiGeneration;
import com.mycma.core.ports.AIServicePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * OpenAI GPT-4 + DALL-E adapter.
 * Uses RestTemplate to call OpenAI's REST API directly (no Azure SDK needed).
 */
@Component("openAIService")
public class OpenAIService implements AIServicePort {

    private static final Logger log = LoggerFactory.getLogger(OpenAIService.class);
    private static final String OPENAI_API_URL = "https://api.openai.com/v1";
    private static final String CHAT_ENDPOINT = "/chat/completions";
    private static final String IMAGE_ENDPOINT = "/images/generations";

    private final String apiKey;
    private final String model;
    private final int defaultMaxTokens;
    private final String defaultImageSize;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public OpenAIService(
            @Value("${mycma.ai.openai.api-key}") String apiKey,
            @Value("${mycma.ai.openai.model:gpt-4o}") String model,
            @Value("${mycma.ai.openai.max-tokens:1000}") int defaultMaxTokens,
            @Value("${mycma.ai.openai.image-size:1024x1024}") String defaultImageSize) {
        this.apiKey = apiKey;
        this.model = model;
        this.defaultMaxTokens = defaultMaxTokens;
        this.defaultImageSize = defaultImageSize;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getProvider() {
        return "openai";
    }

    @Override
    public AiGeneration generateCopy(String prompt, String tone, int maxTokens) {
        String toneInstruction = switch (tone.toLowerCase()) {
            case "professional" -> "Escribe en un tono profesional y formal.";
            case "casual" -> "Escribe en un tono casual y cercano.";
            case "humorous" -> "Escribe con un tono divertido y con humor.";
            case "inspirational" -> "Escribe con un tono inspirador y motivacional.";
            default -> "Escribe con un tono neutral.";
        };

        String systemPrompt = "Sos un Community Manager Senior con 7+ años de experiencia. " +
                "Generás contenido para redes sociales (Facebook, Instagram, LinkedIn). " +
                toneInstruction + " Usá emojis con moderación. " +
                "Respondé SOLO con el texto del post, sin explicaciones adicionales.";

        try {
            // Build request body
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);
            requestBody.put("max_tokens", maxTokens > 0 ? maxTokens : defaultMaxTokens);

            ArrayNode messages = requestBody.putArray("messages");
            ObjectNode systemMsg = messages.addObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);

            ObjectNode userMsg = messages.addObject();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);

            // Execute request
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    OPENAI_API_URL + CHAT_ENDPOINT,
                    HttpMethod.POST,
                    entity,
                    JsonNode.class);

            // Parse response
            JsonNode responseBody = response.getBody();
            String generatedText = responseBody.get("choices").get(0)
                    .get("message").get("content").asText().trim();

            int tokensUsed = responseBody.get("usage").get("total_tokens").asInt();

            return new AiGeneration(
                    UUID.randomUUID().toString(),
                    AiGeneration.Type.COPY,
                    prompt,
                    generatedText,
                    null,
                    "openai/" + model,
                    tokensUsed,
                    Instant.now());

        } catch (Exception e) {
            log.error("Failed to generate copy with OpenAI: {}", e.getMessage());
            throw new RuntimeException("OpenAI copy generation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<AiGeneration> generateImage(String prompt, String size, String outputPath) {
        String imageSize = (size != null && !size.isBlank()) ? size : defaultImageSize;

        try {
            // Build request body for DALL-E
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", "dall-e-3");
            requestBody.put("prompt", prompt);
            requestBody.put("n", 1);
            requestBody.put("size", imageSize);
            requestBody.put("response_format", "b64_json");

            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    OPENAI_API_URL + IMAGE_ENDPOINT,
                    HttpMethod.POST,
                    entity,
                    JsonNode.class);

            // Parse response
            JsonNode responseBody = response.getBody();
            String base64Image = responseBody.get("data").get(0).get("b64_json").asText();
            String revisedPrompt = responseBody.get("data").get(0).has("revised_prompt")
                    ? responseBody.get("data").get(0).get("revised_prompt").asText()
                    : prompt;

            // Save image to local file
            String finalOutputPath = (outputPath != null && !outputPath.isBlank())
                    ? outputPath
                    : System.getProperty("user.home") + "/.mycma/images/" + UUID.randomUUID() + ".png";

            saveBase64Image(base64Image, finalOutputPath);

            return Optional.of(new AiGeneration(
                    UUID.randomUUID().toString(),
                    AiGeneration.Type.IMAGE,
                    revisedPrompt,
                    null,
                    finalOutputPath,
                    "openai/dall-e-3",
                    0,
                    Instant.now()));

        } catch (Exception e) {
            log.error("Failed to generate image with OpenAI DALL-E: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        return headers;
    }

    private void saveBase64Image(String base64, String outputPath) throws IOException {
        java.nio.file.Files.createDirectories(
                java.nio.file.Paths.get(outputPath).getParent());

        byte[] imageBytes = Base64.getDecoder().decode(base64);
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            fos.write(imageBytes);
        }
    }
}
