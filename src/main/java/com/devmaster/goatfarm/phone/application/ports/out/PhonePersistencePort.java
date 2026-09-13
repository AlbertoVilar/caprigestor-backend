package com.devmaster.goatfarm.phone.application.ports.out;

import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;

import java.util.List;
import java.util.Optional;

/**
 * Porta de saída para persistência de telefones
 */
public interface PhonePersistencePort {

    PhoneResponseVO save(Long farmId, PhoneRequestVO phone);

    Optional<PhoneResponseVO> findByDddAndNumber(String ddd, String number);

    Optional<PhoneResponseVO> findByIdAndFarmId(Long id, Long farmId);

    List<PhoneResponseVO> findAllByFarmId(Long farmId);

    long countByFarmId(Long farmId);

    void deleteById(Long id);

    boolean existsByDddAndNumber(String ddd, String number);

    List<PhoneResponseVO> findAllByIds(List<Long> ids);

}
