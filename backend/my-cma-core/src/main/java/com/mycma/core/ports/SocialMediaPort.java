package com.mycma.core.ports;

import com.mycma.core.domain.Post;
import com.mycma.core.domain.SocialAccount;

import java.util.Optional;

/**
 * Output port for social media platform operations.
 * Each social network adapter implements this interface.
 */
public interface SocialMediaPort {

    /**
     * Returns the provider name (e.g., "facebook", "instagram", "linkedin").
     */
    String getProvider();

    /**
     * Exchanges an authorization code for an access token.
     */
    SocialAccount authenticate(String authorizationCode, String redirectUri);

    /**
     * Refreshes an expired access token.
     */
    SocialAccount refreshToken(SocialAccount expiredAccount);

    /**
     * Publishes a post to the social network.
     *
     * @return the provider's post ID
     */
    String publish(Post post, SocialAccount account);

    /**
     * Retrieves analytics for a published post.
     */
    Optional<String> getAnalytics(String providerPostId, SocialAccount account);
}
