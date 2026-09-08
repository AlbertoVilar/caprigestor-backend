package com.devmaster.goatfarm.authority.persistence.adapter;

import com.devmaster.goatfarm.authority.application.ports.out.RefreshSessionPersistencePort;
import com.devmaster.goatfarm.authority.persistence.entity.RefreshSession;
import com.devmaster.goatfarm.authority.persistence.repository.RefreshSessionRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class RefreshSessionPersistenceAdapter implements RefreshSessionPersistencePort {

    private final RefreshSessionRepository repository;

    public RefreshSessionPersistenceAdapter(RefreshSessionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<RefreshSession> findByTokenHashForUpdate(String tokenHash) {
        return repository.findByTokenHashForUpdate(tokenHash);
    }

    @Override
    public RefreshSession save(RefreshSession refreshSession) {
        return repository.save(refreshSession);
    }

    @Override
    public boolean consumeIfActive(Long sessionId, Instant consumedAt) {
        return repository.consumeIfActive(sessionId, consumedAt) == 1;
    }

    @Override
    public void setReplacement(Long sessionId, Long replacementId) {
        repository.setReplacement(sessionId, replacementId);
    }

    @Override
    public void revokeFamily(UUID familyId, Instant revokedAt, String reason) {
        repository.revokeFamily(familyId, revokedAt, reason);
    }

    @Override
    public void revokeAllForUser(Long userId, Instant revokedAt, String reason) {
        repository.revokeAllForUser(userId, revokedAt, reason);
    }
}
