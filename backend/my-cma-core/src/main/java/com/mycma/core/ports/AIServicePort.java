package com.mycma.core.ports;

import com.mycma.core.domain.AiGeneration;

import java.util.Optional;

/**
 * Output port for AI content generation services.
 * Each AI provider (OpenAI, Qwen, etc.) implements this interface.
 */
public interface AIServicePort {

    /**
     * Returns the provider name (e.g., "openai", "qwen").
     */
    String getProvider();

    /**
     * Generates text content (post copy) based on a prompt.
     *
     * @param prompt      the user's instruction for the AI
     * @param tone        the desired tone (professional, casual, humorous, etc.)
     * @param maxTokens   maximum tokens for the response
     * @return generated content result
     */
    AiGeneration generateCopy(String prompt, String tone, int maxTokens);

    /**
     * Generates an image based on a prompt (DALL-E, Stable Diffusion, etc.).
     *
     * @param prompt      the image description
     * @param size        image size (e.g., "1024x1024")
     * @param outputPath  local file path to save the generated image
     * @return generation result or empty if provider doesn't support images
     */
    Optional<AiGeneration> generateImage(String prompt, String size, String outputPath);
}
