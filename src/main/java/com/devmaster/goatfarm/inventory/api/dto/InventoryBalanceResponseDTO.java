package com.devmaster.goatfarm.inventory.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Saldo consolidado por item e lote no estoque da fazenda.")
public record InventoryBalanceResponseDTO(

        @Schema(description = "Identificador do item.", example = "101")
        Long itemId,

        @Schema(description = "Nome do item.", example = "Ração Premium 22%")
        String itemName,

        @Schema(description = "Indica se o item exige controle por lote.", example = "true")
        boolean trackLot,

        @Schema(description = "Identificador do lote, quando houver.", example = "501")
        Long lotId,

        @Schema(description = "Quantidade disponível no saldo atual.", example = "18.750")
        BigDecimal quantity
) {
}
