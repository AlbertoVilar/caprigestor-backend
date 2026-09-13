package com.devmaster.goatfarm.commercial.application.ports.out;

import com.devmaster.goatfarm.commercial.application.model.MilkSaleCommand;
import com.devmaster.goatfarm.commercial.application.model.MilkSaleRecord;

import java.util.List;
import java.util.Optional;

public interface MilkSalePersistencePort {
    MilkSaleRecord save(MilkSaleCommand sale);
    Optional<MilkSaleRecord> findMilkSaleByIdAndFarmId(Long saleId, Long farmId);
    List<MilkSaleRecord> findMilkSalesByFarmId(Long farmId);
}
