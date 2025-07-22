package com.devmaster.goatfarm.goat.dao;

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

@Service
public class GoatDAO {

    @Autowired
    private GoatRepository goatRepository;

    @Autowired
    private GoatFarmRepository goatFarmRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    /**
     * Cria uma nova cabra, associando-a ao proprietário e fazenda informados.
     */
    @Transactional
    public GoatResponseVO createGoat(GoatRequestVO requestVO, Long ownerId, Long farmId) {
        if(goatRepository.existsById(requestVO.getRegistrationNumber())) {
            throw new DuplicateEntityException("Este registro "
                    + requestVO.getRegistrationNumber() + " já existe");
        }

        Goat father = null;
        if (requestVO.getFatherRegistrationNumber() != null) {
            father = goatRepository.findById(requestVO.getFatherRegistrationNumber()).orElse(null);
        }

        Goat mother = null;
        if (requestVO.getMotherRegistrationNumber() != null) {
            mother = goatRepository.findById(requestVO.getMotherRegistrationNumber()).orElse(null);
        }

        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Elemento " + ownerId + " não encontrado."));

        GoatFarm farm = goatFarmRepository.findById(farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Capril " + farmId + " não encontrado."));

        Goat goat = GoatEntityConverter.toEntity(requestVO, father, mother, owner, farm);
        goat = goatRepository.save(goat);

        return GoatEntityConverter.toResponseVO(goat);
    }

    /**
     * Atualiza os dados de uma cabra existente.
     */
    @Transactional
    public GoatResponseVO updateGoat(String numRegistration, GoatRequestVO requestVO) {
        try {
            Goat goatToUpdate = goatRepository.getReferenceById(numRegistration);

            Goat father = null;
            Goat mother = null;
            GoatFarm farm = null;

            if (requestVO.getFatherRegistrationNumber() != null) {
                father = goatRepository.findById(requestVO.getFatherRegistrationNumber()).orElse(null);
            }
            if (requestVO.getMotherRegistrationNumber() != null) {
                mother = goatRepository.findById(requestVO.getMotherRegistrationNumber()).orElse(null);
            }
            if (requestVO.getFarmId() != null) {
                farm = goatFarmRepository.findById(requestVO.getFarmId()).orElse(null);
            }

            GoatEntityConverter.updateGoatEntity(goatToUpdate, requestVO, father, mother, farm);
            goatRepository.save(goatToUpdate);

            return GoatEntityConverter.toResponseVO(goatToUpdate);

        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException("Id " + numRegistration + " não encontrado");
        }
    }

    /**
     * Busca uma cabra pelo número de registro (com joins para pai, mãe, fazenda e proprietário).
     */
    @Transactional
    public GoatResponseVO findGoatById(String registrationNumber) {
        Goat goat = goatRepository.findByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Animal " + registrationNumber + " não encontrado."));

        return GoatEntityConverter.toResponseVO(goat);
    }

    /**
     * Busca paginada de cabras por nome (sem considerar fazenda).
     */
    @Transactional
    public Page<GoatResponseVO> searchGoatByName(String name, Pageable pageable) {
        Page<Goat> goatResult = goatRepository.searchGoatByName(name, pageable);
        return goatResult.map(GoatEntityConverter::toResponseVO);
    }

    /**
     * Busca paginada de cabras por nome dentro de uma fazenda específica.
     */
    @Transactional
    public Page<GoatResponseVO> searchGoatByNameAndFarmId(Long farmId, String name, Pageable pageable) {
        Page<Goat> goatResult = goatRepository.findByNameAndFarmId(farmId, name, pageable);
        return goatResult.map(GoatEntityConverter::toResponseVO);
    }

    /**
     * Busca paginada de cabras por ID da fazenda e número de registro opcional.
     */
    @Transactional(readOnly = true)
    public Page<GoatResponseVO> findByFarmIdAndOptionalRegistrationNumber(
            Long farmId, String registrationNumber, Pageable pageable) {

        Page<Goat> goats = goatRepository.findByFarmIdAndOptionalRegistrationNumber(farmId, registrationNumber, pageable);

        if (goats.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Nenhuma cabra encontrada para a fazenda com ID " + farmId +
                            " e registro " + registrationNumber + "."
            );
        }

        return goats.map(GoatEntityConverter::toResponseVO);
    }

    /**
     * Lista todas as cabras cadastradas com paginação.
     */
    @Transactional
    public Page<GoatResponseVO> findAllGoats(Pageable pageable) {
        Page<Goat> goatResult = goatRepository.findAll(pageable);
        return goatResult.map(GoatEntityConverter::toResponseVO);
    }

    /**
     * Remove uma cabra do sistema, desde que não esteja vinculada a outra cabra.
     */
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
