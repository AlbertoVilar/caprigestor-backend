package com.devmaster.goatfarm.milk.persistence.adapter;

import com.devmaster.goatfarm.milk.application.ports.out.FarmMilkProductionPersistencePort;
import com.devmaster.goatfarm.milk.domain.FarmMilkProduction;
import com.devmaster.goatfarm.milk.persistence.entity.FarmMilkProductionEntity;
import com.devmaster.goatfarm.milk.persistence.mapper.FarmMilkProductionPersistenceMapper;
import com.devmaster.goatfarm.milk.persistence.repository.FarmMilkProductionRepository;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class FarmMilkProductionPersistenceAdapter implements FarmMilkProductionPersistencePort {

    private final FarmMilkProductionRepository repository;
    private final FarmMilkProductionPersistenceMapper mapper;

    public FarmMilkProductionPersistenceAdapter(FarmMilkProductionRepository repository) {
        this(repository, new FarmMilkProductionPersistenceMapper());
    }

    @Autowired
    public FarmMilkProductionPersistenceAdapter(FarmMilkProductionRepository repository,
                                                FarmMilkProductionPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
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
                .map(mapper::toDomain)
                .orElseThrow(() -> new IllegalStateException("Falha ao recuperar o consolidado salvo."));
    }

    @Override
    public Optional<FarmMilkProduction> findByFarmIdAndProductionDate(Long farmId, LocalDate productionDate) {
        return repository.findByFarmIdAndProductionDate(farmId, productionDate).map(mapper::toDomain);
    }

    @Override
    public List<FarmMilkProduction> findByFarmIdAndProductionDateBetween(Long farmId, LocalDate from, LocalDate to) {
        return repository.findByFarmIdAndProductionDateBetweenOrderByProductionDateAsc(farmId, from, to)
                .stream().map(mapper::toDomain).toList();
    }
}
