package com.devmaster.goatfarm.health.business.bo;

import com.devmaster.goatfarm.health.domain.enums.AdministrationRoute;
import com.devmaster.goatfarm.health.domain.enums.DoseUnit;
import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record HealthEventResponseVO(

    Long id,
    Long farmId,
    String goatId,
    HealthEventType type,
    HealthEventStatus status,
    String title,
    String description,
    LocalDate scheduledDate,
    LocalDateTime performedAt,
    String responsible,
    String notes,
    String productName,
    BigDecimal dose,
    DoseUnit doseUnit,
    AdministrationRoute route,
    Integer withdrawalMilkDays,
    Integer withdrawalMeatDays,
    boolean overdue,
    LocalDateTime createdAt,
    LocalDateTime updatedAt

) {
}
