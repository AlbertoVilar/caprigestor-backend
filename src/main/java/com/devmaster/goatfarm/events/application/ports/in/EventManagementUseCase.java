package com.devmaster.goatfarm.events.application.ports.in;

import com.devmaster.goatfarm.events.business.bo.EventRequestVO;
import com.devmaster.goatfarm.events.business.bo.EventResponseVO;
import com.devmaster.goatfarm.events.enums.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * Porta de entrada para casos de uso de gerenciamento de eventos
 * Define as operações de negócio disponíveis para eventos
 */
public interface EventManagementUseCase {

    /**
     * Cria um novo evento para uma cabra
     * @param requestVO Dados do evento
     * @param goatRegistrationNumber Número de registro da cabra
     * @return EventResponseVO com os dados do evento criado
     */
    EventResponseVO createEvent(EventRequestVO requestVO, String goatRegistrationNumber);

    /**
     * Atualiza um evento existente
     * @param id ID do evento
     * @param requestVO Novos dados do evento
     * @param goatRegistrationNumber Número de registro da cabra
     * @return EventResponseVO com os dados atualizados
     */
    EventResponseVO updateEvent(Long id, EventRequestVO requestVO, String goatRegistrationNumber);

    /**
     * Busca eventos por cabra
     * @param goatNumRegistration Número de registro da cabra
     * @return Lista de eventos da cabra
     */
    List<EventResponseVO> findEventsByGoat(String goatNumRegistration);

    /**
     * Busca eventos com filtros e paginação
     * @param registrationNumber Número de registro da cabra
     * @param eventType Tipo do evento (opcional)
     * @param startDate Data inicial (opcional)
     * @param endDate Data final (opcional)
     * @param pageable Configuração de paginação
     * @return Página de eventos filtrados
     */
    Page<EventResponseVO> findEventsWithFilters(String registrationNumber,
                                               EventType eventType,
                                               LocalDate startDate,
                                               LocalDate endDate,
                                               Pageable pageable);

    /**
     * Remove um evento por ID
     * @param id ID do evento
     */
    void deleteEvent(Long id);

    /**
     * Remove eventos de outros usuários (operação administrativa)
     * @param adminId ID do administrador
     */
    void deleteEventsFromOtherUsers(Long adminId);

    /**
     * Busca um evento pelo ID
     * @param id ID do evento
     * @return Dados do evento
     */
    EventResponseVO findEventById(Long id);
}