package com.mycma.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * JPA entity for social media posts.
 */
@Entity
@Table(name = "posts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostEntity {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "content_text", columnDefinition = "TEXT")
    private String contentText;

    @Column(name = "media_paths_json", columnDefinition = "TEXT")
    private String mediaPathsJson;

    @Column(name = "hashtags_json", columnDefinition = "TEXT")
    private String hashtagsJson;

    @Column(length = 20)
    private String provider;

    @Column(name = "provider_post_id", length = 255)
    private String providerPostId;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PostStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

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

    public enum PostStatus {
        DRAFT, SCHEDULED, PUBLISHED, FAILED
    }
}
