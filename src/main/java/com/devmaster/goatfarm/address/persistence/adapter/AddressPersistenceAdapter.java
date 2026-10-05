package com.devmaster.goatfarm.address.persistence.adapter;

import com.devmaster.goatfarm.address.application.ports.out.AddressPersistencePort;
import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;
import com.devmaster.goatfarm.address.persistence.entity.Address;
import com.devmaster.goatfarm.address.persistence.repository.AddressRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AddressPersistenceAdapter implements AddressPersistencePort {

    private final AddressRepository addressRepository;

    public AddressPersistenceAdapter(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Override
    public AddressResponseVO save(AddressResponseVO address) {
        Address entity = address.getId() == null
                ? new Address()
                : addressRepository.findById(address.getId()).orElseGet(Address::new);
        entity.setStreet(address.getStreet());
        entity.setCity(address.getCity());
        entity.setNeighborhood(address.getNeighborhood());
        entity.setState(address.getState());
        entity.setZipCode(address.getZipCode());
        entity.setCountry(address.getCountry());
        return toResponse(addressRepository.save(entity));
    }

    @Override
    public Optional<AddressResponseVO> findById(Long id) {
        return addressRepository.findById(id).map(this::toResponse);
    }

    @Override
    public Optional<AddressResponseVO> findByIdAndFarmId(Long addressId, Long farmId) {
        return addressRepository.findByIdAndFarmId(addressId, farmId).map(this::toResponse);
    }

    @Override
    public void deleteById(Long id) {
        addressRepository.deleteById(id);
    }

    @Override
    public Optional<AddressResponseVO> searchExactAddress(String street, String neighborhood, String city, String state, String zipCode) {
        return addressRepository.searchExactAddress(street, neighborhood, city, state, zipCode).map(this::toResponse);
    }

    private AddressResponseVO toResponse(Address entity) {
        return new AddressResponseVO(entity.getId(), entity.getStreet(), entity.getCity(),
                entity.getNeighborhood(), entity.getState(), entity.getZipCode(), entity.getCountry());
    }

}
