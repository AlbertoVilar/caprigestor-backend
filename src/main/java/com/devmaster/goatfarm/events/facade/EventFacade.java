package com.devmaster.goatfarm.events.facade;

import com.devmaster.goatfarm.events.business.bo.EventRequestVO;
import com.devmaster.goatfarm.events.business.bo.EventResponseVO;
import com.devmaster.goatfarm.events.business.eventbusiness.EventBusiness;
import com.devmaster.goatfarm.events.enuns.EventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class EventFacade {

    @Autowired
    private EventBusiness eventBusiness;

    /**
     * Cria um novo evento para uma cabra.
     * @param requestVO Dados do evento
     * @param goatRegistrationNumber Número de registro da cabra
     * @return EventResponseVO com os dados do evento criado
     */
    @Transactional
    public EventResponseVO createEvent(EventRequestVO requestVO, String goatRegistrationNumber) {
        return eventBusiness.createEvent(requestVO, goatRegistrationNumber);
    }

    /**
     * Atualiza um evento existente.
     * @param id ID do evento
     * @param requestVO Novos dados do evento
     * @param goatRegistrationNumber Número de registro da cabra
     * @return EventResponseVO com os dados atualizados
     */
    @Transactional
    public EventResponseVO updateEvent(Long id, EventRequestVO requestVO, String goatRegistrationNumber) {
        return eventBusiness.updateEvent(id, requestVO, goatRegistrationNumber);
    }

    /**
     * Busca todos os eventos de uma cabra.
     * @param goatNumRegistration Número de registro da cabra
     * @return Lista de EventResponseVO
     */
    public List<EventResponseVO> findEventByGoat(String goatNumRegistration) {
        return eventBusiness.findEventByGoat(goatNumRegistration);
    }

    /**
     * Busca eventos de uma cabra com filtros opcionais.
     * @param registrationNumber Número de registro da cabra
     * @param eventType Tipo do evento (opcional)
     * @param startDate Data inicial (opcional)
     * @param endDate Data final (opcional)
     * @param pageable Configuração de paginação
     * @return Page de EventResponseVO
     */
    public Page<EventResponseVO> findEventsByGoatWithFilters(String registrationNumber,
                                                             EventType eventType,
                                                             LocalDate startDate,
                                                             LocalDate endDate,
                                                             Pageable pageable) {
        return eventBusiness.findEventsByGoatWithFilters(registrationNumber, eventType, startDate, endDate, pageable);
    }

    /**
     * Remove um evento pelo ID.
     * @param id ID do evento a ser removido
     */
    @Transactional
    public void deleteEventById(Long id) {
        eventBusiness.deleteEventById(id);
    }
}


