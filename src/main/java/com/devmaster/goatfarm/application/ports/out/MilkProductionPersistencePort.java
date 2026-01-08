package com.devmaster.goatfarm.application.ports.out;

import com.devmaster.goatfarm.milk.enums.MilkingShift;
import com.devmaster.goatfarm.milk.model.entity.MilkProduction;

import java.time.LocalDate;

public interface MilkProductionPersistencePort {

    MilkProduction save(MilkProduction milkProduction);

    boolean existsByFarmIdAndGoatIdAndDateAndShift(
            Long farmId,
            String goatId,
            LocalDate date,
            MilkingShift shift
    );


}
