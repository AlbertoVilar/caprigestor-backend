package com.devmaster.goatfarm.inventory.api.dto;

import com.devmaster.goatfarm.inventory.domain.enums.InventoryMovementType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Schema(description = "Resposta de movimentacao registrada no ledger.")
public record InventoryMovementResponseDTO(

        @Schema(description = "Identificador do movimento.", example = "9001")
        Long movementId,

        @Schema(description = "Tipo da movimentacao.", example = "IN")
        InventoryMovementType type,

        @Schema(description = "Quantidade movimentada.", example = "5.000")
        BigDecimal quantity,

        @Schema(description = "Identificador do item.", example = "101")
        Long itemId,

        @Schema(description = "Identificador do lote, quando aplicavel.", example = "501")
        Long lotId,

        @Schema(description = "Data da movimentacao.", example = "2026-02-18")
        LocalDate movementDate,

        @Schema(description = "Saldo resultante apos o movimento.", example = "18.750")
        BigDecimal resultingBalance,

        @Schema(description = "Data/hora de criacao do movimento.", example = "2026-02-18T12:00:00Z")
        OffsetDateTime createdAt
) {
}
