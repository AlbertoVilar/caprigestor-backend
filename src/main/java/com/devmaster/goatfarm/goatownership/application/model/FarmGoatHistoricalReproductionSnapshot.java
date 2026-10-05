package com.devmaster.goatfarm.goatownership.application.model;

import com.devmaster.goatfarm.goat.domain.GoatId;

import java.util.List;
import java.util.Objects;

/**
 * Technology-neutral snapshot of a goat's historical reproduction dossier
 * in the context of a specific farm registry.
 */
public record FarmGoatHistoricalReproductionSnapshot(
        GoatId goatId,
        List<HistoricalReproductionProcessItem> processes,
        List<HistoricalReproductionEventItem> events
) {
    public FarmGoatHistoricalReproductionSnapshot {
        Objects.requireNonNull(goatId, "goatId must not be null");
        processes = processes == null ? List.of() : List.copyOf(processes);
        events = events == null ? List.of() : List.copyOf(events);
    }
}
