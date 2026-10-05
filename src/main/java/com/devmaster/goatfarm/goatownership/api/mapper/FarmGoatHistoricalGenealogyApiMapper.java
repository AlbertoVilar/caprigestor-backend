package com.devmaster.goatfarm.goatownership.api.mapper;

import com.devmaster.goatfarm.genealogy.application.model.GenealogyIntegrationSnapshot;
import com.devmaster.goatfarm.genealogy.application.model.GenealogyTreeNode;
import com.devmaster.goatfarm.genealogy.application.model.GenealogyTreeSnapshot;
import com.devmaster.goatfarm.goatownership.api.dto.FarmGoatHistoricalGenealogyNodeDTO;
import com.devmaster.goatfarm.goatownership.api.dto.FarmGoatHistoricalGenealogyResponseDTO;
import com.devmaster.goatfarm.goatownership.api.dto.FarmGoatHistoricalIntegrationDTO;
import org.springframework.stereotype.Component;

@Component
public class FarmGoatHistoricalGenealogyApiMapper {

    public FarmGoatHistoricalGenealogyResponseDTO toResponse(GenealogyTreeSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }

        return new FarmGoatHistoricalGenealogyResponseDTO(
                toNodeDto(snapshot.animalPrincipal()),
                toNodeDto(snapshot.pai()),
                toNodeDto(snapshot.mae()),
                toNodeDto(snapshot.avoPaterno()),
                toNodeDto(snapshot.avoPaterna()),
                toNodeDto(snapshot.avoMaterno()),
                toNodeDto(snapshot.avoMaterna()),
                toNodeDto(snapshot.bisavoPaternoPai()),
                toNodeDto(snapshot.bisavoPaternaPai()),
                toNodeDto(snapshot.bisavoPaternoMae()),
                toNodeDto(snapshot.bisavoPaternaMae()),
                toNodeDto(snapshot.bisavoMaternoPai()),
                toNodeDto(snapshot.bisavoMaternaPai()),
                toNodeDto(snapshot.bisavoMaternoMae()),
                toNodeDto(snapshot.bisavoMaternaMae()),
                toIntegrationDto(snapshot.integration())
        );
    }

    private FarmGoatHistoricalGenealogyNodeDTO toNodeDto(GenealogyTreeNode node) {
        if (node == null) {
            return null;
        }

        return new FarmGoatHistoricalGenealogyNodeDTO(
                node.relationship(),
                node.name(),
                node.registrationNumber(),
                node.source(),
                node.localGoatId() == null ? null : node.localGoatId().value()
        );
    }

    private FarmGoatHistoricalIntegrationDTO toIntegrationDto(GenealogyIntegrationSnapshot integration) {
        if (integration == null) {
            return null;
        }

        return new FarmGoatHistoricalIntegrationDTO(
                integration.status(),
                integration.lookupKey(),
                integration.message()
        );
    }
}
