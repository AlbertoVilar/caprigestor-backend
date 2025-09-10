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
@RequestMapping("/goatfarms")
public class GoatFarmController {

    @Autowired
    private GoatFarmFacade farmFacade;

    @Autowired
    private OwnershipService ownershipService;

    @Operation(summary = "Cadastra um novo capril completo com proprietário, endereço e telefones")
    @PostMapping("/full")
    public ResponseEntity<?> createFullGoatFarm(
            @RequestBody(description = "Dados completos para criação da fazenda")
            @org.springframework.web.bind.annotation.RequestBody com.devmaster.goatfarm.farm.api.dto.GoatFarmFullRequestDTO requestDTO) {

        try {
            GoatFarmFullResponseVO responseVO = farmFacade.createFullGoatFarm(
                    GoatFarmDTOConverter.toVO(requestDTO.getFarm()),
                    UserDTOConverter.toVO(requestDTO.getUser()),
                    AddressDTOConverter.toVO(requestDTO.getAddress()),
                    requestDTO.getPhones().stream().map(PhoneDTOConverter::toVO).toList()
            );

            return new ResponseEntity<>(GoatFarmDTOConverter.toFullDTO(responseVO), HttpStatus.CREATED);

        } catch (com.devmaster.goatfarm.config.exceptions.custom.DuplicateEntityException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Fazenda já existe");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Recurso não encontrado");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (com.devmaster.goatfarm.config.exceptions.custom.DatabaseException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Erro de banco de dados");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Dados inválidos");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Erro interno do servidor");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Erro inesperado");
            errorResponse.put("error", "Ocorreu um erro inesperado. Tente novamente.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @CrossOrigin(origins = "http://localhost:5500")
    @Operation(summary = "Cadastra um novo capril usando IDs de entidades existentes")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')")
    @PostMapping
    public ResponseEntity<?> createGoatFarm(
            @RequestBody(description = "Dados do novo capril")
            @org.springframework.web.bind.annotation.RequestBody @Valid GoatFarmRequestDTO requestDTO) {

        try {
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
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Dados da fazenda inválidos");
                errorResponse.put("errors", validationErrors);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            GoatFarmResponseVO responseVO = farmFacade.createGoatFarm(GoatFarmDTOConverter.toVO(requestDTO));
            return new ResponseEntity<>(GoatFarmDTOConverter.toDTO(responseVO), HttpStatus.CREATED);

        } catch (com.devmaster.goatfarm.config.exceptions.custom.DuplicateEntityException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Fazenda já existe");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Recurso não encontrado");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Erro interno do servidor");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "Atualiza os dados de um capril existente")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')")
    @PutMapping("/{id}")
    public ResponseEntity<GoatFarmFullResponseDTO> updateGoatFarm(
            @Parameter(description = "ID do capril a ser atualizado", example = "1") @PathVariable Long id,
            @RequestBody(description = "Novos dados para o capril")
            @org.springframework.web.bind.annotation.RequestBody GoatFarmUpdateRequestDTO requestDTO) {

        // Verificar ownership antes de atualizar
        GoatFarmFullResponseVO existingFarm = farmFacade.findGoatFarmById(id);
        ownershipService.verifyFarmOwnership(existingFarm);

        GoatFarmFullResponseVO responseVO = farmFacade.updateGoatFarm(
                id,
                GoatFarmDTOConverter.toVO(requestDTO.getFarm()),
                UserDTOConverter.toVO(requestDTO.getUser()),
                AddressDTOConverter.toVO(requestDTO.getAddress()),
                requestDTO.getPhones().stream().map(PhoneDTOConverter::toVO).toList()
        );

        return ResponseEntity.ok(GoatFarmDTOConverter.toFullDTO(responseVO));
    }

    @Operation(summary = "Busca um capril pelo ID")
    @GetMapping("/{id}")
    public ResponseEntity<GoatFarmFullResponseDTO> findGoatFarmById(
            @Parameter(description = "ID do capril", example = "1") @PathVariable Long id) {

        GoatFarmFullResponseVO responseVO = farmFacade.findGoatFarmById(id);
        return ResponseEntity.ok(GoatFarmDTOConverter.toFullDTO(responseVO));
    }

    @Operation(summary = "Busca paginada de capris pelo nome")
    @GetMapping("/name")
    public ResponseEntity<Page<GoatFarmFullResponseDTO>> searchGoatFarmByName(
            @Parameter(description = "Nome ou parte do nome do capril", example = "Capril Vilar")
            @RequestParam(value = "name", defaultValue = "") String name,
            @PageableDefault(size = 12, page = 0) Pageable pageable) {

        return ResponseEntity.ok(farmFacade.searchGoatFarmByName(name, pageable)
                .map(GoatFarmDTOConverter::toFullDTO));
    }

    @Operation(summary = "Lista todos os capris com paginação")
    @GetMapping
    public ResponseEntity<Page<GoatFarmFullResponseDTO>> findAllGoatFarm(
            @PageableDefault(size = 12, page = 0) Pageable pageable) {

        return ResponseEntity.ok(farmFacade.findAllGoatFarm(pageable)
                .map(GoatFarmDTOConverter::toFullDTO));
    }

    @Operation(summary = "Remove um capril pelo ID")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoatFarm(
            @Parameter(description = "ID do capril a ser removido", example = "1") @PathVariable Long id) {

        // Verificar ownership antes de deletar
        GoatFarmFullResponseVO existingFarm = farmFacade.findGoatFarmById(id);
        ownershipService.verifyFarmOwnership(existingFarm);

        farmFacade.deleteGoatFarm(id);
        return ResponseEntity.noContent().build();
    }
}