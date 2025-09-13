package com.devmaster.goatfarm.farm.api.controller;

import com.devmaster.goatfarm.address.converter.AddressDTOConverter;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmFullResponseDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmRequestDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmResponseDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmUpdateRequestDTO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.farm.converters.GoatFarmDTOConverter;
import com.devmaster.goatfarm.farm.facade.GoatFarmFacade;
import com.devmaster.goatfarm.goat.api.dto.GoatResponseDTO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.converter.GoatDTOConverter;
import com.devmaster.goatfarm.authority.conveter.UserDTOConverter;
import com.devmaster.goatfarm.phone.converter.PhoneDTOConverter;
import com.devmaster.goatfarm.config.security.OwnershipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/goatfarms")
public class GoatFarmController {

    @Autowired
    private GoatFarmFacade farmFacade;

    @Autowired
    private OwnershipService ownershipService;

    @CrossOrigin(origins = {"http://localhost:5500", "http://localhost:5173", "http://localhost:8080"})
    @Operation(summary = "Registra uma nova fazenda completa com proprietário, endereço e telefones",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Fazenda criada com sucesso"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Fazenda já existe")
            })
    @PreAuthorize("permitAll()")
    @PostMapping("/full")
    public ResponseEntity<GoatFarmFullResponseDTO> createFullGoatFarm(
            @RequestBody(description = "Dados completos para criação da fazenda")
            @org.springframework.web.bind.annotation.RequestBody com.devmaster.goatfarm.farm.api.dto.GoatFarmFullRequestDTO requestDTO) {

        GoatFarmFullResponseVO responseVO = farmFacade.createFullGoatFarm(
                GoatFarmDTOConverter.toVO(requestDTO.getFarm()),
                UserDTOConverter.toVO(requestDTO.getUser()),
                AddressDTOConverter.toVO(requestDTO.getAddress()),
                requestDTO.getPhones().stream().map(PhoneDTOConverter::toVO).toList()
        );

        return new ResponseEntity<>(GoatFarmDTOConverter.toFullDTO(responseVO), HttpStatus.CREATED);
    }

    @CrossOrigin(origins = {"http://localhost:5500", "http://localhost:5173"})
    @Operation(summary = "Cadastra uma nova fazenda usando IDs de entidades existentes",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Fazenda criada com sucesso"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acesso negado"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Fazenda já existe")
            })
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')")
    @PostMapping
    public ResponseEntity<GoatFarmResponseDTO> createGoatFarm(
            @RequestBody(description = "Dados da nova fazenda")
            @org.springframework.web.bind.annotation.RequestBody @Valid GoatFarmRequestDTO requestDTO) {

        // Validações granulares
        Map<String, String> validationErrors = new HashMap<>();
        if (requestDTO.getName() != null) {
            String nome = requestDTO.getName().trim();
            if (nome.length() < 3 || nome.length() > 100) {
                validationErrors.put("name", "Nome da fazenda deve ter entre 3 e 100 caracteres");
            }
        }
        // ... outras validações que você queira manter ...

        if (!validationErrors.isEmpty()) {
            throw new com.devmaster.goatfarm.config.exceptions.custom.ValidationException(
                "Dados da fazenda inválidos", validationErrors);
        }

        GoatFarmResponseVO responseVO = farmFacade.createGoatFarm(GoatFarmDTOConverter.toVO(requestDTO));
        return new ResponseEntity<>(GoatFarmDTOConverter.toDTO(responseVO), HttpStatus.CREATED);
    }

    @Operation(summary = "Atualiza os dados de uma fazenda existente",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Fazenda atualizada com sucesso"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acesso negado - usuário não é proprietário da fazenda"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Fazenda não encontrada")
            })
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')")
    @PutMapping("/{id}")
    public ResponseEntity<GoatFarmFullResponseDTO> updateGoatFarm(
            @Parameter(description = "ID da fazenda a ser atualizada", example = "1") @PathVariable Long id,
            @RequestBody(description = "Novos dados para a fazenda")
            @org.springframework.web.bind.annotation.RequestBody GoatFarmUpdateRequestDTO requestDTO) {

        GoatFarmFullResponseVO responseVO = farmFacade.updateGoatFarm(
                id,
                GoatFarmDTOConverter.toVO(requestDTO.getFarm()),
                UserDTOConverter.toVO(requestDTO.getUser()),
                AddressDTOConverter.toVO(requestDTO.getAddress()),
                requestDTO.getPhones().stream().map(PhoneDTOConverter::toVO).toList()
        );

        return ResponseEntity.ok(GoatFarmDTOConverter.toFullDTO(responseVO));
    }

    @Operation(summary = "Busca uma fazenda pelo ID",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Fazenda encontrada com sucesso"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acesso negado - usuário não é proprietário da fazenda"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Fazenda não encontrada")
            })
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')")
    @GetMapping("/{id}")
    public ResponseEntity<GoatFarmFullResponseDTO> findGoatFarmById(
            @Parameter(description = "ID da fazenda", example = "1") @PathVariable Long id) {

        GoatFarmFullResponseVO responseVO = farmFacade.findGoatFarmById(id);
        return ResponseEntity.ok(GoatFarmDTOConverter.toFullDTO(responseVO));
    }

    @Operation(summary = "Busca paginada de fazendas pelo nome",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Busca realizada com sucesso")
            })
    @GetMapping("/name")
    public ResponseEntity<Page<GoatFarmFullResponseDTO>> searchGoatFarmByName(
            @Parameter(description = "Nome ou parte do nome da fazenda", example = "Fazenda Vilar")
            @RequestParam(value = "name", defaultValue = "") String name,
            @PageableDefault(size = 12, page = 0) Pageable pageable) {

        return ResponseEntity.ok(farmFacade.searchGoatFarmByName(name, pageable)
                .map(GoatFarmDTOConverter::toFullDTO));
    }

    @Operation(summary = "Lista todas as fazendas com paginação",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista obtida com sucesso")
            })
    @GetMapping
    public ResponseEntity<Page<GoatFarmFullResponseDTO>> findAllGoatFarm(
            @PageableDefault(size = 12, page = 0) Pageable pageable) {

        return ResponseEntity.ok(farmFacade.findAllGoatFarm(pageable)
                .map(GoatFarmDTOConverter::toFullDTO));
    }

    @Operation(summary = "Remove uma fazenda pelo ID",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Fazenda removida com sucesso"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acesso negado - usuário não é proprietário da fazenda"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Fazenda não encontrada"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Fazenda não pode ser removida - possui dependências")
            })
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoatFarm(
            @Parameter(description = "ID da fazenda a ser removida", example = "1") @PathVariable Long id) {

        farmFacade.deleteGoatFarm(id);
        return ResponseEntity.noContent().build();
    }
}
