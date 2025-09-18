package com.devmaster.goatfarm.phone.api.controller;

import com.devmaster.goatfarm.phone.api.dto.PhoneRequestDTO;
import com.devmaster.goatfarm.phone.api.dto.PhoneResponseDTO;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;
import com.devmaster.goatfarm.phone.mapper.PhoneMapper;
import com.devmaster.goatfarm.phone.facade.PhoneFacade;
import com.devmaster.goatfarm.phone.facade.dto.PhoneFacadeResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/phones")
public class PhoneController {

    @Autowired
    private PhoneFacade phoneFacade;

    @Autowired
    private PhoneMapper phoneMapper;

    @Operation(summary = "Cadastra um novo telefone")

    @PostMapping
    public ResponseEntity<?> createPhone(
            @RequestBody(description = "Dados do telefone a ser cadastrado")
            @org.springframework.web.bind.annotation.RequestBody @Valid PhoneRequestDTO requestDTO) {

        try {
            PhoneRequestVO requestVO = phoneMapper.toRequestVO(requestDTO);
            Long goatFarmId = requestDTO.getGoatFarmId();

            PhoneFacadeResponseDTO facadeDTO = phoneFacade.createPhone(requestVO, goatFarmId);
            PhoneResponseVO responseVO = new PhoneResponseVO(facadeDTO.getId(), facadeDTO.getDdd(), facadeDTO.getNumber());
            PhoneResponseDTO responseDTO = phoneMapper.toResponseDTO(responseVO);

            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Dados de telefone inválidos");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (com.devmaster.goatfarm.config.exceptions.custom.DatabaseException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Telefone já existe");
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

    @Operation(summary = "Busca um telefone pelo ID")

    @GetMapping("/{id}")
    public ResponseEntity<PhoneResponseDTO> getPhoneById(
            @Parameter(description = "ID do telefone a ser buscado", example = "1") @PathVariable Long id) {

        PhoneFacadeResponseDTO facadeDTO = phoneFacade.findPhoneById(id);
        if (facadeDTO != null) {
            PhoneResponseVO responseVO = new PhoneResponseVO(facadeDTO.getId(), facadeDTO.getDdd(), facadeDTO.getNumber());
            return ResponseEntity.ok(phoneMapper.toResponseDTO(responseVO));
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Lista todos os telefones cadastrados")

    @GetMapping
    public ResponseEntity<List<PhoneResponseDTO>> getAllPhones() {
        List<PhoneFacadeResponseDTO> facadeDTOs = phoneFacade.findAllPhones();
        return ResponseEntity.ok(facadeDTOs.stream()
                .map(facadeDTO -> {
                    PhoneResponseVO responseVO = new PhoneResponseVO(facadeDTO.getId(), facadeDTO.getDdd(), facadeDTO.getNumber());
                    return phoneMapper.toResponseDTO(responseVO);
                })
                .collect(Collectors.toList()));
    }

    @Operation(summary = "Atualiza um telefone existente")

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePhone(
            @Parameter(description = "ID do telefone a ser atualizado", example = "1") @PathVariable Long id,
            @RequestBody(description = "Novos dados do telefone")
            @org.springframework.web.bind.annotation.RequestBody @Valid PhoneRequestDTO requestDTO) {

        try {
            PhoneRequestVO requestVO = phoneMapper.toRequestVO(requestDTO);
            PhoneFacadeResponseDTO facadeDTO = phoneFacade.updatePhone(id, requestVO);
            PhoneResponseVO responseVO = new PhoneResponseVO(facadeDTO.getId(), facadeDTO.getDdd(), facadeDTO.getNumber());
            return ResponseEntity.ok(phoneMapper.toResponseDTO(responseVO));
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Dados de telefone inválidos");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Telefone não encontrado");
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

    @Operation(summary = "Remove um telefone existente")

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhone(
            @Parameter(description = "ID do telefone a ser removido", example = "1") @PathVariable Long id) {

        phoneFacade.deletePhone(id);
        return ResponseEntity.noContent().build();
    }
}
