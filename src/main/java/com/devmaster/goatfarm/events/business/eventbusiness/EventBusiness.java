package com.devmaster.goatfarm.events.business.eventbusiness;

import com.devmaster.goatfarm.events.business.bo.EventRequestVO;
import com.devmaster.goatfarm.events.business.bo.EventResponseVO;
import com.devmaster.goatfarm.events.dao.EventDao;
import com.devmaster.goatfarm.events.enuns.EventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EventBusiness {

    @Autowired
    private EventDao eventDao;

    /**
     * Cria um novo evento para uma cabra.
     * @param requestVO Dados do evento
     * @param goatRegistrationNumber Número de registro da cabra
     * @return EventResponseVO com os dados do evento criado
     */
    public EventResponseVO createEvent(EventRequestVO requestVO, String goatRegistrationNumber) {
        return eventDao.createEvent(requestVO, goatRegistrationNumber);
    }

    /**
     * Atualiza um evento existente.
     * @param id ID do evento
     * @param requestVO Novos dados do evento
     * @param goatRegistrationNumber Número de registro da cabra
     * @return EventResponseVO com os dados atualizados
     */
    public EventResponseVO updateEvent(Long id, EventRequestVO requestVO, String goatRegistrationNumber) {
        return eventDao.updateGoatEvent(id, requestVO, goatRegistrationNumber);
    }

    /**
     * Busca todos os eventos de uma cabra.
     * @param goatNumRegistration Número de registro da cabra
     * @return Lista de EventResponseVO
     */
    public List<EventResponseVO> findEventByGoat(String goatNumRegistration) {
        return eventDao.findEventByGoat(goatNumRegistration);
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
        return eventDao.findEventsByGoatWithFilters(registrationNumber, eventType, startDate, endDate, pageable);
    }

    /**
     * Remove um evento pelo ID.
     * @param id ID do evento a ser removido
     */
    public void deleteEventById(Long id) {
        eventDao.deleteEventById(id);
    }
}
