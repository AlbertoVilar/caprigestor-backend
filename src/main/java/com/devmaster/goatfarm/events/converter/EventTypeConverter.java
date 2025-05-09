package com.devmaster.goatfarm.events.converter;

import com.devmaster.goatfarm.events.enuns.EventType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;


/**
 * Implementação da interface {@link Converter} do Spring para converter
 * valores de string da URL em {@link EventType}, ignorando letras maiúsculas/minúsculas.
 *
 * Exemplo: "parto" → EventType.PARTO
 */
@Component
public class EventTypeConverter implements Converter<String, EventType> {

    @Override
    public EventType convert(String source) {
        return EventType.valueOf(source.trim().toUpperCase());
    }
}
