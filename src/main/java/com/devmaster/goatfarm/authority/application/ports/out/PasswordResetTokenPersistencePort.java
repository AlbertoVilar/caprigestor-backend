package com.devmaster.goatfarm.authority.application.ports.out;

import com.devmaster.goatfarm.authority.business.bo.PasswordResetTokenRecord;

import java.time.Instant;
import java.util.Optional;

public interface PasswordResetTokenPersistencePort {

    Optional<PasswordResetTokenRecord> findByTokenHash(String tokenHash);

    Optional<PasswordResetTokenRecord> findLatestByUserId(Long userId);

    PasswordResetTokenRecord save(PasswordResetTokenRecord token);

    void revokeActiveTokens(Long userId, Instant revokedAt, Instant referenceTime);
}
