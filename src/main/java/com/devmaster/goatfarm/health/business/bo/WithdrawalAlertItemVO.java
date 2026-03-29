package com.devmaster.goatfarm.health.business.bo;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record WithdrawalAlertItemVO(
        Long eventId,
        String goatId,
        String title,
        String productName,
        String activeIngredient,
        LocalDate performedDate,
        LocalDate withdrawalEndDate,
        int daysRemaining
) {
}
