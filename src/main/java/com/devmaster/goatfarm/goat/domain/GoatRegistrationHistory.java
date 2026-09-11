package com.devmaster.goatfarm.goat.domain;

import com.devmaster.goatfarm.goat.enums.RegistrationRectificationSource;

import java.time.LocalDateTime;

/** Immutable record of one explicit registration correction. */
public record GoatRegistrationHistory(
        Long id,
        GoatId goatId,
        Long farmId,
        RegistrationIdentity oldIdentity,
        RegistrationIdentity newIdentity,
        RegistrationRectificationSource source,
        String evidenceReference,
        String reason,
        Long actorUserId,
        LocalDateTime createdAt
) {
}
