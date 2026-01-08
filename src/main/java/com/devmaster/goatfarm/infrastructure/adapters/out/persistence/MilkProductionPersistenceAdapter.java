package com.devmaster.goatfarm.infrastructure.adapters.out.persistence;

import com.devmaster.goatfarm.application.ports.out.MilkProductionPersistencePort;
import com.devmaster.goatfarm.milk.enums.MilkingShift;
import com.devmaster.goatfarm.milk.model.entity.MilkProduction;
import com.devmaster.goatfarm.milk.model.repository.MilkProductionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class MilkProductionPersistenceAdapter implements MilkProductionPersistencePort {

    private final MilkProductionRepository milkProductionRepository;

    @Override
    public MilkProduction save(MilkProduction milkProduction) {
        return milkProductionRepository.save(milkProduction);
    }

    @Override
    public boolean existsByFarmIdAndGoatIdAndDateAndShift(
            Long farmId,
            String goatId,
            LocalDate date,
            MilkingShift shift
    ) {
        return milkProductionRepository.existsByFarmIdAndGoatIdAndDateAndShift(
                farmId,
                goatId,
                date,
                shift
        );
    }

    @Override
    public Page<MilkProduction> search(
            Long farmId,
            String goatId,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    ) {
        return milkProductionRepository.search(
                farmId,
                goatId,
                from,
                to,
                pageable
        );
    }

}
