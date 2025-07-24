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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/address")
public class AddressController {

    @Autowired
    private AddressFacade addressFacade;

    @Operation(summary = "Cria um novo endereço")
    //@PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public AddressResponseDTO createAddress(
            @RequestBody(description = "Dados do novo endereço")
            @org.springframework.web.bind.annotation.RequestBody @Valid AddressRequestDTO requestDTO) {

        AddressRequestVO requestVO = AddressDTOConverter.toVO(requestDTO);
        AddressResponseVO responseVO = addressFacade.createAddress(requestVO);
        return AddressDTOConverter.toDTO(responseVO);
    }

    @Operation(summary = "Atualiza um endereço existente")
    //@PreAuthorize("hasAuthority('ROLE_ADMIN')")
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
    //@PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/{id}")
    public AddressResponseDTO findAddressById(
            @Parameter(description = "ID do endereço a ser buscado", example = "1") @PathVariable Long id) {

        AddressResponseVO responseVO = addressFacade.findAddressById(id);
        return AddressDTOConverter.toDTO(responseVO);
    }

    @Operation(summary = "Lista todos os endereços cadastrados")
    //@PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    public List<AddressResponseDTO> findAllAddresses() {
        return addressFacade.findAllAddresses().stream()
                .map(AddressDTOConverter::toDTO)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Remove um endereço pelo ID")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public String deleteAddress(
            @Parameter(description = "ID do endereço a ser removido", example = "1") @PathVariable Long id) {

        return addressFacade.deleteAddress(id);
    }
}
