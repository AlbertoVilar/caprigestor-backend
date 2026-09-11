package com.devmaster.goatfarm.goat.api.dto;

import com.devmaster.goatfarm.goat.enums.RegistrationRectificationSource;

import java.time.LocalDateTime;

public record GoatRegistrationRectificationResponseDTO(
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
