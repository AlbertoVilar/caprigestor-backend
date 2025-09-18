package com.devmaster.goatfarm.authority.conveter;

import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
import com.devmaster.goatfarm.authority.api.dto.UserResponseDTO;

public class UserDTOConverter {
    public static UserRequestVO toVO(com.devmaster.goatfarm.authority.api.dto.UserRequestDTO dto) {
        if (dto == null) return null;
        return UserRequestVO.builder()
            .name(dto.getName())
            .email(dto.getEmail())
            .cpf(dto.getCpf())
            .password(dto.getPassword())
            .confirmPassword(dto.getConfirmPassword())
            .roles(dto.getRoles())
            .build();
    }

    public static UserResponseDTO toDTO(UserResponseVO vo) {
        if (vo == null) return null;
        return UserResponseDTO.builder()
            .id(vo.getId())
            .name(vo.getName())
            .email(vo.getEmail())
            .cpf(vo.getCpf())
            .roles(vo.getRoles())
            .build();
    }
}
