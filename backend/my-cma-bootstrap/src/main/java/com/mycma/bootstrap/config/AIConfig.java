package com.mycma.bootstrap.config;

import com.mycma.core.ports.AIServicePort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Pluggable AI provider configuration.
 * Currently configured for OpenAI. Switch provider by changing the profile
 * or the active provider property.
 */
@Configuration
public class AIConfig {

    @Value("${mycma.ai.active-provider:openai}")
    private String activeProvider;

    /**
     * Provides the currently active AI service port.
     * Additional providers (Qwen, Gemini, etc.) can be added and selected here.
     */
    @Bean
    public AIServicePort activeAIService(
            @Qualifier("openAIService") AIServicePort openAIService) {

        return switch (activeProvider.toLowerCase()) {
            // case "qwen" -> qwenService;
            // case "gemini" -> geminiService;
            default -> openAIService;
        };
    }
}
