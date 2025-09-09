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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/goatfarms")
public class GoatFarmController {

    @Autowired
    private GoatFarmFacade farmFacade;

    @Operation(summary = "Cadastra um novo capril completo com proprietário, endereço e telefones")

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


    @CrossOrigin(origins = "http://localhost:5500")
    @Operation(summary = "Cadastra um novo capril")

    @PostMapping
    public ResponseEntity<?> createGoatFarm(
            @RequestBody(description = "Dados do novo capril")
            @org.springframework.web.bind.annotation.RequestBody @Valid GoatFarmRequestDTO requestDTO) {

        try {
            // Validações granulares
            Map<String, String> validationErrors = new HashMap<>();
            
            // Validar nome da fazenda
            if (requestDTO.getName() != null) {
                String nome = requestDTO.getName().trim();
                if (nome.length() < 3) {
                    validationErrors.put("name", "Nome da fazenda deve ter pelo menos 3 caracteres");
                } else if (nome.length() > 100) {
                    validationErrors.put("name", "Nome da fazenda não pode exceder 100 caracteres");
                } else if (!nome.matches("^[a-zA-ZÀ-ÿ0-9\\s\\-\\.]+$")) {
                    validationErrors.put("name", "Nome da fazenda contém caracteres inválidos");
                }
            }
            
            // Validar TOD (Código da fazenda)
            if (requestDTO.getTod() != null) {
                String tod = requestDTO.getTod().trim();
                if (!tod.matches("^[A-Z0-9]{5}$")) {
                    validationErrors.put("tod", "TOD deve conter exatamente 5 caracteres alfanuméricos maiúsculos");
                }
            }
            
            // Validar IDs de referência
            if (requestDTO.getUserId() != null && requestDTO.getUserId() <= 0) {
                validationErrors.put("userId", "ID do usuário deve ser um número positivo");
            }
            
            if (requestDTO.getAddressId() != null && requestDTO.getAddressId() <= 0) {
                validationErrors.put("addressId", "ID do endereço deve ser um número positivo");
            }
            
            // Validar lista de telefones
            if (requestDTO.getPhoneIds() != null) {
                for (int i = 0; i < requestDTO.getPhoneIds().size(); i++) {
                    Long phoneId = requestDTO.getPhoneIds().get(i);
                    if (phoneId == null || phoneId <= 0) {
                        validationErrors.put("phoneIds[" + i + "]", "ID do telefone deve ser um número positivo");
                    }
                }
            }
            
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
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Erro inesperado");
            errorResponse.put("error", "Ocorreu um erro inesperado. Tente novamente.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "Atualiza os dados de um capril existente")

    @PutMapping("/{id}")
    public ResponseEntity<GoatFarmFullResponseDTO> updateGoatFarm(
            @Parameter(description = "ID do capril a ser atualizado", example = "1") @PathVariable Long id,
            @RequestBody(description = "Novos dados para o capril")
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoatFarm(
            @Parameter(description = "ID do capril a ser removido", example = "1") @PathVariable Long id) {

        farmFacade.deleteGoatFarm(id);
        return ResponseEntity.noContent().build();
    }


}
