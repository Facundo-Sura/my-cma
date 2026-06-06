package com.mycma.core.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Represents a social media post (draft, scheduled, or published).
 * Pure domain entity — no framework annotations.
 */
public class Post {

    public enum Status {
        DRAFT,
        SCHEDULED,
        PUBLISHED,
        FAILED
    }

    private final String id;
    private final String contentText;
    private final List<String> mediaPaths;
    private final List<String> hashtags;
    private final String provider;         // target social network
    private final String providerPostId;   // ID returned after publishing
    private final Instant scheduledAt;
    private final Instant publishedAt;
    private final Status status;
    private final String errorMessage;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Post(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id must not be null");
        this.contentText = builder.contentText;
        this.mediaPaths = builder.mediaPaths != null ? List.copyOf(builder.mediaPaths) : List.of();
        this.hashtags = builder.hashtags != null ? List.copyOf(builder.hashtags) : List.of();
        this.provider = builder.provider;
        this.providerPostId = builder.providerPostId;
        this.scheduledAt = builder.scheduledAt;
        this.publishedAt = builder.publishedAt;
        this.status = builder.status != null ? builder.status : Status.DRAFT;
        this.errorMessage = builder.errorMessage;
        this.createdAt = Objects.requireNonNull(builder.createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(builder.updatedAt, "updatedAt must not be null");
    }

    // Getters
    public String getId() { return id; }
    public String getContentText() { return contentText; }
    public List<String> getMediaPaths() { return mediaPaths; }
    public List<String> getHashtags() { return hashtags; }
    public String getProvider() { return provider; }
    public String getProviderPostId() { return providerPostId; }
    public Instant getScheduledAt() { return scheduledAt; }
    public Instant getPublishedAt() { return publishedAt; }
    public Status getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public boolean isDraft() { return status == Status.DRAFT; }
    public boolean isScheduled() { return status == Status.SCHEDULED; }
    public boolean isPublished() { return status == Status.PUBLISHED; }
    public boolean isFailed() { return status == Status.FAILED; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id;
        private String contentText;
        private List<String> mediaPaths;
        private List<String> hashtags;
        private String provider;
        private String providerPostId;
        private Instant scheduledAt;
        private Instant publishedAt;
        private Status status;
        private String errorMessage;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder contentText(String contentText) { this.contentText = contentText; return this; }
        public Builder mediaPaths(List<String> mediaPaths) { this.mediaPaths = mediaPaths; return this; }
        public Builder hashtags(List<String> hashtags) { this.hashtags = hashtags; return this; }
        public Builder provider(String provider) { this.provider = provider; return this; }
        public Builder providerPostId(String providerPostId) { this.providerPostId = providerPostId; return this; }
        public Builder scheduledAt(Instant scheduledAt) { this.scheduledAt = scheduledAt; return this; }
        public Builder publishedAt(Instant publishedAt) { this.publishedAt = publishedAt; return this; }
        public Builder status(Status status) { this.status = status; return this; }
        public Builder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }
        public Post build() { return new Post(this); }
    }
}
