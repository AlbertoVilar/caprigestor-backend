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

      if(goatRepository.existsById(requestVO.getRegistrationNumber())) {
          throw new DuplicateEntityException("Este registro " + requestVO.getRegistrationNumber() +
                                            " já está cadastrado.");
      }
        // Verificação de existência de pai e mãe
        Goat father = null;
        if (requestVO.getFatherRegistrationNumber() != null) {
            father = goatRepository.findById(requestVO.getFatherRegistrationNumber()).orElse(null);
        }

        Goat mother = null;
        if (requestVO.getMotherRegistrationNumber() != null) {
            mother = goatRepository.findById(requestVO.getMotherRegistrationNumber()).orElse(null);
        }

        // Verificando se o proprietário existe
        Owner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Elemento com registro " + ownerId + " não encontrado."));

        // Verificando se a fazenda existe
        GoatFarm farm = goatFarmRepository.findById(farmId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Capril com registro " + farmId + " não encontrada."));

        // Convertendo e salvando a cabra
        Goat goat = GoatEntityConverter.toEntity(requestVO, father, mother, owner, farm);
        goat = goatRepository.save(goat);

        return GoatEntityConverter.toResponseVO(goat);
    }

    @Transactional
    public GoatResponseVO updateGoat(String numRegistration, GoatRequestVO requestVO) {
        // Troquei para getReferenceById para obter a entidade
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
            // Atualizando a cabra
            GoatEntityConverter.updateGoatEntity(goatToUpdate, requestVO, father, mother, farm);
            goatRepository.save(goatToUpdate);

            return GoatEntityConverter.toResponseVO(goatToUpdate);

        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException("Id " + numRegistration + " não encontrado");
        }

    }

    @Transactional
    public GoatResponseVO findGoatById(String registrationNumber) {
        Goat goat = goatRepository.findByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Animal com registro " + registrationNumber + " não encontrada."));

        return GoatEntityConverter.toResponseVO(goat);
    }

    @Transactional
    public List<GoatResponseVO> findAllGoats() {
        List<Goat> goatResult = goatRepository.findAll();

        return goatResult.stream().map(GoatEntityConverter::toResponseVO)
                .collect(Collectors.toList());
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void deleteGoat(String registrationNumber) {
        if (!goatRepository.existsById(registrationNumber)) {
            throw new ResourceNotFoundException("Númro de Registro " + registrationNumber + " não encontrado.");
        }
        try {
            goatRepository.deleteById(registrationNumber);

        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Não pode deletar animal com número de registro "
                    + registrationNumber + " porque ele é referenciado por outro animal.");
        }


    }
}
