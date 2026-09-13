package com.devmaster.goatfarm.phone.business.phoneservice;

import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.phone.application.ports.in.PhoneManagementUseCase;
import com.devmaster.goatfarm.phone.application.ports.out.PhonePersistencePort;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PhoneBusiness implements PhoneManagementUseCase {
    private final PhonePersistencePort phonePort;
    private final FarmAuthorizationUseCase ownershipService;

    @Transactional
    public PhoneResponseVO createPhone(Long farmId, PhoneRequestVO requestVO) {
        ownershipService.verifyFarmOwnership(farmId); validatePhoneData(requestVO); ensureUnique(requestVO.getDdd(), requestVO.getNumber());
        return phonePort.save(farmId, requestVO);
    }
    @Transactional
    public PhoneResponseVO updatePhone(Long farmId, Long phoneId, PhoneRequestVO requestVO) {
        ownershipService.verifyFarmOwnership(farmId); validatePhoneData(requestVO);
        phonePort.findByIdAndFarmId(phoneId, farmId).orElseThrow(() -> new ResourceNotFoundException("Telefone com ID " + phoneId + " não encontrado na fazenda " + farmId));
        Optional<PhoneResponseVO> existing = phonePort.findByDddAndNumber(requestVO.getDdd(), requestVO.getNumber());
        if (existing.isPresent() && !existing.get().getId().equals(phoneId)) throw duplicate(requestVO);
        requestVO.setId(phoneId); return phonePort.save(farmId, requestVO);
    }
    @Transactional(readOnly = true)
    public PhoneResponseVO findPhoneById(Long farmId, Long phoneId) {
        ownershipService.verifyFarmOwnership(farmId);
        return phonePort.findByIdAndFarmId(phoneId, farmId).orElseThrow(() -> new ResourceNotFoundException("Telefone com ID " + phoneId + " não encontrado na fazenda " + farmId));
    }
    @Transactional(readOnly = true)
    public List<PhoneResponseVO> findAllPhonesByFarm(Long farmId) { ownershipService.verifyFarmOwnership(farmId); return phonePort.findAllByFarmId(farmId); }
    @Transactional
    public void deletePhone(Long farmId, Long phoneId) {
        ownershipService.verifyFarmOwnership(farmId);
        phonePort.findByIdAndFarmId(phoneId, farmId).orElseThrow(() -> new ResourceNotFoundException("Telefone com ID " + phoneId + " não encontrado na fazenda " + farmId));
        if (phonePort.countByFarmId(farmId) <= 1) throw new BusinessRuleException("A fazenda deve possuir ao menos um telefone.");
        phonePort.deleteById(phoneId);
    }
    @Transactional
    public void replacePhones(Long farmId, List<PhoneRequestVO> requests) {
        ownershipService.verifyFarmOwnership(farmId);
        if (requests == null || requests.isEmpty()) throw new BusinessRuleException("É obrigatório informar ao menos um telefone.");
        List<PhoneResponseVO> existingPhones = phonePort.findAllByFarmId(farmId);
        Map<Long, PhoneResponseVO> existingById = new HashMap<>(); for (PhoneResponseVO p : existingPhones) if (p.getId() != null) existingById.put(p.getId(), p);
        Set<Long> retainedIds = new HashSet<>(); Set<String> requestedNumbers = new HashSet<>();
        for (PhoneRequestVO request : requests) {
            validatePhoneData(request); String key = request.getDdd() + ":" + request.getNumber(); if (!requestedNumbers.add(key)) throw duplicate(request);
            Long id = request.getId();
            if (id != null) {
                if (!existingById.containsKey(id)) throw new ResourceNotFoundException("Telefone com ID " + id + " não encontrado na fazenda " + farmId);
                Optional<PhoneResponseVO> existing = phonePort.findByDddAndNumber(request.getDdd(), request.getNumber()); if (existing.isPresent() && !existing.get().getId().equals(id)) throw duplicate(request);
                phonePort.save(farmId, request); retainedIds.add(id);
            } else { ensureUnique(request.getDdd(), request.getNumber()); PhoneResponseVO saved = phonePort.save(farmId, request); if (saved.getId() != null) retainedIds.add(saved.getId()); }
        }
        for (PhoneResponseVO p : existingPhones) if (p.getId() != null && !retainedIds.contains(p.getId())) phonePort.deleteById(p.getId());
    }
    @Transactional
    public void createPhones(Long farmId, List<PhoneRequestVO> requests) {
        if (requests == null || requests.isEmpty()) return;
        for (PhoneRequestVO request : requests) { validatePhoneData(request); ensureUnique(request.getDdd(), request.getNumber()); phonePort.save(farmId, request); }
    }
    private void ensureUnique(String ddd, String number) { if (phonePort.existsByDddAndNumber(ddd, number)) throw duplicate(new PhoneRequestVO(null, ddd, number, null)); }
    private DuplicateEntityException duplicate(PhoneRequestVO request) { return new DuplicateEntityException("phone", "Já existe um telefone com DDD (" + request.getDdd() + ") e número " + request.getNumber()); }
    private void validatePhoneData(PhoneRequestVO request) {
        if (request == null) throw new InvalidArgumentException("request", "Os dados do telefone não podem ser nulos.");
        if (request.getNumber() == null || request.getNumber().trim().isEmpty()) throw new InvalidArgumentException("number", "Número do telefone é obrigatório");
        if (request.getDdd() == null || request.getDdd().trim().isEmpty()) throw new InvalidArgumentException("ddd", "DDD é obrigatório");
        if (!request.getDdd().matches("\\d{2}")) throw new InvalidArgumentException("ddd", "DDD deve conter exatamente 2 dígitos");
        if (!request.getNumber().replaceAll("[^0-9]", "").matches("\\d{8,9}")) throw new InvalidArgumentException("number", "Número deve conter 8 ou 9 dígitos");
    }
}
