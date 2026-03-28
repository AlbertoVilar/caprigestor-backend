package com.devmaster.goatfarm.authority.persistence.adapter;

import com.devmaster.goatfarm.authority.application.ports.out.PasswordResetTokenPersistencePort;
import com.devmaster.goatfarm.authority.persistence.entity.PasswordResetToken;
import com.devmaster.goatfarm.authority.persistence.repository.PasswordResetTokenRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class PasswordResetTokenPersistenceAdapter implements PasswordResetTokenPersistencePort {

    private final PasswordResetTokenRepository repository;

    public PasswordResetTokenPersistenceAdapter(PasswordResetTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash);
    }

    @Override
    public Optional<PasswordResetToken> findLatestByUserId(Long userId) {
        return repository.findTopByUser_IdOrderByCreatedAtDesc(userId);
    }

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        return repository.save(token);
    }

    @Override
    public void revokeActiveTokens(Long userId, Instant revokedAt, Instant referenceTime) {
        repository.revokeActiveTokens(userId, revokedAt, referenceTime);
    }
}
