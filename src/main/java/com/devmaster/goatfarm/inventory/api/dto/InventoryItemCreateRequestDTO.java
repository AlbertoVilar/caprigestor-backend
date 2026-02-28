package com.devmaster.goatfarm.inventory.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload para cadastrar um item de estoque da fazenda.")
public record InventoryItemCreateRequestDTO(

        @Schema(description = "Nome do item de estoque.", example = "Ração Premium 22%")
        String name,

        @Schema(description = "Define se o item exige rastreio por lote. Se ausente, assume false.", example = "true")
        Boolean trackLot
) {
}
