package com.devmaster.goatfarm.authority.mapper;

import com.devmaster.goatfarm.authority.api.dto.UserRequestDTO;
import com.devmaster.goatfarm.authority.api.dto.UserUpdateRequestDTO;
import com.devmaster.goatfarm.authority.api.dto.UserResponseDTO;
import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
import com.devmaster.goatfarm.authority.facade.dto.UserFacadeResponseDTO;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.authority.model.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {
        UserResponseDTO toResponseDTO(UserResponseVO vo);

        UserResponseDTO toResponseDTO(UserFacadeResponseDTO facadeDTO);

    UserRequestVO toRequestVO(UserRequestDTO dto);

    // Mapeamento para atualização de perfil sem roles
    UserRequestVO toRequestVO(UserUpdateRequestDTO dto);

        @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToStringList")
    UserResponseVO toResponseVO(User user);

        @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "goatFarms", ignore = true)
    User toEntity(UserRequestVO vo);

    @Named("rolesToStringList")
    default List<String> rolesToStringList(Set<Role> roles) {
        return roles == null ? null : roles.stream().map(Role::getAuthority).collect(Collectors.toList());
    }
}
