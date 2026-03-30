package com.devmaster.goatfarm.milk.application.ports.out;

import com.devmaster.goatfarm.milk.persistence.entity.FarmMilkProduction;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FarmMilkProductionPersistencePort {

    FarmMilkProduction upsertDaily(
            Long farmId,
            LocalDate productionDate,
            java.math.BigDecimal totalProduced,
            java.math.BigDecimal withdrawalProduced,
            java.math.BigDecimal marketableProduced,
            String notes
    );

    Optional<FarmMilkProduction> findByFarmIdAndProductionDate(Long farmId, LocalDate productionDate);

    List<FarmMilkProduction> findByFarmIdAndProductionDateBetween(
            Long farmId,
            LocalDate from,
            LocalDate to
    );
}
