package com.devmaster.goatfarm.milk.application.ports.out;

import com.devmaster.goatfarm.milk.enums.MilkingShift;
import com.devmaster.goatfarm.milk.domain.MilkProduction;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;

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

    boolean existsActiveByGoatTechnicalIdAndDateAndShift(
            GoatId goatId,
            LocalDate date,
            MilkingShift shift
    );

    Optional<MilkProduction> findById(Long farmId, String goatId, Long id);

    PageResult<MilkProduction> search(
            Long farmId,
            String goatId,
            LocalDate from,
            LocalDate to,
            PageQuery pageQuery,
            boolean includeCanceled
    );

    List<MilkProduction> findByFarmIdAndGoatIdAndDateBetween(
            Long farmId,
            String goatId,
            LocalDate from,
            LocalDate to
    );

    void delete(MilkProduction milkProduction);
}
