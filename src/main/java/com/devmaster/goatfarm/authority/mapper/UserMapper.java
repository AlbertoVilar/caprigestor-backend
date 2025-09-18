package com.devmaster.goatfarm.authority.mapper;

import com.devmaster.goatfarm.authority.api.dto.UserRequestDTO;
import com.devmaster.goatfarm.authority.api.dto.UserResponseDTO;
import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
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
    // Conversão direta, pois ambos já são List<String>
    UserResponseDTO toResponseDTO(UserResponseVO vo);

    UserRequestVO toRequestVO(UserRequestDTO dto);

    // Aqui sim, converter Set<Role> para List<String>
    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToStringList")
    UserResponseVO toResponseVO(User user);

    @Named("rolesToStringList")
    default List<String> rolesToStringList(Set<Role> roles) {
        return roles == null ? null : roles.stream().map(Role::getAuthority).collect(Collectors.toList());
    }
}
