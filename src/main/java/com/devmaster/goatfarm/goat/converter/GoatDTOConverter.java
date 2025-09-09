package com.devmaster.goatfarm.goat.converter;

import com.devmaster.goatfarm.goat.api.dto.GoatRequestDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatResponseDTO;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;

public class GoatDTOConverter {

    public static GoatRequestVO toRequestVO(GoatRequestDTO dto) {
        return new GoatRequestVO(
                dto.getRegistrationNumber(),
                dto.getName(),
                dto.getGender(),
                dto.getBreed(),
                dto.getColor(),
                dto.getBirthDate(),
                dto.getStatus(),
                dto.getTod(),
                dto.getToe(),
                dto.getCategory(),
                dto.getFatherRegistrationNumber(),
                dto.getMotherRegistrationNumber(),
                dto.getFarmId(),
                dto.getUserId() // <-- atualizado para userId
        );
    }

    public static GoatRequestDTO toRequestDTO(GoatRequestVO vo) {
        return new GoatRequestDTO(
                vo.getRegistrationNumber(),
                vo.getName(),
                vo.getGender(),
                vo.getBreed(),
                vo.getColor(),
                vo.getBirthDate(),
                vo.getStatus(),
                vo.getTod(),
                vo.getToe(),
                vo.getCategory(),
                vo.getFatherRegistrationNumber(),
                vo.getMotherRegistrationNumber(),
                vo.getFarmId(),
                vo.getUserId() // <-- adicionado aqui
        );
    }

    public static GoatResponseDTO toResponseDTO(GoatResponseVO vo) {
        return new GoatResponseDTO(
                vo.getRegistrationNumber(),
                vo.getName(),
                vo.getGender(),
                vo.getBreed(),
                vo.getColor(),
                vo.getBirthDate(),
                vo.getStatus(),
                vo.getTod(),
                vo.getToe(),
                vo.getCategory(),
                vo.getFatherName(),
                vo.getFatherRegistrationNumber(),
                vo.getMotherName(),
                vo.getMotherRegistrationNumber(),
                vo.getUserName(),
                vo.getFarmId(),
                vo.getFarmName()
        );
    }
}
