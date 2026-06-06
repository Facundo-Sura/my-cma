package com.mycma.infrastructure.persistence.repository;

import com.mycma.infrastructure.persistence.entity.SocialAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for social accounts.
 */
@Repository
public interface JpaSocialAccountRepository extends JpaRepository<SocialAccountEntity, String> {

    Optional<SocialAccountEntity> findByProvider(String provider);
}
