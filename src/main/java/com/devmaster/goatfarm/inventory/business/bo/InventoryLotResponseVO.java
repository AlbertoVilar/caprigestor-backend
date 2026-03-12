package com.devmaster.goatfarm.inventory.business.bo;

import java.time.LocalDate;

public record InventoryLotResponseVO(
        Long id,
        Long farmId,
        Long itemId,
        String code,
        String description,
        LocalDate expirationDate,
        boolean active
) {
}
