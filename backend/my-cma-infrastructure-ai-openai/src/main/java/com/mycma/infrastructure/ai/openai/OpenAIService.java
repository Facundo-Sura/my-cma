package com.mycma.infrastructure.ai.openai;

import com.mycma.core.domain.AiGeneration;
import com.mycma.core.ports.AIServicePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * OpenAI GPT-4 + DALL-E adapter.
 * Implements AIServicePort for AI content generation.
 */
@Component("openAIService")
public class OpenAIService implements AIServicePort {

    private static final Logger log = LoggerFactory.getLogger(OpenAIService.class);

    private final String apiKey;
    private final String model;
    private final int defaultMaxTokens;
    private final String defaultImageSize;

    public OpenAIService(
            @Value("${mycma.ai.openai.api-key}") String apiKey,
            @Value("${mycma.ai.openai.model:gpt-4o}") String model,
            @Value("${mycma.ai.openai.max-tokens:1000}") int defaultMaxTokens,
            @Value("${mycma.ai.openai.image-size:1024x1024}") String defaultImageSize) {
        this.apiKey = apiKey;
        this.model = model;
        this.defaultMaxTokens = defaultMaxTokens;
        this.defaultImageSize = defaultImageSize;
    }

    @Override
    public String getProvider() {
        return "openai";
    }

    @Override
    public AiGeneration generateCopy(String prompt, String tone, int maxTokens) {
        // TODO: Implement GPT-4 API call for copy generation
        log.info("Generating copy with OpenAI: tone={}, maxTokens={}", tone, maxTokens);
        throw new UnsupportedOperationException("OpenAI copy generation not yet implemented");
    }

    @Override
    public Optional<AiGeneration> generateImage(String prompt, String size, String outputPath) {
        // TODO: Implement DALL-E 3 API call for image generation
        log.info("Generating image with OpenAI DALL-E: size={}, output={}", size, outputPath);
        throw new UnsupportedOperationException("OpenAI image generation not yet implemented");
    }
}
