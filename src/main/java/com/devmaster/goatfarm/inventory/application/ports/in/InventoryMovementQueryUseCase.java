package com.devmaster.goatfarm.inventory.application.ports.in;

import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementFilterVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementHistoryResponseVO;
import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;

public interface InventoryMovementQueryUseCase {

    PageResult<InventoryMovementHistoryResponseVO> listMovements(InventoryMovementFilterVO filter, PageQuery page);
}
