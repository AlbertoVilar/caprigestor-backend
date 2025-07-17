package com.devmaster.goatfarm.farm.converters;

import com.devmaster.goatfarm.farm.api.dto.GoatFarmFullResponseDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmRequestDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmResponseDTO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.goat.api.dto.GoatRequestDTO;
import com.devmaster.goatfarm.phone.api.dto.PhoneResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

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
                null,
                goatRequestDTO.getTod(),
                null,
                null,
                null // ✅ Adicionado campo phoneIds como null
        );
    }

    public static GoatFarmRequestDTO toRequestDTO(GoatFarmRequestVO resRequestVO) {
        return new GoatFarmRequestDTO(
                resRequestVO.getId(),
                resRequestVO.getName(),
                resRequestVO.getTod(),
                resRequestVO.getAddressId(),
                resRequestVO.getOwnerId(),
                resRequestVO.getPhoneIds() // ✅ Adicionado
        );
    }

    public static GoatFarmRequestVO toVO(GoatFarmRequestDTO requestDTO) {
        return new GoatFarmRequestVO(
                requestDTO.getId(),
                requestDTO.getName(),
                requestDTO.getTod(),
                requestDTO.getAddressId(),
                requestDTO.getOwnerId(),
                requestDTO.getPhoneIds() // ✅ Adicionado
        );
    }

    public static GoatFarmFullResponseDTO toFullDTO(GoatFarmFullResponseVO vo) {
        List<PhoneResponseDTO> phones = vo.getPhones().stream()
                .map(p -> new PhoneResponseDTO(p.getId(), p.getDdd(), p.getNumber()))
                .collect(Collectors.toList());

        return new GoatFarmFullResponseDTO(
                vo.getId(),
                vo.getName(),
                vo.getTod(),
                vo.getCreatedAt(),
                vo.getUpdatedAt(),

                vo.getOwnerId(),
                vo.getOwnerName(),

                vo.getAddressId(),
                vo.getStreet(),
                vo.getDistrict(),
                vo.getCity(),
                vo.getState(),
                vo.getPostalCode(),

                phones
        );
    }
}
