package com.devmaster.goatfarm.goatownership.domain;

import com.devmaster.goatfarm.goat.domain.GoatId;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Immutable-in-history interval in which a farm owns a goat.
 *
 * <p>The period may be closed exactly once. Cross-period overlap and the
 * single-open-period rule are checked by {@link #ensureConsistent(Collection)}
 * before persistence.</p>
 */
public final class GoatOwnershipPeriod {
    private final Long id;
    private final GoatId goatId;
    private final long farmId;
    private final Instant startedAt;
    private final OwnershipEntryType entryType;
    private final String source;
    private Instant endedAt;
    private OwnershipExitType exitType;

    private GoatOwnershipPeriod(
            Long id,
            GoatId goatId,
            long farmId,
            Instant startedAt,
            Instant endedAt,
            OwnershipEntryType entryType,
            OwnershipExitType exitType,
            String source
    ) {
        this.id = id;
        this.goatId = Objects.requireNonNull(goatId, "goatId must not be null");
        if (farmId <= 0) {
            throw new IllegalArgumentException("farmId must be positive");
        }
        this.farmId = farmId;
        this.startedAt = Objects.requireNonNull(startedAt, "startedAt must not be null");
        this.endedAt = endedAt;
        this.entryType = Objects.requireNonNull(entryType, "entryType must not be null");
        this.exitType = exitType;
        this.source = requireText(source, "source");
        if (endedAt != null && !startedAt.isBefore(endedAt)) {
            throw new IllegalArgumentException("startedAt must be before endedAt");
        }
        if (endedAt == null && exitType != null) {
            throw new IllegalArgumentException("open period cannot have an exit type");
        }
        if (endedAt != null && exitType == null) {
            throw new IllegalArgumentException("closed period must have an exit type");
        }
    }

    public static GoatOwnershipPeriod open(
            GoatId goatId,
            long farmId,
            Instant startedAt,
            OwnershipEntryType entryType,
            String source
    ) {
        return new GoatOwnershipPeriod(null, goatId, farmId, startedAt, null, entryType, null, source);
    }

    public static GoatOwnershipPeriod rehydrate(
            Long id,
            GoatId goatId,
            long farmId,
            Instant startedAt,
            Instant endedAt,
            OwnershipEntryType entryType,
            OwnershipExitType exitType,
            String source
    ) {
        return new GoatOwnershipPeriod(id, goatId, farmId, startedAt, endedAt, entryType, exitType, source);
    }

    public void close(Instant endedAt, OwnershipExitType exitType) {
        if (!isOpen()) {
            throw new IllegalStateException("ownership period is already closed");
        }
        Objects.requireNonNull(endedAt, "endedAt must not be null");
        Objects.requireNonNull(exitType, "exitType must not be null");
        if (!startedAt.isBefore(endedAt)) {
            throw new IllegalArgumentException("endedAt must be after startedAt");
        }
        this.endedAt = endedAt;
        this.exitType = exitType;
    }

    public boolean isOpen() {
        return endedAt == null;
    }

    /** End is exclusive, so a new period may start exactly at endedAt. */
    public boolean contains(Instant moment) {
        Objects.requireNonNull(moment, "moment must not be null");
        return !moment.isBefore(startedAt) && (endedAt == null || moment.isBefore(endedAt));
    }

    public boolean overlaps(GoatOwnershipPeriod other) {
        Objects.requireNonNull(other, "other must not be null");
        if (!goatId.equals(other.goatId)) {
            return false;
        }
        Instant thisEnd = endedAt == null ? Instant.MAX : endedAt;
        Instant otherEnd = other.endedAt == null ? Instant.MAX : other.endedAt;
        return startedAt.isBefore(otherEnd) && other.startedAt.isBefore(thisEnd);
    }

    public static void ensureConsistent(Collection<GoatOwnershipPeriod> periods) {
        Objects.requireNonNull(periods, "periods must not be null");
        periods.stream().filter(Objects::isNull).findAny().ifPresent(p -> {
            throw new IllegalArgumentException("periods must not contain null");
        });
        var ordered = periods.stream().sorted(Comparator.comparing(GoatOwnershipPeriod::startedAt)).toList();
        for (int index = 0; index < ordered.size(); index++) {
            GoatOwnershipPeriod current = ordered.get(index);
            for (int nextIndex = index + 1; nextIndex < ordered.size(); nextIndex++) {
                GoatOwnershipPeriod next = ordered.get(nextIndex);
                if (current.overlaps(next)) {
                    throw new IllegalArgumentException("ownership periods overlap for the same GoatId");
                }
            }
        }
        Map<GoatId, Long> openPeriodsByGoat = periods.stream()
                .filter(GoatOwnershipPeriod::isOpen)
                .collect(Collectors.groupingBy(GoatOwnershipPeriod::goatId, Collectors.counting()));
        if (openPeriodsByGoat.values().stream().anyMatch(count -> count > 1)) {
            throw new IllegalArgumentException("a GoatId may have at most one open ownership period");
        }
    }

    public Long id() { return id; }
    public GoatId goatId() { return goatId; }
    public long farmId() { return farmId; }
    public Instant startedAt() { return startedAt; }
    public Instant endedAt() { return endedAt; }
    public OwnershipEntryType entryType() { return entryType; }
    public OwnershipExitType exitType() { return exitType; }
    public String source() { return source; }

    private static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }
}
