package com.devmaster.goatfarm.inventory.application.ports.in;

import com.devmaster.goatfarm.inventory.business.bo.InventoryLotFilterVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryLotResponseVO;
import org.springframework.data.domain.Page;

public interface InventoryLotQueryUseCase {

    Page<InventoryLotResponseVO> listLots(InventoryLotFilterVO filter);
}
