package com.devmaster.goatfarm.milk.persistence.adapter;

import com.devmaster.goatfarm.milk.application.ports.out.FarmMilkProductionPersistencePort;
import com.devmaster.goatfarm.milk.persistence.entity.FarmMilkProduction;
import com.devmaster.goatfarm.milk.persistence.repository.FarmMilkProductionRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class FarmMilkProductionPersistenceAdapter implements FarmMilkProductionPersistencePort {

    private final FarmMilkProductionRepository repository;

    public FarmMilkProductionPersistenceAdapter(FarmMilkProductionRepository repository) {
        this.repository = repository;
    }

    @Override
    public FarmMilkProduction upsertDaily(
            Long farmId,
            LocalDate productionDate,
            java.math.BigDecimal totalProduced,
            java.math.BigDecimal withdrawalProduced,
            java.math.BigDecimal marketableProduced,
            String notes
    ) {
        repository.upsertDaily(
                farmId,
                productionDate,
                totalProduced,
                withdrawalProduced,
                marketableProduced,
                notes
        );
        return repository.findByFarmIdAndProductionDate(farmId, productionDate)
                .orElseThrow(() -> new IllegalStateException("Falha ao recuperar o consolidado salvo."));
    }

    @Override
    public Optional<FarmMilkProduction> findByFarmIdAndProductionDate(Long farmId, LocalDate productionDate) {
        return repository.findByFarmIdAndProductionDate(farmId, productionDate);
    }

    @Override
    public List<FarmMilkProduction> findByFarmIdAndProductionDateBetween(Long farmId, LocalDate from, LocalDate to) {
        return repository.findByFarmIdAndProductionDateBetweenOrderByProductionDateAsc(farmId, from, to);
    }
}
