package com.devmaster.goatfarm.phone.dao;

import com.devmaster.goatfarm.config.exceptions.custom.DatabaseException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
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
public class PhoneDAO {

    @Autowired
    private PhoneRepository phoneRepository;

    @Autowired
    private PhoneMapper phoneMapper;

    @Transactional
    public Phone save(Phone phone) {
        return phoneRepository.save(phone);
    }

    @Transactional(readOnly = true)
    public Optional<Phone> findByDddAndNumber(String ddd, String number) {
        return phoneRepository.findByDddAndNumber(ddd, number);
    }

    @Transactional(readOnly = true)
    public Optional<Phone> findByIdAndFarmId(Long id, Long farmId) {
        return phoneRepository.findByIdAndGoatFarmId(id, farmId);
    }

    @Transactional(readOnly = true)
    public List<Phone> findAllByFarmId(Long farmId) {
        return phoneRepository.findAllByGoatFarmId(farmId);
    }

    @Transactional
    public PhoneResponseVO createPhone(PhoneRequestVO requestVO, GoatFarm capril) {
        Phone phone = phoneMapper.toEntity(requestVO);
        if (capril != null) {
            phone.setGoatFarm(capril);
        }
        phone = phoneRepository.save(phone);
        return phoneMapper.toResponseVO(phone);
    }

    @Transactional
    public PhoneResponseVO updatePhone(Long id, PhoneRequestVO requestVO) {
        Phone phoneToUpdate = phoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Telefone com ID " + id + " não encontrado."));

        phoneMapper.toEntity(phoneToUpdate, requestVO);
        try {
            Phone saved = phoneRepository.save(phoneToUpdate);
            return phoneMapper.toResponseVO(saved);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Erro ao atualizar o telefone com ID " + id + ": " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public PhoneResponseVO findPhoneById(Long id) {
        Phone phone = phoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Telefone com ID " + id + " não encontrado."));
        return phoneMapper.toResponseVO(phone);
    }

    @Transactional(readOnly = true)
    public List<PhoneResponseVO> findAllPhones() {
        List<Phone> phones = phoneRepository.findAll();
        return phones.stream()
                .map(phoneMapper::toResponseVO)
                .collect(Collectors.toList());
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

    @Transactional(readOnly = true)
    public boolean existsByDddAndNumber(String ddd, String number) {
        return phoneRepository.existsByDddAndNumber(ddd, number);
    }

    @Transactional(readOnly = true)
    public List<Phone> findAllEntitiesById(List<Long> ids) {
        return phoneRepository.findAllById(ids);
    }

    @Transactional
    public void deletePhonesFromOtherUsers(Long adminId) {
        phoneRepository.deletePhonesFromOtherUsers(adminId);
    }
}
