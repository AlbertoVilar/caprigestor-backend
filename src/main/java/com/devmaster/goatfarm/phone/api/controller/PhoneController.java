package com.devmaster.goatfarm.phone.api.controller;

import com.devmaster.goatfarm.phone.api.dto.PhoneRequestDTO;
import com.devmaster.goatfarm.phone.api.dto.PhoneResponseDTO;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;
import com.devmaster.goatfarm.phone.converter.PhoneDTOConverter;
import com.devmaster.goatfarm.phone.facade.PhoneFacade;
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
@RequestMapping("/phones")
public class PhoneController {

    @Autowired
    private PhoneFacade phoneFacade;

    @Operation(summary = "Cadastra um novo telefone")

    @PostMapping
    public ResponseEntity<?> createPhone(
            @RequestBody(description = "Dados do telefone a ser cadastrado")
            @org.springframework.web.bind.annotation.RequestBody @Valid PhoneRequestDTO requestDTO) {

        try {
            // Validações granulares
            Map<String, String> validationErrors = new HashMap<>();
            
            // Validar DDD brasileiro
            if (requestDTO.getDdd() != null) {
                String[] dddsValidos = {"11", "12", "13", "14", "15", "16", "17", "18", "19", // SP
                                      "21", "22", "24", // RJ
                                      "27", "28", // ES
                                      "31", "32", "33", "34", "35", "37", "38", // MG
                                      "41", "42", "43", "44", "45", "46", // PR
                                      "47", "48", "49", // SC
                                      "51", "53", "54", "55", // RS
                                      "61", // DF
                                      "62", "64", // GO
                                      "63", // TO
                                      "65", "66", // MT
                                      "67", // MS
                                      "68", // AC
                                      "69", // RO
                                      "71", "73", "74", "75", "77", // BA
                                      "79", // SE
                                      "81", "87", // PE
                                      "82", // AL
                                      "83", // PB
                                      "84", // RN
                                      "85", "88", // CE
                                      "86", "89", // PI
                                      "91", "93", "94", // PA
                                      "92", "97", // AM
                                      "95", // RR
                                      "96", // AP
                                      "98", "99"}; // MA
                
                boolean dddValido = false;
                for (String ddd : dddsValidos) {
                    if (ddd.equals(requestDTO.getDdd().trim())) {
                        dddValido = true;
                        break;
                    }
                }
                if (!dddValido) {
                    validationErrors.put("ddd", "DDD inválido. Deve ser um DDD brasileiro válido");
                }
            }
            
            // Validar formato do número (8 ou 9 dígitos)
            if (requestDTO.getNumber() != null) {
                String numero = requestDTO.getNumber().trim();
                if (!numero.matches("^\\d{8,9}$")) {
                    validationErrors.put("number", "Número deve ter 8 ou 9 dígitos numéricos");
                } else if (numero.length() == 9 && !numero.startsWith("9")) {
                    validationErrors.put("number", "Números com 9 dígitos devem começar com 9 (celular)");
                }
            }
            
            if (!validationErrors.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Dados de telefone inválidos");
                errorResponse.put("errors", validationErrors);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            PhoneRequestVO requestVO = PhoneDTOConverter.toVO(requestDTO);
            Long goatFarmId = requestDTO.getGoatFarmId();

            PhoneResponseVO responseVO = phoneFacade.createPhone(requestVO, goatFarmId);
            PhoneResponseDTO responseDTO = PhoneDTOConverter.toDTO(responseVO);

            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
            
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

        PhoneResponseVO responseVO = phoneFacade.findPhoneById(id);
        if (responseVO != null) {
            return ResponseEntity.ok(PhoneDTOConverter.toDTO(responseVO));
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Lista todos os telefones cadastrados")

    @GetMapping
    public ResponseEntity<List<PhoneResponseDTO>> getAllPhones() {
        List<PhoneResponseVO> responseVOs = phoneFacade.findAllPhones();
        return ResponseEntity.ok(responseVOs.stream()
                .map(PhoneDTOConverter::toDTO)
                .collect(Collectors.toList()));
    }

    @Operation(summary = "Atualiza um telefone existente")

    @PutMapping("/{id}")
    public ResponseEntity<PhoneResponseDTO> updatePhone(
            @Parameter(description = "ID do telefone a ser atualizado", example = "1") @PathVariable Long id,
            @RequestBody(description = "Novos dados do telefone")
            @org.springframework.web.bind.annotation.RequestBody @Valid PhoneRequestDTO requestDTO) {

        PhoneRequestVO requestVO = PhoneDTOConverter.toVO(requestDTO);
        PhoneResponseVO responseVO = phoneFacade.updatePhone(id, requestVO);
        return ResponseEntity.ok(PhoneDTOConverter.toDTO(responseVO));
    }

    @Operation(summary = "Remove um telefone existente")

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhone(
            @Parameter(description = "ID do telefone a ser removido", example = "1") @PathVariable Long id) {

        phoneFacade.deletePhone(id);
        return ResponseEntity.noContent().build();
    }
}
