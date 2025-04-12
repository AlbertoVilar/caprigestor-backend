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
                dto.getFatherName(),
                dto.getFatherRegistrationNumber(),
                dto.getMotherName(),
                dto.getMotherRegistrationNumber(),
                dto.getFarmId(),
                dto.getFarmName()
        );
    }

    public static GoatRequestDTO toRequestDTO(GoatRequestVO goatRequesVO) {
       
        return new GoatRequestDTO(
                goatRequesVO.getRegistrationNumber(),
                goatRequesVO.getName(),
                goatRequesVO.getGender(),
                goatRequesVO.getBreed(),
                goatRequesVO.getColor(),
                goatRequesVO.getBirthDate(),
                goatRequesVO.getStatus(),
                goatRequesVO.getTod(),
                goatRequesVO.getToe(),
                goatRequesVO.getCategory(),
                goatRequesVO.getFatherName(),
                goatRequesVO.getFatherRegistrationNumber(),
                goatRequesVO.getMotherName(),
                goatRequesVO.getMotherRegistrationNumber(),
                goatRequesVO.getFarmId(),
                goatRequesVO.getFarmName()
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
                vo.getFarmId(),
                vo.getFarmName()
        );
    }
}
