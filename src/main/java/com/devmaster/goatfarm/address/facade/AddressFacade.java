package com.devmaster.goatfarm.address.facade;

import com.devmaster.goatfarm.address.business.AddressBusiness;
import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;
import com.devmaster.goatfarm.address.facade.dto.AddressFacadeResponseDTO;
import com.devmaster.goatfarm.address.facade.mapper.AddressFacadeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressFacade {

    @Autowired
    private AddressBusiness addressBusiness;
    
    @Autowired
    private AddressFacadeMapper facadeMapper;

    public AddressFacadeResponseDTO createAddress(AddressRequestVO requestVO) {
        AddressResponseVO responseVO = addressBusiness.createAddress(requestVO);
        return facadeMapper.toFacadeDTO(responseVO);
    }

    public AddressFacadeResponseDTO updateAddress(Long id, AddressRequestVO requestVO) {
        AddressResponseVO responseVO = addressBusiness.updateAddress(id, requestVO);
        return facadeMapper.toFacadeDTO(responseVO);
    }

    public AddressFacadeResponseDTO findAddressById(Long id) {
        AddressResponseVO responseVO = addressBusiness.findAddressById(id);
        return facadeMapper.toFacadeDTO(responseVO);
    }

    public List<AddressFacadeResponseDTO> findAllAddresses() {
        List<AddressResponseVO> responseVOs = addressBusiness.findAllAddresses();
        return facadeMapper.toFacadeDTOList(responseVOs);
    }

    public String deleteAddress(Long id) {
        return addressBusiness.deleteAddress(id);
    }
}
