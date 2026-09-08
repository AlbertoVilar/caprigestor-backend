package com.devmaster.goatfarm.authority.application.ports.out;

import com.devmaster.goatfarm.authority.persistence.entity.RefreshSession;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshSessionPersistencePort {

    Optional<RefreshSession> findByTokenHashForUpdate(String tokenHash);

    RefreshSession save(RefreshSession refreshSession);

    boolean consumeIfActive(Long sessionId, Instant consumedAt);

    void setReplacement(Long sessionId, Long replacementId);

    void revokeFamily(UUID familyId, Instant revokedAt, String reason);

    void revokeAllForUser(Long userId, Instant revokedAt, String reason);
}
