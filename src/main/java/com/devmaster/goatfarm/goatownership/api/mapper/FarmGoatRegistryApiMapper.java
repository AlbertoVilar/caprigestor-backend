package com.devmaster.goatfarm.goatownership.api.mapper;

import com.devmaster.goatfarm.goatownership.api.dto.FarmGoatHistoricalDossierBasicResponseDTO;
import com.devmaster.goatfarm.goatownership.api.dto.FarmGoatRegistryResponseDTO;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatHistoricalDossierBasicItem;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatRegistryItem;
import org.springframework.stereotype.Component;

import java.util.List;

/** Maps the neutral farm goat registry read model at the HTTP boundary. */
@Component
public class FarmGoatRegistryApiMapper {

    public FarmGoatRegistryResponseDTO toResponse(FarmGoatRegistryItem item) {
        if (item == null) {
            return null;
        }
        return new FarmGoatRegistryResponseDTO(
                item.goatId().value(),
                item.registrationNumber(),
                item.name(),
                item.globalStatus(),
                item.creatorFarmId(),
                item.creatorNameSnapshot(),
                item.roles(),
                item.disposition(),
                item.currentOwnerFarmId()
        );
    }

    public List<FarmGoatRegistryResponseDTO> toResponseList(List<FarmGoatRegistryItem> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream()
                .map(this::toResponse)
                .toList();
    }

    public FarmGoatHistoricalDossierBasicResponseDTO toDossierResponse(FarmGoatHistoricalDossierBasicItem item) {
        if (item == null) {
            return null;
        }
        return new FarmGoatHistoricalDossierBasicResponseDTO(
                item.goatId().value(),
                item.registrationNumber(),
                item.name(),
                item.globalStatus(),
                item.gender(),
                item.breed(),
                item.color(),
                item.birthDate(),
                item.category(),
                item.tod(),
                item.toe(),
                item.fatherName(),
                item.fatherRegistrationNumber(),
                item.motherName(),
                item.motherRegistrationNumber(),
                item.creatorFarmId(),
                item.creatorNameSnapshot(),
                item.roles(),
                item.disposition(),
                item.currentOwnerFarmId()
        );
    }
}
