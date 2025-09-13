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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
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

    private final UserDetailsService userDetailsService;
    private final JwtDebugFilter jwtDebugFilter;

    // Injeção via construtor é uma prática recomendada
    public SecurityConfig(UserDetailsService userDetailsService, JwtDebugFilter jwtDebugFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtDebugFilter = jwtDebugFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Filtro de segurança para ENDPOINTS PÚBLICOS.
     * Ordem 1: processado primeiro. Não tem validação de JWT.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain publicSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/auth/**", "/h2-console/**", "/swagger-ui/**", "/v3/api-docs/**", "/api/goatfarms/full")
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().permitAll() // Permite tudo que corresponder ao securityMatcher
            )
            .csrf(csrf -> csrf.disable()) // Desabilita CSRF
            .headers(headers -> headers.frameOptions().disable()) // Para H2 console
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // API é stateless

        // A configuração de CORS não é necessária aqui se for aplicada globalmente no próximo filtro
        return http.build();
    }

    /**
     * Filtro de segurança para TODOS os outros endpoints da API.
     * Ordem 2: processado depois dos públicos. Exige e valida JWT.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            // CORREÇÃO: Captura TODOS os endpoints /api/** que não foram pegos pelo filtro de ordem 1
            .securityMatcher("/api/**")
            .authorizeHttpRequests(authorize -> authorize
                // Regras específicas PRIMEIRO (mais específicas têm prioridade)
                .requestMatchers(HttpMethod.POST, "/api/auth/register-farm").permitAll()
                
                // Regras de permissão para leitura pública (não exigem token)
                .requestMatchers(HttpMethod.GET, "/api/goats/**", "/api/genealogies/**", "/api/farms/**", "/api/goatfarms/**").permitAll()
                
                // Regras de autorização por ROLE (exigem token)
                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/api/users/**").hasAnyAuthority("ROLE_FARM_OWNER", "ROLE_ADMIN", "ROLE_OPERATOR")
                .requestMatchers(HttpMethod.POST, "/api/farms/**", "/api/goats/**", "/api/genealogy/**").hasAnyAuthority("ROLE_FARM_OWNER", "ROLE_ADMIN", "ROLE_OPERATOR")
                // Excluir /api/goatfarms/full da regra genérica para não conflitar com permitAll()
                .requestMatchers(HttpMethod.POST, "/api/goatfarms").hasAnyAuthority("ROLE_FARM_OWNER", "ROLE_ADMIN", "ROLE_OPERATOR")
                .requestMatchers(HttpMethod.POST, "/api/goatfarms/{id}").hasAnyAuthority("ROLE_FARM_OWNER", "ROLE_ADMIN", "ROLE_OPERATOR")
                .requestMatchers(HttpMethod.POST, "/api/goatfarms/goats").hasAnyAuthority("ROLE_FARM_OWNER", "ROLE_ADMIN", "ROLE_OPERATOR")
                .requestMatchers(HttpMethod.PUT, "/api/farms/**", "/api/goatfarms/**", "/api/goats/**", "/api/genealogy/**").hasAnyAuthority("ROLE_FARM_OWNER", "ROLE_ADMIN", "ROLE_OPERATOR")
                .requestMatchers(HttpMethod.DELETE, "/api/farms/**", "/api/goatfarms/**", "/api/goats/**", "/api/genealogy/**").hasAnyAuthority("ROLE_FARM_OWNER", "ROLE_ADMIN", "ROLE_OPERATOR")
                
                // Qualquer outra requisição /api/** que não corresponda às regras acima precisa de autenticação
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                .jwtAuthenticationConverter(jwtAuthenticationConverter())
            ))
            .addFilterBefore(jwtDebugFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .cors(Customizer.withDefaults()); // Aplica a configuração de CORS do seu CorsConfig

        return http.build();
    }

    // Métodos duplicados removidos - já definidos acima

    /**
     * Filtro de segurança para outros endpoints (frontend, etc.).
     * Ordem 3: processado por último.
     */
    @Bean
    @Order(3)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/", "/*.html", "/*.css", "/*.js", "/static/**", "/test/**") // Adiciona securityMatcher específico
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().permitAll() // Permite tudo que corresponder ao securityMatcher
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(this.rsaPublicKey).build();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        try {
            logger.debug("🔍 SECURITY: Inicializando JwtEncoder...");
            logger.debug("🔍 SECURITY: Chave pública carregada: {}", (this.rsaPublicKey != null));
            logger.debug("🔍 SECURITY: Chave privada carregada: {}", (this.rsaPrivateKey != null));
            
            JWK jwk = new RSAKey.Builder(this.rsaPublicKey).privateKey(this.rsaPrivateKey).build();
            JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
            
            logger.debug("🔍 SECURITY: JwtEncoder criado com sucesso");
            return new NimbusJwtEncoder(jwks);
        } catch (Exception e) {
            logger.error("🔍 SECURITY ERROR: Erro ao criar JwtEncoder - {}: {}", e.getClass().getSimpleName(), e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix(""); // Remover prefixo pois o scope já contém ROLE_
        authoritiesConverter.setAuthoritiesClaimName("scope");

        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        
        // Adicionar logs de debug
        authenticationConverter.setPrincipalClaimName("sub");
        
        return authenticationConverter;
    }

}