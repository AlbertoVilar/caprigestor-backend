package com.devmaster.goatfarm.farm.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Minimal authenticated-workspace farm representation. */
@Schema(description = "Fazenda que o usuário autenticado pode administrar")
public record ManagedFarmSummaryDTO(
        Long id,
        String name,
        String tod,
        String logoUrl
) { }
