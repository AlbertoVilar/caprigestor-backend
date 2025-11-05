package com.devmaster.goatfarm.config.security;

import com.devmaster.goatfarm.authority.model.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private JwtEncoder jwtEncoder;

    public String generateToken(User user) {
        try {
            logger.debug("ðŸ” JWT: Iniciando geraÃ§Ã£o de token para usuÃ¡rio: {}", user.getEmail());
            
            Instant now = Instant.now();
            long expiry = 24L;             
            logger.debug("ðŸ” JWT: Coletando roles do usuÃ¡rio...");
            String scope = user.getRoles()
                    .stream()
                    .map(role -> role.getAuthority())
                    .collect(Collectors.joining(" "));
            logger.debug("ðŸ” JWT: Scope gerado: {}", scope);

            logger.debug("ðŸ” JWT: Construindo claims...");
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
            
            logger.debug("ðŸ” JWT: Codificando token...");
            String token = this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
            logger.debug("ðŸ” JWT: Token gerado com sucesso, tamanho: {}", token.length());
            
            return token;
        } catch (Exception e) {
            logger.error("ðŸ” JWT ERROR: Erro ao gerar token - {}: {}", e.getClass().getSimpleName(), e.getMessage());
            e.printStackTrace();
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
