package com.devmaster.goatfarm.inventory.application.ports.in;

import com.devmaster.goatfarm.inventory.business.bo.InventoryItemResponseVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryItemQueryUseCase {

    Page<InventoryItemResponseVO> listItems(Long farmId, Pageable pageable);
}
