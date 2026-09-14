package com.devmaster.goatfarm.goatownership.application.model;

import com.devmaster.goatfarm.goat.domain.GoatId;

import java.util.List;
import java.util.Objects;

/** Neutral application read model for the canonical ownership timeline. */
public record OwnershipHistory(GoatId goatId, List<OwnershipHistoryPeriod> periods) {
    public OwnershipHistory {
        goatId = Objects.requireNonNull(goatId, "goatId must not be null");
        periods = List.copyOf(Objects.requireNonNull(periods, "periods must not be null"));
    }
}
