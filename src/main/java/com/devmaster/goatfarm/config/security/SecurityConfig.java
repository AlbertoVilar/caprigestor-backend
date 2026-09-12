package com.devmaster.goatfarm.config.security;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Value("${jwt.public.key}")
    private RSAPublicKey rsaPublicKey;

    @Value("${jwt.private.key}")
    private RSAPrivateKey rsaPrivateKey;

    @Value("${security.jwt.issuer:https://caprigestor.local}")
    private String jwtIssuer;

    @Value("${security.jwt.audience:caprigestor-api}")
    private String jwtAudience;

    @Value("${security.jwt.key-id:caprigestor-current}")
    private String jwtKeyId;

    private final UserDetailsService userDetailsService;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    public SecurityConfig(UserDetailsService userDetailsService, CustomAuthenticationEntryPoint customAuthenticationEntryPoint, CustomAccessDeniedHandler customAccessDeniedHandler) {
        this.userDetailsService = userDetailsService;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain publicEndpointsFilterChain(HttpSecurity http) throws Exception {
        http
            // Torna públicos apenas os endpoints de autenticação explícitos (login/register/refresh)
            // Exclui "/api/v1/auth/me" para que ele seja tratado pelo filtro JWT e exija autenticação
            .securityMatcher(
                    "/api/v1/auth/login",
                    "/api/v1/auth/register",
                    "/api/v1/auth/refresh",
                    "/api/v1/auth/logout",
                    "/api/v1/auth/register-farm",
                    "/api/v1/auth/password-reset/request",
                    "/api/v1/auth/password-reset/confirm",
                    "/public/**", "/h2-console/**", "/swagger-ui/**", "/v3/api-docs/**",
                    "/actuator/health", "/actuator/health/**")
            .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions().disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .cors(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/actuator/**")
            .authorizeHttpRequests(authorize -> authorize.anyRequest().denyAll())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .cors(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/v1/**")
            .authorizeHttpRequests(authorize -> authorize
                // Fazendas (público - leitura)
                .requestMatchers(HttpMethod.GET,
                        "/api/v1/goatfarms",
                        "/api/v1/goatfarms/*",
                        "/api/v1/goatfarms/name").permitAll()
                // Cadastro completo de fazenda é o fluxo público de onboarding.
                // A autorização do proprietário é definida internamente e não vem do payload.
                .requestMatchers(HttpMethod.POST, "/api/v1/goatfarms").permitAll()
                // Consultas de cabras dentro da fazenda (públicas)
                .requestMatchers(HttpMethod.GET,
                        "/api/v1/goatfarms/*/goats",
                        "/api/v1/goatfarms/*/goats/*",
                        "/api/v1/goatfarms/*/goats/search",
                        "/api/v1/goatfarms/*/goats/*/offspring",
                        "/api/v1/goatfarms/*/goats/imports/abcc/races").permitAll()
                // Consultas ABCC públicas e somente leitura
                .requestMatchers(HttpMethod.POST,
                        "/api/v1/goatfarms/*/goats/imports/abcc/search",
                        "/api/v1/goatfarms/*/goats/imports/abcc/preview",
                        "/api/v1/goatfarms/*/goats/imports/abcc/registration-lookup").permitAll()
                // Genealogias públicas (apenas leitura)
                .requestMatchers(HttpMethod.GET,
                        "/api/v1/goatfarms/*/goats/*/genealogies").permitAll()
                .requestMatchers("/api/v1/articles/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/api/v1/users/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.POST, "/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_OPERATOR", "ROLE_FARM_OWNER")
                .requestMatchers(HttpMethod.PUT, "/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_OPERATOR", "ROLE_FARM_OWNER")
                .requestMatchers(HttpMethod.DELETE, "/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_OPERATOR", "ROLE_FARM_OWNER")
                // Qualquer outra requisição exige autenticação
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(customAuthenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler)
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                .jwtAuthenticationConverter(jwtAuthenticationConverter())
            ))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .cors(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(this.rsaPublicKey).build();
        decoder.setJwtValidator(jwtValidator("access"));
        return decoder;
    }

    @Bean("refreshJwtDecoder")
    public JwtDecoder refreshJwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(this.rsaPublicKey).build();
        decoder.setJwtValidator(jwtValidator("refresh"));
        return decoder;
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        try {
            JWK jwk = new RSAKey.Builder(this.rsaPublicKey)
                    .privateKey(this.rsaPrivateKey)
                    .keyID(jwtKeyId)
                    .build();
            JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
            return new NimbusJwtEncoder(jwks);
        } catch (Exception e) {
            logger.error("Error creating JwtEncoder: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating JwtEncoder", e);
        }
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("");
        
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        
        return converter;
    }

    private OAuth2TokenValidator<Jwt> jwtValidator(String expectedType) {
        OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(jwtIssuer);
        OAuth2TokenValidator<Jwt> audienceValidator = token -> token.getAudience().contains(jwtAudience)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new org.springframework.security.oauth2.core.OAuth2Error(
                        "invalid_token", "JWT audience inválida", null));
        OAuth2TokenValidator<Jwt> typeValidator = token -> expectedType.equals(token.getClaimAsString("typ"))
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new org.springframework.security.oauth2.core.OAuth2Error(
                        "invalid_token", "JWT type inválido", null));
        return new DelegatingOAuth2TokenValidator<>(issuerValidator, audienceValidator, typeValidator);
    }
}
