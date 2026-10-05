package com.devmaster.goatfarm.goatownership.application.model;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.domain.GoatOwnershipPeriod;

import java.util.Optional;

/** Result of the canonical ownership mutation lock. */
public record GoatOwnershipLockState(GoatId goatId, Optional<GoatOwnershipPeriod> openPeriod) {
    public GoatOwnershipLockState {
        if (goatId == null) {
            throw new IllegalArgumentException("goatId must not be null");
        }
        if (openPeriod == null) {
            throw new IllegalArgumentException("openPeriod must not be null");
        }
    }
}
