package com.devmaster.goatfarm.goat.business.bo;

import com.devmaster.goatfarm.goat.enums.RegistrationRectificationSource;

import java.time.LocalDateTime;

public record GoatRegistrationHistoryResponseVO(
        Long id,
        Long technicalGoatId,
        Long farmId,
        String oldRegistrationNumber,
        String oldTod,
        String oldToe,
        String newRegistrationNumber,
        String newTod,
        String newToe,
        RegistrationRectificationSource source,
        String evidenceReference,
        String reason,
        Long actorUserId,
        LocalDateTime createdAt
) {
}
