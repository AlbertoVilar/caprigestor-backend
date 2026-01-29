package com.devmaster.goatfarm.address.persistence.adapter;

import com.devmaster.goatfarm.address.application.ports.out.AddressPersistencePort;
import com.devmaster.goatfarm.address.api.mapper.AddressMapper;
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
    public Address save(Address address) {
        return addressRepository.save(address);
    }

    @Override
    public Optional<Address> findById(Long id) {
        return addressRepository.findById(id);
    }

    @Override
    public Optional<Address> findByIdAndFarmId(Long addressId, Long farmId) {
        return addressRepository.findByIdAndFarmId(addressId, farmId);
    }

    @Override
    public void deleteById(Long id) {
        addressRepository.deleteById(id);
    }

    @Override
    public Optional<Address> searchExactAddress(String street, String neighborhood, String city, String state, String zipCode) {
        return addressRepository.searchExactAddress(street, neighborhood, city, state, zipCode);
    }

    @Override
    public void deleteAddressesFromOtherUsers(Long adminId) {
        addressRepository.deleteAddressesFromOtherUsers(adminId);
    }
}