package com.devmaster.goatfarm.inventory.application.ports.in;

import com.devmaster.goatfarm.inventory.business.bo.InventoryItemCreateRequestVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryItemResponseVO;

public interface InventoryItemCommandUseCase {

    InventoryItemResponseVO createItem(Long farmId, InventoryItemCreateRequestVO request);
}
