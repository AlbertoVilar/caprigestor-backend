package com.devmaster.goatfarm.farm.dao;

import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO;
import com.devmaster.goatfarm.farm.mapper.GoatFarmMapper;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.farm.model.repository.GoatFarmRepository;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class GoatFarmDAO {

    @Autowired
    private GoatFarmRepository goatFarmRepository;

    @Autowired
    private GoatFarmMapper goatFarmMapper;

    @Transactional(readOnly = true)
    public Optional<GoatFarm> findByIdAndUserId(Long id, Long userId) {
        return goatFarmRepository.findByIdAndUserId(id, userId);
    }

    @Transactional(readOnly = true)
    public Optional<GoatFarm> findByAddressId(Long addressId) {
        return goatFarmRepository.findByAddressId(addressId);
    }

    @Transactional(readOnly = true)
    public GoatFarm findFarmEntityById(Long id) {
        return goatFarmRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada com ID: " + id));
    }

    @Transactional(readOnly = true)
    public GoatFarmFullResponseVO findGoatFarmById(Long id) {
        GoatFarm farm = findFarmEntityById(id);
        return goatFarmMapper.toFullResponseVO(farm);
    }

    @Transactional(readOnly = true)
    public Page<GoatFarmFullResponseVO> searchGoatFarmByName(String name, Pageable pageable) {
        Page<GoatFarm> resultGoatFarms = goatFarmRepository.searchGoatFarmByName(name, pageable);
        return resultGoatFarms.map(goatFarmMapper::toFullResponseVO);
    }

    @Transactional(readOnly = true)
    public Page<GoatFarmFullResponseVO> findAllGoatFarm(Pageable pageable) {
        Page<GoatFarm> farms = goatFarmRepository.findAll(pageable);
        return farms.map(goatFarmMapper::toFullResponseVO);
    }

    @Transactional
    public void deleteGoatFarmsFromOtherUsers(Long adminId) {
        goatFarmRepository.deleteGoatFarmsFromOtherUsers(adminId);
    }

    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return goatFarmRepository.existsByName(name);
    }

    @Transactional(readOnly = true)
    public boolean existsByTod(String tod) {
        return goatFarmRepository.existsByTod(tod);
    }

    @Transactional
    public GoatFarm save(GoatFarm goatFarm) {
        return goatFarmRepository.save(goatFarm);
    }

    @Transactional
    public String deleteGoatFarm(Long id) {
        if (!goatFarmRepository.existsById(id)) {
            throw new ResourceNotFoundException("Fazenda com ID " + id + " não encontrada.");
        }
        goatFarmRepository.deleteById(id);
        return "Fazenda com ID " + id + " foi deletada com sucesso.";
    }
}
