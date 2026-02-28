package com.devmaster.goatfarm.inventory.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta com os dados do item de estoque.")
public record InventoryItemResponseDTO(

        @Schema(description = "Identificador do item de estoque.", example = "101")
        Long id,

        @Schema(description = "Nome exibido do item.", example = "Ração Premium 22%")
        String name,

        @Schema(description = "Indica se o item usa rastreio por lote.", example = "true")
        boolean trackLot,

        @Schema(description = "Indica se o item está ativo.", example = "true")
        boolean active
) {
}
