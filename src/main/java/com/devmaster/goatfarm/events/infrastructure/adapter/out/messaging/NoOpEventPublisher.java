package com.devmaster.goatfarm.events.infrastructure.adapter.out.messaging;

import com.devmaster.goatfarm.events.application.ports.out.EventPublisher;
import com.devmaster.goatfarm.events.persistence.entity.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Publicador no-op para ambientes onde a mensageria está desabilitada.
 * Preserva o fluxo de negócio sem exigir RabbitMQ no boot.
 */
@Component
@ConditionalOnProperty(value = "caprigestor.messaging.enabled", havingValue = "false")
public class NoOpEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(NoOpEventPublisher.class);

    @Override
    public void publishEvent(Event event) {
        if (event != null) {
            log.debug("Mensageria desabilitada. Evento não publicado: id={}", event.getId());
        }
    }
}
