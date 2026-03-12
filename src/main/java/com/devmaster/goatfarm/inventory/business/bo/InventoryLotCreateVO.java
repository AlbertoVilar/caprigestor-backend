package com.devmaster.goatfarm.inventory.business.bo;

import java.time.LocalDate;

public record InventoryLotCreateVO(
        Long farmId,
        Long itemId,
        String code,
        String description,
        LocalDate expirationDate,
        boolean active
) {
}
