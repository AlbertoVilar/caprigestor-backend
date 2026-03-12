package com.devmaster.goatfarm.inventory.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Resposta com os dados do lote de estoque.")
public record InventoryLotResponseDTO(

        @Schema(description = "Identificador do lote.", example = "501")
        Long id,

        @Schema(description = "Identificador da fazenda.", example = "1")
        Long farmId,

        @Schema(description = "Identificador do item ao qual o lote pertence.", example = "101")
        Long itemId,

        @Schema(description = "Código exibido do lote.", example = "RACAO-2026-03")
        String code,

        @Schema(description = "Descrição opcional do lote.", example = "Fornecedor Alfa, entrega de março.")
        String description,

        @Schema(description = "Validade opcional do lote.", example = "2026-09-30")
        LocalDate expirationDate,

        @Schema(description = "Indica se o lote está ativo.", example = "true")
        boolean active
) {
}
