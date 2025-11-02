package com.devmaster.goatfarm.phone.business.business;

import com.devmaster.goatfarm.config.exceptions.custom.DatabaseException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.farm.model.repository.GoatFarmRepository;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;
import com.devmaster.goatfarm.phone.mapper.PhoneMapper;
import com.devmaster.goatfarm.phone.model.entity.Phone;
import com.devmaster.goatfarm.phone.model.repository.PhoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PhoneBusiness {

    @Autowired
    private PhoneRepository phoneRepository;
    
    @Autowired
    private PhoneMapper phoneMapper;
    
    @Autowired
    private GoatFarmRepository goatFarmRepository;
    
    @Transactional
    public List<PhoneResponseVO> createPhones(List<PhoneRequestVO> requestVOs) {
        return requestVOs.stream()
                .map(requestVO -> createPhone(requestVO, null))
                .collect(Collectors.toList());
    }

    @Transactional
    public PhoneResponseVO createPhone(PhoneRequestVO requestVO, Long farmId) {
        // Validação de entrada
        if (requestVO == null) {
            throw new IllegalArgumentException("Os dados do telefone para criação não podem ser nulos.");
        }

        // Validações de negócio
        validatePhoneData(requestVO);

        // Regra de negócio: verificar duplicação de telefone (DDD + número)
        boolean exists = phoneRepository.existsByDddAndNumber(requestVO.getDdd(), requestVO.getNumber());
        if (exists) {
            throw new DatabaseException("Já existe um telefone com DDD (" + requestVO.getDdd() + ") e número " + requestVO.getNumber());
        }

        // Criar entidade
        Phone phone = phoneMapper.toEntity(requestVO);

        // Vincular fazenda (opcional)
        if (farmId != null) {
            GoatFarm farm = goatFarmRepository.findById(farmId)
                    .orElseThrow(() -> new ResourceNotFoundException("Fazenda com ID " + farmId + " não encontrada."));
            phone.setGoatFarm(farm);
        }

        // Persistir
        Phone saved = phoneRepository.save(phone);
        return phoneMapper.toResponseVO(saved);
    }

    @Transactional
    public PhoneResponseVO updatePhone(Long id, PhoneRequestVO requestVO) {
        // Validações de negócio
        validatePhoneData(requestVO);

        // Buscar existente
        Phone phoneToUpdate = phoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Telefone com ID " + id + " não encontrado."));

        // Checar duplicidade (DDD + número) contra outros registros
        Optional<Phone> existing = phoneRepository.findByDddAndNumber(requestVO.getDdd(), requestVO.getNumber());
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new DatabaseException("Já existe um telefone com DDD (" + requestVO.getDdd() + ") e número " + requestVO.getNumber());
        }

        // Aplicar dados
        phoneMapper.toEntity(phoneToUpdate, requestVO);

        try {
            Phone saved = phoneRepository.save(phoneToUpdate);
            return phoneMapper.toResponseVO(saved);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Erro ao atualizar o telefone com ID " + id + ": " + e.getMessage());
        }
    }

    public PhoneResponseVO findPhoneById(Long id) {
        Phone phone = phoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Telefone com ID " + id + " não encontrado."));
        return phoneMapper.toResponseVO(phone);
    }

    @Transactional
    public String deletePhone(Long id) {
        try {
            phoneRepository.deleteById(id);
            return "Telefone com ID " + id + " foi deletado com sucesso.";
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Não é possível deletar o telefone com ID " + id + " pois ele está vinculado a outra entidade.");
        }
    }

    public List<PhoneResponseVO> findAllPhones() {
        List<Phone> phones = phoneRepository.findAll();
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
        return phoneRepository.findByDddAndNumber(phoneVO.getDdd(), phoneVO.getNumber())
                .orElseGet(() -> {
                    Phone phone = new Phone();
                    phone.setDdd(phoneVO.getDdd());
                    phone.setNumber(phoneVO.getNumber());
                    return phoneRepository.save(phone);
                });
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
