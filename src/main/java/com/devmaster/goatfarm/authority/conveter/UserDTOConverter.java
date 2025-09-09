package com.devmaster.goatfarm.authority.conveter;

import com.devmaster.goatfarm.authority.api.dto.UserRequestDTO;
import com.devmaster.goatfarm.authority.api.dto.UserResponseDTO;
import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class UserDTOConverter {

    public static UserResponseDTO toDTO(UserResponseVO responseVO) {
        return new UserResponseDTO(
                responseVO.getId(),
                responseVO.getName(),
                responseVO.getEmail(),
                new ArrayList<>(responseVO.getRoles())
        );
    }

    public static UserRequestVO toVO(UserRequestDTO dto) {
        return new UserRequestVO(
                dto.getName(),
                dto.getEmail(),
                dto.getCpf(),
                dto.getPassword(),
                dto.getConfirmPassword(),
                dto.getRoles() != null ? new ArrayList<>(dto.getRoles()) : new ArrayList<>()
        );
    }
}
