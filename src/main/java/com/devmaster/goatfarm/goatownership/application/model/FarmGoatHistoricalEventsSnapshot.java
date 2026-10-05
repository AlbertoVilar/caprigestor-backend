package com.devmaster.goatfarm.goatownership.application.model;

import com.devmaster.goatfarm.goat.domain.GoatId;

import java.util.List;
import java.util.Objects;

/** Technology-neutral events section of a farm goat historical dossier. */
public record FarmGoatHistoricalEventsSnapshot(
        GoatId goatId,
        List<HistoricalOperationalEventItem> events
) {
    public FarmGoatHistoricalEventsSnapshot {
        Objects.requireNonNull(goatId, "goatId must not be null");
        events = events == null ? List.of() : List.copyOf(events);
    }
}
