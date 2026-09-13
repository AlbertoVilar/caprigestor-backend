package com.devmaster.goatfarm.inventory.application.ports.in;

import com.devmaster.goatfarm.inventory.business.bo.InventoryLotFilterVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryLotResponseVO;
import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;

public interface InventoryLotQueryUseCase {

    PageResult<InventoryLotResponseVO> listLots(InventoryLotFilterVO filter, PageQuery page);
}
