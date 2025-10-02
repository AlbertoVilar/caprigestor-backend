package com.devmaster.goatfarm.phone.business.business;

import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;
import com.devmaster.goatfarm.phone.dao.PhoneDAO;
import com.devmaster.goatfarm.phone.mapper.PhoneMapper;
import com.devmaster.goatfarm.phone.model.entity.Phone;
import com.devmaster.goatfarm.phone.model.repository.PhoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PhoneBusiness {

    @Autowired
    private PhoneDAO phoneDAO;
    
    @Autowired
    private PhoneRepository phoneRepository;
    
    @Autowired
    private PhoneMapper phoneMapper;

    @Transactional
    public List<PhoneResponseVO> createPhones(List<PhoneRequestVO> requestVOs) {
        return requestVOs.stream()
                .map(requestVO -> phoneDAO.createPhone(requestVO, null))
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

        // Regra de negócio: verificar duplicação de telefone (apenas número)
        boolean exists = phoneRepository.existsByDddAndNumber(requestVO.getDdd(), requestVO.getNumber());
        if (exists) {
            throw new IllegalArgumentException("Já existe um telefone com este número cadastrado.");
        }

        // Usar o DAO para criar o telefone
        return phoneDAO.createPhone(requestVO, null);
    }

    @Transactional
    public PhoneResponseVO updatePhone(Long id, PhoneRequestVO requestVO) {
        // Validações de negócio
        validatePhoneData(requestVO);
        
        // Usar o DAO para atualizar o telefone
        return phoneDAO.updatePhone(id, requestVO);
    }

    public PhoneResponseVO findPhoneById(Long id) {
        return phoneDAO.findPhoneById(id);
    }

    @Transactional
    public String deletePhone(Long id) {
        return phoneDAO.deletePhone(id);
    }

    public List<PhoneResponseVO> findAllPhones() {
        return phoneDAO.findAllPhones();
    }

    private void validatePhoneData(PhoneRequestVO requestVO) {
        if (requestVO.getNumber() == null || requestVO.getNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Número do telefone é obrigatório");
        }
        
        if (requestVO.getDdd() == null || requestVO.getDdd().trim().isEmpty()) {
            throw new IllegalArgumentException("DDD é obrigatório");
        }
        
        // Validar formato do DDD (2 dígitos)
        if (!requestVO.getDdd().matches("\\d{2}")) {
            throw new IllegalArgumentException("DDD deve conter exatamente 2 dígitos");
        }
        
        // Validar formato do número (8 ou 9 dígitos)
        String number = requestVO.getNumber().replaceAll("[^0-9]", "");
        if (!number.matches("\\d{8,9}")) {
            throw new IllegalArgumentException("Número deve conter 8 ou 9 dígitos");
        }
    }
}
