package com.devmaster.goatfarm.authority.application.ports.out;

import com.devmaster.goatfarm.authority.persistence.entity.PasswordResetToken;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenPersistencePort {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    Optional<PasswordResetToken> findLatestByUserId(Long userId);

    PasswordResetToken save(PasswordResetToken token);

    void revokeActiveTokens(Long userId, Instant revokedAt, Instant referenceTime);
}
