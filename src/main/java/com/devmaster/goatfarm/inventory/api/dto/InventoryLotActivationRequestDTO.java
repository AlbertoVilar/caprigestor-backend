package com.devmaster.goatfarm.inventory.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Payload para ativar ou inativar um lote de estoque.")
public record InventoryLotActivationRequestDTO(

        @NotNull(message = "active é obrigatório.")
        @Schema(description = "Novo estado do lote.", example = "false")
        Boolean active
) {
}
