package com.devmaster.goatfarm.inventory.application.ports.in;

import com.devmaster.goatfarm.inventory.business.bo.InventoryBalanceFilterVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryBalanceResponseVO;
import org.springframework.data.domain.Page;

public interface InventoryBalanceQueryUseCase {

    Page<InventoryBalanceResponseVO> listBalances(InventoryBalanceFilterVO filter);
}
