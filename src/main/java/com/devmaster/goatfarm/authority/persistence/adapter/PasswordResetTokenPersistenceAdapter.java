package com.devmaster.goatfarm.authority.persistence.adapter;

import com.devmaster.goatfarm.authority.application.ports.out.PasswordResetTokenPersistencePort;
import com.devmaster.goatfarm.authority.business.bo.PasswordResetTokenRecord;
import com.devmaster.goatfarm.authority.persistence.entity.PasswordResetToken;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.authority.persistence.mapper.AuthorityPersistenceMapper;
import com.devmaster.goatfarm.authority.persistence.repository.PasswordResetTokenRepository;
import com.devmaster.goatfarm.authority.persistence.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class PasswordResetTokenPersistenceAdapter implements PasswordResetTokenPersistencePort {

    private final PasswordResetTokenRepository repository;
    private final UserRepository userRepository;
    private final AuthorityPersistenceMapper mapper;

    public PasswordResetTokenPersistenceAdapter(PasswordResetTokenRepository repository, UserRepository userRepository,
                                                AuthorityPersistenceMapper mapper) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<PasswordResetTokenRecord> findByTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash).map(mapper::toPasswordResetToken);
    }

    @Override
    public Optional<PasswordResetTokenRecord> findLatestByUserId(Long userId) {
        return repository.findTopByUser_IdOrderByCreatedAtDesc(userId).map(mapper::toPasswordResetToken);
    }

    @Override
    public PasswordResetTokenRecord save(PasswordResetTokenRecord token) {
        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new IllegalStateException("Usuário do token não encontrado"));
        return mapper.toPasswordResetToken(repository.save(mapper.toEntity(token, user)));
    }

    @Override
    public void revokeActiveTokens(Long userId, Instant revokedAt, Instant referenceTime) {
        repository.revokeActiveTokens(userId, revokedAt, referenceTime);
    }
}
