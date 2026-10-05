package com.devmaster.goatfarm.goatownership.application.model;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.health.domain.enums.AdministrationRoute;
import com.devmaster.goatfarm.health.domain.enums.DoseUnit;
import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Technology-neutral read model representing one historical health event fact.
 */
public record HistoricalHealthEventItem(
        Long id,
        GoatId goatId,
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
        Integer withdrawalMeatDays
) {
    public HistoricalHealthEventItem {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(goatId, "goatId must not be null");
        Objects.requireNonNull(farmId, "farmId must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(scheduledDate, "scheduledDate must not be null");
    }

    public LocalDate milkWithdrawalEndDate() {
        if (performedAt == null || withdrawalMilkDays == null || withdrawalMilkDays <= 0) {
            return null;
        }
        return performedAt.toLocalDate().plusDays(withdrawalMilkDays);
    }

    public LocalDate meatWithdrawalEndDate() {
        if (performedAt == null || withdrawalMeatDays == null || withdrawalMeatDays <= 0) {
            return null;
        }
        return performedAt.toLocalDate().plusDays(withdrawalMeatDays);
    }
}
