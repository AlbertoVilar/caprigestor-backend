package com.devmaster.goatfarm.events.dao;

import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.events.business.bo.EventRequestVO;
import com.devmaster.goatfarm.events.business.bo.EventResponseVO;
import com.devmaster.goatfarm.events.converter.EventEntityConverter;
import com.devmaster.goatfarm.events.enuns.EventType;
import com.devmaster.goatfarm.events.model.entity.Event;
import com.devmaster.goatfarm.events.model.repository.EventRepository;
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
public class EventDao {

    @Autowired
    public EventRepository eventRepository;

    @Autowired
    public GoatRepository goatRepository;

    @Autowired
    private OwnershipService ownershipService;

    /**
     * Cria um novo evento para uma cabra específica.
     * @param requestVO Dados do evento a ser criado
     * @param goatRegistrationNumber Número de registro da cabra
     * @return EventResponseVO com os dados do evento criado
     * @throws ResourceNotFoundException se a cabra não for encontrada
     */
    @Transactional
    public EventResponseVO createEvent(EventRequestVO requestVO, String goatRegistrationNumber) {
        Goat goat = goatRepository.findById(goatRegistrationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado: " + goatRegistrationNumber));

        // Check if logged user is owner of the goat's farm
        verifyFarmOwnership(goat);

        Event event = eventRepository.save(EventEntityConverter.toEntity(requestVO, goat));
        return EventEntityConverter.toResponseVO(event);
    }

    /**
     * Atualiza um evento existente de uma cabra.
     * @param id ID do evento a ser atualizado
     * @param requestVO Novos dados do evento
     * @param goatRegistrationNumber Número de registro da cabra
     * @return EventResponseVO com os dados atualizados
     * @throws ResourceNotFoundException se o evento ou cabra não forem encontrados
     */
    @Transactional
    public EventResponseVO updateGoatEvent(Long id, EventRequestVO requestVO, String goatRegistrationNumber) {
        Goat goat = goatRepository.findById(goatRegistrationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado: " + goatRegistrationNumber));

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado: " + id));

        if (!event.getGoat().getRegistrationNumber().equals(goatRegistrationNumber)) {
            throw new ResourceNotFoundException("Este evento não pertence à cabra de registro: " + goatRegistrationNumber);
        }

        // Verificar se o usuário logado é proprietário da fazenda da cabra
        verifyFarmOwnership(goat);

        EventEntityConverter.toUpdateEvent(event, requestVO);
        eventRepository.save(event);
        return EventEntityConverter.toResponseVO(event);
    }

    /**
     * Busca todos os eventos de uma cabra pelo número de registro.
     * @param goatNumRegistration Número de registro da cabra
     * @return Lista de EventResponseVO com os eventos encontrados
     * @throws ResourceNotFoundException se a cabra não for encontrada ou não tiver eventos
     */
    @Transactional(readOnly = true)
    public List<EventResponseVO> findEventByGoat(String goatNumRegistration) {
        Goat goat = goatRepository.findById(goatNumRegistration)
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado: " + goatNumRegistration));

        // Verificar se o usuário logado é proprietário da fazenda da cabra
        verifyFarmOwnership(goat);

        List<Event> events = eventRepository.findEventsByGoatNumRegistro(goatNumRegistration);
        if (events.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum evento encontrado para a cabra com número de registro: " + goatNumRegistration);
        }
        return events.stream().map(EventEntityConverter::toResponseVO).toList();
    }

    /**
     * Busca eventos de uma cabra com filtros opcionais e paginação.
     * @param registrationNumber Número de registro da cabra
     * @param eventType Tipo do evento (opcional)
     * @param startDate Data inicial do período (opcional)
     * @param endDate Data final do período (opcional)
     * @param pageable Configuração de paginação
     * @return Page de EventResponseVO com os eventos encontrados
     * @throws ResourceNotFoundException se a cabra não for encontrada ou não houver eventos
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

        Page<Event> page = eventRepository.findEventsByGoatWithFilters(registrationNumber, eventType, startDate, endDate, pageable);
        if (page.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum evento encontrado para a cabra com os filtros fornecidos.");
        }
        return page.map(EventEntityConverter::toResponseVO);
    }

    /**
     * Remove um evento pelo ID.
     * @param id ID do evento a ser removido
     * @throws ResourceNotFoundException se o evento não for encontrado
     */
    @Transactional
    public void deleteEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado: " + id));

        // Verificar se o usuário logado é proprietário da fazenda da cabra
        verifyFarmOwnership(event.getGoat());

        eventRepository.deleteById(id);
    }

    /**
     * Verifica se o usuário logado é proprietário da fazenda da cabra
     * Permite acesso apenas para ADMIN ou proprietário da fazenda
     */
    private void verifyFarmOwnership(Goat goat) {
        User currentUser = ownershipService.getCurrentUser();
        
        // ADMIN has access to everything - simplified check
        if (ownershipService.isCurrentUserAdmin()) {
            return;
        }

        // Check if user is owner of the farm
        if (goat.getFarm() == null) {
            throw new ResourceNotFoundException("Cabra não está associada a nenhuma fazenda");
        }

        if (!goat.getFarm().getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Acesso negado: Você não tem permissão para acessar eventos desta fazenda");
        }
    }
}
