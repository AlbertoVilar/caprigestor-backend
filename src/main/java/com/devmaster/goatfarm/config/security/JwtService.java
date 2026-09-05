package com.devmaster.goatfarm.config.security;

import com.devmaster.goatfarm.authority.persistence.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    private final JwtEncoder jwtEncoder;

    public JwtService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String generateToken(User user) {
        try {
            logger.debug("event=jwt_generation_started userId={}", user.getId());
            
            Instant now = Instant.now();
            long expiry = 24L;             
            String scope = user.getRoles()
                    .stream()
                    .map(role -> role.getAuthority())
                    .collect(Collectors.joining(" "));
            JwtClaimsSet claims = JwtClaimsSet.builder()
                    .issuer("goatfarm-api")
                    .issuedAt(now)
                    .expiresAt(now.plus(expiry, ChronoUnit.HOURS))
                    .subject(user.getEmail())
                    .claim("scope", scope)
                    .claim("userId", user.getId())
                    .claim("name", user.getName())
                    .claim("email", user.getEmail())
                    .build();
            
            String token = this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
            logger.debug("event=jwt_generation_completed userId={} expiresInHours={}", user.getId(), expiry);
            
            return token;
        } catch (Exception e) {
            logger.error("event=jwt_generation_failed userId={} exception={}",
                    user.getId(), e.getClass().getSimpleName(), e);
            throw e;
        }
    }

    public String generateRefreshToken(User user) {
        Instant now = Instant.now();
        long expiry = 168L; 
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("goatfarm-api")
                .issuedAt(now)
                .expiresAt(now.plus(expiry, ChronoUnit.HOURS))
                .subject(user.getEmail())
                .claim("scope", "REFRESH")
                .claim("userId", user.getId())
                .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
