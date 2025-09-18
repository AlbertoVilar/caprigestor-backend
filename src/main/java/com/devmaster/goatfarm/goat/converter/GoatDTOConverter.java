package com.devmaster.goatfarm.goat.converter;

import com.devmaster.goatfarm.goat.api.dto.GoatResponseDTO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.mapper.GoatMapper;

public class GoatDTOConverter {
    private static final GoatMapper goatMapper = GoatMapper.INSTANCE;

    public static GoatResponseDTO toResponseDTO(GoatResponseVO vo) {
        if (vo == null) return null;
        return GoatResponseDTO.builder()
            .registrationNumber(vo.getRegistrationNumber())
            .name(vo.getName())
            .gender(vo.getGender())
            .breed(vo.getBreed())
            .color(vo.getColor())
            .birthDate(vo.getBirthDate())
            .status(vo.getStatus())
            .tod(vo.getTod())
            .toe(vo.getToe())
            .category(vo.getCategory())
            .fatherName(vo.getFatherName())
            .fatherRegistrationNumber(vo.getFatherRegistrationNumber())
            .motherName(vo.getMotherName())
            .motherRegistrationNumber(vo.getMotherRegistrationNumber())
            .userName(vo.getUserName())
            .farmId(vo.getFarmId())
            .farmName(vo.getFarmName())
            .build();
    }
}
