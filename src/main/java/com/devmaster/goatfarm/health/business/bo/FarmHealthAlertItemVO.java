package com.devmaster.goatfarm.health.business.bo;

import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record FarmHealthAlertItemVO(
        Long id,
        String goatId,
        HealthEventType type,
        HealthEventStatus status,
        String title,
        LocalDate scheduledDate,
        boolean overdue
) {
}
