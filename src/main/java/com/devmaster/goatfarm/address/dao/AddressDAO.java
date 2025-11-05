package com.devmaster.goatfarm.address.dao;

import com.devmaster.goatfarm.address.mapper.AddressMapper;
import com.devmaster.goatfarm.address.model.entity.Address;
import com.devmaster.goatfarm.address.model.repository.AddressRepository;
import com.devmaster.goatfarm.config.exceptions.custom.DatabaseException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AddressDAO {

    @Autowired
    private AddressRepository adressRepository;

    @Autowired
    private AddressMapper addressMapper;

    @Transactional
    public Address createAddress(Address address) {
        if (address == null) {
            throw new IllegalArgumentException("A entidade Address para criação não pode ser nula.");
        }
        try {
            return adressRepository.save(address);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Erro ao salvar o endereço: " + e.getMessage());
        }
    }

    @Transactional
    public Address updateAddress(Long id, Address address) {
        Address addressToUpdate = adressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço com ID " + id + " não encontrado."));
        addressToUpdate.setStreet(address.getStreet());
        addressToUpdate.setNeighborhood(address.getNeighborhood());
        addressToUpdate.setCity(address.getCity());
        addressToUpdate.setState(address.getState());
        addressToUpdate.setZipCode(address.getZipCode());
        addressToUpdate.setCountry(address.getCountry());
        try {
            return adressRepository.save(addressToUpdate);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Erro ao atualizar o endereço com ID " + id + ": " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Address findAddressById(Long id) {
        return adressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço com ID " + id + " não encontrado."));
    }

    @Transactional(readOnly = true)
    public Optional<Address> findByIdAndFarmId(Long addressId, Long farmId) {
        return adressRepository.findByIdAndFarmId(addressId, farmId);
    }



    @Transactional
    public String deleteAddress(Long id) {
        if (!adressRepository.existsById(id)) {
            throw new ResourceNotFoundException("Endereço com ID " + id + " não encontrado.");
        }
        try {
            adressRepository.deleteById(id);
            return "Endereço com ID " + id + " foi deletado com sucesso.";
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Não é possível deletar o endereço com ID " + id + " porque ele possui relacionamentos com outras entidades.");
        }
    }

    @Transactional(readOnly = true)
    public Optional<Address> searchExactAddress(String street,
                                                String neighborhood,
                                                String city,
                                                String state,
                                                String zipCode) {
        return adressRepository.searchExactAddress(street, neighborhood, city, state, zipCode);
    }

    @Transactional
    public void deleteAddressesFromOtherUsers(Long adminId) {
        adressRepository.deleteAddressesFromOtherUsers(adminId);
    }
}
