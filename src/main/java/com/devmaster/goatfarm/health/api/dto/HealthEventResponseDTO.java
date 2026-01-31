package com.devmaster.goatfarm.health.api.dto;

import com.devmaster.goatfarm.health.domain.enums.AdministrationRoute;
import com.devmaster.goatfarm.health.domain.enums.DoseUnit;
import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record HealthEventResponseDTO(
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
    String activeIngredient,
    BigDecimal dose,
    DoseUnit doseUnit,
    AdministrationRoute route,
    String batchNumber,
    Integer withdrawalMilkDays,
    Integer withdrawalMeatDays,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    boolean overdue
) {}
