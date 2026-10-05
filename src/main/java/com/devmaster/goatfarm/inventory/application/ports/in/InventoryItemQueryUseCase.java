package com.devmaster.goatfarm.inventory.application.ports.in;

import com.devmaster.goatfarm.inventory.business.bo.InventoryItemResponseVO;
import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;

public interface InventoryItemQueryUseCase {

    PageResult<InventoryItemResponseVO> listItems(Long farmId, PageQuery page);
}
