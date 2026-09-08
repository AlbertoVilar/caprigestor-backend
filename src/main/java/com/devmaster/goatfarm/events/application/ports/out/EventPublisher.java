package com.devmaster.goatfarm.events.application.ports.out;

import com.devmaster.goatfarm.events.business.bo.EventPublication;

/**
 * Porta de saída para publicação de eventos em sistema de mensageria
 * Define o contrato para publicar eventos de forma assíncrona
 */
public interface EventPublisher {

    /**
     * Publica um evento no sistema de mensageria
     * @param event Evento serializável a ser publicado
     */
    void publishEvent(EventPublication event);
}
