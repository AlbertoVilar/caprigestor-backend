package com.devmaster.goatfarm.reproduction.persistence.repository;

import com.devmaster.goatfarm.reproduction.enums.ReproductiveEventType;
import com.devmaster.goatfarm.reproduction.persistence.entity.ReproductiveEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ReproductiveEventRepository extends JpaRepository<ReproductiveEvent, Long> {

    Page<ReproductiveEvent> findAllByFarmIdAndGoatIdOrderByEventDateDescIdDesc(Long farmId, String goatId, Pageable pageable);

    Optional<ReproductiveEvent> findByIdAndFarmIdAndGoatId(Long id, Long farmId, String goatId);

    Optional<ReproductiveEvent> findTopByFarmIdAndGoatIdAndEventTypeAndEventDateLessThanEqualOrderByEventDateDescIdDesc(
            Long farmId,
            String goatId,
            ReproductiveEventType eventType,
            LocalDate eventDate
    );

    @Query("""
            select c from ReproductiveEvent c
            left join ReproductiveEvent corr
                on corr.relatedEventId = c.id
                and corr.eventType = com.devmaster.goatfarm.reproduction.enums.ReproductiveEventType.COVERAGE_CORRECTION
            where c.farmId = :farmId
              and c.goatId = :goatId
              and c.eventType = com.devmaster.goatfarm.reproduction.enums.ReproductiveEventType.COVERAGE
              and coalesce(corr.correctedEventDate, c.eventDate) <= :eventDate
            order by coalesce(corr.correctedEventDate, c.eventDate) desc, c.id desc
            """)
    Optional<ReproductiveEvent> findLatestEffectiveCoverageOnOrBefore(
            @Param("farmId") Long farmId,
            @Param("goatId") String goatId,
            @Param("eventDate") LocalDate eventDate
    );

    Optional<ReproductiveEvent> findTopByFarmIdAndGoatIdAndEventTypeAndRelatedEventIdOrderByEventDateDescIdDesc(
            Long farmId,
            String goatId,
            ReproductiveEventType eventType,
            Long relatedEventId
    );


}
