package com.devmaster.goatfarm.inventory.application.ports.out;

import com.devmaster.goatfarm.inventory.business.bo.InventoryBalanceFilterVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryBalanceResponseVO;
import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;

public interface InventoryBalanceQueryPort {

    PageResult<InventoryBalanceResponseVO> listBalances(InventoryBalanceFilterVO filter, PageQuery page);
}
