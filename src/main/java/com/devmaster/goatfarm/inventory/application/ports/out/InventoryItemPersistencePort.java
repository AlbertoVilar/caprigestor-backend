package com.devmaster.goatfarm.inventory.application.ports.out;

import com.devmaster.goatfarm.inventory.business.bo.InventoryItemCreateVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryItemResponseVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryItemPersistencePort {

    InventoryItemResponseVO save(InventoryItemCreateVO item);

    Page<InventoryItemResponseVO> listByFarmId(Long farmId, Pageable pageable);
}
