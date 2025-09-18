package com.devmaster.goatfarm.address.converter;

import com.devmaster.goatfarm.address.api.dto.AddressRequestDTO;
import com.devmaster.goatfarm.address.api.dto.AddressResponseDTO;
import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;

public class AddressDTOConverter {

    public static AddressResponseDTO toDTO(AddressResponseVO responseVO) {
        return new AddressResponseDTO(

                responseVO.getId(),
                responseVO.getStreet(),
                responseVO.getNeighborhood(),
                responseVO.getCity(),
                responseVO.getState(),
                responseVO.getZipCode(),
                responseVO.getCountry()
        );
    }

    public static AddressRequestVO toVO(AddressRequestDTO requestDTO) {
        // VERIFICAÇÃO DE NULO -- ADICIONE ESTA VERIFICAÇÃO
        if (requestDTO == null) {
            return null;
        }

        return new AddressRequestVO(
                null,
                requestDTO.getStreet(),
                requestDTO.getNeighborhood(),
                requestDTO.getCity(),
                requestDTO.getState(),
                requestDTO.getZipCode(),
                requestDTO.getCountry()
        );
    }
}
