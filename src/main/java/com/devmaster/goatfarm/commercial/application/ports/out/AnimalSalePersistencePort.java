package com.devmaster.goatfarm.commercial.application.ports.out;

import com.devmaster.goatfarm.commercial.application.model.AnimalSaleCommand;
import com.devmaster.goatfarm.commercial.application.model.AnimalSaleRecord;

import java.util.List;
import java.util.Optional;

public interface AnimalSalePersistencePort {
    AnimalSaleRecord save(AnimalSaleCommand sale);
    boolean existsByFarmIdAndGoatTechnicalId(Long farmId, Long goatTechnicalId);
    boolean existsByLegacyRegistrationNumber(String registrationNumber);
    Optional<AnimalSaleRecord> findAnimalSaleByIdAndFarmId(Long saleId, Long farmId);
    List<AnimalSaleRecord> findAnimalSalesByFarmId(Long farmId);
}
