package com.devmaster.goatfarm.phone.application.ports.out;

import com.devmaster.goatfarm.phone.persistence.entity.Phone;

import java.util.List;
import java.util.Optional;

/**
 * Porta de saída para persistência de telefones
 */
public interface PhonePersistencePort {

    Phone save(Phone phone);

    Optional<Phone> findByDddAndNumber(String ddd, String number);

    Optional<Phone> findByIdAndFarmId(Long id, Long farmId);

    List<Phone> findAllByFarmId(Long farmId);

    long countByFarmId(Long farmId);

    void deleteById(Long id);

    boolean existsByDddAndNumber(String ddd, String number);

    List<Phone> findAllByIds(List<Long> ids);

    void deletePhonesFromOtherUsers(Long adminId);
}
