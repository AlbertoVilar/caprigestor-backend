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

        @Schema(description = "Custo unitario da compra quando a entrada representar uma aquisicao.", example = "18.5000")
        BigDecimal unitCost,

        @Schema(description = "Custo total da compra quando a entrada representar uma aquisicao.", example = "185.00")
        BigDecimal totalCost,

        @Schema(description = "Data da compra quando houver custo associado.", example = "2026-03-28")
        LocalDate purchaseDate,

        @Schema(description = "Nome do fornecedor da compra.", example = "Casa do Campo")
        String supplierName,

        @Schema(description = "Data/hora de criacao do movimento.", example = "2026-02-18T12:00:00Z")
        OffsetDateTime createdAt
) {

    public InventoryMovementResponseDTO(
            Long movementId,
            InventoryMovementType type,
            BigDecimal quantity,
            Long itemId,
            Long lotId,
            LocalDate movementDate,
            BigDecimal resultingBalance,
            OffsetDateTime createdAt
    ) {
        this(movementId, type, quantity, itemId, lotId, movementDate, resultingBalance, null, null, null, null, createdAt);
    }
}
