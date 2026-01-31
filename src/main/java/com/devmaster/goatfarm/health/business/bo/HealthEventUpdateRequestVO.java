package com.devmaster.goatfarm.health.business.bo;

import com.devmaster.goatfarm.health.domain.enums.AdministrationRoute;
import com.devmaster.goatfarm.health.domain.enums.DoseUnit;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record HealthEventUpdateRequestVO(
    HealthEventType type,
    String title,
    String description,
    LocalDate scheduledDate,
    String notes,
    String productName,
    String activeIngredient,
    BigDecimal dose,
    DoseUnit doseUnit,
    AdministrationRoute route,
    String batchNumber,
    Integer withdrawalMilkDays,
    Integer withdrawalMeatDays
) {}
