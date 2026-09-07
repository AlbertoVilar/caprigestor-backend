package com.devmaster.goatfarm.config.security;

import com.devmaster.goatfarm.authority.persistence.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    private final JwtEncoder jwtEncoder;
    private final long accessTokenDurationSeconds;
    private final long refreshTokenDurationSeconds;

    public JwtService(JwtEncoder jwtEncoder,
                      @Value("${security.jwt.duration:86400}") long accessTokenDurationSeconds,
                      @Value("${security.jwt.refresh-duration:604800}") long refreshTokenDurationSeconds) {
        this.jwtEncoder = jwtEncoder;
        this.accessTokenDurationSeconds = requirePositiveDuration("security.jwt.duration", accessTokenDurationSeconds);
        this.refreshTokenDurationSeconds = requirePositiveDuration("security.jwt.refresh-duration", refreshTokenDurationSeconds);
    }

    public String generateToken(User user) {
        try {
            logger.debug("event=jwt_generation_started userId={}", user.getId());
            
            Instant now = Instant.now();
            String scope = user.getRoles()
                    .stream()
                    .map(role -> role.getAuthority())
                    .collect(Collectors.joining(" "));
            JwtClaimsSet claims = JwtClaimsSet.builder()
                    .issuer("goatfarm-api")
                    .issuedAt(now)
                    .expiresAt(now.plus(accessTokenDurationSeconds, ChronoUnit.SECONDS))
                    .subject(user.getEmail())
                    .claim("scope", scope)
                    .claim("userId", user.getId())
                    .claim("name", user.getName())
                    .claim("email", user.getEmail())
                    .build();
            
            String token = this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
            logger.debug("event=jwt_generation_completed userId={} expiresInSeconds={}", user.getId(), accessTokenDurationSeconds);
            
            return token;
        } catch (Exception e) {
            logger.error("event=jwt_generation_failed userId={} exception={}",
                    user.getId(), e.getClass().getSimpleName(), e);
            throw e;
        }
    }

    public String generateRefreshToken(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("goatfarm-api")
                .issuedAt(now)
                .expiresAt(now.plus(refreshTokenDurationSeconds, ChronoUnit.SECONDS))
                .subject(user.getEmail())
                .claim("scope", "REFRESH")
                .claim("userId", user.getId())
                .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    private long requirePositiveDuration(String propertyName, long durationSeconds) {
        if (durationSeconds <= 0) {
            throw new IllegalArgumentException(propertyName + " deve ser maior que zero.");
        }
        return durationSeconds;
    }
}
