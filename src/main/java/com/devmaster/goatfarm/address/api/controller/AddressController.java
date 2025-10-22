package com.devmaster.goatfarm.address.api.controller;

import com.devmaster.goatfarm.address.api.dto.AddressRequestDTO;
import com.devmaster.goatfarm.address.api.dto.AddressResponseDTO;
import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;
import com.devmaster.goatfarm.address.facade.AddressFacade;
import com.devmaster.goatfarm.address.mapper.AddressMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    @Autowired
    private AddressFacade addressFacade;

    @Autowired
    private AddressMapper addressMapper;

    @Operation(summary = "Create a new address", description = "New address data")

    @PostMapping
    public ResponseEntity<?> createAddress(
            @RequestBody @Valid AddressRequestDTO requestDTO) {

        AddressRequestVO requestVO = addressMapper.toVO(requestDTO);
        AddressResponseVO responseVO = addressFacade.createAddress(requestVO);
        AddressResponseDTO responseDTO = addressMapper.toDTO(responseVO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @Operation(summary = "Update an existing address", description = "Updated address data")

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAddress(
            @PathVariable("id") Long id,
            @RequestBody @Valid AddressRequestDTO requestDTO) {

        AddressRequestVO requestVO = addressMapper.toVO(requestDTO);
        AddressResponseVO responseVO = addressFacade.updateAddress(id, requestVO);
        AddressResponseDTO responseDTO = addressMapper.toDTO(responseVO);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(summary = "Find an address by ID")

    @GetMapping("/{id}")
    public AddressResponseDTO findAddressById(
            @Parameter(description = "ID of the address to be searched", example = "1") @PathVariable Long id) {

        AddressResponseVO responseVO = addressFacade.findAddressById(id);
        return addressMapper.toDTO(responseVO);
    }

    @Operation(summary = "List all registered addresses")

    @GetMapping
    public List<AddressResponseDTO> findAllAddresses() {
        return addressFacade.findAllAddresses()
                .stream()
                .map(addressMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Remove an address by ID")

    @DeleteMapping("/{id}")
    public String deleteAddress(
            @Parameter(description = "ID of the address to be removed", example = "1") @PathVariable Long id) {

        return addressFacade.deleteAddress(id);
    }
}
