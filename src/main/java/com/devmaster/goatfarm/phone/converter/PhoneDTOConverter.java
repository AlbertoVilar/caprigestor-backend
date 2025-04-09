package com.devmaster.goatfarm.phone.converter;

import com.devmaster.goatfarm.phone.api.dto.PhoneRequestDTO;
import com.devmaster.goatfarm.phone.api.dto.PhoneResponseDTO;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;

public class PhoneDTOConverter {

    public static PhoneResponseDTO toDTO(PhoneResponseVO responseVO) {
        return new PhoneResponseDTO(
                responseVO.getId(),
                responseVO.getDdd(),
                responseVO.getNumber()
        );
    }

    public static PhoneRequestVO toVO(PhoneRequestDTO requestDTO) {
        return new PhoneRequestVO(
                null,
                requestDTO.getDdd(),
                requestDTO.getNumber()
        );
    }
}
