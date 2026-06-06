package com.mycma.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * My CMA — Community Manager Assistant
 * <p>
 * Entry point for the Spring Boot backend application.
 * Scans all modules for components, entities, and repositories.
 */
@SpringBootApplication(scanBasePackages = "com.mycma")
@EntityScan(basePackages = "com.mycma.infrastructure.persistence.entity")
@EnableJpaRepositories(basePackages = "com.mycma.infrastructure.persistence.repository")
public class SocialMediaCMApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialMediaCMApplication.class, args);
    }
}
