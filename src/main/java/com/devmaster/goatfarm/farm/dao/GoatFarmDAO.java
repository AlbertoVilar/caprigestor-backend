package com.devmaster.goatfarm.farm.dao;

import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.farm.mapper.GoatFarmMapper;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.farm.model.repository.GoatFarmRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.exceptions.custom.DatabaseException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

@Service
public class GoatFarmDAO {

    private static final Logger logger = LoggerFactory.getLogger(GoatFarmDAO.class);

    @Autowired
    private GoatFarmRepository goatFarmRepository;

    @Autowired
    private GoatFarmMapper goatFarmMapper;

    @Transactional
    public GoatFarmFullResponseVO createFullGoatFarm(GoatFarmRequestVO farmRequestVO,
                                                     com.devmaster.goatfarm.authority.business.bo.UserRequestVO userRequestVO,
                                                     AddressRequestVO addressRequestVO,
                                                     List<com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO> phoneRequestVOs) {
        throw new IllegalStateException("Operação movida para camada Business. Use GoatFarmBusiness.createFullGoatFarm.");
    }

    @Transactional(readOnly = true)
    public GoatFarm findFarmEntityById(Long id) {
        return goatFarmRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada com ID: " + id));
    }

    @Transactional(readOnly = true)
    public GoatFarmFullResponseVO findGoatFarmById(Long id) {
        GoatFarm farm = goatFarmRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fazenda não encontrada: " + id));
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
    public String deleteGoatFarm(Long id) {
        if (!goatFarmRepository.existsById(id)) {
            throw new ResourceNotFoundException("Fazenda com ID " + id + " não encontrada.");
        }
        try {
            goatFarmRepository.deleteById(id);
            return "Fazenda com ID " + id + " foi deletada com sucesso.";
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Não é possível deletar a fazenda com ID " + id + " porque ela possui relacionamentos com outras entidades.");
        }
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
    public void deleteGoatFarmsFromOtherUsers(Long adminId) {
        goatFarmRepository.deleteGoatFarmsFromOtherUsers(adminId);
    }
}
