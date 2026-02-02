package com.devmaster.goatfarm.health.api.dto;

import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;

import java.time.LocalDate;
import java.util.List;

public record FarmHealthAlertsResponseDTO(
        int dueTodayCount,
        int upcomingCount,
        int overdueCount,

        List<AlertItemDTO> dueTodayTop,
        List<AlertItemDTO> upcomingTop,
        List<AlertItemDTO> overdueTop,

        Integer windowDays
) {

    public record AlertItemDTO(
            Long id,
            String goatId,
            HealthEventType type,
            HealthEventStatus status,
            String title,
            LocalDate scheduledDate,
            boolean overdue
    ) {}
}
