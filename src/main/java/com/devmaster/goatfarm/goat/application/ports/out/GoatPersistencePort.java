package com.devmaster.goatfarm.goat.application.ports.out;

import com.devmaster.goatfarm.goat.domain.Goat;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.enums.GoatBreed;

import java.util.List;
import java.util.Optional;

/**
 * Persistence boundary owned by the Goat application core.
 *
 * <p>No Spring Data, JPA entity or infrastructure projection crosses this
 * interface.</p>
 */
public interface GoatPersistencePort {

    Goat save(Goat goat);

    Optional<Goat> findById(GoatId id);

    Optional<Goat> findByIdAndFarmId(GoatId id, Long farmId);

    Optional<Goat> findDomainByRegistrationNumber(String registrationNumber);

    Optional<Goat> findByRegistrationNumberAndFarmId(String registrationNumber, Long farmId);

    GoatPage<Goat> findAllByFarmId(Long farmId, GoatPageQuery query);

    GoatPage<Goat> findAllByFarmIdAndBreed(Long farmId, GoatBreed breed, GoatPageQuery query);

    GoatPage<Goat> findByNameAndFarmId(Long farmId, String name, GoatPageQuery query);

    GoatPage<Goat> findByNameAndFarmIdAndBreed(Long farmId, String name, GoatBreed breed, GoatPageQuery query);

    List<Goat> findOffspringByParentId(Long farmId, GoatId parentId);

    GoatHerdSnapshot getHerdSummary(Long farmId);

    void deleteById(GoatId id);

    boolean existsByRegistrationNumber(String registrationNumber);
}
