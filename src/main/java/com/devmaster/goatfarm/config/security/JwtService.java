package com.devmaster.goatfarm.config.security;

import com.devmaster.goatfarm.authority.persistence.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final long accessTokenDurationSeconds;
    private final long refreshTokenDurationSeconds;
    private final String issuer;
    private final String audience;
    private final String keyId;
    private final Clock clock;

    public JwtService(JwtEncoder jwtEncoder,
                      @Value("${security.jwt.duration:900}") long accessTokenDurationSeconds,
                      @Value("${security.jwt.refresh-duration:604800}") long refreshTokenDurationSeconds,
                      @Value("${security.jwt.issuer:https://caprigestor.local}") String issuer,
                      @Value("${security.jwt.audience:caprigestor-api}") String audience,
                      @Value("${security.jwt.key-id:caprigestor-current}") String keyId) {
        this.jwtEncoder = jwtEncoder;
        this.accessTokenDurationSeconds = requirePositiveDuration("security.jwt.duration", accessTokenDurationSeconds);
        this.refreshTokenDurationSeconds = requirePositiveDuration("security.jwt.refresh-duration", refreshTokenDurationSeconds);
        this.issuer = requireText("security.jwt.issuer", issuer);
        this.audience = requireText("security.jwt.audience", audience);
        this.keyId = requireText("security.jwt.key-id", keyId);
        this.clock = Clock.systemUTC();
    }

    public String generateToken(User user) {
        Instant now = Instant.now(clock);
        String scope = user.getRoles().stream().map(role -> role.getAuthority()).collect(Collectors.joining(" "));
        JwtClaimsSet claims = baseClaims(user, now, accessTokenDurationSeconds, "access", UUID.randomUUID())
                .claim("scope", scope)
                .claim("name", user.getName())
                .claim("email", user.getEmail())
                .build();
        return encode(claims);
    }

    public IssuedRefreshToken issueRefreshToken(User user, UUID familyId) {
        Instant now = Instant.now(clock);
        UUID tokenId = UUID.randomUUID();
        UUID resolvedFamilyId = familyId == null ? UUID.randomUUID() : familyId;
        Instant expiresAt = now.plusSeconds(refreshTokenDurationSeconds);
        JwtClaimsSet claims = baseClaims(user, now, refreshTokenDurationSeconds, "refresh", tokenId)
                .claim("scope", "REFRESH")
                .claim("familyId", resolvedFamilyId.toString())
                .build();
        return new IssuedRefreshToken(encode(claims), tokenId, resolvedFamilyId, now, expiresAt);
    }

    public long getAccessTokenDurationSeconds() {
        return accessTokenDurationSeconds;
    }

    private JwtClaimsSet.Builder baseClaims(User user, Instant now, long durationSeconds, String type, UUID tokenId) {
        return JwtClaimsSet.builder()
                .issuer(issuer)
                .audience(List.of(audience))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(durationSeconds))
                .subject(user.getEmail())
                .id(tokenId.toString())
                .claim("typ", type)
                .claim("userId", user.getId());
    }

    private String encode(JwtClaimsSet claims) {
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).type("JWT").keyId(keyId).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private long requirePositiveDuration(String propertyName, long durationSeconds) {
        if (durationSeconds <= 0) {
            throw new IllegalArgumentException(propertyName + " deve ser maior que zero.");
        }
        return durationSeconds;
    }

    private String requireText(String propertyName, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(propertyName + " deve ser informado.");
        }
        return value;
    }

    public record IssuedRefreshToken(String token, UUID tokenId, UUID familyId, Instant issuedAt, Instant expiresAt) {
    }
}
