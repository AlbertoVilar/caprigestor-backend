package com.devmaster.goatfarm.goatownership.api.mapper;

import com.devmaster.goatfarm.goatownership.api.dto.FarmGoatHistoricalForeignCoverageContextDTO;
import com.devmaster.goatfarm.goatownership.api.dto.FarmGoatHistoricalReproductionEventDTO;
import com.devmaster.goatfarm.goatownership.api.dto.FarmGoatHistoricalReproductionProcessDTO;
import com.devmaster.goatfarm.goatownership.api.dto.FarmGoatHistoricalReproductionResponseDTO;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatHistoricalReproductionSnapshot;
import com.devmaster.goatfarm.goatownership.application.model.HistoricalReproductionEventItem;
import com.devmaster.goatfarm.goatownership.application.model.HistoricalReproductionProcessItem;
import com.devmaster.goatfarm.goatownership.application.model.MinimalForeignCoverageContext;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper between application models and HTTP response DTOs for the historical reproduction dossier.
 */
@Component
public class FarmGoatHistoricalReproductionApiMapper {

    public FarmGoatHistoricalReproductionResponseDTO toResponse(FarmGoatHistoricalReproductionSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }

        List<FarmGoatHistoricalReproductionProcessDTO> processes = snapshot.processes().stream()
                .map(this::toProcessDto)
                .toList();

        List<FarmGoatHistoricalReproductionEventDTO> events = snapshot.events().stream()
                .map(this::toEventDto)
                .toList();

        return new FarmGoatHistoricalReproductionResponseDTO(
                snapshot.goatId().value(),
                processes,
                events
        );
    }

    public FarmGoatHistoricalReproductionProcessDTO toProcessDto(HistoricalReproductionProcessItem item) {
        if (item == null) {
            return null;
        }
        return new FarmGoatHistoricalReproductionProcessDTO(
                item.pregnancyId(),
                item.processOriginFarmId(),
                item.breedingDate(),
                item.confirmDate(),
                item.expectedDueDate(),
                item.coverageEventId(),
                item.status(),
                item.closedAt(),
                item.closeReason(),
                toForeignCoverageContextDto(item.foreignCoverageContext())
        );
    }

    public FarmGoatHistoricalReproductionEventDTO toEventDto(HistoricalReproductionEventItem item) {
        if (item == null) {
            return null;
        }
        return new FarmGoatHistoricalReproductionEventDTO(
                item.id(),
                item.farmId(),
                item.eventType(),
                item.eventDate(),
                item.breedingType(),
                item.breederRef(),
                item.pregnancyId(),
                item.relatedEventId(),
                item.correctedEventDate(),
                item.checkScheduledDate(),
                item.checkResult(),
                item.notes()
        );
    }

    public FarmGoatHistoricalForeignCoverageContextDTO toForeignCoverageContextDto(MinimalForeignCoverageContext context) {
        if (context == null) {
            return null;
        }
        return new FarmGoatHistoricalForeignCoverageContextDTO(
                context.coverageEventId(),
                context.originFarmId(),
                context.coverageDate(),
                context.breedingType(),
                context.breederRef()
        );
    }
}
