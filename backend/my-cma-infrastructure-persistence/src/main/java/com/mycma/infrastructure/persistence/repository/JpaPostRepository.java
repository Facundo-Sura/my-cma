package com.mycma.infrastructure.persistence.repository;

import com.mycma.infrastructure.persistence.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Spring Data JPA repository for posts.
 */
@Repository
public interface JpaPostRepository extends JpaRepository<PostEntity, String> {

    List<PostEntity> findByStatus(PostEntity.PostStatus status);

    List<PostEntity> findByProvider(String provider);

    List<PostEntity> findByScheduledAtBetween(Instant from, Instant to);
}
