package com.devmaster.goatfarm.goat.application.ports.out;

import java.util.List;

/** Aggregated herd data without leaking a Spring projection. */
public record GoatHerdSnapshot(
        long total,
        long males,
        long females,
        long active,
        long inactive,
        long sold,
        long deceased,
        List<GoatBreedCount> breeds,
        long withoutBreed
) {
    public GoatHerdSnapshot {
        breeds = List.copyOf(breeds == null ? List.of() : breeds);
    }
}
