package com.devmaster.goatfarm.inventory.persistence.adapter;

import com.devmaster.goatfarm.inventory.application.ports.out.InventoryBalanceQueryPort;
import com.devmaster.goatfarm.inventory.business.bo.InventoryBalanceFilterVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryBalanceResponseVO;
import com.devmaster.goatfarm.inventory.persistence.repository.InventoryBalanceRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class InventoryBalanceQueryPersistenceAdapter implements InventoryBalanceQueryPort {

    private final InventoryBalanceRepository balanceRepository;

    public InventoryBalanceQueryPersistenceAdapter(InventoryBalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    @Override
    public Page<InventoryBalanceResponseVO> listBalances(InventoryBalanceFilterVO filter) {
        return balanceRepository.searchBalances(
                filter.farmId(),
                filter.itemId(),
                filter.lotId(),
                filter.activeOnly(),
                filter.pageable()
        ).map(row -> new InventoryBalanceResponseVO(
                row.itemId(),
                row.itemName(),
                row.trackLot(),
                row.lotId(),
                row.quantity()
        ));
    }
}
