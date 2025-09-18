package com.devmaster.goatfarm.events.dao;

import com.devmaster.goatfarm.events.enuns.EventType;
import com.devmaster.goatfarm.events.model.entity.Event;
import com.devmaster.goatfarm.events.model.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class EventDao {

    @Autowired
    private EventRepository eventRepository;

    /**
     * Salva um evento no banco de dados.
     * @param event Evento a ser salvo
     * @return Event salvo
     */
    public Event saveEvent(Event event) {
        return eventRepository.save(event);
    }

    /**
     * Busca um evento pelo ID.
     * @param id ID do evento
     * @return Optional contendo o evento se encontrado
     */
    public Optional<Event> findEventById(Long id) {
        return eventRepository.findById(id);
    }

    /**
     * Busca todos os eventos de uma cabra pelo número de registro.
     * @param goatNumRegistration Número de registro da cabra
     * @return Lista de eventos
     */
    public List<Event> findEventsByGoatNumRegistro(String goatNumRegistration) {
        return eventRepository.findEventsByGoatNumRegistro(goatNumRegistration);
    }

    /**
     * Busca eventos de uma cabra com filtros opcionais e paginação.
     * @param registrationNumber Número de registro da cabra
     * @param eventType Tipo do evento (opcional)
     * @param startDate Data inicial do período (opcional)
     * @param endDate Data final do período (opcional)
     * @param pageable Configuração de paginação
     * @return Page de eventos
     */
    public Page<Event> findEventsByGoatWithFilters(String registrationNumber,
                                                   EventType eventType,
                                                   LocalDate startDate,
                                                   LocalDate endDate,
                                                   Pageable pageable) {
        return eventRepository.findEventsByGoatWithFilters(registrationNumber, eventType, startDate, endDate, pageable);
    }

    /**
     * Remove um evento pelo ID.
     * @param id ID do evento a ser removido
     */
    public void deleteEventById(Long id) {
        eventRepository.deleteById(id);
    }
}
