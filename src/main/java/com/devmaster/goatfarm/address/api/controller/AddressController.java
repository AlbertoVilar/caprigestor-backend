package com.devmaster.goatfarm.address.api.controller;

import com.devmaster.goatfarm.address.api.dto.AddressRequestDTO;
import com.devmaster.goatfarm.address.api.dto.AddressResponseDTO;
import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;
import com.devmaster.goatfarm.address.converter.AddressDTOConverter;
import com.devmaster.goatfarm.address.facade.AddressFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/address")
public class AddressController {

    @Autowired
    private AddressFacade addressFacade;

    @PostMapping
    public AddressResponseDTO createAddress(@RequestBody AddressRequestDTO requestDTO) {
        AddressRequestVO requestVO = AddressDTOConverter.toVO(requestDTO);
        AddressResponseVO responseVO = addressFacade.createAddress(requestVO);
        return AddressDTOConverter.toDTO(responseVO);
    }

    @PutMapping("/{id}")
    public AddressResponseDTO updateAddress(@PathVariable Long id, @RequestBody AddressRequestDTO requestDTO) {
        AddressRequestVO requestVO = AddressDTOConverter.toVO(requestDTO);
        AddressResponseVO responseVO = addressFacade.updateAddress(id, requestVO);
        return AddressDTOConverter.toDTO(responseVO);
    }

    @GetMapping("/{id}")
    public AddressResponseDTO findAddressById(@PathVariable Long id) {
        AddressResponseVO responseVO = addressFacade.findAddressById(id);
        return AddressDTOConverter.toDTO(responseVO);
    }

    @GetMapping
    public List<AddressResponseDTO> findAllAddresses() {
        return addressFacade.findAllAddresses().stream()
                .map(AddressDTOConverter::toDTO)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{id}")
    public String deleteAddress(@PathVariable Long id) {
        return addressFacade.deleteAddress(id);
    }
}

