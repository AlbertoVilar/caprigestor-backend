package com.devmaster.goatfarm.address.facade;

import com.devmaster.goatfarm.address.api.dto.AddressRequestDTO;
import com.devmaster.goatfarm.address.api.dto.AddressResponseDTO;
import com.devmaster.goatfarm.address.business.AddressBusiness;
import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;
import com.devmaster.goatfarm.address.mapper.AddressMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressFacade {

    @Autowired
    private AddressBusiness addressBusiness;

    @Autowired
    private AddressMapper addressMapper;

    public AddressResponseDTO createAddress(AddressRequestDTO requestDTO) {
        AddressResponseVO responseVO = addressBusiness.createAddress(addressMapper.toVO(requestDTO));
        return addressMapper.toDTO(responseVO);
    }

    public AddressResponseDTO updateAddress(Long id, AddressRequestDTO requestDTO) {
        AddressResponseVO responseVO = addressBusiness.updateAddress(id, addressMapper.toVO(requestDTO));
        return addressMapper.toDTO(responseVO);
    }

    public AddressResponseDTO findAddressById(Long id) {
        AddressResponseVO responseVO = addressBusiness.findAddressById(id);
        return addressMapper.toDTO(responseVO);
    }

    public List<AddressResponseDTO> findAllAddresses() {
        return addressBusiness.findAllAddresses().stream()
                .map(addressMapper::toDTO)
                .collect(Collectors.toList());
    }

    public String deleteAddress(Long id) {
        return addressBusiness.deleteAddress(id);
    }
}
