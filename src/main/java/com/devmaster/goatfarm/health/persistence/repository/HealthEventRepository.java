package com.devmaster.goatfarm.health.persistence.repository;

import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;
import com.devmaster.goatfarm.health.persistence.entity.HealthEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface HealthEventRepository extends JpaRepository<HealthEvent, Long> {

    Optional<HealthEvent> findByIdAndFarmIdAndGoatId(Long id, Long farmId, String goatId);

    @Query("""
        select e
        from HealthEvent e
        where e.farmId = :farmId
          and e.goatId = :goatId
          and (:from is null or e.scheduledDate >= :from)
          and (:to is null or e.scheduledDate <= :to)
          and (:type is null or e.type = :type)
          and (:status is null or e.status = :status)
        """)
    Page<HealthEvent> searchByGoat(
            @Param("farmId") Long farmId,
            @Param("goatId") String goatId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("type") HealthEventType type,
            @Param("status") HealthEventStatus status,
            Pageable pageable
    );

    @Query("""
        select e
        from HealthEvent e
        where e.farmId = :farmId
          and (:from is null or e.scheduledDate >= :from)
          and (:to is null or e.scheduledDate <= :to)
          and (:type is null or e.type = :type)
          and (:status is null or e.status = :status)
        """)
    Page<HealthEvent> searchCalendar(
            @Param("farmId") Long farmId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("type") HealthEventType type,
            @Param("status") HealthEventStatus status,
            Pageable pageable
    );
}

