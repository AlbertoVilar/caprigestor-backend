package com.devmaster.goatfarm.authority.mapper;

import com.devmaster.goatfarm.authority.api.dto.LoginResponseDTO;
import com.devmaster.goatfarm.authority.api.dto.UserResponseDTO;
import com.devmaster.goatfarm.authority.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface AuthMapper {

    @Mapping(target = "user", source = "user")
    @Mapping(target = "tokenType", constant = "Bearer")
    @Mapping(target = "expiresIn", constant = "3600L")
    LoginResponseDTO toLoginResponseDTO(User user, String accessToken, String refreshToken);

    @Mapping(target = "user", ignore = true) // User é nulo no refresh
    @Mapping(target = "tokenType", constant = "Bearer")
    @Mapping(target = "expiresIn", constant = "3600L")
    LoginResponseDTO toLoginResponseDTO(String accessToken, String refreshToken);
}
