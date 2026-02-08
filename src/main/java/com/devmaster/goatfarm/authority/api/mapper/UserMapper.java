package com.devmaster.goatfarm.authority.api.mapper;

import com.devmaster.goatfarm.authority.api.dto.UserRequestDTO;
import com.devmaster.goatfarm.authority.api.dto.UserUpdateRequestDTO;
import com.devmaster.goatfarm.authority.api.dto.UserResponseDTO;
import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDTO toResponseDTO(UserResponseVO vo);


    UserRequestVO toRequestVO(UserRequestDTO dto);

    // Mapeamento para atualização de perfil sem roles
    UserRequestVO toRequestVO(UserUpdateRequestDTO dto);
}
