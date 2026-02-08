package com.devmaster.goatfarm.authority.api.controller;

import com.devmaster.goatfarm.authority.api.dto.*;
import com.devmaster.goatfarm.authority.application.ports.in.AuthManagementUseCase;
import com.devmaster.goatfarm.authority.application.ports.in.UserManagementUseCase;
import com.devmaster.goatfarm.authority.api.mapper.AuthMapper;
import com.devmaster.goatfarm.farm.application.ports.in.GoatFarmManagementUseCase;
import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.api.mapper.UserMapper;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmFullRequestDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmFullResponseDTO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.farm.api.mapper.GoatFarmMapper;
import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.api.mapper.AddressMapper;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.api.mapper.PhoneMapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthManagementUseCase authUseCase;
    private final UserManagementUseCase userUseCase;
    private final GoatFarmManagementUseCase farmUseCase;
    private final AuthMapper authMapper;
    private final UserMapper userMapper;
    private final GoatFarmMapper farmMapper;
    private final AddressMapper addressMapper;
    private final PhoneMapper phoneMapper;

    public AuthController(AuthManagementUseCase authUseCase,
                          UserManagementUseCase userUseCase,
                          GoatFarmManagementUseCase farmUseCase,
                          AuthMapper authMapper,
                          UserMapper userMapper,
                          GoatFarmMapper farmMapper,
                          AddressMapper addressMapper,
                          PhoneMapper phoneMapper) {
        this.authUseCase = authUseCase;
        this.userUseCase = userUseCase;
        this.farmUseCase = farmUseCase;
        this.authMapper = authMapper;
        this.userMapper = userMapper;
        this.farmMapper = farmMapper;
        this.addressMapper = addressMapper;
        this.phoneMapper = phoneMapper;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        return ResponseEntity.ok(authMapper.toLoginResponseDTO(authUseCase.login(authMapper.toRequestVO(loginRequest))));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        UserRequestVO vo = UserRequestVO.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .cpf(registerRequest.getCpf())
                .password(registerRequest.getPassword())
                .confirmPassword(registerRequest.getConfirmPassword())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(
                userMapper.toResponseDTO(userUseCase.saveUser(vo))
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO refreshRequest) {
        return ResponseEntity.ok(authMapper.toLoginResponseDTO(authUseCase.refreshToken(authMapper.toRefreshRequestVO(refreshRequest))));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser() {
        return ResponseEntity.ok(userMapper.toResponseDTO(userUseCase.getMe()));
    }

    @PostMapping("/register-farm")
    @Operation(summary = "Register new farm and user", description = "Public endpoint to create a new farm along with its owner user")
    public ResponseEntity<GoatFarmFullResponseDTO> registerFarm(@Valid @RequestBody GoatFarmFullRequestDTO farmRequest) {
        GoatFarmRequestVO farmVO = farmMapper.toRequestVO(farmRequest.getFarm());
        UserRequestVO userVO = userMapper.toRequestVO(farmRequest.getUser());
        AddressRequestVO addressVO = addressMapper.toVO(farmRequest.getAddress());
        java.util.List<PhoneRequestVO> phoneVOs = farmRequest.getPhones() == null ? java.util.Collections.emptyList()
                : farmRequest.getPhones().stream().map(phoneMapper::toRequestVO).toList();

        GoatFarmFullRequestVO fullRequestVO = GoatFarmFullRequestVO.builder()
                .farm(farmVO)
                .user(userVO)
                .address(addressVO)
                .phones(phoneVOs)
                .build();

        GoatFarmFullResponseVO responseVO = farmUseCase.createGoatFarm(fullRequestVO);
        return ResponseEntity.status(HttpStatus.CREATED).body(farmMapper.toFullDTO(responseVO));
    }
}
