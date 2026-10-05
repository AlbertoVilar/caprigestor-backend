package com.devmaster.goatfarm.goatownership.application.model;

import com.devmaster.goatfarm.goat.domain.GoatId;

import java.util.List;
import java.util.Objects;

/**
 * Technology-neutral snapshot of a goat's historical health dossier
 * in the context of a specific farm registry.
 */
public record FarmGoatHistoricalHealthSnapshot(
        GoatId goatId,
        List<HistoricalHealthEventItem> events
) {
    public FarmGoatHistoricalHealthSnapshot {
        Objects.requireNonNull(goatId, "goatId must not be null");
        events = events == null ? List.of() : List.copyOf(events);
    }
}
