package com.devmaster.goatfarm.milk.persistence.adapter;

import com.devmaster.goatfarm.milk.application.ports.out.MilkProductionPersistencePort;
import com.devmaster.goatfarm.milk.enums.MilkingShift;
import com.devmaster.goatfarm.milk.persistence.entity.MilkProduction;
import com.devmaster.goatfarm.milk.persistence.repository.MilkProductionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class MilkProductionPersistenceAdapter implements MilkProductionPersistencePort {

    private final MilkProductionRepository milkProductionRepository;

    public MilkProductionPersistenceAdapter(MilkProductionRepository milkProductionRepository) {
        this.milkProductionRepository = milkProductionRepository;
    }

    @Override
    public MilkProduction save(MilkProduction milkProduction) {
        return milkProductionRepository.save(milkProduction);
    }

    @Override
    public void delete(MilkProduction milkProduction) {
        milkProductionRepository.delete(milkProduction);
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
    public Optional<MilkProduction> findById(Long farmId, String goatId, Long id) {
        return milkProductionRepository.findByIdAndFarmIdAndGoatId(id, farmId, goatId);
    }

    @Override
    public Page<MilkProduction> search(
            Long farmId,
            String goatId,
            LocalDate from,
            LocalDate to,
            Pageable pageable,
            boolean includeCanceled
    ) {
        return milkProductionRepository.search(
                farmId,
                goatId,
                from,
                to,
                pageable,
                includeCanceled
        );
    }

    @Override
    public List<MilkProduction> findByFarmIdAndGoatIdAndDateBetween(
            Long farmId,
            String goatId,
            LocalDate from,
            LocalDate to
    ) {
        return milkProductionRepository.findByFarmIdAndGoatIdAndDateBetween(farmId, goatId, from, to);
    }

}
