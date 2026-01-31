package com.devmaster.goatfarm.goat.business;

import com.devmaster.goatfarm.application.core.business.common.EntityFinder;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.goat.api.dto.GoatRequestDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatResponseDTO;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.goat.api.mapper.GoatMapper;
import com.devmaster.goatfarm.goat.persistence.entity.Goat;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.goat.application.ports.in.GoatManagementUseCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class GoatBusiness implements GoatManagementUseCase {
    private final GoatPersistencePort goatPort;
    private final GoatFarmPersistencePort goatFarmPort;
    private final OwnershipService ownershipService;
    private final GoatMapper goatMapper;
    private final EntityFinder entityFinder;

    public GoatBusiness(GoatPersistencePort goatPort, GoatFarmPersistencePort goatFarmPort,
                        OwnershipService ownershipService, GoatMapper goatMapper, EntityFinder entityFinder) {
        this.goatPort = goatPort;
        this.goatFarmPort = goatFarmPort;
        this.ownershipService = ownershipService;
        this.goatMapper = goatMapper;
        this.entityFinder = entityFinder;
    }

    @Transactional
    public GoatResponseVO createGoat(Long farmId, GoatRequestVO requestVO) {
        ownershipService.verifyFarmOwnership(farmId);

        if (requestVO.getRegistrationNumber() != null && goatPort.existsByRegistrationNumber(requestVO.getRegistrationNumber())) {
            throw new DuplicateEntityException("Número de registro já existe.");
        }

        GoatFarm farm = entityFinder.findOrThrow(
                () -> goatFarmPort.findById(farmId),
                "Fazenda não encontrada."
        );
        Goat father = findOptionalGoat(requestVO.getFatherRegistrationNumber()).orElse(null);
        Goat mother = findOptionalGoat(requestVO.getMotherRegistrationNumber()).orElse(null);

        Goat goat = goatMapper.toEntity(requestVO);
        User user = ownershipService.getCurrentUser();
        goat.setUser(user);
        goat.setFarm(farm);
        goat.setFather(father);
        goat.setMother(mother);
        
        Goat savedGoat = goatPort.save(goat);

        return goatMapper.toResponseVO(savedGoat);
    }

    @Transactional
    public GoatResponseVO updateGoat(Long farmId, String goatId, GoatRequestVO requestVO) {
        ownershipService.verifyFarmOwnership(farmId);

        Goat goatToUpdate = entityFinder.findOrThrow(
                () -> goatPort.findByIdAndFarmId(goatId, farmId),
                "Cabra não encontrada nesta fazenda."
        );

        Goat father = findOptionalGoat(requestVO.getFatherRegistrationNumber()).orElse(null);
        Goat mother = findOptionalGoat(requestVO.getMotherRegistrationNumber()).orElse(null);

        goatMapper.updateEntity(goatToUpdate, requestVO, father, mother);
        
        Goat updatedGoat = goatPort.save(goatToUpdate);
        return goatMapper.toResponseVO(updatedGoat);
    }

    @Transactional
    public void deleteGoat(Long farmId, String goatId) {
        ownershipService.verifyGoatOwnership(farmId, goatId);
        entityFinder.findOrThrow(
                () -> goatPort.findByIdAndFarmId(goatId, farmId),
                "Cabra não encontrada nesta fazenda."
        );
        goatPort.deleteById(goatId);
    }

    @Transactional(readOnly = true)
    public GoatResponseVO findGoatById(Long farmId, String goatId) {
        Goat goat = entityFinder.findOrThrow(
                () -> goatPort.findByIdAndFarmId(goatId, farmId),
                "Cabra não encontrada nesta fazenda."
        );
        return goatMapper.toResponseVO(goat);
    }

    @Transactional(readOnly = true)
    public Page<GoatResponseVO> findAllGoatsByFarm(Long farmId, Pageable pageable) {
        return goatPort.findAllByFarmId(farmId, pageable).map(goatMapper::toResponseVO);
    }

    @Transactional(readOnly = true)
    public Page<GoatResponseVO> findGoatsByNameAndFarm(Long farmId, String name, Pageable pageable) {
        return goatPort.findByNameAndFarmId(farmId, name, pageable).map(goatMapper::toResponseVO);
    }

    private Optional<Goat> findOptionalGoat(String registrationNumber) {
        if (registrationNumber == null) return Optional.empty();
        return goatPort.findByRegistrationNumber(registrationNumber);
    }
}


