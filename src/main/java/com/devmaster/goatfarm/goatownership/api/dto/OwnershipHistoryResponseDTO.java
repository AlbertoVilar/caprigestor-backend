package com.devmaster.goatfarm.goatownership.api.dto;

import java.util.List;

/** Stable HTTP representation of a goat ownership history. */
public record OwnershipHistoryResponseDTO(
        Long goatId,
        List<OwnershipHistoryPeriodResponseDTO> periods
) {
}
