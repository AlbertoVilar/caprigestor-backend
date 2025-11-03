package com.devmaster.goatfarm.phone.business.business;

import com.devmaster.goatfarm.config.exceptions.custom.DatabaseException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.farm.business.farmbusiness.GoatFarmBusiness;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;
import com.devmaster.goatfarm.phone.dao.PhoneDAO;
import com.devmaster.goatfarm.phone.mapper.PhoneMapper;
import com.devmaster.goatfarm.phone.model.entity.Phone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PhoneBusiness {

    @Autowired
    private PhoneDAO phoneDAO;
    
    @Autowired
    private PhoneMapper phoneMapper;
    
    @Autowired
    @Lazy
    private GoatFarmBusiness goatFarmBusiness;
    
    @Transactional
    public List<PhoneResponseVO> createPhones(List<PhoneRequestVO> requestVOs) {
        return requestVOs.stream()
                .map(requestVO -> createPhone(requestVO, null))
                .collect(Collectors.toList());
    }

    @Transactional
    public PhoneResponseVO createPhone(PhoneRequestVO requestVO, Long farmId) {
        if (requestVO == null) {
            throw new IllegalArgumentException("Os dados do telefone para criação não podem ser nulos.");
        }

        validatePhoneData(requestVO);

        boolean exists = phoneDAO.existsByDddAndNumber(requestVO.getDdd(), requestVO.getNumber());
        if (exists) {
            throw new DatabaseException("Já existe um telefone com DDD (" + requestVO.getDdd() + ") e número " + requestVO.getNumber());
        }

        Phone phone = phoneMapper.toEntity(requestVO);

        if (farmId != null) {
            GoatFarm farm = goatFarmBusiness.getFarmEntityById(farmId);
            phone.setGoatFarm(farm);
        }

        Phone saved = phoneDAO.save(phone);
        return phoneMapper.toResponseVO(saved);
    }

    @Transactional
    public PhoneResponseVO updatePhone(Long id, PhoneRequestVO requestVO) {
        validatePhoneData(requestVO);

        Phone phoneToUpdate = phoneDAO.findById(id);

        Optional<Phone> existing = phoneDAO.findByDddAndNumber(requestVO.getDdd(), requestVO.getNumber());
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new DatabaseException("Já existe um telefone com DDD (" + requestVO.getDdd() + ") e número " + requestVO.getNumber());
        }

        phoneMapper.toEntity(phoneToUpdate, requestVO);

        try {
            Phone saved = phoneDAO.save(phoneToUpdate);
            return phoneMapper.toResponseVO(saved);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Erro ao atualizar o telefone com ID " + id + ": " + e.getMessage());
        }
    }

    public PhoneResponseVO findPhoneById(Long id) {
        Phone phone = phoneDAO.findById(id);
        return phoneMapper.toResponseVO(phone);
    }

    @Transactional
    public String deletePhone(Long id) {
        try {
            phoneDAO.deleteById(id);
            return "Telefone com ID " + id + " foi deletado com sucesso.";
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Não é possível deletar o telefone com ID " + id + " pois ele está vinculado a outra entidade.");
        }
    }

    public List<PhoneResponseVO> findAllPhones() {
        List<Phone> phones = phoneDAO.findAll();
        return phones.stream()
                .map(phoneMapper::toResponseVO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<Phone> findOrCreatePhones(List<PhoneRequestVO> phoneVOList) {
        return phoneVOList.stream()
                .map(this::findOrCreatePhone)
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public Phone findOrCreatePhone(PhoneRequestVO phoneVO) {
        return phoneDAO.findByDddAndNumber(phoneVO.getDdd(), phoneVO.getNumber())
                .orElseGet(() -> phoneDAO.save(phoneMapper.toEntity(phoneVO)));
    }

    @Transactional(readOnly = true)
    public boolean existsByDddAndNumber(String ddd, String number) {
        return phoneDAO.existsByDddAndNumber(ddd, number);
    }

    @Transactional(readOnly = true)
    public java.util.List<Phone> findAllEntitiesById(java.util.List<Long> ids) {
        return phoneDAO.findAllEntitiesById(ids);
    }

    @Transactional(readOnly = true)
    public Phone getPhoneEntityById(Long id) {
        return phoneDAO.findById(id);
    }

    @Transactional
    public void deletePhonesFromOtherUsers(Long adminId) {
        phoneDAO.deletePhonesFromOtherUsers(adminId);
    }

    private void validatePhoneData(PhoneRequestVO requestVO) {
        if (requestVO.getNumber() == null || requestVO.getNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Número do telefone é obrigatório");
        }
        if (requestVO.getDdd() == null || requestVO.getDdd().trim().isEmpty()) {
            throw new IllegalArgumentException("DDD é obrigatório");
        }
        if (!requestVO.getDdd().matches("\\d{2}")) {
            throw new IllegalArgumentException("DDD deve conter exatamente 2 dígitos");
        }
        String number = requestVO.getNumber().replaceAll("[^0-9]", "");
        if (!number.matches("\\d{8,9}")) {
            throw new IllegalArgumentException("Número deve conter 8 ou 9 dígitos");
        }
    }
}
