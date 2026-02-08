package com.devmaster.goatfarm.reproduction.business.reproductionservice;

import com.devmaster.goatfarm.reproduction.application.ports.in.ReproductionCommandUseCase;
import com.devmaster.goatfarm.reproduction.application.ports.in.ReproductionQueryUseCase;
import com.devmaster.goatfarm.reproduction.application.ports.out.PregnancyPersistencePort;
import com.devmaster.goatfarm.reproduction.application.ports.out.ReproductiveEventPersistencePort;
import com.devmaster.goatfarm.application.core.business.validation.GoatGenderValidator;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.reproduction.business.bo.BreedingRequestVO;
import com.devmaster.goatfarm.reproduction.business.bo.CoverageCorrectionRequestVO;
import com.devmaster.goatfarm.reproduction.business.bo.DiagnosisRecommendationCheckVO;
import com.devmaster.goatfarm.reproduction.business.bo.DiagnosisRecommendationCoverageVO;
import com.devmaster.goatfarm.reproduction.business.bo.DiagnosisRecommendationResponseVO;
import com.devmaster.goatfarm.reproduction.business.bo.PregnancyCheckRequestVO;
import com.devmaster.goatfarm.reproduction.business.bo.PregnancyCloseRequestVO;
import com.devmaster.goatfarm.reproduction.business.bo.PregnancyConfirmRequestVO;
import com.devmaster.goatfarm.reproduction.business.bo.PregnancyDiagnosisAlertVO;
import com.devmaster.goatfarm.reproduction.business.bo.PregnancyResponseVO;
import com.devmaster.goatfarm.reproduction.business.bo.ReproductiveEventResponseVO;
import com.devmaster.goatfarm.reproduction.enums.DiagnosisRecommendationStatus;
import com.devmaster.goatfarm.reproduction.enums.PregnancyCheckResult;
import com.devmaster.goatfarm.reproduction.enums.PregnancyCloseReason;
import com.devmaster.goatfarm.reproduction.enums.PregnancyStatus;
import com.devmaster.goatfarm.reproduction.enums.ReproductiveEventType;
import com.devmaster.goatfarm.reproduction.business.mapper.ReproductionBusinessMapper;
import com.devmaster.goatfarm.reproduction.persistence.entity.Pregnancy;
import com.devmaster.goatfarm.reproduction.persistence.entity.ReproductiveEvent;
import com.devmaster.goatfarm.reproduction.persistence.projection.PregnancyDiagnosisAlertProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReproductionBusiness implements ReproductionCommandUseCase, ReproductionQueryUseCase {

    private final PregnancyPersistencePort pregnancyPersistencePort;
    private final ReproductiveEventPersistencePort reproductiveEventPersistencePort;
    private final GoatGenderValidator goatGenderValidator;
    private final ReproductionBusinessMapper reproductionBusinessMapper;
    private final Clock clock;

    public ReproductionBusiness(PregnancyPersistencePort pregnancyPersistencePort,
                                ReproductiveEventPersistencePort reproductiveEventPersistencePort,
                                GoatGenderValidator goatGenderValidator,
                                ReproductionBusinessMapper reproductionBusinessMapper,
                                Clock clock) {
        this.pregnancyPersistencePort = pregnancyPersistencePort;
        this.reproductiveEventPersistencePort = reproductiveEventPersistencePort;
        this.goatGenderValidator = goatGenderValidator;
        this.reproductionBusinessMapper = reproductionBusinessMapper;
        this.clock = clock;
    }

    private static final int GESTATION_DAYS = 150;
    private static final int MIN_CONFIRMATION_DAYS = 60;
    private static final int DEFAULT_ALERT_PAGE_SIZE = 20;
    private static final String CONFIRMATION_MIN_DAYS_MESSAGE =
            "Diagnóstico de prenhez só pode ser registrado a partir de 60 dias após a última cobertura.";
    private static final String WARNING_ACTIVE_PREGNANCY_WITHOUT_VALID_CHECK = "GESTACAO_ATIVA_SEM_CHECK_VALIDO";
    private static final String BREEDING_BLOCKED_ACTIVE_PREGNANCY_MESSAGE =
            "Não é permitido registrar nova cobertura: existe uma gestação ativa para esta cabra. " +
                    "Encerre/corrija a gestação atual (ex.: falso positivo/aborto) para liberar novas coberturas.";

    @Override
    @Transactional
    public ReproductiveEventResponseVO registerBreeding(Long farmId, String goatId, BreedingRequestVO vo) {
        goatGenderValidator.requireFemale(farmId, goatId);
        if (vo.getEventDate() == null) {
            throw new InvalidArgumentException("eventDate", "Data do evento é obrigatória");
        }
        if (vo.getBreedingType() == null) {
            throw new InvalidArgumentException("breedingType", "Tipo de cobertura é obrigatório");
        }
        if (vo.getEventDate().isAfter(LocalDate.now(clock))) {
            throw new InvalidArgumentException("eventDate", "Data do evento não pode ser futura");
        }

        Optional<Pregnancy> activePregnancy = pregnancyPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId);
        if (activePregnancy.isPresent()) {
            LocalDate referenceDate = resolveActivePregnancyReferenceDate(farmId, goatId, activePregnancy.get());
            if (referenceDate == null || !vo.getEventDate().isBefore(referenceDate)) {
                throw new BusinessRuleException("eventDate", BREEDING_BLOCKED_ACTIVE_PREGNANCY_MESSAGE);
            }
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
        return reproductionBusinessMapper.toReproductiveEventResponseVO(savedEvent);
    }

    @Override
    @Transactional
    public ReproductiveEventResponseVO correctCoverage(Long farmId, String goatId, Long coverageEventId, CoverageCorrectionRequestVO vo) {
        goatGenderValidator.requireFemale(farmId, goatId);
        if (coverageEventId == null || coverageEventId <= 0) {
            throw new InvalidArgumentException("coverageEventId", "Identificador de cobertura inválido");
        }
        if (vo.getCorrectedDate() == null) {
            throw new InvalidArgumentException("correctedDate", "Data corrigida é obrigatória");
        }
        if (vo.getCorrectedDate().isAfter(LocalDate.now(clock))) {
            throw new InvalidArgumentException("correctedDate", "Data corrigida não pode ser futura");
        }

        ReproductiveEvent coverage = reproductiveEventPersistencePort
                .findByIdAndFarmIdAndGoatId(coverageEventId, farmId, goatId)
                .orElseThrow(() -> new ResourceNotFoundException("Cobertura não encontrada para o identificador informado: " + coverageEventId));

        if (coverage.getEventType() != ReproductiveEventType.COVERAGE) {
            throw new BusinessRuleException("coverageEventId", "Evento informado não é uma cobertura");
        }

        Optional<ReproductiveEvent> existingCorrection = reproductiveEventPersistencePort
                .findCoverageCorrectionByRelatedEventId(farmId, goatId, coverageEventId);
        if (existingCorrection.isPresent()) {
            throw new BusinessRuleException("coverageEventId", "Cobertura já possui correção registrada");
        }

        Optional<Pregnancy> linkedPregnancy = pregnancyPersistencePort
                .findByFarmIdAndCoverageEventId(farmId, coverageEventId);
        if (linkedPregnancy.isPresent()) {
            throw new BusinessRuleException("coverageEventId", "Não é possível corrigir uma cobertura associada a uma gestação");
        }

        ReproductiveEvent correctionEvent = ReproductiveEvent.builder()
                .farmId(farmId)
                .goatId(goatId)
                .eventType(ReproductiveEventType.COVERAGE_CORRECTION)
                .eventDate(LocalDate.now(clock))
                .relatedEventId(coverageEventId)
                .correctedEventDate(vo.getCorrectedDate())
                .notes(vo.getNotes())
                .build();

        ReproductiveEvent saved = reproductiveEventPersistencePort.save(correctionEvent);
        return reproductionBusinessMapper.toReproductiveEventResponseVO(saved);
    }

    @Override
    @Transactional(noRollbackFor = {InvalidArgumentException.class, DuplicateEntityException.class})
    public PregnancyResponseVO confirmPregnancy(Long farmId, String goatId, PregnancyConfirmRequestVO vo) {
        goatGenderValidator.requireFemale(farmId, goatId);
        if (vo.getCheckDate() == null) {
            throw new InvalidArgumentException("checkDate", "Data do exame de gestação é obrigatória");
        }
        if (vo.getCheckDate().isAfter(LocalDate.now(clock))) {
            throw new InvalidArgumentException("checkDate", "Data do exame de gestação não pode ser futura");
        }
        if (vo.getCheckResult() == null) {
            throw new InvalidArgumentException("checkResult", "Resultado do exame de gestação é obrigatório");
        }

        if (vo.getCheckResult() == PregnancyCheckResult.POSITIVE) {
            var activeList = pregnancyPersistencePort.findAllActiveByFarmIdAndGoatIdOrdered(farmId, goatId);
            if (activeList.size() > 1) {
                throw new DuplicateEntityException("status", "Foram encontradas múltiplas gestações ativas para a mesma cabra na fazenda");
            }
        }

        Optional<ReproductiveEvent> latestCoverage = reproductiveEventPersistencePort
                .findLatestEffectiveCoverageByFarmIdAndGoatIdOnOrBefore(farmId, goatId, vo.getCheckDate());

        if (latestCoverage.isEmpty()) {
            throw new InvalidArgumentException("checkDate", "Não foi encontrada cobertura anterior à data do exame de gestação");
        }

        ReproductiveEvent coverageEvent = latestCoverage.get();
        LocalDate breedingDate = resolveEffectiveCoverageDate(farmId, goatId, coverageEvent);
        if (vo.getCheckResult() == PregnancyCheckResult.POSITIVE) {
            if (vo.getCheckDate().isBefore(breedingDate.plusDays(MIN_CONFIRMATION_DAYS))) {
                throw new BusinessRuleException("checkDate", CONFIRMATION_MIN_DAYS_MESSAGE);
            }
            Optional<Pregnancy> activePregnancy = pregnancyPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId);
            if (activePregnancy.isPresent()) {
                throw new InvalidArgumentException("checkResult", "Já existe uma gestação ativa para esta cabra nesta fazenda");
            }
        }

        if (vo.getCheckResult() == PregnancyCheckResult.NEGATIVE) {
            throw new InvalidArgumentException("checkResult", "Resultado NEGATIVE não é permitido neste endpoint de confirmação. Utilize o endpoint de diagnóstico negativo.");
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
        
        // breedingDate already defined above
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
                .coverageEventId(coverageEvent.getId())
                .build();

        Pregnancy savedPregnancy = pregnancyPersistencePort.save(pregnancy);
        return reproductionBusinessMapper.toPregnancyResponseVO(savedPregnancy);
    }

    @Override
    @Transactional(noRollbackFor = {InvalidArgumentException.class, DuplicateEntityException.class})
    public ReproductiveEventResponseVO registerPregnancyCheck(Long farmId, String goatId, PregnancyCheckRequestVO vo) {
        goatGenderValidator.requireFemale(farmId, goatId);
        if (vo.getCheckDate() == null) {
            throw new InvalidArgumentException("checkDate", "Data do diagnóstico de prenhez é obrigatória");
        }
        if (vo.getCheckDate().isAfter(LocalDate.now(clock))) {
            throw new InvalidArgumentException("checkDate", "Data do diagnóstico de prenhez não pode ser futura");
        }
        if (vo.getCheckResult() == null) {
            throw new InvalidArgumentException("checkResult", "Resultado do diagnóstico de prenhez é obrigatório");
        }
        if (vo.getCheckResult() != PregnancyCheckResult.NEGATIVE) {
            throw new InvalidArgumentException("checkResult", "Resultado deve ser NEGATIVE neste endpoint de diagnóstico.");
        }

        Optional<ReproductiveEvent> latestCoverage = reproductiveEventPersistencePort
                .findLatestEffectiveCoverageByFarmIdAndGoatIdOnOrBefore(farmId, goatId, vo.getCheckDate());

        if (latestCoverage.isEmpty()) {
            throw new BusinessRuleException("checkDate",
                    "Não foi encontrada cobertura anterior à data do diagnóstico de prenhez");
        }

        ReproductiveEvent coverageEvent = latestCoverage.get();
        LocalDate breedingDate = resolveEffectiveCoverageDate(farmId, goatId, coverageEvent);
        if (vo.getCheckDate().isBefore(breedingDate.plusDays(MIN_CONFIRMATION_DAYS))) {
            throw new BusinessRuleException("checkDate", CONFIRMATION_MIN_DAYS_MESSAGE);
        }

        ReproductiveEvent checkEvent = ReproductiveEvent.builder()
                .farmId(farmId)
                .goatId(goatId)
                .eventType(ReproductiveEventType.PREGNANCY_CHECK)
                .eventDate(vo.getCheckDate())
                .checkResult(PregnancyCheckResult.NEGATIVE)
                .notes(vo.getNotes())
                .build();

        ReproductiveEvent savedCheckEvent = reproductiveEventPersistencePort.save(checkEvent);

        Optional<Pregnancy> activePregnancy = pregnancyPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId);
        if (activePregnancy.isPresent()) {
            Pregnancy pregnancy = activePregnancy.get();
            pregnancy.setStatus(PregnancyStatus.CLOSED);
            pregnancy.setClosedAt(vo.getCheckDate());
            pregnancy.setCloseReason(PregnancyCloseReason.FALSE_POSITIVE);
            if (vo.getNotes() != null && !vo.getNotes().isBlank()) {
                pregnancy.setNotes(vo.getNotes());
            }

            Pregnancy savedPregnancy = pregnancyPersistencePort.save(pregnancy);

            ReproductiveEvent closeEvent = ReproductiveEvent.builder()
                    .farmId(farmId)
                    .goatId(goatId)
                    .pregnancyId(savedPregnancy.getId())
                    .eventType(ReproductiveEventType.PREGNANCY_CLOSE)
                    .eventDate(vo.getCheckDate())
                    .notes(vo.getNotes())
                    .build();

            reproductiveEventPersistencePort.save(closeEvent);
        }

        return reproductionBusinessMapper.toReproductiveEventResponseVO(savedCheckEvent);
    }

    @Override
    @Transactional(noRollbackFor = {InvalidArgumentException.class, DuplicateEntityException.class})
    public PregnancyResponseVO closePregnancy(Long farmId, String goatId, Long pregnancyId, PregnancyCloseRequestVO vo) {
        goatGenderValidator.requireFemale(farmId, goatId);
        Pregnancy pregnancy = pregnancyPersistencePort.findByIdAndFarmIdAndGoatId(pregnancyId, farmId, goatId)
                .orElseThrow(() -> new ResourceNotFoundException("Gestação não encontrada para o identificador informado: " + pregnancyId));

        if (pregnancy.getStatus() != PregnancyStatus.ACTIVE) {
            throw new InvalidArgumentException("status", "Gestação não está ativa");
        }
        if (vo.getCloseDate() == null) {
            throw new InvalidArgumentException("closeDate", "Data de encerramento é obrigatória");
        }
        if (vo.getCloseReason() == null) {
            throw new InvalidArgumentException("closeReason", "Motivo de encerramento é obrigatório");
        }
        if (pregnancy.getBreedingDate() != null && vo.getCloseDate().isBefore(pregnancy.getBreedingDate())) {
            throw new InvalidArgumentException("closeDate", "Data de encerramento não pode ser anterior à data de cobertura");
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

        return reproductionBusinessMapper.toPregnancyResponseVO(savedPregnancy);
    }

    @Override
    public PregnancyResponseVO getActivePregnancy(Long farmId, String goatId) {
        goatGenderValidator.requireFemale(farmId, goatId);
        Pregnancy pregnancy = pregnancyPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId)
                .orElseThrow(() -> new ResourceNotFoundException("Nenhuma gestação ativa encontrada para esta cabra"));
        return reproductionBusinessMapper.toPregnancyResponseVO(pregnancy);
    }

    @Override
    public PregnancyResponseVO getPregnancyById(Long farmId, String goatId, Long pregnancyId) {
        goatGenderValidator.requireFemale(farmId, goatId);
        if (pregnancyId == null || pregnancyId <= 0) {
            throw new InvalidArgumentException("Identificador de gestação inválido");
        }
        Pregnancy pregnancy = pregnancyPersistencePort.findByIdAndFarmIdAndGoatId(pregnancyId, farmId, goatId)
                .orElseThrow(() -> new ResourceNotFoundException("Gestação não encontrada para o identificador informado: " + pregnancyId));
        return reproductionBusinessMapper.toPregnancyResponseVO(pregnancy);
    }

    @Override
    public Page<PregnancyResponseVO> getPregnancies(Long farmId, String goatId, Pageable pageable) {
        goatGenderValidator.requireFemale(farmId, goatId);
        Pageable stablePageable = withStableSort(pageable, Sort.by(Sort.Order.desc("breedingDate")));
        return pregnancyPersistencePort.findAllByFarmIdAndGoatId(farmId, goatId, stablePageable)
                .map(reproductionBusinessMapper::toPregnancyResponseVO);
    }

    @Override
    public Page<ReproductiveEventResponseVO> getReproductiveEvents(Long farmId, String goatId, Pageable pageable) {
        goatGenderValidator.requireFemale(farmId, goatId);
        Pageable stablePageable = withStableSort(pageable, Sort.by(Sort.Order.desc("eventDate")));
        return reproductiveEventPersistencePort.findAllByFarmIdAndGoatId(farmId, goatId, stablePageable)
                .map(reproductionBusinessMapper::toReproductiveEventResponseVO);
    }

    @Override
    @Transactional(readOnly = true)
    public DiagnosisRecommendationResponseVO getDiagnosisRecommendation(Long farmId, String goatId, LocalDate referenceDate) {
        goatGenderValidator.requireFemale(farmId, goatId);
        LocalDate reference = referenceDate != null ? referenceDate : LocalDate.now(clock);

        Optional<ReproductiveEvent> latestCoverage = reproductiveEventPersistencePort
                .findLatestEffectiveCoverageByFarmIdAndGoatIdOnOrBefore(farmId, goatId, reference);
        Optional<ReproductiveEvent> latestCheck = reproductiveEventPersistencePort
                .findLatestPregnancyCheckByFarmIdAndGoatIdOnOrBefore(farmId, goatId, reference);

        DiagnosisRecommendationStatus status = DiagnosisRecommendationStatus.NOT_ELIGIBLE;
        LocalDate eligibleDate = null;
        DiagnosisRecommendationCoverageVO lastCoverage = null;

        if (latestCoverage.isPresent()) {
            ReproductiveEvent coverage = latestCoverage.get();
            LocalDate effectiveCoverageDate = resolveEffectiveCoverageDate(farmId, goatId, coverage);
            eligibleDate = effectiveCoverageDate.plusDays(MIN_CONFIRMATION_DAYS);
            lastCoverage = toCoverageVO(coverage, effectiveCoverageDate);

            if (!reference.isBefore(eligibleDate)) {
                boolean hasValidCheck = isValidCheck(latestCheck.orElse(null), effectiveCoverageDate, eligibleDate);
                status = hasValidCheck ? DiagnosisRecommendationStatus.RESOLVED : DiagnosisRecommendationStatus.ELIGIBLE_PENDING;
            }
        }

        DiagnosisRecommendationCheckVO lastCheck = latestCheck
                .map(this::toCheckVO)
                .orElse(null);

        List<String> warnings = new ArrayList<>();
        Optional<Pregnancy> activePregnancy = pregnancyPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId);
        if (activePregnancy.isPresent()) {
            LocalDate effectiveCoverageDate = lastCoverage != null ? lastCoverage.getEffectiveDate() : null;
            boolean hasValidPositiveCheck = isValidPositiveCheck(latestCheck.orElse(null), effectiveCoverageDate, eligibleDate);
            if (!hasValidPositiveCheck) {
                warnings.add(WARNING_ACTIVE_PREGNANCY_WITHOUT_VALID_CHECK);
                status = DiagnosisRecommendationStatus.ELIGIBLE_PENDING;
            }
        }

        return DiagnosisRecommendationResponseVO.builder()
                .status(status)
                .eligibleDate(eligibleDate)
                .lastCoverage(lastCoverage)
                .lastCheck(lastCheck)
                .warnings(warnings)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PregnancyDiagnosisAlertVO> getPendingPregnancyDiagnosisAlerts(Long farmId, LocalDate referenceDate, Pageable pageable) {
        LocalDate reference = referenceDate != null ? referenceDate : LocalDate.now(clock);
        Pageable pageRequest = normalizeAlertsPageable(pageable);

        return reproductiveEventPersistencePort
                .findPendingPregnancyDiagnosisAlerts(farmId, reference, MIN_CONFIRMATION_DAYS, pageRequest)
                .map(projection -> toPregnancyDiagnosisAlertVO(projection, reference));
    }

    private DiagnosisRecommendationCoverageVO toCoverageVO(ReproductiveEvent coverage, LocalDate effectiveCoverageDate) {
        return DiagnosisRecommendationCoverageVO.builder()
                .id(coverage.getId())
                .eventDate(coverage.getEventDate())
                .effectiveDate(effectiveCoverageDate)
                .breedingType(coverage.getBreedingType())
                .breederRef(coverage.getBreederRef())
                .notes(coverage.getNotes())
                .build();
    }

    private DiagnosisRecommendationCheckVO toCheckVO(ReproductiveEvent checkEvent) {
        return DiagnosisRecommendationCheckVO.builder()
                .id(checkEvent.getId())
                .checkDate(checkEvent.getEventDate())
                .checkResult(checkEvent.getCheckResult())
                .notes(checkEvent.getNotes())
                .build();
    }

    private LocalDate resolveEffectiveCoverageDate(Long farmId, String goatId, ReproductiveEvent coverage) {
        Optional<ReproductiveEvent> correction = reproductiveEventPersistencePort
                .findCoverageCorrectionByRelatedEventId(farmId, goatId, coverage.getId());
        if (correction.isPresent() && correction.get().getCorrectedEventDate() != null) {
            return correction.get().getCorrectedEventDate();
        }
        return coverage.getEventDate();
    }

    private LocalDate resolveActivePregnancyReferenceDate(Long farmId, String goatId, Pregnancy pregnancy) {
        if (pregnancy.getCoverageEventId() != null) {
            Optional<ReproductiveEvent> coverage = reproductiveEventPersistencePort
                    .findByIdAndFarmIdAndGoatId(pregnancy.getCoverageEventId(), farmId, goatId);
            if (coverage.isPresent() && coverage.get().getEventType() == ReproductiveEventType.COVERAGE) {
                LocalDate effectiveCoverageDate = resolveEffectiveCoverageDate(farmId, goatId, coverage.get());
                if (effectiveCoverageDate != null) {
                    return effectiveCoverageDate;
                }
            }
        }
        return pregnancy.getBreedingDate();
    }

    private boolean isValidCheck(ReproductiveEvent checkEvent, LocalDate effectiveCoverageDate, LocalDate eligibleDate) {
        if (checkEvent == null || checkEvent.getEventDate() == null) {
            return false;
        }
        if (checkEvent.getCheckResult() == null || checkEvent.getCheckResult() == PregnancyCheckResult.PENDING) {
            return false;
        }
        if (effectiveCoverageDate != null && checkEvent.getEventDate().isBefore(effectiveCoverageDate)) {
            return false;
        }
        if (eligibleDate != null && checkEvent.getEventDate().isBefore(eligibleDate)) {
            return false;
        }
        return true;
    }

    private boolean isValidPositiveCheck(ReproductiveEvent checkEvent, LocalDate effectiveCoverageDate, LocalDate eligibleDate) {
        if (checkEvent == null || checkEvent.getEventDate() == null) {
            return false;
        }
        if (checkEvent.getCheckResult() != PregnancyCheckResult.POSITIVE) {
            return false;
        }
        if (effectiveCoverageDate != null && checkEvent.getEventDate().isBefore(effectiveCoverageDate)) {
            return false;
        }
        if (eligibleDate != null && checkEvent.getEventDate().isBefore(eligibleDate)) {
            return false;
        }
        return true;
    }

    private PregnancyDiagnosisAlertVO toPregnancyDiagnosisAlertVO(PregnancyDiagnosisAlertProjection projection, LocalDate referenceDate) {
        LocalDate eligibleDate = projection.getEligibleDate() != null
                ? projection.getEligibleDate()
                : projection.getLastCoverageDate().plusDays(MIN_CONFIRMATION_DAYS);

        long overdueDays = Math.max(0L, ChronoUnit.DAYS.between(eligibleDate, referenceDate));

        return PregnancyDiagnosisAlertVO.builder()
                .goatId(projection.getGoatId())
                .eligibleDate(eligibleDate)
                .daysOverdue((int) overdueDays)
                .lastCoverageDate(projection.getLastCoverageDate())
                .lastCheckDate(projection.getLastCheckDate())
                .build();
    }

    private Pageable normalizeAlertsPageable(Pageable pageable) {
        if (pageable == null) {
            return PageRequest.of(0, DEFAULT_ALERT_PAGE_SIZE);
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
    }

    private Pageable withStableSort(Pageable pageable, Sort defaultSort) {
        if (pageable == null) {
            return PageRequest.of(0, 20, defaultSort.and(Sort.by(Sort.Order.desc("id"))));
        }
        Sort sort = pageable.getSort();
        if (sort.isUnsorted()) {
            sort = defaultSort;
        }
        if (sort.getOrderFor("id") == null) {
            sort = sort.and(Sort.by(Sort.Order.desc("id")));
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
}
