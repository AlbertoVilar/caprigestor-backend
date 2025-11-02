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

    // Injeção do mapper conforme solicitado, ainda que o DAO trabalhe apenas com entidades
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
        // Como a lógica de mapeamento é centralizada no Business/Mapper, o DAO apenas persiste.
        // Presume-se que 'address' já contenha os dados atualizados ou que o Business tenha aplicado o merge no entity.
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
    public List<Address> findAllAddresses() {
        return adressRepository.findAll();
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
}