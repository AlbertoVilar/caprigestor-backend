package com.devmaster.goatfarm.application.ports.out;

import com.devmaster.goatfarm.events.model.entity.Event;

/**
 * Porta de saída para publicação de eventos em sistema de mensageria
 * Define o contrato para publicar eventos de forma assíncrona
 */
public interface EventPublisher {

    /**
     * Publica um evento no sistema de mensageria
     * @param event Evento a ser publicado
     */
    void publishEvent(Event event);
}
