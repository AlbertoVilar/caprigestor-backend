package com.devmaster.goatfarm.health.api.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record HealthWithdrawalOriginDTO(
        Long eventId,
        String title,
        String productName,
        String activeIngredient,
        String batchNumber,
        LocalDate performedDate,
        LocalDate withdrawalEndDate
) {
}
