package com.devmaster.goatfarm.authority.facade;

import com.devmaster.goatfarm.authority.api.dto.*;
import com.devmaster.goatfarm.authority.business.AuthBusiness;
import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmFullRequestDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmFullResponseDTO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO;
import com.devmaster.goatfarm.farm.facade.GoatFarmFacade;
import com.devmaster.goatfarm.farm.mapper.GoatFarmMapper;
import com.devmaster.goatfarm.address.mapper.AddressMapper;
import com.devmaster.goatfarm.phone.mapper.PhoneMapper;
import com.devmaster.goatfarm.authority.mapper.UserMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthFacade {

    private final AuthBusiness authBusiness;
    private final UserFacade userFacade;
    private final GoatFarmFacade farmFacade;
    private final UserMapper userMapper;
    private final GoatFarmMapper farmMapper;
    private final AddressMapper addressMapper;
    private final PhoneMapper phoneMapper;

    public AuthFacade(AuthBusiness authBusiness, UserFacade userFacade, GoatFarmFacade farmFacade,
                      UserMapper userMapper, GoatFarmMapper farmMapper, AddressMapper addressMapper, PhoneMapper phoneMapper) {
        this.authBusiness = authBusiness;
        this.userFacade = userFacade;
        this.farmFacade = farmFacade;
        this.userMapper = userMapper;
        this.farmMapper = farmMapper;
        this.addressMapper = addressMapper;
        this.phoneMapper = phoneMapper;
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        return authBusiness.authenticateUser(loginRequest);
    }

    public UserResponseDTO register(RegisterRequestDTO registerRequest) {
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException("As senhas não coincidem");
        }

        UserRequestDTO userDTO = new UserRequestDTO();
        userDTO.setName(registerRequest.getName());
        userDTO.setEmail(registerRequest.getEmail());
        userDTO.setCpf(registerRequest.getCpf());
        userDTO.setPassword(registerRequest.getPassword());
        userDTO.setConfirmPassword(registerRequest.getConfirmPassword());
        userDTO.setRoles(List.of("ROLE_OPERATOR"));

        return userFacade.saveUser(userDTO);
    }

    public LoginResponseDTO refreshToken(RefreshTokenRequestDTO refreshRequest) {
        return authBusiness.refreshToken(refreshRequest);
    }

    public UserResponseDTO getMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new com.devmaster.goatfarm.config.exceptions.custom.UnauthorizedException("Usuário não autenticado.");
        }

        String email = authentication.getName();
        UserResponseDTO userDTO = userFacade.findByEmail(email);
        if (userDTO == null) {
            throw new com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException("Usuário não encontrado: " + email);
        }
        return userDTO;
    }

    public GoatFarmFullResponseDTO registerFarm(GoatFarmFullRequestDTO farmRequest) {
        GoatFarmFullResponseDTO farmResponse = farmFacade.createGoatFarm(farmRequest);
        return farmResponse;
    }
}
