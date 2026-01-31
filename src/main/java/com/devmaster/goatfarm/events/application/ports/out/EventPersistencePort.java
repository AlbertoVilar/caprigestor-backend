package com.devmaster.goatfarm.events.application.ports.out;

import com.devmaster.goatfarm.events.enums.EventType;
import com.devmaster.goatfarm.events.persistence.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Porta de saída para persistência de eventos
 * Define as operações de persistência necessárias para eventos
 */
public interface EventPersistencePort {

    /**
     * Salva um evento
     * @param event Evento a ser salvo
     * @return Evento salvo com ID gerado
     */
    Event save(Event event);

    /**
     * Busca um evento por ID
     * @param id ID do evento
     * @return Optional contendo o evento se encontrado
     */
    Optional<Event> findById(Long id);

    /**
     * Busca eventos por número de registro da cabra
     * @param goatRegistrationNumber Número de registro da cabra
     * @return Lista de eventos da cabra
     */
    List<Event> findByGoatRegistrationNumber(String goatRegistrationNumber);

    /**
     * Busca eventos com filtros e paginação
     * @param registrationNumber Número de registro da cabra
     * @param eventType Tipo do evento (opcional)
     * @param startDate Data inicial (opcional)
     * @param endDate Data final (opcional)
     * @param pageable Configuração de paginação
     * @return Página de eventos filtrados
     */
    Page<Event> findWithFilters(String registrationNumber,
                               EventType eventType,
                               LocalDate startDate,
                               LocalDate endDate,
                               Pageable pageable);

    /**
     * Remove um evento por ID
     * @param id ID do evento
     */
    void deleteById(Long id);

    /**
     * Remove eventos de outros usuários (operação administrativa)
     * @param adminId ID do administrador
     */
    void deleteEventsFromOtherUsers(Long adminId);

    /**
     * Verifica se um evento existe
     * @param id ID do evento
     * @return true se o evento existe
     */
    boolean existsById(Long id);
}