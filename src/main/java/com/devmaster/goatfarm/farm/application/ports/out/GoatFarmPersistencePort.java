package com.devmaster.goatfarm.farm.application.ports.out;

import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Porta de saída para persistência de GoatFarm.
 */
public interface GoatFarmPersistencePort {
    Optional<GoatFarm> findById(Long id);
    Optional<GoatFarm> findByIdAndUserId(Long id, Long userId);

    Optional<GoatFarm> findByAddressId(Long addressId);

    Optional<GoatFarm> findByIdWithDetails(Long id);
    
    Page<GoatFarm> searchByName(String name, Pageable pageable);
    Page<GoatFarm> findAll(Pageable pageable);

    boolean existsByName(String name);
    boolean existsByTod(String tod);

    GoatFarm save(GoatFarm goatFarm);
    void deleteById(Long id);
    void deleteGoatFarmsFromOtherUsers(Long adminId);
}