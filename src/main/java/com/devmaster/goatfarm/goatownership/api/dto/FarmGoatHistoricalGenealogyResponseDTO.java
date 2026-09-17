package com.devmaster.goatfarm.goatownership.api.dto;

public record FarmGoatHistoricalGenealogyResponseDTO(
        FarmGoatHistoricalGenealogyNodeDTO animalPrincipal,
        FarmGoatHistoricalGenealogyNodeDTO pai,
        FarmGoatHistoricalGenealogyNodeDTO mae,
        FarmGoatHistoricalGenealogyNodeDTO avoPaterno,
        FarmGoatHistoricalGenealogyNodeDTO avoPaterna,
        FarmGoatHistoricalGenealogyNodeDTO avoMaterno,
        FarmGoatHistoricalGenealogyNodeDTO avoMaterna,
        FarmGoatHistoricalGenealogyNodeDTO bisavoPaternoPai,
        FarmGoatHistoricalGenealogyNodeDTO bisavoPaternaPai,
        FarmGoatHistoricalGenealogyNodeDTO bisavoPaternoMae,
        FarmGoatHistoricalGenealogyNodeDTO bisavoPaternaMae,
        FarmGoatHistoricalGenealogyNodeDTO bisavoMaternoPai,
        FarmGoatHistoricalGenealogyNodeDTO bisavoMaternaPai,
        FarmGoatHistoricalGenealogyNodeDTO bisavoMaternoMae,
        FarmGoatHistoricalGenealogyNodeDTO bisavoMaternaMae,
        FarmGoatHistoricalIntegrationDTO integration
) {}
