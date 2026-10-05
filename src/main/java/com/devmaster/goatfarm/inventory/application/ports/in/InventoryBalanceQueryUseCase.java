package com.devmaster.goatfarm.inventory.application.ports.in;

import com.devmaster.goatfarm.inventory.business.bo.InventoryBalanceFilterVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryBalanceResponseVO;
import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;

public interface InventoryBalanceQueryUseCase {

    PageResult<InventoryBalanceResponseVO> listBalances(InventoryBalanceFilterVO filter, PageQuery page);
}
