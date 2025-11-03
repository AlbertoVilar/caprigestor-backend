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
@RequestMapping("/api/addresses")
public class AddressController {

    @Autowired
    private AddressFacade addressFacade;

    @Operation(summary = "Create a new address", description = "New address data")
    @PostMapping
    public ResponseEntity<AddressResponseDTO> createAddress(@RequestBody @Valid AddressRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addressFacade.createAddress(requestDTO));
    }

    @Operation(summary = "Update an existing address", description = "Updated address data")
    @PutMapping("/{id}")
    public ResponseEntity<AddressResponseDTO> updateAddress(@PathVariable("id") Long id, @RequestBody @Valid AddressRequestDTO requestDTO) {
        return ResponseEntity.ok(addressFacade.updateAddress(id, requestDTO));
    }

    @Operation(summary = "Find an address by ID")
    @GetMapping("/{id}")
    public ResponseEntity<AddressResponseDTO> findAddressById(@Parameter(description = "ID of the address to be searched", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(addressFacade.findAddressById(id));
    }

    @Operation(summary = "List all registered addresses")
    @GetMapping
    public ResponseEntity<List<AddressResponseDTO>> findAllAddresses() {
        return ResponseEntity.ok(addressFacade.findAllAddresses());
    }

    @Operation(summary = "Remove an address by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAddress(@Parameter(description = "ID of the address to be removed", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(addressFacade.deleteAddress(id));
    }
}
