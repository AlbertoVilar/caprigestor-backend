package com.devmaster.goatfarm.infrastructure.config;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração da arquitetura hexagonal
 * Define os beans para injeção de dependência das portas e adaptadores
 */
@Configuration
public class HexagonalConfiguration {
    // Os adapters de persistência são registrados via @Component.
    // Os casos de uso (ports in) são providos por @Service nas classes de negócio.
}