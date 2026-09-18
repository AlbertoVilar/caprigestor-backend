package com.devmaster.goatfarm.goatownership.application.model;

import com.devmaster.goatfarm.goat.domain.GoatId;

import java.util.List;
import java.util.Objects;

/**
 * Technology-neutral aggregate snapshot of a goat's historical lactation and milk production
 * in the context of a specific farm registry.
 */
public record FarmGoatHistoricalMilkLactationSnapshot(
        GoatId goatId,
        List<FarmGoatHistoricalLactationItem> lactations,
        List<FarmGoatHistoricalMilkProductionItem> milkProductions
) {
    public FarmGoatHistoricalMilkLactationSnapshot {
        Objects.requireNonNull(goatId, "goatId must not be null");
        lactations = lactations == null ? List.of() : List.copyOf(lactations);
        milkProductions = milkProductions == null ? List.of() : List.copyOf(milkProductions);
    }
}
