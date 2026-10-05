package com.devmaster.goatfarm.farm.application.ports.out;

import com.devmaster.goatfarm.farm.application.model.FarmPersistenceCommand;
import com.devmaster.goatfarm.farm.application.model.FarmRecord;
import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;

import java.util.Optional;

/**
 * Porta de saída para persistência de GoatFarm.
 */
public interface GoatFarmPersistencePort {
    Optional<FarmRecord> findById(Long id);
    Optional<FarmRecord> findByIdAndUserId(Long id, Long userId);

    Optional<FarmRecord> findByAddressId(Long addressId);

    Optional<FarmRecord> findByIdWithDetails(Long id);
    
    PageResult<FarmRecord> searchByName(String name, PageQuery pageQuery);
    PageResult<FarmRecord> findAll(PageQuery pageQuery);

    boolean existsByName(String name);
    boolean existsByTod(String tod);

    FarmRecord save(FarmPersistenceCommand command);
    void deleteById(Long id);
}
