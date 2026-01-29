package com.devmaster.goatfarm.phone.application.ports.in;

import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;

import java.util.List;

/**
 * Porta de entrada (Use Case) para gerenciamento de telefones ligados à fazenda.
 */
public interface PhoneManagementUseCase {

    PhoneResponseVO createPhone(Long farmId, PhoneRequestVO requestVO);

    PhoneResponseVO updatePhone(Long farmId, Long phoneId, PhoneRequestVO requestVO);

    PhoneResponseVO findPhoneById(Long farmId, Long phoneId);

    List<PhoneResponseVO> findAllPhonesByFarm(Long farmId);

    void deletePhone(Long farmId, Long phoneId);
}