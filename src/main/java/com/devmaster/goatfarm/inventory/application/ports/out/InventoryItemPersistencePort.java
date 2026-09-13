package com.devmaster.goatfarm.inventory.application.ports.out;

import com.devmaster.goatfarm.inventory.business.bo.InventoryItemCreateVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryItemResponseVO;
import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;

public interface InventoryItemPersistencePort {

    InventoryItemResponseVO save(InventoryItemCreateVO item);

    PageResult<InventoryItemResponseVO> listByFarmId(Long farmId, PageQuery page);
}
