package com.devmaster.goatfarm.health.business.bo;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record GoatWithdrawalStatusVO(
        String goatId,
        LocalDate referenceDate,
        boolean hasActiveMilkWithdrawal,
        boolean hasActiveMeatWithdrawal,
        HealthWithdrawalOriginVO milkWithdrawal,
        HealthWithdrawalOriginVO meatWithdrawal
) {
}
