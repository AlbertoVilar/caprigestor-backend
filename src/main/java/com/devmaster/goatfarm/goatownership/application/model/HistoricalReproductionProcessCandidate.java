package com.devmaster.goatfarm.goatownership.application.model;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.reproduction.enums.PregnancyCloseReason;
import com.devmaster.goatfarm.reproduction.enums.PregnancyStatus;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Technology-neutral representation of a candidate pregnancy loaded from persistence
 * before business visibility rules and field redaction are applied.
 */
public record HistoricalReproductionProcessCandidate(
        Long id,
        GoatId goatId,
        Long farmId,
        PregnancyStatus status,
        LocalDate breedingDate,
        LocalDate confirmDate,
        LocalDate expectedDueDate,
        LocalDate closedAt,
        PregnancyCloseReason closeReason,
        Long coverageEventId,
        String notes
) {
    public HistoricalReproductionProcessCandidate {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(goatId, "goatId must not be null");
        Objects.requireNonNull(farmId, "farmId must not be null");
        Objects.requireNonNull(status, "status must not be null");
    }
}
