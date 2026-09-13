package com.devmaster.goatfarm.farm.application.ports.out;

import com.devmaster.goatfarm.farm.application.model.FarmPersistenceCommand;
import com.devmaster.goatfarm.farm.application.model.FarmRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Porta de saída para persistência de GoatFarm.
 */
public interface GoatFarmPersistencePort {
    Optional<FarmRecord> findById(Long id);
    Optional<FarmRecord> findByIdAndUserId(Long id, Long userId);

    Optional<FarmRecord> findByAddressId(Long addressId);

    Optional<FarmRecord> findByIdWithDetails(Long id);
    
    Page<FarmRecord> searchByName(String name, Pageable pageable);
    Page<FarmRecord> findAll(Pageable pageable);

    boolean existsByName(String name);
    boolean existsByTod(String tod);

    FarmRecord save(FarmPersistenceCommand command);
    void deleteById(Long id);
}
