package com.devmaster.goatfarm.milk.model.repository;

import com.devmaster.goatfarm.milk.model.entity.MilkProduction;
import com.devmaster.goatfarm.milk.enums.MilkingShift;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
