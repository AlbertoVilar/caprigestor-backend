package com.devmaster.goatfarm.inventory.api.dto;

import com.devmaster.goatfarm.inventory.domain.enums.InventoryAdjustDirection;
import com.devmaster.goatfarm.inventory.domain.enums.InventoryMovementType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Schema(description = "Registro de histórico de movimentações do estoque.")
public record InventoryMovementHistoryResponseDTO(

        @Schema(description = "Identificador do movimento.", example = "9001")
        Long movementId,

        @Schema(description = "Tipo do movimento.", example = "OUT")
        InventoryMovementType type,

        @Schema(description = "Direção do ajuste, quando aplicável.", example = "DECREASE")
        InventoryAdjustDirection adjustDirection,

        @Schema(description = "Quantidade movimentada.", example = "2.000")
        BigDecimal quantity,

        @Schema(description = "Identificador do item.", example = "101")
        Long itemId,

        @Schema(description = "Nome do item.", example = "Ração Premium 22%")
        String itemName,

        @Schema(description = "Identificador do lote, quando houver.", example = "501")
        Long lotId,

        @Schema(description = "Data da movimentação.", example = "2026-02-28")
        LocalDate movementDate,

        @Schema(description = "Motivo informado no comando.", example = "Baixa por aplicação sanitária")
        String reason,

        @Schema(description = "Saldo resultante após a movimentação.", example = "18.750")
        BigDecimal resultingBalance,

        @Schema(description = "Data e hora da gravação do movimento.", example = "2026-02-28T12:15:00Z")
        OffsetDateTime createdAt
) {
}
