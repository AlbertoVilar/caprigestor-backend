package com.devmaster.goatfarm.health.business.bo;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record HealthWithdrawalOriginVO(
        Long eventId,
        String title,
        String productName,
        String activeIngredient,
        String batchNumber,
        LocalDate performedDate,
        LocalDate withdrawalEndDate
) {
}
