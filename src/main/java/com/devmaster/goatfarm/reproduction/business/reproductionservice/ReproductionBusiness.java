package com.devmaster.goatfarm.reproduction.business.reproductionservice;

import com.devmaster.goatfarm.application.ports.in.ReproductionCommandUseCase;
import com.devmaster.goatfarm.application.ports.in.ReproductionQueryUseCase;
import com.devmaster.goatfarm.application.ports.out.PregnancyPersistencePort;
import com.devmaster.goatfarm.application.ports.out.ReproductiveEventPersistencePort;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class ReproductionBusiness implements ReproductionCommandUseCase, ReproductionQueryUseCase {

    private final PregnancyPersistencePort pregnancyPersistencePort;
    private final ReproductiveEventPersistencePort reproductiveEventPersistencePort;
    private final ReproductionMapper reproductionMapper;

    public ReproductionBusiness(PregnancyPersistencePort pregnancyPersistencePort, ReproductiveEventPersistencePort reproductiveEventPersistencePort, ReproductionMapper reproductionMapper) {
        this.pregnancyPersistencePort = pregnancyPersistencePort;
        this.reproductiveEventPersistencePort = reproductiveEventPersistencePort;
        this.reproductionMapper = reproductionMapper;
    }

    private static final int GESTATION_DAYS = 150;

    @Override
    @Transactional
    public ReproductiveEventResponseVO registerBreeding(Long farmId, String goatId, BreedingRequestVO vo) {
        if (vo.getEventDate() == null) {
            throw buildValidationException("eventDate", "Data do evento é obrigatória");
        }
        if (vo.getBreedingType() == null) {
            throw buildValidationException("breedingType", "Tipo de cobertura é obrigatório");
        }
        if (vo.getEventDate().isAfter(LocalDate.now())) {
            throw buildValidationException("eventDate", "Data do evento não pode ser futura");
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
    @Transactional(noRollbackFor = ValidationException.class)
    public PregnancyResponseVO confirmPregnancy(Long farmId, String goatId, PregnancyConfirmRequestVO vo) {
        if (vo.getCheckDate() == null) {
            throw buildValidationException("checkDate", "Data do exame de gestação é obrigatória");
        }
        if (vo.getCheckDate().isAfter(LocalDate.now())) {
            throw buildValidationException("checkDate", "Data do exame de gestação não pode ser futura");
        }
        if (vo.getCheckResult() == null) {
            throw buildValidationException("checkResult", "Resultado do exame de gestação é obrigatório");
        }

        if (vo.getCheckResult() == PregnancyCheckResult.POSITIVE) {
            var activeList = pregnancyPersistencePort.findAllActiveByFarmIdAndGoatIdOrdered(farmId, goatId);
            if (activeList.size() > 1) {
                throw buildValidationConflictException("status", "Foram encontradas múltiplas gestações ativas para a mesma cabra na fazenda");
            }
        }

        Optional<ReproductiveEvent> latestCoverage = reproductiveEventPersistencePort
                .findLatestCoverageByFarmIdAndGoatIdOnOrBefore(farmId, goatId, vo.getCheckDate());

        if (latestCoverage.isEmpty()) {
            throw buildValidationException("checkDate", "Não foi encontrada cobertura anterior à data do exame de gestação");
        }

        if (vo.getCheckResult() == PregnancyCheckResult.POSITIVE) {
            Optional<Pregnancy> activePregnancy = pregnancyPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId);
            if (activePregnancy.isPresent()) {
                throw buildValidationException("checkResult", "Já existe uma gestação ativa para esta cabra nesta fazenda");
            }
        }

        if (vo.getCheckResult() == PregnancyCheckResult.NEGATIVE) {
            throw buildValidationException("checkResult", "Resultado NEGATIVE não é permitido neste endpoint de confirmação. Utilize o fluxo específico de registro de exame de gestação quando disponível.");
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
                .orElseThrow(() -> new ResourceNotFoundException("Gestação não encontrada para o identificador informado: " + pregnancyId));

        if (pregnancy.getStatus() != PregnancyStatus.ACTIVE) {
            throw buildValidationException("status", "Gestação não está ativa");
        }
        if (vo.getCloseDate() == null) {
            throw buildValidationException("closeDate", "Data de encerramento é obrigatória");
        }
        if (vo.getCloseReason() == null) {
            throw buildValidationException("closeReason", "Motivo de encerramento é obrigatório");
        }
        if (pregnancy.getBreedingDate() != null && vo.getCloseDate().isBefore(pregnancy.getBreedingDate())) {
            throw buildValidationException("closeDate", "Data de encerramento não pode ser anterior à data de cobertura");
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
                .orElseThrow(() -> new ResourceNotFoundException("Nenhuma gestação ativa encontrada para esta cabra"));
        return reproductionMapper.toPregnancyResponseVO(pregnancy);
    }

    @Override
    public PregnancyResponseVO getPregnancyById(Long farmId, String goatId, Long pregnancyId) {
        if (pregnancyId == null || pregnancyId <= 0) {
            throw new InvalidArgumentException("Identificador de gestação inválido");
        }
        Pregnancy pregnancy = pregnancyPersistencePort.findByIdAndFarmIdAndGoatId(pregnancyId, farmId, goatId)
                .orElseThrow(() -> new ResourceNotFoundException("Gestação não encontrada para o identificador informado: " + pregnancyId));
        return reproductionMapper.toPregnancyResponseVO(pregnancy);
    }

    @Override
    public Page<PregnancyResponseVO> getPregnancies(Long farmId, String goatId, Pageable pageable) {
        return pregnancyPersistencePort.findAllByFarmIdAndGoatId(farmId, goatId, pageable)
                .map(reproductionMapper::toPregnancyResponseVO);
    }

    @Override
    public Page<ReproductiveEventResponseVO> getReproductiveEvents(Long farmId, String goatId, Pageable pageable) {
        return reproductiveEventPersistencePort.findAllByFarmIdAndGoatId(farmId, goatId, pageable)
                .map(reproductionMapper::toReproductiveEventResponseVO);
    }

    private ValidationException buildValidationException(String field, String message) {
        return buildValidationException(HttpStatus.BAD_REQUEST, field, message);
    }

    private ValidationException buildValidationConflictException(String field, String message) {
        return buildValidationException(HttpStatus.CONFLICT, field, message);
    }

    private ValidationException buildValidationException(HttpStatus status, String field, String message) {
        ValidationError error = new ValidationError(Instant.now(), status.value(), "Erro de validação");
        error.addError(field, message);
        return new ValidationException(error);
    }
}
