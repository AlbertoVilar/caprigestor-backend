package com.devmaster.goatfarm.phone.business.business;

import com.devmaster.goatfarm.config.exceptions.custom.*;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;
import com.devmaster.goatfarm.application.ports.out.PhonePersistencePort;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationError;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationException;
import com.devmaster.goatfarm.phone.mapper.PhoneMapper;
import com.devmaster.goatfarm.phone.model.entity.Phone;
import com.devmaster.goatfarm.config.security.OwnershipService;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PhoneBusiness implements com.devmaster.goatfarm.application.ports.in.PhoneManagementUseCase {

    private final PhonePersistencePort phonePort;
    private final PhoneMapper phoneMapper;
    private final GoatFarmPersistencePort goatFarmPort;
    private final OwnershipService ownershipService;

    public PhoneBusiness(PhonePersistencePort phonePort, PhoneMapper phoneMapper, @Lazy GoatFarmPersistencePort goatFarmPort, OwnershipService ownershipService) {
        this.phonePort = phonePort;
        this.phoneMapper = phoneMapper;
        this.goatFarmPort = goatFarmPort;
        this.ownershipService = ownershipService;
    }
    
    @Transactional
    public PhoneResponseVO createPhone(Long farmId, PhoneRequestVO requestVO) {
        ownershipService.verifyFarmOwnership(farmId);
        if (requestVO == null) {
            throw new IllegalArgumentException("Os dados do telefone para criação não podem ser nulos.");
        }

        validatePhoneData(requestVO);

        boolean exists = phonePort.existsByDddAndNumber(requestVO.getDdd(), requestVO.getNumber());
        if (exists) {
            throw new DatabaseException("Já existe um telefone com DDD (" + requestVO.getDdd() + ") e número " + requestVO.getNumber());
        }

        GoatFarm farm = goatFarmPort.findById(farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada: " + farmId));
        Phone phone = phoneMapper.toEntity(requestVO);
        phone.setGoatFarm(farm);
        
        Phone saved = phonePort.save(phone);
        return phoneMapper.toResponseVO(saved);
    }

    @Transactional
    public PhoneResponseVO updatePhone(Long farmId, Long phoneId, PhoneRequestVO requestVO) {
        ownershipService.verifyFarmOwnership(farmId);
        validatePhoneData(requestVO);

        Phone phoneToUpdate = phonePort.findByIdAndFarmId(phoneId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Telefone com ID " + phoneId + " não encontrado na fazenda " + farmId));

        Optional<Phone> existing = phonePort.findByDddAndNumber(requestVO.getDdd(), requestVO.getNumber());
        if (existing.isPresent() && !existing.get().getId().equals(phoneId)) {
            throw new DatabaseException("Já existe um telefone com DDD (" + requestVO.getDdd() + ") e número " + requestVO.getNumber());
        }

        phoneMapper.updatePhone(phoneToUpdate, requestVO);

        try {
            Phone saved = phonePort.save(phoneToUpdate);
            return phoneMapper.toResponseVO(saved);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Erro ao atualizar o telefone com ID " + phoneId + ": " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public PhoneResponseVO findPhoneById(Long farmId, Long phoneId) {
        ownershipService.verifyFarmOwnership(farmId);
        Phone phone = phonePort.findByIdAndFarmId(phoneId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Telefone com ID " + phoneId + " não encontrado na fazenda " + farmId));
        return phoneMapper.toResponseVO(phone);
    }

    @Transactional(readOnly = true)
    public List<PhoneResponseVO> findAllPhonesByFarm(Long farmId) {
        ownershipService.verifyFarmOwnership(farmId);
        return phonePort.findAllByFarmId(farmId).stream()
                .map(phoneMapper::toResponseVO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletePhone(Long farmId, Long phoneId) {
        ownershipService.verifyFarmOwnership(farmId);
        phonePort.findByIdAndFarmId(phoneId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Telefone com ID " + phoneId + " não encontrado na fazenda " + farmId));
        phonePort.deleteById(phoneId);
    }

    private void validatePhoneData(PhoneRequestVO requestVO) {
        ValidationError validationError = new ValidationError(Instant.now(), 422, "Erro de validação");

        if (requestVO.getNumber() == null || requestVO.getNumber().trim().isEmpty()) {
            validationError.addError("number", "Número do telefone é obrigatório");
        }
        if (requestVO.getDdd() == null || requestVO.getDdd().trim().isEmpty()) {
            validationError.addError("ddd", "DDD é obrigatório");
        }
        if (requestVO.getDdd() != null && !requestVO.getDdd().matches("\\d{2}")) {
            validationError.addError("ddd", "DDD deve conter exatamente 2 dígitos");
        }
        if (requestVO.getNumber() != null) {
            String number = requestVO.getNumber().replaceAll("[^0-9]", "");
            if (!number.matches("\\d{8,9}")) {
                validationError.addError("number", "Número deve conter 8 ou 9 dígitos");
            }
        }

        if (!validationError.getErrors().isEmpty()) {
            throw new ValidationException(validationError);
        }
    }

    /**
     * Cria múltiplos telefones associados à fazenda informada.
     * Este método é utilizado no fluxo de criação completa de fazenda e, por isso,
     * não verifica propriedade/autorização do usuário atual.
     */
    @Transactional
    public void createPhones(Long farmId, List<PhoneRequestVO> requests) {
        if (requests == null || requests.isEmpty()) {
            return;
        }

        GoatFarm farm = goatFarmPort.findById(farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada: " + farmId));

        for (PhoneRequestVO requestVO : requests) {
            if (requestVO == null) {
                throw new IllegalArgumentException("Os dados do telefone para criação não podem ser nulos.");
            }

            validatePhoneData(requestVO);

            boolean exists = phonePort.existsByDddAndNumber(requestVO.getDdd(), requestVO.getNumber());
            if (exists) {
                throw new DatabaseException("Já existe um telefone com DDD (" + requestVO.getDdd() + ") e número " + requestVO.getNumber());
            }

            Phone phone = phoneMapper.toEntity(requestVO);
            phone.setGoatFarm(farm);
            try {
                phonePort.save(phone);
            } catch (DataIntegrityViolationException e) {
                throw new DatabaseException("Erro ao salvar telefone: " + e.getMessage());
            }
        }
    }
}