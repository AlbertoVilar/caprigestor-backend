package com.devmaster.goatfarm.inventory.business.bo;

import org.springframework.data.domain.Pageable;

public record InventoryBalanceFilterVO(
        Long farmId,
        Long itemId,
        Long lotId,
        boolean activeOnly,
        Pageable pageable
) {
}
