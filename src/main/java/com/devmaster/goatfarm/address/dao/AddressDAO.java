package com.devmaster.goatfarm.address.dao;

import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;
import com.devmaster.goatfarm.address.converter.AddressEntityConverter;
import com.devmaster.goatfarm.address.model.entity.Address;
import com.devmaster.goatfarm.address.model.repository.AdressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressDAO {

    @Autowired
    private AdressRepository adressRepository;

    public AddressResponseVO createAddress(AddressRequestVO requestVO) {
        if (requestVO != null) {
            Address address = AddressEntityConverter.toEntity(requestVO);
            address = adressRepository.save(address);
            return AddressEntityConverter.toVO(address);
        } else {
            return null;
        }
    }

    public AddressResponseVO updateAddress(Long id, AddressRequestVO requestVO) {
        Address address = adressRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found with id " + id));

        AddressEntityConverter.toUpdateEntity(address, requestVO);
        return AddressEntityConverter.toVO(adressRepository.save(address));
    }

    public AddressResponseVO findAddressById(Long id) {
        Address address = adressRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found with id " + id));
        return AddressEntityConverter.toVO(address);
    }

    public List<AddressResponseVO> findAllAddresses() {
        List<Address> result = adressRepository.findAll();
        return result.stream()
                .map(AddressEntityConverter::toVO)
                .collect(Collectors.toList());
    }

    public String deleteAddress(Long id) {
        if (!adressRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found with id " + id);
        }
        adressRepository.deleteById(id);
        return "Address with ID " + id + " was successfully deleted.";
    }
}
