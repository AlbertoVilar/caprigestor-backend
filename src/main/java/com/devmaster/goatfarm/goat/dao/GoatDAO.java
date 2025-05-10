package com.devmaster.goatfarm.goat.dao;

import com.devmaster.goatfarm.authority.api.dto.UserResponseDTO;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.config.exceptions.custom.DatabaseException;
import com.devmaster.goatfarm.config.exceptions.custom.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.farm.model.repository.GoatFarmRepository;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.converter.GoatEntityConverter;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import com.devmaster.goatfarm.goat.model.repository.GoatRepository;
import com.devmaster.goatfarm.owner.model.entity.Owner;
import com.devmaster.goatfarm.owner.model.repository.OwnerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoatDAO {

    @Autowired
    private GoatRepository goatRepository;

    @Autowired
    private GoatFarmRepository goatFarmRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Transactional
    public GoatResponseVO createGoat(GoatRequestVO requestVO, Long ownerId, Long farmId) {

        // Verify if this Id already exists
        if(goatRepository.existsById(requestVO.getRegistrationNumber())) {
            throw new DuplicateEntityException("Este registro "
                    + requestVO.getRegistrationNumber() + " já existe");
        }

        // Verification of father and mother existence
        Goat father = null;
        if (requestVO.getFatherRegistrationNumber() != null) {
            father = goatRepository.findById(requestVO.getFatherRegistrationNumber()).orElse(null);
        }

        Goat mother = null;
        if (requestVO.getMotherRegistrationNumber() != null) {
            mother = goatRepository.findById(requestVO.getMotherRegistrationNumber()).orElse(null);
        }

        // Verifying if the owner exists
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Elemento " + ownerId + " não encontrado."));

        // Verifying if the farm exists
        GoatFarm farm = goatFarmRepository.findById(farmId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Capril " + farmId + " não encontrado."));

        // Converting and saving the goat
        Goat goat = GoatEntityConverter.toEntity(requestVO, father, mother, owner, farm);
        goat = goatRepository.save(goat);

        return GoatEntityConverter.toResponseVO(goat);
    }

    @Transactional
    public GoatResponseVO updateGoat(String numRegistration, GoatRequestVO requestVO) {
        // Changed to getReferenceById to obtain the entity
        try {
            Goat goatToUpdate = goatRepository.getReferenceById(numRegistration);
            Goat father = null;
            Goat mother = null;
            GoatFarm farm = null;

            if (requestVO.getFatherRegistrationNumber() != null) {
                father = goatRepository.findById(requestVO.getFatherRegistrationNumber())
                        .orElse(null);
            }
            if (requestVO.getMotherRegistrationNumber() != null) {
                mother = goatRepository.findById(requestVO.getMotherRegistrationNumber())
                        .orElse(null);
            }
            if (requestVO.getFarmId() != null) {
                farm = goatFarmRepository.findById(requestVO.getFarmId())
                        .orElse(null);
            }

            // Updating the goat
            GoatEntityConverter.updateGoatEntity(goatToUpdate, requestVO, father, mother, farm);
            goatRepository.save(goatToUpdate);

            return GoatEntityConverter.toResponseVO(goatToUpdate);

        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException("Id " + numRegistration + " not found");
        }
    }

    @Transactional
    public GoatResponseVO findGoatById(String registrationNumber) {
        Goat goat = goatRepository.findByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Animal " + registrationNumber + " não encontrado."));

        return GoatEntityConverter.toResponseVO(goat);
    }

    @Transactional
    public Page<GoatResponseVO> searchGoatByName(String name, Pageable pageable) {
        Page<Goat> goatResult = goatRepository.searchGoatByName(name, pageable);

        return goatResult.map(GoatEntityConverter::toResponseVO);
    }

    @Transactional
    public Page<GoatResponseVO> findAllGoats(Pageable pageable) {
        Page<Goat> goatResult = goatRepository.findAll(pageable);

        return goatResult.map(GoatEntityConverter::toResponseVO);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void deleteGoat(String registrationNumber) {
        if (!goatRepository.existsById(registrationNumber)) {
            throw new ResourceNotFoundException("Registro " + registrationNumber + " não encontrado.");
        }
        try {
            goatRepository.deleteById(registrationNumber);

        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Animal com registro " + registrationNumber
                    + " não pode ser deletado, pois está referenciado por outro animal.");
        }
    }
}
