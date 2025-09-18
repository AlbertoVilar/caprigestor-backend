package com.devmaster.goatfarm.phone.business.business;

import com.devmaster.goatfarm.config.exceptions.custom.DatabaseException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.farm.model.repository.GoatFarmRepository;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;
import com.devmaster.goatfarm.phone.dao.PhoneDAO;
import com.devmaster.goatfarm.phone.model.repository.PhoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PhoneBusiness {

    @Autowired
    private PhoneDAO phoneDAO;
    
    @Autowired
    private PhoneRepository phoneRepository;
    
    @Autowired
    private GoatFarmRepository goatFarmRepository;

    @Transactional
    public PhoneResponseVO createPhone(PhoneRequestVO requestVO, Long goatFarmId) {
        // Validação de entrada
        if (requestVO == null) {
            throw new IllegalArgumentException("Os dados do telefone para criação não podem ser nulos.");
        }

        // Validações de negócio
        validatePhoneData(requestVO);

        // Regra de negócio: verificar duplicação de telefone (DDD + Número)
        boolean exists = phoneRepository.existsByDddAndNumber(requestVO.getDdd(), requestVO.getNumber());
        if (exists) {
            throw new DatabaseException("Já existe um telefone com este DDD e número cadastrado.");
        }

        // Regra de negócio: buscar fazenda se fornecida
        GoatFarm capril = null;
        if (goatFarmId != null) {
            capril = goatFarmRepository.findById(goatFarmId)
                    .orElseThrow(() -> new ResourceNotFoundException("Capril com ID " + goatFarmId + " não encontrado."));
        }

        // Delegar operação CRUD para o DAO
        return phoneDAO.createPhone(requestVO, capril);
    }

    @Transactional
    public PhoneResponseVO updatePhone(Long id, PhoneRequestVO requestVO) {
        // Regra de negócio: verificar se o telefone existe
        if (!phoneRepository.existsById(id)) {
            throw new ResourceNotFoundException("Telefone com ID " + id + " não encontrado.");
        }
        
        // Validações de negócio
        validatePhoneData(requestVO);
        
        // Delegar operação CRUD para o DAO
        return phoneDAO.updatePhone(id, requestVO);
    }

    @Transactional(readOnly = true)
    public PhoneResponseVO findPhoneById(Long id) {
        return phoneDAO.findPhoneById(id);
    }

    @Transactional(readOnly = true)
    public List<PhoneResponseVO> findAllPhones() {
        return phoneDAO.findAllPhones();
    }

    @Transactional
    public String deletePhone(Long id) {
        // Regra de negócio: verificar se o telefone existe antes de deletar
        if (!phoneRepository.existsById(id)) {
            throw new ResourceNotFoundException("Telefone com ID " + id + " não encontrado.");
        }
        
        // Delegar operação CRUD para o DAO
        return phoneDAO.deletePhone(id);
    }

    private void validatePhoneData(PhoneRequestVO requestVO) {
        // Validar DDD brasileiro
        if (requestVO.getDdd() != null) {
            String[] dddsValidos = {"11", "12", "13", "14", "15", "16", "17", "18", "19", // SP
                                  "21", "22", "24", // RJ
                                  "27", "28", // ES
                                  "31", "32", "33", "34", "35", "37", "38", // MG
                                  "41", "42", "43", "44", "45", "46", // PR
                                  "47", "48", "49", // SC
                                  "51", "53", "54", "55", // RS
                                  "61", // DF
                                  "62", "64", // GO
                                  "63", // TO
                                  "65", "66", // MT
                                  "67", // MS
                                  "68", // AC
                                  "69", // RO
                                  "71", "73", "74", "75", "77", // BA
                                  "79", // SE
                                  "81", "87", // PE
                                  "82", // AL
                                  "83", // PB
                                  "84", // RN
                                  "85", "88", // CE
                                  "86", "89", // PI
                                  "91", "93", "94", // PA
                                  "92", "97", // AM
                                  "95", // RR
                                  "96", // AP
                                  "98", "99"}; // MA
            
            boolean dddValido = false;
            for (String ddd : dddsValidos) {
                if (ddd.equals(requestVO.getDdd().trim())) {
                    dddValido = true;
                    break;
                }
            }
            if (!dddValido) {
                throw new IllegalArgumentException("DDD inválido. Deve ser um DDD brasileiro válido");
            }
        }
        
        // Validar formato do número (8 ou 9 dígitos)
        if (requestVO.getNumber() != null) {
            String numero = requestVO.getNumber().trim();
            if (!numero.matches("^\\d{8,9}$")) {
                throw new IllegalArgumentException("Número deve ter 8 ou 9 dígitos numéricos");
            } else if (numero.length() == 9 && !numero.startsWith("9")) {
                throw new IllegalArgumentException("Números com 9 dígitos devem começar com 9 (celular)");
            }
        }
    }
}
