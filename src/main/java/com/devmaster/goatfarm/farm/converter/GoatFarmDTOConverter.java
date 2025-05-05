package com.devmaster.goatfarm.farm.converter;

import com.devmaster.goatfarm.farm.api.dto.GoatFarmRequestDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmResponseDTO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.goat.api.dto.GoatRequestDTO;
import org.springframework.stereotype.Component;

@Component
public class GoatFarmDTOConverter {

    public static GoatFarmResponseDTO toDTO(GoatFarmResponseVO responseVO) {
        return new GoatFarmResponseDTO(
                responseVO.getId(),
                responseVO.getName(),
                responseVO.getTod(),
                responseVO.getCreatedAt(),
                responseVO.getUpdatedAt()
        );
    }

    public static GoatFarmRequestDTO fromGoatRequestDTO(GoatRequestDTO goatRequestDTO) {
        return new GoatFarmRequestDTO(
                goatRequestDTO.getFarmId(),
                null, //name n tem no goatRequestDTO
                goatRequestDTO.getTod(),
                null, //addressId n tem
                null //ownerId n tem
        );
    }

    public static GoatFarmRequestDTO toRequestDTO(GoatFarmRequestVO resRequesVO) {
        return new GoatFarmRequestDTO(
                resRequesVO.getId(),
                resRequesVO.getName(),
                resRequesVO.getTod(),
                resRequesVO.getAddressId(), // Adiciona addressId
                resRequesVO.getOwnerId()    // Adiciona ownerId
        );
    }

    public static GoatFarmRequestVO toVO(GoatFarmRequestDTO requestDTO) {
        return new GoatFarmRequestVO(
                requestDTO.getId(),
                requestDTO.getName(),
                requestDTO.getTod(),
                requestDTO.getAddressId(), // Adiciona addressId
                requestDTO.getOwnerId()    // Adiciona ownerId
        );
    }
}
