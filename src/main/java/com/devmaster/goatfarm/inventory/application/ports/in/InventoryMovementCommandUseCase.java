package com.devmaster.goatfarm.inventory.application.ports.in;

import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementCreateRequestVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementResponseVO;

public interface InventoryMovementCommandUseCase {
    InventoryMovementResponseVO createMovement(
            Long farmId,
            String idempotencyKey,
            InventoryMovementCreateRequestVO request
    );
}

