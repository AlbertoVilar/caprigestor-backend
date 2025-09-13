package com.devmaster.goatfarm.events.model.repository;

import com.devmaster.goatfarm.events.enuns.EventType;
import com.devmaster.goatfarm.events.model.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

        // Simple search
        @Query("SELECT e FROM Event e WHERE e.goat.registrationNumber = :registrationNumber")
        List<Event> findEventsByGoatNumRegistro(String registrationNumber);

        @Query("""
    SELECT e FROM Event e
    WHERE e.goat.registrationNumber = :registrationNumber
      AND (:eventType IS NULL OR e.eventType = :eventType)
      AND (:startDate IS NULL OR e.date >= :startDate)
      AND (:endDate IS NULL OR e.date <= :endDate)
""")
        Page<Event> findEventsByGoatWithFilters(
                @Param("registrationNumber") String registrationNumber,
                @Param("eventType") EventType eventType,
                @Param("startDate") LocalDate startDate,
                @Param("endDate") LocalDate endDate,
                Pageable pageable
        );


}

