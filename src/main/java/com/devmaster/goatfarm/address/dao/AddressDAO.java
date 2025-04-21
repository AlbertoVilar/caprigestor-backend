package com.devmaster.goatfarm.address.dao;

import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;
import com.devmaster.goatfarm.address.converter.AddressEntityConverter;
import com.devmaster.goatfarm.address.model.entity.Address;
import com.devmaster.goatfarm.address.model.repository.AdressRepository;
import com.devmaster.goatfarm.config.exceptions.custom.DatabaseException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressDAO {

    @Autowired
    private AdressRepository adressRepository;

    @Transactional
    public AddressResponseVO createAddress(AddressRequestVO requestVO) {
        if (requestVO == null) {
            throw new IllegalArgumentException("Os dados do endereço para criação não podem ser nulos.");
        }
        Address address = AddressEntityConverter.toEntity(requestVO);
        try {
            address = adressRepository.save(address);
            return AddressEntityConverter.toVO(address);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Erro ao salvar o endereço: " + e.getMessage());
        }
    }

    @Transactional
    public AddressResponseVO updateAddress(Long id, AddressRequestVO requestVO) {
        Address addressToUpdate = adressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço com ID " + id + " não encontrado."));
        AddressEntityConverter.toUpdateEntity(addressToUpdate, requestVO);
        try {
            return AddressEntityConverter.toVO(adressRepository.save(addressToUpdate));
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Erro ao atualizar o endereço com ID " + id + ": " + e.getMessage());
        }
    }

    @Transactional
    public AddressResponseVO findAddressById(Long id) {
        Address address = adressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço com ID " + id + " não encontrado."));
        return AddressEntityConverter.toVO(address);
    }

    @Transactional
    public List<AddressResponseVO> findAllAddresses() {
        List<Address> result = adressRepository.findAll();
        return result.stream()
                .map(AddressEntityConverter::toVO)
                .collect(Collectors.toList());
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
}