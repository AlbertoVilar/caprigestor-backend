package com.devmaster.goatfarm.goatownership.api.mapper;

import com.devmaster.goatfarm.goatownership.api.dto.FarmGoatHistoricalLactationDTO;
import com.devmaster.goatfarm.goatownership.api.dto.FarmGoatHistoricalMilkProductionDTO;
import com.devmaster.goatfarm.goatownership.api.dto.FarmGoatHistoricalMilkLactationResponseDTO;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatHistoricalLactationItem;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatHistoricalMilkLactationSnapshot;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatHistoricalMilkProductionItem;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper between application models and external HTTP response DTOs for the historical milk/lactation dossier.
 */
@Component
public class FarmGoatHistoricalMilkLactationApiMapper {

    public FarmGoatHistoricalMilkLactationResponseDTO toResponse(FarmGoatHistoricalMilkLactationSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }

        List<FarmGoatHistoricalLactationDTO> lactations = snapshot.lactations().stream()
                .map(this::toLactationDto)
                .toList();

        List<FarmGoatHistoricalMilkProductionDTO> productions = snapshot.milkProductions().stream()
                .map(this::toProductionDto)
                .toList();

        return new FarmGoatHistoricalMilkLactationResponseDTO(
                snapshot.goatId().value(),
                lactations,
                productions
        );
    }

    public FarmGoatHistoricalLactationDTO toLactationDto(FarmGoatHistoricalLactationItem item) {
        if (item == null) {
            return null;
        }
        return new FarmGoatHistoricalLactationDTO(
                item.id(),
                item.goatId().value(),
                item.farmId(),
                item.status(),
                item.startDate(),
                item.endDate(),
                item.pregnancyStartDate(),
                item.dryStartDate(),
                item.dryAtPregnancyDays(),
                item.restDays(),
                item.active()
        );
    }

    public FarmGoatHistoricalMilkProductionDTO toProductionDto(FarmGoatHistoricalMilkProductionItem item) {
        if (item == null) {
            return null;
        }
        return new FarmGoatHistoricalMilkProductionDTO(
                item.id(),
                item.goatId().value(),
                item.lactationId(),
                item.farmId(),
                item.date(),
                item.shift(),
                item.volumeLiters(),
                item.status(),
                item.notes(),
                item.canceledAt(),
                item.canceledReason(),
                item.recordedDuringMilkWithdrawal(),
                item.milkWithdrawalEventId(),
                item.milkWithdrawalEndDate(),
                item.milkWithdrawalSource()
        );
    }
}
