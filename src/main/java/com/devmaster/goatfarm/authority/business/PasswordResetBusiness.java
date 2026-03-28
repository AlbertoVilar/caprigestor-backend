package com.devmaster.goatfarm.authority.business;

import com.devmaster.goatfarm.authority.application.ports.in.PasswordResetManagementUseCase;
import com.devmaster.goatfarm.authority.application.ports.out.PasswordResetMailPort;
import com.devmaster.goatfarm.authority.application.ports.out.PasswordResetTokenPersistencePort;
import com.devmaster.goatfarm.authority.application.ports.out.UserPersistencePort;
import com.devmaster.goatfarm.authority.business.bo.PasswordResetConfirmVO;
import com.devmaster.goatfarm.authority.business.bo.PasswordResetRequestVO;
import com.devmaster.goatfarm.authority.business.bo.PasswordResetResponseVO;
import com.devmaster.goatfarm.authority.persistence.entity.PasswordResetToken;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
@Transactional
public class PasswordResetBusiness implements PasswordResetManagementUseCase {

    static final String NEUTRAL_MESSAGE = "Se existir uma conta com esse email, enviaremos um link de redefinicao.";
    static final String SUCCESS_MESSAGE = "Senha redefinida com sucesso.";

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetBusiness.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserPersistencePort userPersistencePort;
    private final PasswordResetTokenPersistencePort passwordResetTokenPersistencePort;
    private final PasswordResetMailPort passwordResetMailPort;
    private final PasswordEncoder passwordEncoder;
    private final long ttlMinutes;
    private final long cooldownSeconds;
    private final Clock clock;

    @Autowired
    public PasswordResetBusiness(UserPersistencePort userPersistencePort,
                                 PasswordResetTokenPersistencePort passwordResetTokenPersistencePort,
                                 PasswordResetMailPort passwordResetMailPort,
                                 PasswordEncoder passwordEncoder,
                                 @Value("${caprigestor.auth.password-reset.ttl-minutes:30}") long ttlMinutes,
                                 @Value("${caprigestor.auth.password-reset.cooldown-seconds:60}") long cooldownSeconds) {
        this(userPersistencePort, passwordResetTokenPersistencePort, passwordResetMailPort, passwordEncoder,
                ttlMinutes, cooldownSeconds, Clock.systemUTC());
    }

    PasswordResetBusiness(UserPersistencePort userPersistencePort,
                          PasswordResetTokenPersistencePort passwordResetTokenPersistencePort,
                          PasswordResetMailPort passwordResetMailPort,
                          PasswordEncoder passwordEncoder,
                          long ttlMinutes,
                          long cooldownSeconds,
                          Clock clock) {
        this.userPersistencePort = userPersistencePort;
        this.passwordResetTokenPersistencePort = passwordResetTokenPersistencePort;
        this.passwordResetMailPort = passwordResetMailPort;
        this.passwordEncoder = passwordEncoder;
        this.ttlMinutes = ttlMinutes;
        this.cooldownSeconds = cooldownSeconds;
        this.clock = clock;
    }

    @Override
    public PasswordResetResponseVO requestPasswordReset(PasswordResetRequestVO requestVO) {
        String normalizedEmail = normalizeEmail(requestVO == null ? null : requestVO.getEmail());
        User user = userPersistencePort.findByEmail(normalizedEmail).orElse(null);
        if (user == null) {
            return neutralResponse();
        }

        Instant now = now();
        if (isCooldownActive(user.getId(), now)) {
            logger.info("Cooldown de recuperacao de senha ativo para userId={}", user.getId());
            return neutralResponse();
        }

        passwordResetTokenPersistencePort.revokeActiveTokens(user.getId(), now, now);

        String rawToken = generateRawToken();
        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .tokenHash(hashToken(rawToken))
                .createdAt(now)
                .expiresAt(now.plus(Duration.ofMinutes(ttlMinutes)))
                .build();

        PasswordResetToken persisted = passwordResetTokenPersistencePort.save(token);

        try {
            passwordResetMailPort.sendPasswordResetMail(normalizedEmail, rawToken, Duration.ofMinutes(ttlMinutes));
        } catch (RuntimeException ex) {
            logger.warn("Falha ao enviar email de recuperacao para userId={}: {}", user.getId(), ex.getMessage());
            persisted.setRevokedAt(now());
            passwordResetTokenPersistencePort.save(persisted);
        }

        return neutralResponse();
    }

    @Override
    public PasswordResetResponseVO confirmPasswordReset(PasswordResetConfirmVO confirmVO) {
        String rawToken = normalizeRequired(confirmVO == null ? null : confirmVO.getToken(), "token", "Token de redefinicao invalido.");
        String newPassword = normalizeRequired(confirmVO == null ? null : confirmVO.getNewPassword(), "newPassword", "Nova senha e obrigatoria.");
        String confirmPassword = normalizeRequired(confirmVO == null ? null : confirmVO.getConfirmPassword(), "confirmPassword", "Confirmacao de senha e obrigatoria.");

        if (newPassword.length() < 6) {
            throw new InvalidArgumentException("newPassword", "Nova senha deve ter pelo menos 6 caracteres.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new InvalidArgumentException("confirmPassword", "Confirmacao de senha nao confere.");
        }

        PasswordResetToken token = passwordResetTokenPersistencePort.findByTokenHash(hashToken(rawToken))
                .orElseThrow(() -> new InvalidArgumentException("token", "Token de redefinicao invalido."));

        Instant now = now();
        if (token.getUsedAt() != null) {
            throw new InvalidArgumentException("token", "Token de redefinicao ja foi utilizado.");
        }
        if (token.getRevokedAt() != null) {
            throw new InvalidArgumentException("token", "Token de redefinicao ja foi invalidado.");
        }
        if (token.getExpiresAt() == null || !token.getExpiresAt().isAfter(now)) {
            throw new InvalidArgumentException("token", "Token de redefinicao expirado.");
        }

        userPersistencePort.updatePassword(token.getUser().getId(), passwordEncoder.encode(newPassword));
        token.setUsedAt(now);
        passwordResetTokenPersistencePort.save(token);
        passwordResetTokenPersistencePort.revokeActiveTokens(token.getUser().getId(), now, now);

        return PasswordResetResponseVO.builder()
                .message(SUCCESS_MESSAGE)
                .build();
    }

    private boolean isCooldownActive(Long userId, Instant now) {
        return passwordResetTokenPersistencePort.findLatestByUserId(userId)
                .map(PasswordResetToken::getCreatedAt)
                .filter(createdAt -> createdAt.plusSeconds(cooldownSeconds).isAfter(now))
                .isPresent();
    }

    private PasswordResetResponseVO neutralResponse() {
        return PasswordResetResponseVO.builder()
                .message(NEUTRAL_MESSAGE)
                .build();
    }

    private String normalizeEmail(String email) {
        String normalized = normalizeRequired(email, "email", "Email e obrigatorio.");
        return normalized.toLowerCase(Locale.ROOT);
    }

    private String normalizeRequired(String value, String fieldName, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidArgumentException(fieldName, message);
        }
        return value.trim();
    }

    private Instant now() {
        return Instant.now(clock);
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 nao disponivel no ambiente", ex);
        }
    }
}
