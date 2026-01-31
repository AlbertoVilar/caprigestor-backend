package com.devmaster.goatfarm.health.business.bo;

import com.devmaster.goatfarm.health.domain.enums.AdministrationRoute;
import com.devmaster.goatfarm.health.domain.enums.DoseUnit;
import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthEventResponseVO {
    private Long id;
    private Long farmId;
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
    private boolean overdue;
}
