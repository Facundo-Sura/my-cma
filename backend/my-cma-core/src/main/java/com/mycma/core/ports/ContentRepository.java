package com.mycma.core.ports;

import com.mycma.core.domain.AiGeneration;
import com.mycma.core.domain.Post;
import com.mycma.core.domain.SocialAccount;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Output port for data persistence.
 * The persistence adapter implements this interface.
 */
public interface ContentRepository {

    // Social accounts
    SocialAccount saveAccount(SocialAccount account);
    Optional<SocialAccount> findAccountById(String id);
    Optional<SocialAccount> findAccountByProvider(String provider);
    List<SocialAccount> findAllAccounts();
    void deleteAccount(String id);

    // Posts
    Post savePost(Post post);
    Optional<Post> findPostById(String id);
    List<Post> findAllPosts();
    List<Post> findPostsByStatus(Post.Status status);
    List<Post> findPostsByProvider(String provider);
    List<Post> findPostsScheduledBetween(Instant from, Instant to);
    void deletePost(String id);

    // AI generations
    AiGeneration saveGeneration(AiGeneration generation);
    List<AiGeneration> findAllGenerations();
}
