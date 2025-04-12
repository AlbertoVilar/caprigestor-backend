package com.devmaster.goatfarm.goat.dao;

import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.farm.model.repository.GoatFarmRepository;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.converter.GoatEntityConverter;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import com.devmaster.goatfarm.goat.model.repository.GoatRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoatDAO {

    @Autowired
    private GoatRepository goatRepository;

    @Autowired
    private GoatFarmRepository goatFarmRepository;

    @Transactional
    public GoatResponseVO createGoat(GoatRequestVO requestVO, GoatFarmRequestVO goatFarmRequestVO) {

        // Não lançar exceção se pai ou mãe não existirem
        Goat father = null;
        if (requestVO.getFatherRegistrationNumber() != null) {
            father = goatRepository.findById(requestVO.getFatherRegistrationNumber()).orElse(null);
        }

        Goat mother = null;
        if (requestVO.getMotherRegistrationNumber() != null) {
            mother = goatRepository.findById(requestVO.getMotherRegistrationNumber()).orElse(null);
        }

        // Aqui sim é obrigatório
        GoatFarm farm = goatFarmRepository.findById(goatFarmRequestVO.getId())
                .orElseThrow(() -> new EntityNotFoundException("Goat farm not found"));

        Goat goat = GoatEntityConverter.toEntity(requestVO, father, mother, farm);

        goat = goatRepository.save(goat);

        return GoatEntityConverter.toResponseVO(goat);
    }

    public GoatResponseVO updateGoat(GoatRequestVO requestVO) {
        Goat goatToUpdate = goatRepository.getReferenceById(requestVO.getRegistrationNumber());

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

        GoatEntityConverter.updateGoatEntity(goatToUpdate, requestVO, father, mother, farm);

        goatRepository.save(goatToUpdate);
        return GoatEntityConverter.toResponseVO(goatToUpdate);
    }


    @Transactional
    public GoatResponseVO findGoatById(String registrationNumber) {
        Goat goat = goatRepository.findByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Goat not found with registration number: " + registrationNumber));

        return GoatEntityConverter.toResponseVO(goat);
    }

    @Transactional
    public List<GoatResponseVO> findAllGoats() {
        List<Goat> goatResult = goatRepository.findAll();

        return goatResult.stream().map(GoatEntityConverter::toResponseVO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteGoat(String registrationNumber) {
        if (!goatRepository.existsById(registrationNumber)) {
            throw new EntityNotFoundException("Cabra com registro " + registrationNumber + " não encontrada.");
        }

        goatRepository.deleteById(registrationNumber);
    }

}
