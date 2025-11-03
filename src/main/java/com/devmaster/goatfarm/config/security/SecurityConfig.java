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

    public SecurityConfig(UserDetailsService userDetailsService, JwtDebugFilter jwtDebugFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtDebugFilter = jwtDebugFilter;
    }

    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Filtro de seguranÃ§a para ENDPOINTS PÃšBLICOS.
     * Ordem 1: processado primeiro. NÃ£o tem validaÃ§Ã£o de JWT.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain publicSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/auth/login", "/api/auth/register", "/api/auth/refresh", "/api/auth/register-farm", "/h2-console/**", "/swagger-ui/**", "/v3/api-docs/**", "/api/goatfarms/full")
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions().disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    /**
     * Filtro de seguranÃ§a para TODOS os outros endpoints da API.
     * Ordem 2: processado depois dos pÃºblicos. Exige e valida JWT.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/**")
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(HttpMethod.POST, "/api/auth/register-farm").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/goats/**", "/api/genealogies/**", "/api/farms/**", "/api/goatfarms/**").permitAll()
                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/api/users/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_OPERATOR")
                .requestMatchers(HttpMethod.POST, "/api/farms/**", "/api/goats/**", "/api/genealogies/**", "/api/phones/**", "/api/addresses/**", "/api/users/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_OPERATOR")
                .requestMatchers(HttpMethod.POST, "/api/goatfarms").hasAnyAuthority("ROLE_ADMIN", "ROLE_OPERATOR")
                .requestMatchers(HttpMethod.POST, "/api/goatfarms/{id}").hasAnyAuthority("ROLE_ADMIN", "ROLE_OPERATOR")
                .requestMatchers(HttpMethod.POST, "/api/goatfarms/goats").hasAnyAuthority("ROLE_ADMIN", "ROLE_OPERATOR")
                .requestMatchers(HttpMethod.PUT, "/api/farms/**", "/api/goatfarms/**", "/api/goats/**", "/api/genealogies/**", "/api/phones/**", "/api/addresses/**", "/api/users/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_OPERATOR")
                .requestMatchers(HttpMethod.DELETE, "/api/farms/**", "/api/goatfarms/**", "/api/goats/**", "/api/genealogies/**", "/api/phones/**", "/api/addresses/**", "/api/users/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_OPERATOR")
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.disable())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                .jwtAuthenticationConverter(jwtAuthenticationConverter())
            ))
            .addFilterBefore(jwtDebugFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .cors(Customizer.withDefaults());

        return http.build();
    }

    
    /**
     * Filtro de seguranÃ§a para outros endpoints (frontend, etc.).
     * Ordem 3: processado por Ãºltimo.
     */
    @Bean
    @Order(3)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
            .securityMatcher("/", "/*.html", "/*.css", "/*.js", "/static/**", "/test/**")
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable())
            .build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(this.rsaPublicKey).build();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        try {
            logger.debug("ðŸ” SECURITY: Inicializando JwtEncoder...");
            logger.debug("ðŸ” SECURITY: Chave pÃºblica carregada: {}", (this.rsaPublicKey != null));
            logger.debug("ðŸ” SECURITY: Chave privada carregada: {}", (this.rsaPrivateKey != null));
            
            JWK jwk = new RSAKey.Builder(this.rsaPublicKey).privateKey(this.rsaPrivateKey).build();
            JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
            
            logger.debug("ðŸ” SECURITY: JwtEncoder criado com sucesso");
            return new NimbusJwtEncoder(jwks);
        } catch (Exception e) {
            logger.error("ðŸ” SECURITY ERROR: Erro ao criar JwtEncoder - {}: {}", e.getClass().getSimpleName(), e.getMessage());
            e.printStackTrace();
            throw e;
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

}
