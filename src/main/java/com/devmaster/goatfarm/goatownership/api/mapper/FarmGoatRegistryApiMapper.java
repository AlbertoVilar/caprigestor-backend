package com.devmaster.goatfarm.goatownership.api.mapper;

import com.devmaster.goatfarm.goatownership.api.dto.FarmGoatRegistryResponseDTO;
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
}
