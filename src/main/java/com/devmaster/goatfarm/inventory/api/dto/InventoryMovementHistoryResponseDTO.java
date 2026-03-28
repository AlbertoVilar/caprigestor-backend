package com.devmaster.goatfarm.inventory.api.dto;

import com.devmaster.goatfarm.inventory.domain.enums.InventoryAdjustDirection;
import com.devmaster.goatfarm.inventory.domain.enums.InventoryMovementType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Schema(description = "Registro de historico de movimentacoes do estoque.")
public record InventoryMovementHistoryResponseDTO(

        @Schema(description = "Identificador do movimento.", example = "9001")
        Long movementId,

        @Schema(description = "Tipo do movimento.", example = "OUT")
        InventoryMovementType type,

        @Schema(description = "Direcao do ajuste, quando aplicavel.", example = "DECREASE")
        InventoryAdjustDirection adjustDirection,

        @Schema(description = "Quantidade movimentada.", example = "2.000")
        BigDecimal quantity,

        @Schema(description = "Identificador do item.", example = "101")
        Long itemId,

        @Schema(description = "Nome do item.", example = "Racao Premium 22%")
        String itemName,

        @Schema(description = "Identificador do lote, quando houver.", example = "501")
        Long lotId,

        @Schema(description = "Data da movimentacao.", example = "2026-02-28")
        LocalDate movementDate,

        @Schema(description = "Motivo informado no comando.", example = "Baixa por aplicacao sanitaria")
        String reason,

        @Schema(description = "Saldo resultante apos a movimentacao.", example = "18.750")
        BigDecimal resultingBalance,

        @Schema(description = "Custo unitario da compra, quando a entrada representar aquisicao.", example = "18.5000")
        BigDecimal unitCost,

        @Schema(description = "Custo total da compra, quando houver.", example = "185.00")
        BigDecimal totalCost,

        @Schema(description = "Data da compra, quando houver.", example = "2026-03-28")
        LocalDate purchaseDate,

        @Schema(description = "Fornecedor da compra, quando houver.", example = "Casa do Campo")
        String supplierName,

        @Schema(description = "Data e hora da gravacao do movimento.", example = "2026-02-28T12:15:00Z")
        OffsetDateTime createdAt
) {

    public InventoryMovementHistoryResponseDTO(
            Long movementId,
            InventoryMovementType type,
            InventoryAdjustDirection adjustDirection,
            BigDecimal quantity,
            Long itemId,
            String itemName,
            Long lotId,
            LocalDate movementDate,
            String reason,
            BigDecimal resultingBalance,
            OffsetDateTime createdAt
    ) {
        this(
                movementId,
                type,
                adjustDirection,
                quantity,
                itemId,
                itemName,
                lotId,
                movementDate,
                reason,
                resultingBalance,
                null,
                null,
                null,
                null,
                createdAt
        );
    }
}
