package com.devmaster.goatfarm.goat.business.goatbusiness;

import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.farm.dao.GoatFarmDAO;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.dao.GoatDAO;
import com.devmaster.goatfarm.goat.mapper.GoatMapper;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import com.devmaster.goatfarm.authority.dao.UserDAO;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.genealogy.business.genealogyservice.GenealogyBusiness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class GoatBusiness {

    private final GoatDAO goatDAO;
    private final GoatFarmDAO goatFarmDAO;
    private final UserDAO userDAO;
    private final GenealogyBusiness genealogyBusiness;
    private final OwnershipService ownershipService;
    private final GoatMapper goatMapper;

    @Autowired
    public GoatBusiness(GoatDAO goatDAO, GoatFarmDAO goatFarmDAO, UserDAO userDAO,
                        GenealogyBusiness genealogyBusiness, OwnershipService ownershipService, GoatMapper goatMapper) {
        this.goatDAO = goatDAO;
        this.goatFarmDAO = goatFarmDAO;
        this.userDAO = userDAO;
        this.genealogyBusiness = genealogyBusiness;
        this.ownershipService = ownershipService;
        this.goatMapper = goatMapper;
    }

    @Transactional
    public GoatResponseVO createGoat(Long farmId, GoatRequestVO requestVO) {

        ownershipService.verifyFarmOwnership(farmId);

        if (requestVO.getRegistrationNumber() != null && goatDAO.existsById(requestVO.getRegistrationNumber())) {
            throw new DuplicateEntityException("Número de registro já existe.");
        }

        GoatFarm farm = goatFarmDAO.findFarmEntityById(farmId);
        Goat father = findOptionalGoat(requestVO.getFatherRegistrationNumber()).orElse(null);
        Goat mother = findOptionalGoat(requestVO.getMotherRegistrationNumber()).orElse(null);

        Goat goat = goatMapper.toEntity(requestVO);
        User user = ownershipService.getCurrentUser();
        goat.setUser(user);
        goat.setFarm(farm);
        goat.setFather(father);
        goat.setMother(mother);
        
        Goat savedGoat = goatDAO.save(goat);

        if (savedGoat.getRegistrationNumber() != null) {
            genealogyBusiness.createGenealogy(farmId, savedGoat.getRegistrationNumber());
        }

        return goatMapper.toResponseVO(savedGoat);
    }

    @Transactional
    public GoatResponseVO updateGoat(Long farmId, String goatId, GoatRequestVO requestVO) {
        ownershipService.verifyFarmOwnership(farmId);

        Goat goatToUpdate = goatDAO.findByIdAndFarmId(goatId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Cabra não encontrada nesta fazenda."));

        Goat father = findOptionalGoat(requestVO.getFatherRegistrationNumber()).orElse(null);
        Goat mother = findOptionalGoat(requestVO.getMotherRegistrationNumber()).orElse(null);

        goatMapper.updateEntity(goatToUpdate, requestVO, father, mother);
        
        Goat updatedGoat = goatDAO.save(goatToUpdate);
        return goatMapper.toResponseVO(updatedGoat);
    }

    @Transactional
    public void deleteGoat(Long farmId, String goatId) {
        ownershipService.verifyGoatOwnership(farmId, goatId);
        Goat goat = goatDAO.findByIdAndFarmId(goatId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Cabra não encontrada nesta fazenda."));
        goatDAO.delete(goat);
    }

    @Transactional(readOnly = true)
    public GoatResponseVO findGoatById(Long farmId, String goatId) {
        Goat goat = goatDAO.findByIdAndFarmId(goatId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Cabra não encontrada nesta fazenda."));
        return goatMapper.toResponseVO(goat);
    }

    @Transactional(readOnly = true)
    public Page<GoatResponseVO> findAllGoatsByFarm(Long farmId, Pageable pageable) {
        return goatDAO.findAllByFarmId(farmId, pageable).map(goatMapper::toResponseVO);
    }

    @Transactional(readOnly = true)
    public Page<GoatResponseVO> findGoatsByNameAndFarm(Long farmId, String name, Pageable pageable) {
        return goatDAO.findByNameAndFarmId(farmId, name, pageable).map(goatMapper::toResponseVO);
    }

    private Optional<Goat> findOptionalGoat(String registrationNumber) {
        if (registrationNumber == null) return Optional.empty();
        return goatDAO.findByRegistrationNumber(registrationNumber);
    }
}

