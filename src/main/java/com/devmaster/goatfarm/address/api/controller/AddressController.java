package com.devmaster.goatfarm.address.api.controller;

import com.devmaster.goatfarm.address.api.dto.AddressRequestDTO;
import com.devmaster.goatfarm.address.api.dto.AddressResponseDTO;
import com.devmaster.goatfarm.address.api.mapper.AddressMapper;
import com.devmaster.goatfarm.address.application.ports.in.AddressManagementUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goatfarms/{farmId}/addresses")
public class AddressController {

    private final AddressManagementUseCase addressUseCase;
    private final AddressMapper addressMapper;

    public AddressController(AddressManagementUseCase addressUseCase, AddressMapper addressMapper) {
        this.addressUseCase = addressUseCase;
        this.addressMapper = addressMapper;
    }

    @Operation(summary = "Create a new address for a farm", description = "New address data")
    @PostMapping
    public ResponseEntity<AddressResponseDTO> createAddress(@PathVariable Long farmId, @RequestBody @Valid AddressRequestDTO requestDTO) {
        var responseVO = addressUseCase.createAddress(farmId, addressMapper.toVO(requestDTO));
        return ResponseEntity.status(HttpStatus.CREATED).body(addressMapper.toDTO(responseVO));
    }

    @Operation(summary = "Update an existing address for a farm", description = "Updated address data")
    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponseDTO> updateAddress(
            @PathVariable Long farmId,
            @PathVariable("addressId") Long addressId,
            @RequestBody @Valid AddressRequestDTO requestDTO) {
        var responseVO = addressUseCase.updateAddress(farmId, addressId, addressMapper.toVO(requestDTO));
        return ResponseEntity.ok(addressMapper.toDTO(responseVO));
    }

    @Operation(summary = "Find an address by ID for a farm")
    @GetMapping("/{addressId}")
    public ResponseEntity<AddressResponseDTO> findAddressById(
            @PathVariable Long farmId,
            @Parameter(description = "ID of the address to be searched", example = "1") @PathVariable Long addressId) {
        return ResponseEntity.ok(addressMapper.toDTO(addressUseCase.findAddressById(farmId, addressId)));
    }

    @Operation(summary = "Remove an address by ID for a farm")
    @DeleteMapping("/{addressId}")
    public ResponseEntity<String> deleteAddress(
            @PathVariable Long farmId,
            @Parameter(description = "ID of the address to be removed", example = "1") @PathVariable Long addressId) {
        return ResponseEntity.ok(addressUseCase.deleteAddress(farmId, addressId));
    }


}
