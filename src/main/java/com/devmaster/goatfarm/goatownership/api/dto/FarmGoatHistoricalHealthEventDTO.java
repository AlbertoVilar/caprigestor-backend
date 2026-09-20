package com.devmaster.goatfarm.goatownership.api.dto;

import com.devmaster.goatfarm.health.domain.enums.AdministrationRoute;
import com.devmaster.goatfarm.health.domain.enums.DoseUnit;
import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Transport-only representation of a historical health fact. */
public record FarmGoatHistoricalHealthEventDTO(
        Long id,
        Long farmId,
        HealthEventType type,
        HealthEventStatus status,
        String title,
        String description,
        LocalDate scheduledDate,
        LocalDateTime performedAt,
        String responsible,
        String notes,
        String productName,
        String activeIngredient,
        BigDecimal dose,
        DoseUnit doseUnit,
        AdministrationRoute route,
        String batchNumber,
        Integer withdrawalMilkDays,
        Integer withdrawalMeatDays,
        LocalDate milkWithdrawalEndDate,
        LocalDate meatWithdrawalEndDate
) {
}
