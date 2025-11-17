package com.devmaster.goatfarm.infrastructure.config;

import com.devmaster.goatfarm.application.ports.out.EventPersistencePort;
import com.devmaster.goatfarm.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.infrastructure.adapters.out.persistence.EventPersistenceAdapter;
import com.devmaster.goatfarm.infrastructure.adapters.out.persistence.GoatPersistenceAdapter;
import com.devmaster.goatfarm.events.model.repository.EventRepository;
import com.devmaster.goatfarm.goat.model.repository.GoatRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração da arquitetura hexagonal
 * Define os beans para injeção de dependência das portas e adaptadores
 */
@Configuration
public class HexagonalConfiguration {

    /**
     * Configura o adaptador de persistência para eventos
     */
    @Bean
    public EventPersistencePort eventPersistencePort(EventRepository eventRepository) {
        return new EventPersistenceAdapter(eventRepository);
    }

    /**
     * Configura o adaptador de persistência para cabras
     */
    @Bean
    public GoatPersistencePort goatPersistencePort(GoatRepository goatRepository) {
        return new GoatPersistenceAdapter(goatRepository);
    }

    // O caso de uso EventManagementUseCase é provido pelo bean @Service EventBusiness
}