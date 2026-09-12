package com.devmaster.goatfarm.authority.application.ports.out;

import com.devmaster.goatfarm.authority.business.bo.RefreshSessionRecord;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshSessionPersistencePort {

    Optional<RefreshSessionRecord> findByTokenHashForUpdate(String tokenHash);

    RefreshSessionRecord save(RefreshSessionRecord refreshSession);

    boolean consumeIfActive(Long sessionId, Instant consumedAt);

    void setReplacement(Long sessionId, Long replacementId);

    void revokeFamily(UUID familyId, Instant revokedAt, String reason);

    void revokeAllForUser(Long userId, Instant revokedAt, String reason);
}
