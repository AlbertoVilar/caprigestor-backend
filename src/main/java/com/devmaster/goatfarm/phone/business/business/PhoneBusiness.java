package com.devmaster.goatfarm.phone.business.business;

import com.devmaster.goatfarm.config.exceptions.custom.*;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
        long total = phonePort.countByFarmId(farmId);
        if (total <= 1) {
            ValidationError ve = new ValidationError(Instant.now(), 422, "Erro de validação");
            ve.addError("phones", "A fazenda deve possuir ao menos um telefone.");
            throw new ValidationException(ve);
        }
        phonePort.deleteById(phoneId);
    }

    /**
     * Sincroniza a lista de telefones da fazenda com a lista informada.
     * Atualiza existentes, cria novos e remove os ausentes.
     */
    @Transactional
    public void replacePhones(Long farmId, List<PhoneRequestVO> requests) {
        ownershipService.verifyFarmOwnership(farmId);
        if (requests == null || requests.isEmpty()) {
            ValidationError ve = new ValidationError(Instant.now(), 422, "Erro de validação");
            ve.addError("phones", "É obrigatório informar ao menos um telefone.");
            throw new ValidationException(ve);
        }

        GoatFarm farm = goatFarmPort.findById(farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada: " + farmId));

        List<Phone> existingPhones = phonePort.findAllByFarmId(farmId);
        Map<Long, Phone> existingById = new HashMap<>();
        for (Phone phone : existingPhones) {
            if (phone.getId() != null) {
                existingById.put(phone.getId(), phone);
            }
        }

        Set<Long> retainedIds = new HashSet<>();
        for (PhoneRequestVO requestVO : requests) {
            if (requestVO == null) {
                throw new IllegalArgumentException("Os dados do telefone para atualização não podem ser nulos.");
            }
            validatePhoneData(requestVO);

            Long requestId = requestVO.getId();
            if (requestId != null) {
                Phone current = existingById.get(requestId);
                if (current == null) {
                    throw new ResourceNotFoundException("Telefone com ID " + requestId + " não encontrado na fazenda " + farmId);
                }
                Optional<Phone> existing = phonePort.findByDddAndNumber(requestVO.getDdd(), requestVO.getNumber());
                if (existing.isPresent() && !existing.get().getId().equals(requestId)) {
                    throw new DatabaseException("Já existe um telefone com DDD (" + requestVO.getDdd() + ") e número " + requestVO.getNumber());
                }
                phoneMapper.updatePhone(current, requestVO);
                phonePort.save(current);
                retainedIds.add(requestId);
            } else {
                if (phonePort.existsByDddAndNumber(requestVO.getDdd(), requestVO.getNumber())) {
                    throw new DatabaseException("Já existe um telefone com DDD (" + requestVO.getDdd() + ") e número " + requestVO.getNumber());
                }
                Phone phone = phoneMapper.toEntity(requestVO);
                phone.setGoatFarm(farm);
                Phone saved = phonePort.save(phone);
                if (saved.getId() != null) {
                    retainedIds.add(saved.getId());
                }
            }
        }

        for (Phone phone : existingPhones) {
            if (phone.getId() != null && !retainedIds.contains(phone.getId())) {
                phonePort.deleteById(phone.getId());
            }
        }
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
