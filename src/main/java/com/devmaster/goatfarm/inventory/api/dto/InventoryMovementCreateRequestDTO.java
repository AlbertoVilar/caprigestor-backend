package com.devmaster.goatfarm.inventory.api.dto;

import com.devmaster.goatfarm.inventory.domain.enums.InventoryAdjustDirection;
import com.devmaster.goatfarm.inventory.domain.enums.InventoryMovementType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Payload para registrar movimentação no ledger de estoque.")
public record InventoryMovementCreateRequestDTO(

        @NotNull(message = "type é obrigatório.")
        @Schema(description = "Tipo de movimentação.", example = "OUT")
        InventoryMovementType type,

        @NotNull(message = "quantity é obrigatório.")
        @Positive(message = "quantity deve ser maior que zero.")
        @Schema(description = "Quantidade movimentada, sempre maior que zero.", example = "2.500")
        BigDecimal quantity,

        @NotNull(message = "itemId é obrigatório.")
        @Positive(message = "itemId deve ser positivo.")
        @Schema(description = "Identificador do item de estoque.", example = "101")
        Long itemId,

        @Positive(message = "lotId deve ser positivo quando informado.")
        @Schema(description = "Identificador do lote (obrigatório quando trackLot=true).", example = "501")
        Long lotId,

        @Schema(
                description = "Direção do ajuste: obrigatória quando type=ADJUST e deve ser nula em IN/OUT.",
                example = "DECREASE"
        )
        InventoryAdjustDirection adjustDirection,

        @Schema(description = "Data do movimento. Se ausente, usa a data atual.", example = "2026-02-18")
        LocalDate movementDate,

        @Size(max = 500, message = "reason deve ter no máximo 500 caracteres.")
        @Schema(description = "Motivo ou observação do movimento.", example = "Baixa por aplicação sanitária")
        String reason,

        @Positive(message = "unitCost deve ser maior que zero quando informado.")
        @Digits(integer = 10, fraction = 4, message = "unitCost deve ter no maximo 10 inteiros e 4 decimais.")
        @Schema(description = "Custo unitario da compra. Usado apenas em entradas por compra.", example = "18.5000")
        BigDecimal unitCost,

        @Positive(message = "totalCost deve ser maior que zero quando informado.")
        @Digits(integer = 12, fraction = 2, message = "totalCost deve ter no maximo 12 inteiros e 2 decimais.")
        @Schema(description = "Custo total da compra. Usado apenas em entradas por compra.", example = "185.00")
        BigDecimal totalCost,

        @PositiveOrZero(message = "freightCost nao pode ser negativo.")
        @Digits(integer = 12, fraction = 2, message = "freightCost deve ter no maximo 12 inteiros e 2 decimais.")
        @Schema(description = "Frete vinculado a esta compra de estoque.", example = "25.00")
        BigDecimal freightCost,

        @PositiveOrZero(message = "discountAmount nao pode ser negativo.")
        @Digits(integer = 12, fraction = 2, message = "discountAmount deve ter no maximo 12 inteiros e 2 decimais.")
        @Schema(description = "Desconto concedido nesta compra de estoque.", example = "10.00")
        BigDecimal discountAmount,

        @Schema(description = "Data da compra, quando a entrada representar uma aquisicao.", example = "2026-03-28")
        LocalDate purchaseDate,

        @Size(max = 120, message = "supplierName deve ter no maximo 120 caracteres.")
        @Schema(description = "Fornecedor da compra, quando houver.", example = "Casa do Campo")
        String supplierName
) {

    public InventoryMovementCreateRequestDTO(
            InventoryMovementType type,
            BigDecimal quantity,
            Long itemId,
            Long lotId,
            InventoryAdjustDirection adjustDirection,
            LocalDate movementDate,
            String reason
    ) {
        this(type, quantity, itemId, lotId, adjustDirection, movementDate, reason,
                null, null, null, null, null, null);
    }

    public InventoryMovementCreateRequestDTO(
            InventoryMovementType type,
            BigDecimal quantity,
            Long itemId,
            Long lotId,
            InventoryAdjustDirection adjustDirection,
            LocalDate movementDate,
            String reason,
            BigDecimal unitCost,
            BigDecimal totalCost,
            LocalDate purchaseDate,
            String supplierName
    ) {
        this(type, quantity, itemId, lotId, adjustDirection, movementDate, reason,
                unitCost, totalCost, null, null, purchaseDate, supplierName);
    }
}
