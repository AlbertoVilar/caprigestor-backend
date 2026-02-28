package com.devmaster.goatfarm.inventory.application.ports.out;

import com.devmaster.goatfarm.inventory.business.bo.InventoryBalanceFilterVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryBalanceResponseVO;
import org.springframework.data.domain.Page;

public interface InventoryBalanceQueryPort {

    Page<InventoryBalanceResponseVO> listBalances(InventoryBalanceFilterVO filter);
}
