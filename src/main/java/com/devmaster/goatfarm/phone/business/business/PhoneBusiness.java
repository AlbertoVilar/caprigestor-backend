package com.devmaster.goatfarm.phone.business.business;

import com.devmaster.goatfarm.config.exceptions.custom.DatabaseException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.farm.dao.GoatFarmDAO;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;
import com.devmaster.goatfarm.phone.dao.PhoneDAO;
import com.devmaster.goatfarm.phone.mapper.PhoneMapper;
import com.devmaster.goatfarm.phone.model.entity.Phone;
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
    private GoatFarmDAO goatFarmDAO;

    @Autowired
    private OwnershipService ownershipService;
    
    @Transactional
    public List<PhoneResponseVO> createPhones(Long farmId, List<PhoneRequestVO> requestVOs) {
        ownershipService.verifyFarmOwnership(farmId);
        return requestVOs.stream()
                .map(requestVO -> createPhone(farmId, requestVO))
                .collect(Collectors.toList());
    }

    @Transactional
    public PhoneResponseVO createPhone(Long farmId, PhoneRequestVO requestVO) {
        ownershipService.verifyFarmOwnership(farmId);
        if (requestVO == null) {
            throw new IllegalArgumentException("Os dados do telefone para criação não podem ser nulos.");
        }

        validatePhoneData(requestVO);

        boolean exists = phoneDAO.existsByDddAndNumber(requestVO.getDdd(), requestVO.getNumber());
        if (exists) {
            throw new DatabaseException("Já existe um telefone com DDD (" + requestVO.getDdd() + ") e número " + requestVO.getNumber());
        }

        GoatFarm farm = goatFarmDAO.findFarmEntityById(farmId);
        Phone phone = phoneMapper.toEntity(requestVO);
        phone.setGoatFarm(farm);
        
        Phone saved = phoneDAO.save(phone);
        return phoneMapper.toResponseVO(saved);
    }

    @Transactional
    public PhoneResponseVO updatePhone(Long farmId, Long phoneId, PhoneRequestVO requestVO) {
        ownershipService.verifyFarmOwnership(farmId);
        validatePhoneData(requestVO);

        Phone phoneToUpdate = phoneDAO.findByIdAndFarmId(phoneId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Telefone com ID " + phoneId + " não encontrado na fazenda " + farmId));

        Optional<Phone> existing = phoneDAO.findByDddAndNumber(requestVO.getDdd(), requestVO.getNumber());
        if (existing.isPresent() && !existing.get().getId().equals(phoneId)) {
            throw new DatabaseException("Já existe um telefone com DDD (" + requestVO.getDdd() + ") e número " + requestVO.getNumber());
        }

        phoneMapper.updatePhone(phoneToUpdate, requestVO);

        try {
            Phone saved = phoneDAO.save(phoneToUpdate);
            return phoneMapper.toResponseVO(saved);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Erro ao atualizar o telefone com ID " + phoneId + ": " + e.getMessage());
        }
    }

    public PhoneResponseVO findPhoneById(Long farmId, Long phoneId) {
        ownershipService.verifyFarmOwnership(farmId);
        Phone phone = phoneDAO.findByIdAndFarmId(phoneId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Telefone com ID " + phoneId + " não encontrado na fazenda " + farmId));
        return phoneMapper.toResponseVO(phone);
    }

    @Transactional
    public String deletePhone(Long farmId, Long phoneId) {
        ownershipService.verifyFarmOwnership(farmId);
        Phone phone = phoneDAO.findByIdAndFarmId(phoneId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Telefone com ID " + phoneId + " não encontrado na fazenda " + farmId));
        phoneDAO.deletePhone(phoneId);
        return "Telefone com ID " + phoneId + " foi deletado com sucesso.";
    }

    public List<PhoneResponseVO> findAllPhonesByFarm(Long farmId) {
        ownershipService.verifyFarmOwnership(farmId);
        List<Phone> phones = phoneDAO.findAllByFarmId(farmId);
        return phones.stream()
                .map(phoneMapper::toResponseVO)
                .collect(Collectors.toList());
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

    public boolean existsByDddAndNumber(String ddd, String number) {
        return phoneDAO.existsByDddAndNumber(ddd, number);
    }

    public List<Phone> findAllEntitiesById(List<Long> ids) {
        return phoneDAO.findAllEntitiesById(ids);
    }
}
