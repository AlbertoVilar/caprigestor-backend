package com.devmaster.goatfarm.farm.api.controller;

import com.devmaster.goatfarm.address.api.dto.AddressResponseDTO;
import com.devmaster.goatfarm.authority.api.dto.UserResponseDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmFullRequestDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmFullResponseDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmUpdateRequestDTO;
import com.devmaster.goatfarm.farm.api.dto.FarmPermissionsDTO;
import com.devmaster.goatfarm.farm.application.ports.in.GoatFarmManagementUseCase;
import com.devmaster.goatfarm.farm.api.mapper.GoatFarmMapper;
import com.devmaster.goatfarm.authority.api.mapper.UserMapper;
import com.devmaster.goatfarm.address.api.mapper.AddressMapper;
import com.devmaster.goatfarm.phone.api.mapper.PhoneMapper;
import com.devmaster.goatfarm.config.security.OwnershipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping({"/api/v1/goatfarms", "/api/goatfarms"})
@Tag(name = "Goat Farm API", description = "Gestão de fazendas caprinas. Caminho canônico /api/v1; legado /api em descontinuação.")
public class GoatFarmController {

    private final GoatFarmManagementUseCase farmUseCase;
    private final GoatFarmMapper farmMapper;
    private final UserMapper userMapper;
    private final AddressMapper addressMapper;
    private final PhoneMapper phoneMapper;
    private final OwnershipService ownershipService;

    public GoatFarmController(GoatFarmManagementUseCase farmUseCase,
                              GoatFarmMapper farmMapper,
                              UserMapper userMapper,
                              AddressMapper addressMapper,
                              PhoneMapper phoneMapper,
                              OwnershipService ownershipService) {
        this.farmUseCase = farmUseCase;
        this.farmMapper = farmMapper;
        this.userMapper = userMapper;
        this.addressMapper = addressMapper;
        this.phoneMapper = phoneMapper;
        this.ownershipService = ownershipService;
    }

    @PostMapping
    @Operation(summary = "Cadastra uma nova fazenda")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Fazenda criada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Payload inválido."),
            @ApiResponse(responseCode = "409", description = "Conflito de unicidade (nome, TOD ou usuário)."),
            @ApiResponse(responseCode = "422", description = "Falha de validação dos dados informados.")
    })
    public ResponseEntity<GoatFarmFullResponseDTO> createGoatFarm(@RequestBody @Valid GoatFarmFullRequestDTO requestDTO) {
        var responseVO = farmUseCase.createGoatFarm(farmMapper.toFullRequestVO(requestDTO));
        return new ResponseEntity<>(farmMapper.toFullDTO(responseVO), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')")
    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados completos de uma fazenda")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fazenda atualizada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Payload inválido."),
            @ApiResponse(responseCode = "404", description = "Fazenda não encontrada."),
            @ApiResponse(responseCode = "422", description = "Falha de validação dos dados informados.")
    })
    public ResponseEntity<GoatFarmFullResponseDTO> updateGoatFarm(@PathVariable Long id, @RequestBody @Valid GoatFarmUpdateRequestDTO requestDTO) {
        var responseVO = farmUseCase.updateGoatFarm(
                id,
                farmMapper.toRequestVO(requestDTO.getFarm()),
                userMapper.toRequestVO(requestDTO.getUser()),
                addressMapper.toVO(requestDTO.getAddress()),
                requestDTO.getPhones().stream().map(phoneMapper::toRequestVO).collect(java.util.stream.Collectors.toList())
        );
        return ResponseEntity.ok(farmMapper.toFullDTO(responseVO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma fazenda pelo identificador")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fazenda encontrada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Fazenda não encontrada.")
    })
    public ResponseEntity<GoatFarmFullResponseDTO> findGoatFarmById(@PathVariable Long id) {
        return ResponseEntity.ok(toPublicSafeDTO(farmMapper.toFullDTO(farmUseCase.findGoatFarmById(id))));
    }

    @GetMapping("/name")
    @Operation(summary = "Busca fazendas por nome com paginação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca executada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos.")
    })
    public ResponseEntity<Page<GoatFarmFullResponseDTO>> searchGoatFarmByName(
            @Parameter(description = "Trecho do nome da fazenda para busca.", example = "Capril")
            @RequestParam(value = "name", defaultValue = "") String name,
            @PageableDefault(size = 12, page = 0) Pageable pageable) {
        return ResponseEntity.ok(farmUseCase.searchGoatFarmByName(name, pageable)
                .map(farmMapper::toFullDTO)
                .map(this::toPublicSafeDTO));
    }

    @GetMapping
    @Operation(summary = "Lista fazendas com paginação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listagem executada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos.")
    })
    public ResponseEntity<Page<GoatFarmFullResponseDTO>> findAllGoatFarm(@PageableDefault(size = 12, page = 0) Pageable pageable) {
        return ResponseEntity.ok(farmUseCase.findAllGoatFarm(pageable)
                .map(farmMapper::toFullDTO)
                .map(this::toPublicSafeDTO));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma fazenda")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Fazenda removida com sucesso."),
            @ApiResponse(responseCode = "404", description = "Fazenda não encontrada.")
    })
    public ResponseEntity<Void> deleteGoatFarm(@PathVariable Long id) {
        farmUseCase.deleteGoatFarm(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')")
    @GetMapping("/{farmId}/permissions")
    @Operation(summary = "Consulta permissões do usuário na fazenda")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permissões retornadas com sucesso."),
            @ApiResponse(responseCode = "404", description = "Fazenda não encontrada, quando aplicável.")
    })
    public ResponseEntity<FarmPermissionsDTO> getFarmPermissions(@PathVariable Long farmId) {
        var vo = farmUseCase.getFarmPermissions(farmId);
        FarmPermissionsDTO dto = new FarmPermissionsDTO();
        dto.setCanCreateGoat(vo.isCanCreateGoat());
        return ResponseEntity.ok(dto);
    }

    private GoatFarmFullResponseDTO toPublicSafeDTO(GoatFarmFullResponseDTO dto) {
        if (dto == null || canManageFarm(dto.getId())) {
            return dto;
        }

        GoatFarmFullResponseDTO sanitized = new GoatFarmFullResponseDTO();
        sanitized.setId(dto.getId());
        sanitized.setName(dto.getName());
        sanitized.setTod(dto.getTod());
        sanitized.setLogoUrl(dto.getLogoUrl());
        sanitized.setCreatedAt(dto.getCreatedAt());
        sanitized.setUpdatedAt(dto.getUpdatedAt());
        sanitized.setVersion(dto.getVersion());
        sanitized.setPhones(dto.getPhones());

        if (dto.getUser() != null) {
            UserResponseDTO user = new UserResponseDTO();
            user.setId(dto.getUser().getId());
            user.setName(dto.getUser().getName());
            sanitized.setUser(user);
        }

        if (dto.getAddress() != null) {
            AddressResponseDTO address = new AddressResponseDTO(
                    dto.getAddress().getId(),
                    null,
                    null,
                    dto.getAddress().getCity(),
                    dto.getAddress().getState(),
                    null,
                    dto.getAddress().getCountry()
            );
            sanitized.setAddress(address);
        }

        return sanitized;
    }

    private boolean canManageFarm(Long farmId) {
        return farmId != null && ownershipService.canManageFarm(farmId);
    }
}
