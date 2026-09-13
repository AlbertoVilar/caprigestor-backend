package com.devmaster.goatfarm.inventory.persistence.adapter;

import com.devmaster.goatfarm.inventory.application.ports.out.InventoryBalanceQueryPort;
import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;
import com.devmaster.goatfarm.application.pagination.SortDirection;
import com.devmaster.goatfarm.application.pagination.SortSpec;
import com.devmaster.goatfarm.inventory.business.bo.InventoryBalanceFilterVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryBalanceResponseVO;
import com.devmaster.goatfarm.inventory.persistence.repository.InventoryBalanceRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class InventoryBalanceQueryPersistenceAdapter implements InventoryBalanceQueryPort {

    private final InventoryBalanceRepository balanceRepository;

    public InventoryBalanceQueryPersistenceAdapter(InventoryBalanceRepository balanceRepository) {
        this.balanceRepository = balanceRepository;
    }

    @Override
    public PageResult<InventoryBalanceResponseVO> listBalances(InventoryBalanceFilterVO filter, PageQuery page) {
        var springPage = balanceRepository.searchBalances(
                filter.farmId(),
                filter.itemId(),
                filter.lotId(),
                filter.activeOnly(),
                toPageable(page)
        );
        return new PageResult<>(springPage.getContent().stream().map(row -> new InventoryBalanceResponseVO(
                row.itemId(),
                row.itemName(),
                row.trackLot(),
                row.lotId(),
                row.quantity()
        )).toList(), springPage.getTotalElements(), page.page(), page.size());
    }

    private Pageable toPageable(PageQuery page) {
        var orders = page.sort().stream().map(this::toOrder).toList();
        return PageRequest.of(page.page(), page.size(), Sort.by(orders));
    }

    private Sort.Order toOrder(SortSpec spec) {
        return spec.direction() == SortDirection.ASC
                ? Sort.Order.asc(spec.field())
                : Sort.Order.desc(spec.field());
    }
}
