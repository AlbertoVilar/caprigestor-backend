package com.devmaster.goatfarm.commercial.application.ports.out;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface InventoryPurchaseCostQueryPort {

    BigDecimal sumPurchaseCostsByFarmIdAndPeriod(Long farmId, LocalDate fromDate, LocalDate toDate);
}
