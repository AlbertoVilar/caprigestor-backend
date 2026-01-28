package com.devmaster.goatfarm.application.ports.out;

import com.devmaster.goatfarm.milk.enums.MilkingShift;
import com.devmaster.goatfarm.milk.model.entity.MilkProduction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MilkProductionPersistencePort {

    MilkProduction save(MilkProduction milkProduction);

    boolean existsByFarmIdAndGoatIdAndDateAndShift(
            Long farmId,
            String goatId,
            LocalDate date,
            MilkingShift shift
    );

    Optional<MilkProduction> findById(Long farmId, String goatId, Long id);

    Page<MilkProduction> search(
            Long farmId,
            String goatId,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    );

    List<MilkProduction> findByFarmIdAndGoatIdAndDateBetween(
            Long farmId,
            String goatId,
            LocalDate from,
            LocalDate to
    );

    void delete(MilkProduction milkProduction);
}
