package com.devmaster.goatfarm.config.security;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Value("${jwt.public.key}")
    private RSAPublicKey rsaPublicKey;

    @Value("${jwt.private.key}")
    private RSAPrivateKey rsaPrivateKey;

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/oauth/**")
                .authorizeHttpRequests(authorize ->
                        authorize.anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .authorizeHttpRequests(authorize -> authorize
                        // Endpoints públicos (sem autenticação)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        
                        // Endpoints públicos para leitura (GET apenas)
                        .requestMatchers(HttpMethod.GET, "/api/goats/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/genealogy/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/farms/**").permitAll()
                        
                        // Endpoints administrativos (apenas ADMIN)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        
                        // Endpoints de fazenda - CRUD (FARM_OWNER ou ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/farms/**").hasAnyRole("FARM_OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/farms/**").hasAnyRole("FARM_OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/farms/**").hasAnyRole("FARM_OWNER", "ADMIN")
                        
                        // Endpoints de caprinos - CRUD (FARM_OWNER ou ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/goats/**").hasAnyRole("FARM_OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/goats/**").hasAnyRole("FARM_OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/goats/**").hasAnyRole("FARM_OWNER", "ADMIN")
                        
                        // Endpoints de genealogia - CRUD (FARM_OWNER ou ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/genealogy/**").hasAnyRole("FARM_OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/genealogy/**").hasAnyRole("FARM_OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/genealogy/**").hasAnyRole("FARM_OWNER", "ADMIN")
                        
                        // Endpoints de usuários (apenas próprios dados ou ADMIN)
                        .requestMatchers("/api/users/**").hasAnyRole("FARM_OWNER", "ADMIN")
                        
                        // Todos os outros endpoints requerem autenticação
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                ))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // Swagger e documentação
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        // H2 Console (apenas para desenvolvimento)
                        .requestMatchers("/h2-console/**").permitAll()
                        // Frontend estático
                        .requestMatchers("/", "/*.html", "/*.css", "/*.js", "/static/**").permitAll()
                        // Endpoints de teste
                        .requestMatchers("/test/**").permitAll()
                        // Qualquer outra requisição requer autenticação
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .headers(headers -> headers.frameOptions().disable()); // Para H2 Console
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(this.rsaPublicKey).build();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        JWK jwk = new RSAKey.Builder(this.rsaPublicKey).privateKey(this.rsaPrivateKey).build();
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("ROLE_");
        authoritiesConverter.setAuthoritiesClaimName("scope");

        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return authenticationConverter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}