package com.devmaster.goatfarm.address.api.controller;

import com.devmaster.goatfarm.address.api.dto.AddressRequestDTO;
import com.devmaster.goatfarm.address.api.dto.AddressResponseDTO;
import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;
import com.devmaster.goatfarm.address.converter.AddressDTOConverter;
import com.devmaster.goatfarm.address.facade.AddressFacade;
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
@RequestMapping("/address")
public class AddressController {

    @Autowired
    private AddressFacade addressFacade;

    @Operation(summary = "Cria um novo endereço")

    @PostMapping
    public ResponseEntity<?> createAddress(
            @RequestBody(description = "Dados do novo endereço")
            @org.springframework.web.bind.annotation.RequestBody @Valid AddressRequestDTO requestDTO) {

        try {
            // Validações granulares
            Map<String, String> validationErrors = new HashMap<>();
            
            // Validar CEP brasileiro mais rigorosamente
            if (requestDTO.getPostalCode() != null) {
                String cep = requestDTO.getPostalCode().replaceAll("[^0-9]", "");
                if (!cep.matches("^\\d{8}$")) {
                    validationErrors.put("postalCode", "CEP deve conter exatamente 8 dígitos numéricos");
                }
            }
            
            // Validar estado brasileiro (siglas válidas)
            if (requestDTO.getState() != null) {
                String[] estadosValidos = {"AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", 
                                         "MA", "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", 
                                         "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO"};
                boolean estadoValido = false;
                for (String estado : estadosValidos) {
                    if (estado.equalsIgnoreCase(requestDTO.getState().trim())) {
                        estadoValido = true;
                        break;
                    }
                }
                if (!estadoValido) {
                    validationErrors.put("state", "Estado deve ser uma sigla válida (ex: SP, RJ, MG)");
                }
            }
            
            // Validar país (aceitar apenas Brasil por enquanto)
            if (requestDTO.getCountry() != null && 
                !requestDTO.getCountry().trim().equalsIgnoreCase("Brasil") && 
                !requestDTO.getCountry().trim().equalsIgnoreCase("Brazil")) {
                validationErrors.put("country", "Por enquanto, apenas endereços do Brasil são aceitos");
            }
            
            if (!validationErrors.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Dados de endereço inválidos");
                errorResponse.put("errors", validationErrors);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
            
            AddressRequestVO requestVO = AddressDTOConverter.toVO(requestDTO);
            AddressResponseVO responseVO = addressFacade.createAddress(requestVO);
            return ResponseEntity.status(HttpStatus.CREATED).body(AddressDTOConverter.toDTO(responseVO));
            
        } catch (com.devmaster.goatfarm.config.exceptions.custom.DuplicateEntityException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Endereço já existe");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
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

    @Operation(summary = "Atualiza um endereço existente")

    @PutMapping("/{id}")
    public AddressResponseDTO updateAddress(
            @Parameter(description = "ID do endereço a ser atualizado", example = "1") @PathVariable Long id,
            @RequestBody(description = "Novos dados do endereço")
            @org.springframework.web.bind.annotation.RequestBody @Valid AddressRequestDTO requestDTO) {

        AddressRequestVO requestVO = AddressDTOConverter.toVO(requestDTO);
        AddressResponseVO responseVO = addressFacade.updateAddress(id, requestVO);
        return AddressDTOConverter.toDTO(responseVO);
    }

    @Operation(summary = "Busca um endereço pelo ID")

    @GetMapping("/{id}")
    public AddressResponseDTO findAddressById(
            @Parameter(description = "ID do endereço a ser buscado", example = "1") @PathVariable Long id) {

        AddressResponseVO responseVO = addressFacade.findAddressById(id);
        return AddressDTOConverter.toDTO(responseVO);
    }

    @Operation(summary = "Lista todos os endereços cadastrados")

    @GetMapping
    public List<AddressResponseDTO> findAllAddresses() {
        return addressFacade.findAllAddresses().stream()
                .map(AddressDTOConverter::toDTO)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Remove um endereço pelo ID")

    @DeleteMapping("/{id}")
    public String deleteAddress(
            @Parameter(description = "ID do endereço a ser removido", example = "1") @PathVariable Long id) {

        return addressFacade.deleteAddress(id);
    }
}
