package com.devmaster.goatfarm.inventory.application.ports.out;

import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementFilterVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementHistoryResponseVO;
import org.springframework.data.domain.Page;

public interface InventoryMovementQueryPort {

    Page<InventoryMovementHistoryResponseVO> listMovements(InventoryMovementFilterVO filter);
}
