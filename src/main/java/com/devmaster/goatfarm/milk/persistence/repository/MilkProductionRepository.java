package com.devmaster.goatfarm.milk.persistence.repository;

import com.devmaster.goatfarm.milk.persistence.entity.MilkProductionEntity;
import com.devmaster.goatfarm.milk.enums.MilkingShift;
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
public interface MilkProductionRepository extends JpaRepository<MilkProductionEntity, Long> {

    @Query("""
    select case when count(mp) > 0 then true else false end from MilkProduction mp
    where mp.farmId = :farmId
      and mp.goatId = :goatId
      and mp.date = :date
      and mp.shift = :shift
      and mp.status = com.devmaster.goatfarm.milk.enums.MilkProductionStatus.ACTIVE
    """)
    boolean existsByFarmIdAndGoatIdAndDateAndShift(
            @Param("farmId") Long farmId,
            @Param("goatId") String goatId,
            @Param("date") LocalDate date,
            @Param("shift") MilkingShift shift
    );

    @Query("""
    select case when count(mp) > 0 then true else false end from MilkProduction mp
    where mp.farmId = :farmId
      and mp.goatTechnicalId = :goatTechnicalId
      and mp.date = :date
      and mp.shift = :shift
      and mp.status = com.devmaster.goatfarm.milk.enums.MilkProductionStatus.ACTIVE
    """)
    boolean existsByFarmIdAndGoatTechnicalIdAndDateAndShift(
            @Param("farmId") Long farmId,
            @Param("goatTechnicalId") Long goatTechnicalId,
            @Param("date") LocalDate date,
            @Param("shift") MilkingShift shift
    );

    Optional<MilkProductionEntity> findByIdAndFarmIdAndGoatId(Long id, Long farmId, String goatId);
    Optional<MilkProductionEntity> findByIdAndFarmIdAndGoatTechnicalId(Long id, Long farmId, Long goatTechnicalId);

    @Query("""
    select mp from MilkProduction mp
    where mp.farmId = :farmId
      and mp.goatId = :goatId
      and (:includeCanceled = true or mp.status = com.devmaster.goatfarm.milk.enums.MilkProductionStatus.ACTIVE)
      and mp.date >= coalesce(:from, mp.date)
      and mp.date <= coalesce(:to, mp.date)
    """)
    Page<MilkProductionEntity> search(
            @Param("farmId") Long farmId,
            @Param("goatId") String goatId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            Pageable pageable,
            @Param("includeCanceled") boolean includeCanceled
    );

    @Query("""
    select mp from MilkProduction mp
    where mp.farmId = :farmId
      and mp.goatTechnicalId = :goatTechnicalId
      and (:includeCanceled = true or mp.status = com.devmaster.goatfarm.milk.enums.MilkProductionStatus.ACTIVE)
      and mp.date >= coalesce(:from, mp.date)
      and mp.date <= coalesce(:to, mp.date)
    """)
    Page<MilkProductionEntity> searchByTechnicalId(
            @Param("farmId") Long farmId,
            @Param("goatTechnicalId") Long goatTechnicalId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            Pageable pageable,
            @Param("includeCanceled") boolean includeCanceled
    );

    @Query("""
    select mp from MilkProduction mp
    where mp.farmId = :farmId
      and mp.goatId = :goatId
      and mp.status = com.devmaster.goatfarm.milk.enums.MilkProductionStatus.ACTIVE
      and mp.date >= :from
      and mp.date <= :to
    """)
    List<MilkProductionEntity> findByFarmIdAndGoatIdAndDateBetween(
            @Param("farmId") Long farmId,
            @Param("goatId") String goatId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
    select mp from MilkProduction mp
    where mp.farmId = :farmId
      and mp.goatTechnicalId = :goatTechnicalId
      and mp.status = com.devmaster.goatfarm.milk.enums.MilkProductionStatus.ACTIVE
      and mp.date >= :from
      and mp.date <= :to
    """)
    List<MilkProductionEntity> findByFarmIdAndGoatTechnicalIdAndDateBetween(
            @Param("farmId") Long farmId,
            @Param("goatTechnicalId") Long goatTechnicalId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );



}
