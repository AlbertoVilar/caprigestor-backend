package com.devmaster.goatfarm.inventory.business.inventoryservice;

import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.inventory.application.ports.in.InventoryMovementCommandUseCase;
import com.devmaster.goatfarm.inventory.application.ports.out.InventoryMovementPersistencePort;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementCreateRequestVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementResponseVO;
import com.devmaster.goatfarm.inventory.domain.enums.InventoryMovementType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class InventoryMovementBusiness implements InventoryMovementCommandUseCase {

    private final InventoryMovementPersistencePort persistencePort;

    public InventoryMovementBusiness(InventoryMovementPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    @Override
    public InventoryMovementResponseVO createMovement(
            Long farmId,
            String idempotencyKey,
            InventoryMovementCreateRequestVO request
    ) {
        validateInput(farmId, idempotencyKey, request);

        // TODO: idempotência (replay vs conflict)
        // TODO: lock balance (SELECT FOR UPDATE) e aplicar invariantes (não negativo)
        // TODO: persistir movement + balance + idempotency record
        return null;
    }

    private void validateInput(Long farmId, String idempotencyKey, InventoryMovementCreateRequestVO request) {
        if (farmId == null) {
            throw new InvalidArgumentException("farmId", "farmId é obrigatório.");
        }

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new InvalidArgumentException("idempotencyKey", "Idempotency-Key é obrigatório.");
        }

        if (request == null) {
            throw new InvalidArgumentException("request", "Payload da requisição é obrigatório.");
        }

        if (request.type() == null) {
            throw new InvalidArgumentException("type", "Tipo do movimento é obrigatório.");
        }

        if (request.quantity() == null || request.quantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidArgumentException("quantity", "Quantidade deve ser maior que zero.");
        }

        if (request.itemId() == null) {
            throw new InvalidArgumentException("itemId", "itemId é obrigatório.");
        }

        if (InventoryMovementType.ADJUST.equals(request.type()) && request.adjustDirection() == null) {
            throw new InvalidArgumentException("adjustDirection", "adjustDirection é obrigatório quando o tipo é ADJUST.");
        }
    }
}
