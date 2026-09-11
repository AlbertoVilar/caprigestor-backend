package com.devmaster.goatfarm.goat.business.bo;

import com.devmaster.goatfarm.goat.enums.RegistrationRectificationSource;

import java.time.LocalDateTime;

public record GoatRegistrationRectificationResponseVO(
        Long technicalGoatId,
        String previousRegistrationNumber,
        String previousTod,
        String previousToe,
        String currentRegistrationNumber,
        String currentTod,
        String currentToe,
        RegistrationRectificationSource source,
        LocalDateTime changedAt
) {
}
