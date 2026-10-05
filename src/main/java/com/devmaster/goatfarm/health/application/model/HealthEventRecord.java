package com.devmaster.goatfarm.health.application.model;

import com.devmaster.goatfarm.health.domain.enums.AdministrationRoute;
import com.devmaster.goatfarm.health.domain.enums.DoseUnit;
import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Technology-neutral health event representation shared by the application
 * services and the persistence port. Persistence annotations and relations
 * intentionally remain outside this model.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthEventRecord {

    private Long id;
    private Long farmId;
    private Long goatTechnicalId;
    private String goatId;
    private HealthEventType type;
    private HealthEventStatus status;
    private String title;
    private String description;
    private LocalDate scheduledDate;
    private LocalDateTime performedAt;
    private String responsible;
    private String notes;
    private String productName;
    private String activeIngredient;
    private BigDecimal dose;
    private DoseUnit doseUnit;
    private AdministrationRoute route;
    private String batchNumber;
    private Integer withdrawalMilkDays;
    private Integer withdrawalMeatDays;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
