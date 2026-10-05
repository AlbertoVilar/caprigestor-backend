package com.devmaster.goatfarm.goatownership.api.mapper;

import com.devmaster.goatfarm.goatownership.api.dto.OwnershipHistoryPeriodResponseDTO;
import com.devmaster.goatfarm.goatownership.api.dto.OwnershipHistoryResponseDTO;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipHistory;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipHistoryPeriod;
import org.springframework.stereotype.Component;

/** Maps the neutral ownership-history read model at the HTTP boundary. */
@Component
public class OwnershipHistoryApiMapper {
    public OwnershipHistoryResponseDTO toResponse(OwnershipHistory history) {
        return new OwnershipHistoryResponseDTO(
                history.goatId().value(),
                history.periods().stream().map(this::toPeriodResponse).toList());
    }

    private OwnershipHistoryPeriodResponseDTO toPeriodResponse(OwnershipHistoryPeriod period) {
        return new OwnershipHistoryPeriodResponseDTO(
                period.farmId(), period.startedAt(), period.endedAt(), period.entryType(), period.exitType(), period.current());
    }
}
