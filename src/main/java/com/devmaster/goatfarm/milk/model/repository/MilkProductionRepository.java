package com.devmaster.goatfarm.milk.model.repository;

import com.devmaster.goatfarm.milk.model.entity.MilkProduction;
import com.devmaster.goatfarm.milk.enums.MilkingShift;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface MilkProductionRepository extends JpaRepository<MilkProduction, Long> {

    boolean existsByFarmIdAndGoatIdAndDateAndShift(
            Long farmId,
            String goatId,
            LocalDate date,
            MilkingShift shift
    );

    @Query("""
    select mp from MilkProduction mp
    where mp.farmId = :farmId
      and mp.goatId = :goatId
      and mp.date >= coalesce(:from, mp.date)
      and mp.date <= coalesce(:to, mp.date)
    """)
    Page<MilkProduction> search(
            @Param("farmId") Long farmId,
            @Param("goatId") String goatId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            Pageable pageable
    );



}
