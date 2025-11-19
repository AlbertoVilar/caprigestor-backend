package com.devmaster.goatfarm.application.ports.out;

import com.devmaster.goatfarm.genealogy.model.entity.Genealogy;

import java.util.Optional;

/**
 * Porta de saída para persistência de Genealogy.
 */
public interface GenealogyPersistencePort {
    Optional<Genealogy> findByGoatRegistration(String goatRegistration);
    Optional<Genealogy> findByGoatRegistrationAndGoatFarmId(String goatRegistrationNumber, Long farmId);
    boolean existsByGoatRegistration(String goatRegistration);
    Genealogy save(Genealogy genealogy);
}