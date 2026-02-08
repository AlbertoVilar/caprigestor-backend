package com.devmaster.goatfarm.reproduction.persistence.repository;

import com.devmaster.goatfarm.reproduction.enums.ReproductiveEventType;
import com.devmaster.goatfarm.reproduction.persistence.entity.ReproductiveEvent;
import com.devmaster.goatfarm.reproduction.persistence.projection.PregnancyDiagnosisAlertProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
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

    @Query(
            value = """
                    with coverage_candidates as (
                        select
                            c.id as coverage_id,
                            c.goat_id as goatId,
                            coalesce(corr.corrected_event_date, c.event_date) as lastCoverageDate
                        from reproductive_event c
                        left join reproductive_event corr
                            on corr.related_event_id = c.id
                            and corr.event_type = 'COVERAGE_CORRECTION'
                            and corr.farm_id = c.farm_id
                            and corr.goat_id = c.goat_id
                        where c.farm_id = :farmId
                          and c.event_type = 'COVERAGE'
                          and coalesce(corr.corrected_event_date, c.event_date) <= :referenceDate
                    ),
                    latest_coverage as (
                        select coverage_id, goatId, lastCoverageDate
                        from (
                            select
                                cc.coverage_id,
                                cc.goatId,
                                cc.lastCoverageDate,
                                row_number() over (
                                    partition by cc.goatId
                                    order by cc.lastCoverageDate desc, cc.coverage_id desc
                                ) as rn
                            from coverage_candidates cc
                        ) ranked
                        where rn = 1
                    )
                    select
                        lc.goatId as goatId,
                        lc.lastCoverageDate as lastCoverageDate,
                        (
                            select max(ch.event_date)
                            from reproductive_event ch
                            where ch.farm_id = :farmId
                              and ch.goat_id = lc.goatId
                              and ch.event_type = 'PREGNANCY_CHECK'
                              and ch.event_date <= :referenceDate
                        ) as lastCheckDate,
                        null as eligibleDate
                    from latest_coverage lc
                    where lc.lastCoverageDate <= :eligibleThresholdDate
                      and not exists (
                          select 1
                          from reproductive_event b
                          where b.farm_id = :farmId
                            and b.goat_id = lc.goatId
                            and b.event_type in (:blockingTypes)
                            and b.event_date > lc.lastCoverageDate
                            and b.event_date <= :referenceDate
                      )
                    order by lc.lastCoverageDate asc, lc.coverage_id asc
                    """,
            countQuery = """
                    with coverage_candidates as (
                        select
                            c.id as coverage_id,
                            c.goat_id as goatId,
                            coalesce(corr.corrected_event_date, c.event_date) as lastCoverageDate
                        from reproductive_event c
                        left join reproductive_event corr
                            on corr.related_event_id = c.id
                            and corr.event_type = 'COVERAGE_CORRECTION'
                            and corr.farm_id = c.farm_id
                            and corr.goat_id = c.goat_id
                        where c.farm_id = :farmId
                          and c.event_type = 'COVERAGE'
                          and coalesce(corr.corrected_event_date, c.event_date) <= :referenceDate
                    ),
                    latest_coverage as (
                        select coverage_id, goatId, lastCoverageDate
                        from (
                            select
                                cc.coverage_id,
                                cc.goatId,
                                cc.lastCoverageDate,
                                row_number() over (
                                    partition by cc.goatId
                                    order by cc.lastCoverageDate desc, cc.coverage_id desc
                                ) as rn
                            from coverage_candidates cc
                        ) ranked
                        where rn = 1
                    )
                    select count(*)
                    from latest_coverage lc
                    where lc.lastCoverageDate <= :eligibleThresholdDate
                      and not exists (
                          select 1
                          from reproductive_event b
                          where b.farm_id = :farmId
                            and b.goat_id = lc.goatId
                            and b.event_type in (:blockingTypes)
                            and b.event_date > lc.lastCoverageDate
                            and b.event_date <= :referenceDate
                      )
                    """
            ,
            nativeQuery = true
    )
    Page<PregnancyDiagnosisAlertProjection> findPendingPregnancyDiagnosisAlerts(
            @Param("farmId") Long farmId,
            @Param("referenceDate") LocalDate referenceDate,
            @Param("eligibleThresholdDate") LocalDate eligibleThresholdDate,
            @Param("blockingTypes") List<String> blockingTypes,
            Pageable pageable
    );

}
