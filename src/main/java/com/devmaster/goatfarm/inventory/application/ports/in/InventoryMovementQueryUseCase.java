package com.devmaster.goatfarm.inventory.application.ports.in;

import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementFilterVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementHistoryResponseVO;
import org.springframework.data.domain.Page;

public interface InventoryMovementQueryUseCase {

    Page<InventoryMovementHistoryResponseVO> listMovements(InventoryMovementFilterVO filter);
}
