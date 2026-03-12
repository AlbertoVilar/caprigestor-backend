package com.devmaster.goatfarm.inventory.application.ports.in;

import com.devmaster.goatfarm.inventory.business.bo.InventoryLotActivationRequestVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryLotCreateRequestVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryLotResponseVO;

public interface InventoryLotCommandUseCase {

    InventoryLotResponseVO createLot(Long farmId, InventoryLotCreateRequestVO request);

    InventoryLotResponseVO updateLotActive(Long farmId, Long lotId, InventoryLotActivationRequestVO request);
}
