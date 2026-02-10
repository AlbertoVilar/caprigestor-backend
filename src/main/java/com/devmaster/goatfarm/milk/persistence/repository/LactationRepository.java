package com.devmaster.goatfarm.milk.persistence.repository;

import com.devmaster.goatfarm.milk.enums.LactationStatus;
import com.devmaster.goatfarm.milk.persistence.entity.Lactation;
import com.devmaster.goatfarm.milk.persistence.projection.LactationDryOffAlertProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface LactationRepository extends JpaRepository<Lactation, Long> {

    Optional<Lactation> findByFarmIdAndGoatIdAndStatus(
            Long farmId,
            String goatId,
            LactationStatus status
    );

    Optional<Lactation> findByIdAndFarmIdAndGoatId(Long id, Long farmId, String goatId);

    Page<Lactation> findAllByFarmIdAndGoatId(Long farmId, String goatId, Pageable pageable);

    @Query(
            value = """
                    with pregnancy_candidates as (
                        select
                            p.id,
                            p.farm_id,
                            p.goat_id,
                            p.status,
                            p.breeding_date,
                            p.confirm_date,
                            p.closed_at,
                            coalesce(p.breeding_date, p.confirm_date) as start_date
                        from pregnancy p
                        where p.farm_id = :farmId
                          and coalesce(p.breeding_date, p.confirm_date) is not null
                          and coalesce(p.breeding_date, p.confirm_date) <= :referenceDate
                    ),
                    latest_pregnancy as (
                        select
                            ranked.id,
                            ranked.farm_id,
                            ranked.goat_id,
                            ranked.status,
                            ranked.breeding_date,
                            ranked.confirm_date,
                            ranked.closed_at,
                            ranked.start_date
                        from (
                            select
                                pc.*,
                                row_number() over (
                                    partition by pc.goat_id
                                    order by pc.start_date desc, pc.id desc
                                ) as rn
                            from pregnancy_candidates pc
                        ) ranked
                        where ranked.rn = 1
                    )
                    select
                        l.id as lactationId,
                        l.goat_id as goatId,
                        coalesce(l.dry_at_pregnancy_days, 90) as dryAtPregnancyDays,
                        lp.start_date as startDatePregnancy,
                        lp.breeding_date as breedingDate,
                        lp.confirm_date as confirmDate,
                        (lp.start_date + coalesce(l.dry_at_pregnancy_days, 90)) as dryOffDate
                    from lactation l
                    join latest_pregnancy lp
                      on lp.farm_id = l.farm_id
                     and lp.goat_id = l.goat_id
                    where l.farm_id = :farmId
                      and l.status = 'ACTIVE'
                      and upper(lp.status) = 'ACTIVE'
                      and (lp.closed_at is null or lp.closed_at > :referenceDate)
                      and (lp.start_date + coalesce(l.dry_at_pregnancy_days, 90)) <= :referenceDate
                    order by (lp.start_date + coalesce(l.dry_at_pregnancy_days, 90)) asc, l.goat_id asc, l.id asc
                    """,
            countQuery = """
                    with pregnancy_candidates as (
                        select
                            p.id,
                            p.farm_id,
                            p.goat_id,
                            p.status,
                            p.breeding_date,
                            p.confirm_date,
                            p.closed_at,
                            coalesce(p.breeding_date, p.confirm_date) as start_date
                        from pregnancy p
                        where p.farm_id = :farmId
                          and coalesce(p.breeding_date, p.confirm_date) is not null
                          and coalesce(p.breeding_date, p.confirm_date) <= :referenceDate
                    ),
                    latest_pregnancy as (
                        select
                            ranked.id,
                            ranked.farm_id,
                            ranked.goat_id,
                            ranked.status,
                            ranked.closed_at,
                            ranked.start_date
                        from (
                            select
                                pc.*,
                                row_number() over (
                                    partition by pc.goat_id
                                    order by pc.start_date desc, pc.id desc
                                ) as rn
                            from pregnancy_candidates pc
                        ) ranked
                        where ranked.rn = 1
                    )
                    select count(*)
                    from lactation l
                    join latest_pregnancy lp
                      on lp.farm_id = l.farm_id
                     and lp.goat_id = l.goat_id
                    where l.farm_id = :farmId
                      and l.status = 'ACTIVE'
                      and upper(lp.status) = 'ACTIVE'
                      and (lp.closed_at is null or lp.closed_at > :referenceDate)
                      and (lp.start_date + coalesce(l.dry_at_pregnancy_days, 90)) <= :referenceDate
                    """,
            nativeQuery = true
    )
    Page<LactationDryOffAlertProjection> findDryOffAlerts(
            @Param("farmId") Long farmId,
            @Param("referenceDate") LocalDate referenceDate,
            Pageable pageable
    );
}
