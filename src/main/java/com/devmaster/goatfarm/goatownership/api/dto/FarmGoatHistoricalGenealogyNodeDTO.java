package com.devmaster.goatfarm.goatownership.api.dto;

import com.devmaster.goatfarm.genealogy.application.model.GenealogyNodeSource;

public record FarmGoatHistoricalGenealogyNodeDTO(
        String relationship,
        String name,
        String registrationNumber,
        GenealogyNodeSource source,
        Long localTechnicalGoatId
) {}
