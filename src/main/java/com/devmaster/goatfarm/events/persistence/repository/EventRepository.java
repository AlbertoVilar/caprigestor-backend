package com.devmaster.goatfarm.events.persistence.repository;

import com.devmaster.goatfarm.events.enums.EventType;
import com.devmaster.goatfarm.events.persistence.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

/** JPA implementation detail for event persistence. */
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("""
            SELECT e FROM Event e
            JOIN FETCH e.goat g
            WHERE g.technicalId = :goatTechnicalId
              AND g.farm.id = :farmId
              AND (:eventType IS NULL OR e.eventType = :eventType)
              AND (:startDate IS NULL OR e.date >= :startDate)
              AND (:endDate IS NULL OR e.date <= :endDate)
            """)
    Page<Event> findAllByGoatTechnicalIdAndFarmIdWithFilters(
            @Param("goatTechnicalId") Long goatTechnicalId,
            @Param("farmId") Long farmId,
            @Param("eventType") EventType eventType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("""
            SELECT e FROM Event e
            JOIN FETCH e.goat g
            WHERE e.id = :eventId
              AND g.technicalId = :goatTechnicalId
              AND g.farm.id = :farmId
            """)
    Optional<Event> findByIdAndGoatTechnicalIdAndFarmId(
            @Param("eventId") Long eventId,
            @Param("goatTechnicalId") Long goatTechnicalId,
            @Param("farmId") Long farmId
    );

}
