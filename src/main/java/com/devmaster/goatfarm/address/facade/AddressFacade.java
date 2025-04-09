package com.devmaster.goatfarm.address.facade;

import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;
import com.devmaster.goatfarm.address.dao.AddressDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressFacade {

    @Autowired
    private AddressDAO addressDAO;

    public AddressResponseVO createAddress(AddressRequestVO requestVO) {
        return addressDAO.createAddress(requestVO);
    }

    public AddressResponseVO updateAddress(Long id, AddressRequestVO requestVO) {
        return addressDAO.updateAddress(id, requestVO);
    }

    public AddressResponseVO findAddressById(Long id) {
        return addressDAO.findAddressById(id);
    }

    public List<AddressResponseVO> findAllAddresses() {
        return addressDAO.findAllAddresses();
    }

    public String deleteAddress(Long id) {
        return addressDAO.deleteAddress(id);
    }
}
