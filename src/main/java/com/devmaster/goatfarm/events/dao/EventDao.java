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
    private com.devmaster.goatfarm.authority.model.repository.UserRepository userRepository;

    // CREATE
    @Transactional
    public EventResponseVO createEvent(EventRequestVO requestVO, String goatRegistrationNumber) {
        Goat goat = goatRepository.findById(goatRegistrationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado: " + goatRegistrationNumber));

        // Verificar se o usuário logado é proprietário da fazenda da cabra
        verifyFarmOwnership(goat);

        Event event = eventRepository.save(EventEntityConverter.toEntity(requestVO, goat));
        return EventEntityConverter.toResponseVO(event);
    }

    // UPDATE
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

    // FIND BY GOAT REGISTRATION
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

    // FIND GOAT EVENT WITH FILTERS + PAGINATION
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

    // DELETE
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
        User currentUser = getCurrentUser();
        
        // ADMIN tem acesso a tudo - verificação simplificada
        if (currentUser.getRoles().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"))) {
            return;
        }

        // Verificar se o usuário é proprietário da fazenda
        if (goat.getFarm() == null) {
            throw new ResourceNotFoundException("Cabra não está associada a nenhuma fazenda");
        }

        if (!goat.getFarm().getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Acesso negado: Você não tem permissão para acessar eventos desta fazenda");
        }
    }

    /**
     * Obtém um usuário padrão (sem autenticação)
     */
    private User getCurrentUser() {
        // Retorna o primeiro usuário disponível ou cria um usuário padrão
        return userRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Nenhum usuário encontrado no sistema"));
    }
}
