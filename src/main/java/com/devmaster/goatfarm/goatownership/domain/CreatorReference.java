package com.devmaster.goatfarm.goatownership.domain;

import java.time.Instant;
import java.util.Locale;

/**
 * Immutable provenance of the farm or external party that created a goat.
 *
 * <p>The creator TOD is deliberately independent from the current owner's
 * farm TOD. A purchase/import may therefore have an owner with a different
 * TOD while retaining the original registral origin.</p>
 */
public record CreatorReference(
        String creatorTod,
        Long creatorFarmId,
        String creatorNameSnapshot,
        CreatorSource source,
        String evidenceReference,
        Instant recordedAt
) {

    public CreatorReference {
        creatorTod = normalizeOptional(creatorTod);
        creatorNameSnapshot = normalizeOptional(creatorNameSnapshot);
        evidenceReference = normalizeOptional(evidenceReference);
        if (creatorFarmId != null && creatorFarmId <= 0) {
            throw new IllegalArgumentException("creatorFarmId must be positive when provided");
        }
        if (source == null) {
            throw new IllegalArgumentException("source must not be null");
        }
        if (recordedAt == null) {
            throw new IllegalArgumentException("recordedAt must not be null");
        }
        if (creatorFarmId != null && creatorTod == null) {
            throw new IllegalArgumentException("creatorTod is required when creatorFarmId is provided");
        }
        if (source == CreatorSource.UNKNOWN && (creatorFarmId != null || creatorTod != null)) {
            throw new IllegalArgumentException("UNKNOWN creator cannot carry a farm or TOD");
        }
    }

    public static CreatorReference farm(
            String creatorTod,
            long creatorFarmId,
            String creatorNameSnapshot,
            CreatorSource source,
            String evidenceReference,
            Instant recordedAt
    ) {
        return new CreatorReference(creatorTod, creatorFarmId, creatorNameSnapshot, source, evidenceReference, recordedAt);
    }

    public static CreatorReference external(
            String creatorTod,
            String creatorNameSnapshot,
            CreatorSource source,
            String evidenceReference,
            Instant recordedAt
    ) {
        return new CreatorReference(creatorTod, null, creatorNameSnapshot, source, evidenceReference, recordedAt);
    }

    public static CreatorReference unknown(Instant recordedAt) {
        return new CreatorReference(null, null, null, CreatorSource.UNKNOWN, null, recordedAt);
    }

    public boolean isFarmLinked() {
        return creatorFarmId != null;
    }

    private static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }
}
