package com.mycma.core.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a connected social media account.
 * Pure domain entity — no framework annotations.
 */
public class SocialAccount {

    private final String id;
    private final String provider;       // facebook, instagram, linkedin
    private final String accessToken;
    private final String refreshToken;
    private final String providerUserId;
    private final String pageId;
    private final String pageName;
    private final Instant expiresAt;
    private final Instant connectedAt;

    public SocialAccount(String id, String provider, String accessToken, String refreshToken,
                         String providerUserId, String pageId, String pageName,
                         Instant expiresAt, Instant connectedAt) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.provider = Objects.requireNonNull(provider, "provider must not be null");
        this.accessToken = Objects.requireNonNull(accessToken, "accessToken must not be null");
        this.refreshToken = refreshToken;
        this.providerUserId = providerUserId;
        this.pageId = pageId;
        this.pageName = pageName;
        this.expiresAt = expiresAt;
        this.connectedAt = Objects.requireNonNull(connectedAt, "connectedAt must not be null");
    }

    public String getId() { return id; }
    public String getProvider() { return provider; }
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getProviderUserId() { return providerUserId; }
    public String getPageId() { return pageId; }
    public String getPageName() { return pageName; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getConnectedAt() { return connectedAt; }

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SocialAccount that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
