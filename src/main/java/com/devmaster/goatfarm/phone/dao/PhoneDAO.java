package com.devmaster.goatfarm.phone.dao;

import com.devmaster.goatfarm.config.exceptions.custom.DatabaseException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;
import com.devmaster.goatfarm.phone.converter.PhoneEntityConverter;
import com.devmaster.goatfarm.phone.model.entity.Phone;
import com.devmaster.goatfarm.phone.model.repository.PhoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PhoneDAO {

    @Autowired
    private PhoneRepository phoneRepository;

    @Transactional
    public PhoneResponseVO createPhone(PhoneRequestVO requestVO) {
        if (requestVO == null) {
            throw new IllegalArgumentException("Os dados do telefone para criação não podem ser nulos.");
        }
        Phone phone = PhoneEntityConverter.toEntity(requestVO);
        try {
            phone = phoneRepository.save(phone);
            return PhoneEntityConverter.toVO(phone);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Erro ao salvar o telefone: " + e.getMessage());
        }
    }

    @Transactional
    public PhoneResponseVO updatePhone(Long id, PhoneRequestVO requestVO) {
        Phone phoneToUpdate = phoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Telefone com ID " + id + " não encontrado."));
        PhoneEntityConverter.toUpdateEntity(phoneToUpdate, requestVO);
        try {
            return PhoneEntityConverter.toVO(phoneRepository.save(phoneToUpdate));
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Erro ao atualizar o telefone com ID " + id + ": " + e.getMessage());
        }
    }

    @Transactional
    public PhoneResponseVO findPhoneById(Long id) {
        Phone phone = phoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Telefone com ID " + id + " não encontrado."));
        return PhoneEntityConverter.toVO(phone);
    }

    @Transactional
    public List<PhoneResponseVO> findAllPhones() {
        List<Phone> phones = phoneRepository.findAll();
        return phones.stream()
                .map(PhoneEntityConverter::toVO)
                .collect(Collectors.toList());
    }

    @Transactional
    public String deletePhone(Long id) {
        if (!phoneRepository.existsById(id)) {
            throw new ResourceNotFoundException("Telefone com ID " + id + " não encontrado.");
        }
        try {
            phoneRepository.deleteById(id);
            return "Telefone com ID " + id + " foi deletado com sucesso.";
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Não é possível deletar o telefone com ID " + id + " porque ele possui relacionamentos com outras entidades.");
        }
    }
}