package com.devmaster.goatfarm.phone.dao;

import com.devmaster.goatfarm.config.exceptions.custom.DatabaseException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.farm.model.repository.GoatFarmRepository;
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

    @Autowired
    private GoatFarmRepository farmRepository;

    // ✅ Create or reuse a list of phones
    public List<Phone> findOrCreatePhones(List<PhoneRequestVO> phoneVOList) {
        return phoneVOList.stream()
                .map(this::findOrCreatePhone)
                .collect(Collectors.toList());
    }

    // ✅ Create or reuse a single phone
    public Phone findOrCreatePhone(PhoneRequestVO phoneVO) {
        return phoneRepository.findByDddAndNumber(phoneVO.getDdd(), phoneVO.getNumber())
                .orElseGet(() -> {
                    Phone phone = new Phone();
                    phone.setDdd(phoneVO.getDdd());
                    phone.setNumber(phoneVO.getNumber());
                    return phoneRepository.save(phone);
                });
    }

    @Transactional
    public PhoneResponseVO createPhone(PhoneRequestVO requestVO, Long goatFarmId) {
        if (requestVO == null) {
            throw new IllegalArgumentException("Os dados do telefone para criação não podem ser nulos.");
        }

        // Check phone duplication (Area Code + Number)
        boolean exists = phoneRepository.existsByDddAndNumber(requestVO.getDdd(), requestVO.getNumber());
        if (exists) {
            throw new DatabaseException("Já existe um telefone com este DDD e número cadastrado.");
        }

        // If goatFarmId is provided, try to find the farm
        GoatFarm capril = null;
        if (goatFarmId != null) {
            capril = farmRepository.findById(goatFarmId)
                    .orElseThrow(() -> new ResourceNotFoundException("Capril com ID " + goatFarmId + " não encontrado."));
        }

        // Convert VO to entity with or without associated farm
        Phone phone = PhoneEntityConverter.toEntity(requestVO, capril);

        // Salva e retorna
        phone = phoneRepository.save(phone);
        return PhoneEntityConverter.toVO(phone);
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

    @Transactional(readOnly = true)
    public PhoneResponseVO findPhoneById(Long id) {
        Phone phone = phoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Telefone com ID " + id + " não encontrado."));
        return PhoneEntityConverter.toVO(phone);
    }

    @Transactional(readOnly = true)
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
            throw new DatabaseException("Não é possível deletar o telefone com ID " + id + " pois ele está vinculado a outra entidade.");
        }
    }
}
