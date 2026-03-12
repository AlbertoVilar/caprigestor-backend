package com.devmaster.goatfarm.inventory.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Payload para cadastrar um lote real de estoque.")
public record InventoryLotCreateRequestDTO(

        @NotNull(message = "itemId é obrigatório.")
        @Positive(message = "itemId deve ser positivo.")
        @Schema(description = "Identificador do item de estoque ao qual o lote pertence.", example = "101")
        Long itemId,

        @Schema(description = "Código exibido do lote.", example = "RACAO-2026-03")
        String code,

        @Size(max = 500, message = "description deve ter no máximo 500 caracteres.")
        @Schema(description = "Descrição opcional do lote.", example = "Fornecedor Alfa, entrega de março.")
        String description,

        @Schema(description = "Validade opcional do lote.", example = "2026-09-30")
        LocalDate expirationDate,

        @Schema(description = "Situação inicial do lote. Se ausente, assume ativo.", example = "true")
        Boolean active
) {
}
