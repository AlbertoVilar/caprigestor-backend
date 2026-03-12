package com.devmaster.goatfarm.inventory.business.bo;

import org.springframework.data.domain.Pageable;

public record InventoryLotFilterVO(
        Long farmId,
        Long itemId,
        Boolean active,
        Pageable pageable
) {
}
