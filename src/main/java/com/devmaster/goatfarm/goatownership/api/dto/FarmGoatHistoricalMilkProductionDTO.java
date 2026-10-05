package com.devmaster.goatfarm.goatownership.api.dto;

import com.devmaster.goatfarm.milk.enums.MilkProductionStatus;
import com.devmaster.goatfarm.milk.enums.MilkingShift;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Historical milk production record exposed in the farm goat registry historical dossier.
 */
public record FarmGoatHistoricalMilkProductionDTO(
        Long id,
        Long goatId,
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
}
