package com.devmaster.goatfarm.authority.persistence.adapter;

import com.devmaster.goatfarm.authority.application.ports.out.RefreshSessionPersistencePort;
import com.devmaster.goatfarm.authority.business.bo.RefreshSessionRecord;
import com.devmaster.goatfarm.authority.persistence.entity.RefreshSession;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.authority.persistence.mapper.AuthorityPersistenceMapper;
import com.devmaster.goatfarm.authority.persistence.repository.RefreshSessionRepository;
import com.devmaster.goatfarm.authority.persistence.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class RefreshSessionPersistenceAdapter implements RefreshSessionPersistencePort {

    private final RefreshSessionRepository repository;
    private final UserRepository userRepository;
    private final AuthorityPersistenceMapper mapper;

    public RefreshSessionPersistenceAdapter(RefreshSessionRepository repository, UserRepository userRepository,
                                            AuthorityPersistenceMapper mapper) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<RefreshSessionRecord> findByTokenHashForUpdate(String tokenHash) {
        return repository.findByTokenHashForUpdate(tokenHash).map(mapper::toRefreshSession);
    }

    @Override
    public RefreshSessionRecord save(RefreshSessionRecord refreshSession) {
        User user = userRepository.findById(refreshSession.getUser().id())
                .orElseThrow(() -> new IllegalStateException("Usuário da sessão não encontrado"));
        return mapper.toRefreshSession(repository.save(mapper.toEntity(refreshSession, user)));
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
