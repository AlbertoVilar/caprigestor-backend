package com.devmaster.goatfarm.events.business.eventbusiness;

import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.events.business.bo.EventRequestVO;
import com.devmaster.goatfarm.events.business.bo.EventResponseVO;
import com.devmaster.goatfarm.events.converter.EventEntityConverter;
import com.devmaster.goatfarm.events.dao.EventDao;
import com.devmaster.goatfarm.events.enuns.EventType;
import com.devmaster.goatfarm.events.model.entity.Event;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import com.devmaster.goatfarm.goat.model.repository.GoatRepository;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.config.security.OwnershipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class EventBusiness {

    @Autowired
    private EventDao eventDao;
    
    @Autowired
    private GoatRepository goatRepository;
    
    @Autowired
    private OwnershipService ownershipService;

    /**
     * Cria um novo evento para uma cabra.
     * @param requestVO Dados do evento
     * @param goatRegistrationNumber Número de registro da cabra
     * @return EventResponseVO com os dados do evento criado
     */
    @Transactional
    public EventResponseVO createEvent(EventRequestVO requestVO, String goatRegistrationNumber) {
        Goat goat = goatRepository.findById(goatRegistrationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado: " + goatRegistrationNumber));

        // Verificar se o usuário logado é proprietário da fazenda da cabra
        verifyFarmOwnership(goat);

        Event event = eventDao.saveEvent(EventEntityConverter.toEntity(requestVO, goat));
        return EventEntityConverter.toResponseVO(event);
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
        Goat goat = goatRepository.findById(goatRegistrationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado: " + goatRegistrationNumber));

        Event event = eventDao.findEventById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado: " + id));

        if (!event.getGoat().getRegistrationNumber().equals(goatRegistrationNumber)) {
            throw new ResourceNotFoundException("Este evento não pertence à cabra de registro: " + goatRegistrationNumber);
        }

        // Verificar se o usuário logado é proprietário da fazenda da cabra
        verifyFarmOwnership(goat);

        EventEntityConverter.toUpdateEvent(event, requestVO);
        Event updatedEvent = eventDao.saveEvent(event);
        return EventEntityConverter.toResponseVO(updatedEvent);
    }

    /**
     * Busca todos os eventos de uma cabra.
     * @param goatNumRegistration Número de registro da cabra
     * @return Lista de EventResponseVO
     */
    @Transactional(readOnly = true)
    public List<EventResponseVO> findEventByGoat(String goatNumRegistration) {
        Goat goat = goatRepository.findById(goatNumRegistration)
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado: " + goatNumRegistration));

        // Verificar se o usuário logado é proprietário da fazenda da cabra
        verifyFarmOwnership(goat);

        List<Event> events = eventDao.findEventsByGoatNumRegistro(goatNumRegistration);
        if (events.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum evento encontrado para a cabra com número de registro: " + goatNumRegistration);
        }
        return events.stream().map(EventEntityConverter::toResponseVO).toList();
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
    @Transactional(readOnly = true)
    public Page<EventResponseVO> findEventsByGoatWithFilters(String registrationNumber,
                                                             EventType eventType,
                                                             LocalDate startDate,
                                                             LocalDate endDate,
                                                             Pageable pageable) {
        Goat goat = goatRepository.findById(registrationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado: " + registrationNumber));

        // Verificar se o usuário logado é proprietário da fazenda da cabra
        verifyFarmOwnership(goat);

        Page<Event> page = eventDao.findEventsByGoatWithFilters(registrationNumber, eventType, startDate, endDate, pageable);
        if (page.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum evento encontrado para a cabra com os filtros fornecidos.");
        }
        return page.map(EventEntityConverter::toResponseVO);
    }

    /**
     * Remove um evento pelo ID.
     * @param id ID do evento a ser removido
     */
    @Transactional
    public void deleteEventById(Long id) {
        Event event = eventDao.findEventById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado: " + id));

        // Verificar se o usuário logado é proprietário da fazenda da cabra
        verifyFarmOwnership(event.getGoat());

        eventDao.deleteEventById(id);
    }

    /**
     * Verifica se o usuário logado é proprietário da fazenda da cabra
     * Permite acesso apenas para ADMIN ou proprietário da fazenda
     */
    private void verifyFarmOwnership(Goat goat) {
        // Usar o OwnershipService que já tem a lógica correta implementada
        // e resolve problemas de lazy loading
        ownershipService.verifyGoatOwnership(goat);
    }
}
