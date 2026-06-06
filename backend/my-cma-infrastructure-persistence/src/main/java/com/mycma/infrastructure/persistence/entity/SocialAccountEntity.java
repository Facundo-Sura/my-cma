package com.mycma.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * JPA entity for social media accounts.
 */
@Entity
@Table(name = "social_accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialAccountEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 20)
    private String provider;

    @Column(nullable = false, length = 1024)
    private String accessToken;

    @Column(length = 1024)
    private String refreshToken;

    @Column(name = "provider_user_id", length = 255)
    private String providerUserId;

    @Column(name = "page_id", length = 255)
    private String pageId;

    @Column(name = "page_name", length = 255)
    private String pageName;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "connected_at", nullable = false)
    private Instant connectedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
