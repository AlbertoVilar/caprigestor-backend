package com.devmaster.goatfarm.milk.business.lactationservice;

import com.devmaster.goatfarm.application.ports.in.LactationCommandUseCase;
import com.devmaster.goatfarm.application.ports.in.LactationQueryUseCase;
import com.devmaster.goatfarm.application.ports.out.LactationPersistencePort;
import com.devmaster.goatfarm.milk.business.bo.LactationRequestVO;
import com.devmaster.goatfarm.milk.business.bo.LactationResponseVO;
import com.devmaster.goatfarm.milk.mapper.LactationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.devmaster.goatfarm.config.exceptions.custom.ValidationException;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationError;
import com.devmaster.goatfarm.milk.enums.LactationStatus;
import com.devmaster.goatfarm.milk.model.entity.Lactation;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
public class LactationBusiness implements LactationCommandUseCase, LactationQueryUseCase {

    private final LactationPersistencePort lactationPersistencePort;
    private final LactationMapper lactationMapper;

    @Override
    public LactationResponseVO openLactation(Long farmId, String goatId, LactationRequestVO vo) {
        // Validar data futura
        if (vo.getStartDate() != null && vo.getStartDate().isAfter(LocalDate.now())) {
             throw new ValidationException(new ValidationError(Instant.now(), HttpStatus.BAD_REQUEST.value(), "Start date cannot be in the future."));
        }

        // Validar se já existe lactação ativa
        Optional<Lactation> activeLactation = lactationPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId);
        if (activeLactation.isPresent()) {
            throw new ValidationException(new ValidationError(Instant.now(), HttpStatus.BAD_REQUEST.value(), "There is already an active lactation for this goat."));
        }
        
        Lactation entity = new Lactation();
        entity.setFarmId(farmId);
        entity.setGoatId(goatId);
        entity.setStartDate(vo.getStartDate());
        entity.setStatus(LactationStatus.ACTIVE);
        entity.setEndDate(null);

        Lactation saved = lactationPersistencePort.save(entity);
        return lactationMapper.toResponseVO(saved);
    }

    @Override
    public LactationResponseVO getActiveLactation(Long farmId, String goatId) {
        return null;
    }

    @Override
    public void dryLactation(Long farmId, String goatId, Long lactationId) {
        // no-op (wiring only)
    }
}
