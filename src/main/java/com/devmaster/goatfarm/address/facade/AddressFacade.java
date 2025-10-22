package com.devmaster.goatfarm.address.facade;

import com.devmaster.goatfarm.address.business.AddressBusiness;
import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressFacade {

    @Autowired
    private AddressBusiness addressBusiness;

    public AddressResponseVO createAddress(AddressRequestVO requestVO) {
        return addressBusiness.createAddress(requestVO);
    }

    public AddressResponseVO updateAddress(Long id, AddressRequestVO requestVO) {
        return addressBusiness.updateAddress(id, requestVO);
    }

    public AddressResponseVO findAddressById(Long id) {
        return addressBusiness.findAddressById(id);
    }

    public List<AddressResponseVO> findAllAddresses() {
        return addressBusiness.findAllAddresses();
    }

    public String deleteAddress(Long id) {
        return addressBusiness.deleteAddress(id);
    }
}
