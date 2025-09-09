package com.devmaster.goatfarm.config; // <-- Verifique se este é o seu pacote correto

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuração centralizada de CORS para a aplicação.
 * Permite que o frontend (rodando em outra porta/domínio) acesse a API.
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // AQUI ESTÁ A CORREÇÃO:
        // Usar allowedOriginPatterns em vez de allowedOrigins para ser compatível com allowCredentials = true.
        // Isso permite requisições de qualquer origem, ideal para desenvolvimento.
        configuration.setAllowedOriginPatterns(List.of("*"));

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Cabeçalhos permitidos
        configuration.setAllowedHeaders(List.of("*"));

        // Permitir que o navegador envie credenciais (cookies, tokens de autenticação, etc.)
        configuration.setAllowCredentials(true);

        // Registra a configuração de CORS para todos os endpoints ("/**") da aplicação
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}