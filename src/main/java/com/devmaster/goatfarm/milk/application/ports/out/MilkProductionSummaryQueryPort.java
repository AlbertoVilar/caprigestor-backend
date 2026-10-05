package com.devmaster.goatfarm.milk.application.ports.out;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Minimal application read contract used by Lactation summaries. */
public interface MilkProductionSummaryQueryPort {

    List<MilkProductionSnapshot> findSummaryByFarmIdAndGoatIdAndDateBetween(
            Long farmId,
            String goatId,
            LocalDate from,
            LocalDate to
    );

    record MilkProductionSnapshot(LocalDate date, BigDecimal volumeLiters) {
    }
}
