package com.devmaster.goatfarm.address.facade;

import com.devmaster.goatfarm.address.api.dto.AddressRequestDTO;
import com.devmaster.goatfarm.address.api.dto.AddressResponseDTO;
import com.devmaster.goatfarm.address.business.AddressBusiness;
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

    public AddressResponseDTO createAddress(Long farmId, AddressRequestDTO requestDTO) {
        return addressMapper.toDTO(addressBusiness.createAddress(farmId, addressMapper.toVO(requestDTO)));
    }

    public AddressResponseDTO updateAddress(Long farmId, Long addressId, AddressRequestDTO requestDTO) {
        return addressMapper.toDTO(addressBusiness.updateAddress(farmId, addressId, addressMapper.toVO(requestDTO)));
    }

    public AddressResponseDTO findAddressById(Long farmId, Long addressId) {
        return addressMapper.toDTO(addressBusiness.findAddressById(farmId, addressId));
    }

    public List<AddressResponseDTO> findAllAddresses() {
        // Este método não tem farmId, pois lista todos os endereços. Manter como está ou refatorar se necessário.
        return addressBusiness.findAllAddresses().stream()
                .map(addressMapper::toDTO)
                .collect(Collectors.toList());
    }

    public String deleteAddress(Long farmId, Long addressId) {
        return addressBusiness.deleteAddress(farmId, addressId);
    }
}
