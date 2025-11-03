package com.devmaster.goatfarm.address.api.controller;

import com.devmaster.goatfarm.address.api.dto.AddressRequestDTO;
import com.devmaster.goatfarm.address.api.dto.AddressResponseDTO;
import com.devmaster.goatfarm.address.facade.AddressFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goatfarms/{farmId}/addresses")
public class AddressController {

    @Autowired
    private AddressFacade addressFacade;

    @Operation(summary = "Create a new address for a farm", description = "New address data")
    @PostMapping
    public ResponseEntity<AddressResponseDTO> createAddress(@PathVariable Long farmId, @RequestBody @Valid AddressRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addressFacade.createAddress(farmId, requestDTO));
    }

    @Operation(summary = "Update an existing address for a farm", description = "Updated address data")
    @PutMapping("/{addressId}")
    public ResponseEntity<AddressResponseDTO> updateAddress(
            @PathVariable Long farmId,
            @PathVariable("addressId") Long addressId,
            @RequestBody @Valid AddressRequestDTO requestDTO) {
        return ResponseEntity.ok(addressFacade.updateAddress(farmId, addressId, requestDTO));
    }

    @Operation(summary = "Find an address by ID for a farm")
    @GetMapping("/{addressId}")
    public ResponseEntity<AddressResponseDTO> findAddressById(
            @PathVariable Long farmId,
            @Parameter(description = "ID of the address to be searched", example = "1") @PathVariable Long addressId) {
        return ResponseEntity.ok(addressFacade.findAddressById(farmId, addressId));
    }

    @Operation(summary = "Remove an address by ID for a farm")
    @DeleteMapping("/{addressId}")
    public ResponseEntity<String> deleteAddress(
            @PathVariable Long farmId,
            @Parameter(description = "ID of the address to be removed", example = "1") @PathVariable Long addressId) {
        return ResponseEntity.ok(addressFacade.deleteAddress(farmId, addressId));
    }

    // Este endpoint pode precisar ser revisto se a intenção é listar apenas endereços de uma fazenda específica
    @Operation(summary = "List all registered addresses (consider if this should be farm-specific)")
    @GetMapping("/all") // Mudei o path para evitar conflito com o GET /api/goatfarms/{farmId}/addresses
    public ResponseEntity<List<AddressResponseDTO>> findAllAddresses() {
        return ResponseEntity.ok(addressFacade.findAllAddresses());
    }
}
