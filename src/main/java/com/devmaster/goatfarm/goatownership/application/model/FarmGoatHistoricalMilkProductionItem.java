package com.devmaster.goatfarm.goatownership.application.model;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.milk.enums.MilkProductionStatus;
import com.devmaster.goatfarm.milk.enums.MilkingShift;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Technology-neutral application read model representing one historical milk production record.
 */
public record FarmGoatHistoricalMilkProductionItem(
        Long id,
        GoatId goatId,
        Long lactationId,
        Long farmId,
        LocalDate date,
        MilkingShift shift,
        BigDecimal volumeLiters,
        MilkProductionStatus status,
        String notes,
        LocalDateTime canceledAt,
        String canceledReason,
        boolean recordedDuringMilkWithdrawal,
        Long milkWithdrawalEventId,
        LocalDate milkWithdrawalEndDate,
        String milkWithdrawalSource
) {
    public FarmGoatHistoricalMilkProductionItem {
        Objects.requireNonNull(goatId, "goatId must not be null");
        Objects.requireNonNull(farmId, "farmId must not be null");
        Objects.requireNonNull(date, "date must not be null");
        Objects.requireNonNull(shift, "shift must not be null");
        Objects.requireNonNull(volumeLiters, "volumeLiters must not be null");
        Objects.requireNonNull(status, "status must not be null");
    }
}
