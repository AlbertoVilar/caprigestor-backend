package com.devmaster.goatfarm.events.model.repository;

import com.devmaster.goatfarm.events.enuns.EventType;
import com.devmaster.goatfarm.events.model.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findEventsByGoatRegistrationNumber(String registrationNumber);

    @Query("SELECT e FROM Event e WHERE e.goat.registrationNumber = :registrationNumber " +
            "AND (:eventType IS NULL OR e.eventType = :eventType) " +
            "AND (:startDate IS NULL OR e.date >= :startDate) " +
            "AND (:endDate IS NULL OR e.date <= :endDate)")
    Page<Event> findEventsByGoatWithFilters(@Param("registrationNumber") String registrationNumber,
                                            @Param("eventType") EventType eventType,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate,
                                            Pageable pageable);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "DELETE FROM eventos WHERE goat_id IN (SELECT g.num_registro FROM cabras g JOIN capril c ON g.capril_id = c.id WHERE c.user_id != :adminId)")
    void deleteEventsFromOtherUsers(@Param("adminId") Long adminId);

    // Busca por ID do evento, número de registro da cabra e ID da fazenda usando query customizada
    @Query("SELECT e FROM Event e WHERE e.id = :eventId AND e.goat.registrationNumber = :registrationNumber AND e.goat.farm.id = :farmId")
    Optional<Event> findByIdAndGoatRegistrationNumberAndFarmId(@Param("eventId") Long eventId, 
                                                               @Param("registrationNumber") String registrationNumber, 
                                                               @Param("farmId") Long farmId);

    // Busca todos os eventos de uma cabra específica em uma fazenda usando query customizada
    @Query("SELECT e FROM Event e WHERE e.goat.registrationNumber = :registrationNumber AND e.goat.farm.id = :farmId")
    Page<Event> findAllByGoatRegistrationNumberAndFarmId(@Param("registrationNumber") String registrationNumber, 
                                                         @Param("farmId") Long farmId, 
                                                         Pageable pageable);
}
