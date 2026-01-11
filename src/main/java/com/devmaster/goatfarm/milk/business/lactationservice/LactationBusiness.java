package com.devmaster.goatfarm.milk.business.lactationservice;

import com.devmaster.goatfarm.application.ports.in.LactationCommandUseCase;
import com.devmaster.goatfarm.application.ports.in.LactationQueryUseCase;
import com.devmaster.goatfarm.application.ports.out.LactationPersistencePort;
import com.devmaster.goatfarm.milk.business.bo.LactationRequestVO;
import com.devmaster.goatfarm.milk.business.bo.LactationResponseVO;
import com.devmaster.goatfarm.milk.business.bo.LactationDryRequestVO;
import com.devmaster.goatfarm.milk.mapper.LactationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
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
    public LactationResponseVO dryLactation(Long farmId, String goatId, Long lactationId, LactationDryRequestVO vo) {
        Lactation lactation = lactationPersistencePort.findByIdAndFarmIdAndGoatId(lactationId, farmId, goatId)
                .orElseThrow(() -> new ResourceNotFoundException("Lactation not found for this goat"));

        if (lactation.getStatus() != LactationStatus.ACTIVE) {
            throw new ValidationException(new ValidationError(Instant.now(), HttpStatus.BAD_REQUEST.value(), "Lactation is not active."));
        }

        if (vo.getEndDate() == null) {
            throw new ValidationException(new ValidationError(Instant.now(), HttpStatus.BAD_REQUEST.value(), "End date is required."));
        }

        if (vo.getEndDate().isBefore(lactation.getStartDate())) {
            throw new ValidationException(new ValidationError(Instant.now(), HttpStatus.BAD_REQUEST.value(), "End date cannot be before start date."));
        }

        lactation.setStatus(LactationStatus.CLOSED);
        lactation.setEndDate(vo.getEndDate());
        lactation.setDryStartDate(vo.getEndDate()); // Assumindo dryStartDate = endDate da lactação

        Lactation saved = lactationPersistencePort.save(lactation);
        return lactationMapper.toResponseVO(saved);
    }

    @Override
    public LactationResponseVO getActiveLactation(Long farmId, String goatId) {
        Lactation lactation = lactationPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId)
                .orElseThrow(() -> new ResourceNotFoundException("No active lactation found for this goat"));
        return lactationMapper.toResponseVO(lactation);
    }

    @Override
    public LactationResponseVO getLactationById(Long farmId, String goatId, Long lactationId) {
        Lactation lactation = lactationPersistencePort.findByIdAndFarmIdAndGoatId(lactationId, farmId, goatId)
                .orElseThrow(() -> new ResourceNotFoundException("Lactation not found"));
        return lactationMapper.toResponseVO(lactation);
    }

    @Override
    public Page<LactationResponseVO> getAllLactations(Long farmId, String goatId, Pageable pageable) {
        Page<Lactation> page = lactationPersistencePort.findAllByFarmIdAndGoatId(farmId, goatId, pageable);
        return page.map(lactationMapper::toResponseVO);
    }
}
