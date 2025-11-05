package com.devmaster.goatfarm.address.facade;

import com.devmaster.goatfarm.address.business.AddressBusiness;
import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;
import com.devmaster.goatfarm.address.api.dto.AddressRequestDTO;
import com.devmaster.goatfarm.address.api.dto.AddressResponseDTO;
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
        AddressRequestVO requestVO = addressMapper.toVO(requestDTO);
        AddressResponseVO responseVO = addressBusiness.createAddress(farmId, requestVO);
        return addressMapper.toDTO(responseVO);
    }

    public AddressResponseDTO updateAddress(Long farmId, Long addressId, AddressRequestDTO requestDTO) {
        AddressRequestVO requestVO = addressMapper.toVO(requestDTO);
        AddressResponseVO responseVO = addressBusiness.updateAddress(farmId, addressId, requestVO);
        return addressMapper.toDTO(responseVO);
    }

    public AddressResponseDTO findAddressById(Long farmId, Long addressId) {
        AddressResponseVO responseVO = addressBusiness.findAddressById(farmId, addressId);
        return addressMapper.toDTO(responseVO);
    }

    public String deleteAddress(Long farmId, Long addressId) {
        return addressBusiness.deleteAddress(farmId, addressId);
    }


}
