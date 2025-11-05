package com.devmaster.goatfarm.goat.dao;

import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.mapper.GoatMapper;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.goat.model.repository.GoatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class GoatDAO {

    private final GoatRepository goatRepository;
    private final GoatMapper goatMapper;

    @Autowired
    public GoatDAO(GoatRepository goatRepository, GoatMapper goatMapper) {
        this.goatRepository = goatRepository;
        this.goatMapper = goatMapper;
    }

    @Transactional
    public Goat save(Goat goat) {
        return goatRepository.save(goat);
    }

    @Transactional(readOnly = true)
    public boolean existsById(String registrationNumber) {
        return goatRepository.existsById(registrationNumber);
    }

    @Transactional(readOnly = true)
    public Optional<Goat> findByRegistrationNumber(String registrationNumber) {
        return goatRepository.findByRegistrationNumber(registrationNumber);
    }

    @Transactional(readOnly = true)
    public Optional<Goat> findByIdAndFarmId(String id, Long farmId) {
        return goatRepository.findByIdAndFarmId(id, farmId);
    }

    @Transactional(readOnly = true)
    public Page<Goat> findAllByFarmId(Long farmId, Pageable pageable) {
        return goatRepository.findAllByFarmId(farmId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Goat> findByNameAndFarmId(Long farmId, String name, Pageable pageable) {
        return goatRepository.findByNameAndFarmId(farmId, name, pageable);
    }

    @Transactional
    public void delete(Goat goat) {
        goatRepository.delete(goat);
    }

    @Transactional
    public void deleteGoatsFromOtherUsers(Long adminId) {
        goatRepository.deleteGoatsFromOtherUsers(adminId);
    }
}
