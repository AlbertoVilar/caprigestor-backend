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
    /** Raw Optional retained temporarily for source compatibility with legacy consumers; values are FarmRecord at runtime. */
    Optional findById(Long id);
    Optional findByIdAndUserId(Long id, Long userId);

    Optional<FarmRecord> findByAddressId(Long addressId);

    Optional findByIdWithDetails(Long id);
    
    Page<FarmRecord> searchByName(String name, Pageable pageable);
    Page<FarmRecord> findAll(Pageable pageable);

    boolean existsByName(String name);
    boolean existsByTod(String tod);

    FarmRecord save(FarmPersistenceCommand command);
    void deleteById(Long id);
}
