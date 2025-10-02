package com.devmaster.goatfarm.address.api.controller;

import com.devmaster.goatfarm.address.api.dto.AddressRequestDTO;
import com.devmaster.goatfarm.address.api.dto.AddressResponseDTO;
import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;
import com.devmaster.goatfarm.address.mapper.AddressMapper;
import com.devmaster.goatfarm.address.facade.AddressFacade;
import com.devmaster.goatfarm.address.facade.dto.AddressFacadeResponseDTO;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
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
@RequestMapping("/api/addresses")
public class AddressController {

    @Autowired
    private AddressFacade addressFacade;

    @Autowired
    private AddressMapper addressMapper;

    @Operation(summary = "Create a new address")

    @PostMapping
    public ResponseEntity<?> createAddress(
            @RequestBody(description = "New address data")
            @org.springframework.web.bind.annotation.RequestBody @Valid AddressRequestDTO requestDTO) {

        try {
            AddressRequestVO requestVO = addressMapper.toVO(requestDTO);
            AddressFacadeResponseDTO facadeDTO = addressFacade.createAddress(requestVO);
            AddressResponseVO responseVO = new AddressResponseVO(facadeDTO.getId(), facadeDTO.getStreet(), facadeDTO.getCity(), facadeDTO.getNeighborhood(), facadeDTO.getState(), facadeDTO.getZipCode(), facadeDTO.getCountry());
            return ResponseEntity.status(HttpStatus.CREATED).body(addressMapper.toDTO(responseVO));
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Dados de endereço inválidos");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (com.devmaster.goatfarm.config.exceptions.DuplicateEntityException e) {
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

    @Operation(summary = "Update an existing address")

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAddress(
            @PathVariable("id") Long id,
            @RequestBody(description = "Updated address data")
            @org.springframework.web.bind.annotation.RequestBody @Valid AddressRequestDTO requestDTO) {

        try {
            AddressRequestVO requestVO = addressMapper.toVO(requestDTO);
            AddressFacadeResponseDTO facadeDTO = addressFacade.updateAddress(id, requestVO);
            AddressResponseVO responseVO = new AddressResponseVO(facadeDTO.getId(), facadeDTO.getStreet(), facadeDTO.getCity(), facadeDTO.getNeighborhood(), facadeDTO.getState(), facadeDTO.getZipCode(), facadeDTO.getCountry());
            return ResponseEntity.ok(addressMapper.toDTO(responseVO));
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Dados de endereço inválidos");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (ResourceNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Endereço não encontrado");
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

    @Operation(summary = "Find an address by ID")

    @GetMapping("/{id}")
    public AddressResponseDTO findAddressById(
            @Parameter(description = "ID of the address to be searched", example = "1") @PathVariable Long id) {

        AddressFacadeResponseDTO facadeDTO = addressFacade.findAddressById(id);
        AddressResponseVO responseVO = new AddressResponseVO(facadeDTO.getId(), facadeDTO.getStreet(), facadeDTO.getCity(), facadeDTO.getNeighborhood(), facadeDTO.getState(), facadeDTO.getZipCode(), facadeDTO.getCountry());
        return addressMapper.toDTO(responseVO);
    }

    @Operation(summary = "List all registered addresses")

    @GetMapping
    public List<AddressResponseDTO> findAllAddresses() {
        return addressFacade.findAllAddresses().stream()
                .map(facadeDTO -> {
                    AddressResponseVO responseVO = new AddressResponseVO(facadeDTO.getId(), facadeDTO.getStreet(), facadeDTO.getCity(), facadeDTO.getNeighborhood(), facadeDTO.getState(), facadeDTO.getZipCode(), facadeDTO.getCountry());
                    return addressMapper.toDTO(responseVO);
                })
                .collect(Collectors.toList());
    }

    @Operation(summary = "Remove an address by ID")

    @DeleteMapping("/{id}")
    public String deleteAddress(
            @Parameter(description = "ID of the address to be removed", example = "1") @PathVariable Long id) {

        return addressFacade.deleteAddress(id);
    }
}
