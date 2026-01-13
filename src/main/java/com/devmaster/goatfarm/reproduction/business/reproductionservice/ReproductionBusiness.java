package com.devmaster.goatfarm.reproduction.business.reproductionservice;

import com.devmaster.goatfarm.application.ports.in.ReproductionCommandUseCase;
import com.devmaster.goatfarm.application.ports.in.ReproductionQueryUseCase;
import com.devmaster.goatfarm.application.ports.out.PregnancyPersistencePort;
import com.devmaster.goatfarm.application.ports.out.ReproductiveEventPersistencePort;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationError;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationException;
import com.devmaster.goatfarm.reproduction.business.bo.BreedingRequestVO;
import com.devmaster.goatfarm.reproduction.business.bo.PregnancyCloseRequestVO;
import com.devmaster.goatfarm.reproduction.business.bo.PregnancyConfirmRequestVO;
import com.devmaster.goatfarm.reproduction.business.bo.PregnancyResponseVO;
import com.devmaster.goatfarm.reproduction.business.bo.ReproductiveEventResponseVO;
import com.devmaster.goatfarm.reproduction.enums.PregnancyCheckResult;
import com.devmaster.goatfarm.reproduction.enums.PregnancyStatus;
import com.devmaster.goatfarm.reproduction.enums.ReproductiveEventType;
import com.devmaster.goatfarm.reproduction.mapper.ReproductionMapper;
import com.devmaster.goatfarm.reproduction.model.entity.Pregnancy;
import com.devmaster.goatfarm.reproduction.model.entity.ReproductiveEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReproductionBusiness implements ReproductionCommandUseCase, ReproductionQueryUseCase {

    private final PregnancyPersistencePort pregnancyPersistencePort;
    private final ReproductiveEventPersistencePort reproductiveEventPersistencePort;
    private final ReproductionMapper reproductionMapper;

    private static final int GESTATION_DAYS = 150;

    @Override
    @Transactional
    public ReproductiveEventResponseVO registerBreeding(Long farmId, String goatId, BreedingRequestVO vo) {
        if (vo.getEventDate() == null) {
            throw buildValidationException("eventDate", "Event date is required");
        }
        if (vo.getBreedingType() == null) {
            throw buildValidationException("breedingType", "Breeding type is required");
        }
        if (vo.getEventDate().isAfter(LocalDate.now())) {
            throw buildValidationException("eventDate", "Event date cannot be in the future");
        }

        ReproductiveEvent event = ReproductiveEvent.builder()
                .farmId(farmId)
                .goatId(goatId)
                .eventType(ReproductiveEventType.COVERAGE)
                .eventDate(vo.getEventDate())
                .breedingType(vo.getBreedingType())
                .breederRef(vo.getBreederRef())
                .notes(vo.getNotes())
                .build();

        ReproductiveEvent savedEvent = reproductiveEventPersistencePort.save(event);
        return reproductionMapper.toReproductiveEventResponseVO(savedEvent);
    }

    @Override
    @Transactional
    public PregnancyResponseVO confirmPregnancy(Long farmId, String goatId, PregnancyConfirmRequestVO vo) {
        if (vo.getCheckDate() == null) {
            throw buildValidationException("checkDate", "Check date is required");
        }
        if (vo.getCheckDate().isAfter(LocalDate.now())) {
            throw buildValidationException("checkDate", "Check date cannot be in the future");
        }
        if (vo.getCheckResult() == null) {
            throw buildValidationException("checkResult", "Check result is required");
        }

        Optional<ReproductiveEvent> latestCoverage = reproductiveEventPersistencePort
                .findLatestCoverageByFarmIdAndGoatIdOnOrBefore(farmId, goatId, vo.getCheckDate());

        if (latestCoverage.isEmpty()) {
            throw buildValidationException("checkDate", "No coverage found before check date");
        }

        if (vo.getCheckResult() == PregnancyCheckResult.POSITIVE) {
            Optional<Pregnancy> activePregnancy = pregnancyPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId);
            if (activePregnancy.isPresent()) {
                throw buildValidationException("checkResult", "Active pregnancy already exists");
            }
        }

        ReproductiveEvent checkEvent = ReproductiveEvent.builder()
                .farmId(farmId)
                .goatId(goatId)
                .eventType(ReproductiveEventType.PREGNANCY_CHECK)
                .eventDate(vo.getCheckDate())
                .checkResult(vo.getCheckResult())
                .notes(vo.getNotes())
                .build();

        reproductiveEventPersistencePort.save(checkEvent);

        if (vo.getCheckResult() == PregnancyCheckResult.NEGATIVE) {
            throw buildValidationException("checkResult", "NEGATIVE is not allowed for confirm. Use pregnancy check registration endpoint in the future.");
        }

        LocalDate breedingDate = latestCoverage.get().getEventDate();
        LocalDate expectedDueDate = breedingDate.plusDays(GESTATION_DAYS); // Domain rule: gestation = 150 days

        Pregnancy pregnancy = Pregnancy.builder()
                .farmId(farmId)
                .goatId(goatId)
                .status(PregnancyStatus.ACTIVE)
                .breedingDate(breedingDate)
                .confirmDate(vo.getCheckDate())
                .expectedDueDate(expectedDueDate)
                .closedAt(null)
                .closeReason(null)
                .notes(vo.getNotes())
                .build();

        Pregnancy savedPregnancy = pregnancyPersistencePort.save(pregnancy);
        return reproductionMapper.toPregnancyResponseVO(savedPregnancy);
    }

    @Override
    @Transactional
    public PregnancyResponseVO closePregnancy(Long farmId, String goatId, Long pregnancyId, PregnancyCloseRequestVO vo) {
        Pregnancy pregnancy = pregnancyPersistencePort.findByIdAndFarmIdAndGoatId(pregnancyId, farmId, goatId)
                .orElseThrow(() -> new ResourceNotFoundException("Pregnancy not found with id: " + pregnancyId));

        if (pregnancy.getStatus() != PregnancyStatus.ACTIVE) {
            throw buildValidationException("status", "Pregnancy is not active");
        }
        if (vo.getCloseDate() == null) {
            throw buildValidationException("closeDate", "Close date is required");
        }
        if (vo.getCloseReason() == null) {
            throw buildValidationException("closeReason", "Close reason is required");
        }
        if (pregnancy.getBreedingDate() != null && vo.getCloseDate().isBefore(pregnancy.getBreedingDate())) {
            throw buildValidationException("closeDate", "Close date cannot be before breeding date");
        }

        pregnancy.setStatus(PregnancyStatus.CLOSED);
        pregnancy.setClosedAt(vo.getCloseDate());
        pregnancy.setCloseReason(vo.getCloseReason());

        Pregnancy savedPregnancy = pregnancyPersistencePort.save(pregnancy);

        ReproductiveEvent closeEvent = ReproductiveEvent.builder()
                .farmId(farmId)
                .goatId(goatId)
                .pregnancyId(pregnancy.getId())
                .eventType(ReproductiveEventType.PREGNANCY_CLOSE)
                .eventDate(vo.getCloseDate())
                .notes(vo.getNotes())
                .build();

        reproductiveEventPersistencePort.save(closeEvent);

        return reproductionMapper.toPregnancyResponseVO(savedPregnancy);
    }

    @Override
    public PregnancyResponseVO getActivePregnancy(Long farmId, String goatId) {
        Pregnancy pregnancy = pregnancyPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId)
                .orElseThrow(() -> new ResourceNotFoundException("Active pregnancy not found"));
        return reproductionMapper.toPregnancyResponseVO(pregnancy);
    }

    @Override
    public Page<PregnancyResponseVO> getPregnancies(Long farmId, String goatId, Pageable pageable) {
        return Page.empty();
    }

    @Override
    public Page<ReproductiveEventResponseVO> getReproductiveEvents(Long farmId, String goatId, Pageable pageable) {
        return Page.empty();
    }

    private ValidationException buildValidationException(String field, String message) {
        ValidationError error = new ValidationError(Instant.now(), HttpStatus.BAD_REQUEST.value(), "Validation error");
        error.addError(field, message);
        return new ValidationException(error);
    }
}
