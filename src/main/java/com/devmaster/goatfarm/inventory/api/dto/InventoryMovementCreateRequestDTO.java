package com.devmaster.goatfarm.inventory.api.dto;

import com.devmaster.goatfarm.inventory.domain.enums.InventoryAdjustDirection;
import com.devmaster.goatfarm.inventory.domain.enums.InventoryMovementType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Payload para registrar movimentacao no ledger de estoque.")
public record InventoryMovementCreateRequestDTO(

        @NotNull(message = "type e obrigatorio.")
        @Schema(description = "Tipo de movimentacao.", example = "OUT")
        InventoryMovementType type,

        @NotNull(message = "quantity e obrigatorio.")
        @Positive(message = "quantity deve ser maior que zero.")
        @Schema(description = "Quantidade movimentada, sempre maior que zero.", example = "2.500")
        BigDecimal quantity,

        @NotNull(message = "itemId e obrigatorio.")
        @Positive(message = "itemId deve ser positivo.")
        @Schema(description = "Identificador do item de estoque.", example = "101")
        Long itemId,

        @Positive(message = "lotId deve ser positivo quando informado.")
        @Schema(description = "Identificador do lote (obrigatorio quando trackLot=true).", example = "501")
        Long lotId,

        @Schema(description = "Direcao do ajuste quando type=ADJUST.", example = "DECREASE")
        InventoryAdjustDirection adjustDirection,

        @Schema(description = "Data do movimento. Se ausente, usa a data atual.", example = "2026-02-18")
        LocalDate movementDate,

        @Size(max = 500, message = "reason deve ter no maximo 500 caracteres.")
        @Schema(description = "Motivo ou observacao do movimento.", example = "Baixa por aplicacao sanitaria")
        String reason
) {
}
