package com.devmaster.goatfarm.commercial.application.ports.out;

import com.devmaster.goatfarm.commercial.application.model.AnimalSaleCommand;
import com.devmaster.goatfarm.commercial.application.model.AnimalSaleRecord;

import java.util.List;
import java.util.Optional;

public interface AnimalSalePersistencePort {
    AnimalSaleRecord save(AnimalSaleCommand sale);
    default void deleteById(Long saleId) {
        throw new UnsupportedOperationException("sale deletion is not supported");
    }
    boolean existsByFarmIdAndGoatTechnicalId(Long farmId, Long goatTechnicalId);
    default boolean existsExternalSaleByGoatTechnicalId(Long goatTechnicalId) {
        return existsByFarmIdAndGoatTechnicalId(null, goatTechnicalId);
    }
    boolean existsByLegacyRegistrationNumber(String registrationNumber);
    default Optional<AnimalSaleRecord> findAnimalSaleById(Long saleId) {
        return Optional.empty();
    }
    Optional<AnimalSaleRecord> findAnimalSaleByIdAndFarmId(Long saleId, Long farmId);
    List<AnimalSaleRecord> findAnimalSalesByFarmId(Long farmId);
    default List<AnimalSaleRecord> findOwnershipSalesByTargetFarmId(Long targetFarmId) {
        return List.of();
    }
}
