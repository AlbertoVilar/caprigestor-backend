package com.devmaster.goatfarm.application.ports.out;

import com.devmaster.goatfarm.milk.business.bo.MilkProductionResponseVO;
import com.devmaster.goatfarm.milk.enums.MilkingShift;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface MilkProductionPersistencePort {

    MilkProductionResponseVO save(
            Long farmId,
            String goatId,
            LocalDate date,
            MilkingShift shift,
            BigDecimal volumeLiters,
            String notes
    );

    boolean existsByFarmIdAndGoatIdAndDateAndShift(
            Long farmId,
            String goatId,
            LocalDate date,
            MilkingShift shift
    );
}
