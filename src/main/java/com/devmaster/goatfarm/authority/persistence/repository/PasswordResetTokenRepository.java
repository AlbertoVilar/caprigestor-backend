package com.devmaster.goatfarm.authority.persistence.repository;

import com.devmaster.goatfarm.authority.persistence.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    Optional<PasswordResetToken> findTopByUser_IdOrderByCreatedAtDesc(Long userId);

    @Modifying
    @Transactional
    @Query("""
            UPDATE PasswordResetToken token
               SET token.revokedAt = :revokedAt
             WHERE token.user.id = :userId
               AND token.revokedAt IS NULL
               AND token.usedAt IS NULL
               AND token.expiresAt > :referenceTime
            """)
    void revokeActiveTokens(Long userId, Instant revokedAt, Instant referenceTime);
}
