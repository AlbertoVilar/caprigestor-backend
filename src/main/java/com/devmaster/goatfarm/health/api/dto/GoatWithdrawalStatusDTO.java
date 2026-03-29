package com.devmaster.goatfarm.health.api.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record GoatWithdrawalStatusDTO(
        String goatId,
        LocalDate referenceDate,
        boolean hasActiveMilkWithdrawal,
        boolean hasActiveMeatWithdrawal,
        HealthWithdrawalOriginDTO milkWithdrawal,
        HealthWithdrawalOriginDTO meatWithdrawal
) {
}
