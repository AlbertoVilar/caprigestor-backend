package com.devmaster.goatfarm.reproduction.business.reproductionservice;

import com.devmaster.goatfarm.application.core.business.validation.GoatGenderValidator;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.reproduction.application.ports.in.ReproductionQueryUseCase;
import com.devmaster.goatfarm.reproduction.application.ports.out.PregnancyPersistencePort;
import com.devmaster.goatfarm.reproduction.application.ports.out.ReproductiveEventPersistencePort;
import com.devmaster.goatfarm.reproduction.application.model.PregnancyDiagnosisAlertSnapshot;
import com.devmaster.goatfarm.reproduction.business.bo.*;
import com.devmaster.goatfarm.reproduction.business.mapper.ReproductionBusinessMapper;
import com.devmaster.goatfarm.reproduction.enums.*;
import com.devmaster.goatfarm.reproduction.domain.Pregnancy;
import com.devmaster.goatfarm.reproduction.domain.ReproductiveEvent;
import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;
import com.devmaster.goatfarm.application.pagination.SortDirection;
import com.devmaster.goatfarm.application.pagination.SortSpec;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class ReproductionQueryBusiness implements ReproductionQueryUseCase {
    private static final String WARNING_ACTIVE_PREGNANCY_WITHOUT_VALID_CHECK = "GESTACAO_ATIVA_SEM_CHECK_VALIDO";
    private final PregnancyPersistencePort pregnancyPersistencePort;
    private final ReproductiveEventPersistencePort eventPersistencePort;
    private final GoatGenderValidator goatGenderValidator;
    private final ReproductionBusinessMapper mapper;
    private final EffectiveCoverageDateResolver coverageDateResolver;
    private final Clock clock;

    public ReproductionQueryBusiness(PregnancyPersistencePort pregnancyPersistencePort, ReproductiveEventPersistencePort eventPersistencePort,
                                     GoatGenderValidator goatGenderValidator, ReproductionBusinessMapper mapper,
                                     EffectiveCoverageDateResolver coverageDateResolver, Clock clock) {
        this.pregnancyPersistencePort = pregnancyPersistencePort; this.eventPersistencePort = eventPersistencePort; this.goatGenderValidator = goatGenderValidator; this.mapper = mapper; this.coverageDateResolver = coverageDateResolver; this.clock = clock;
    }
    @Override public PregnancyResponseVO getActivePregnancy(Long farmId, String goatId) { goatGenderValidator.requireFemale(farmId, goatId); return mapper.toPregnancyResponseVO(pregnancyPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId).orElseThrow(() -> new ResourceNotFoundException("Nenhuma gestação ativa encontrada para esta cabra"))); }
    @Override public PregnancyResponseVO getPregnancyById(Long farmId, String goatId, Long pregnancyId) { goatGenderValidator.requireFemale(farmId, goatId); if (pregnancyId == null || pregnancyId <= 0) throw new InvalidArgumentException("Identificador de gestação inválido"); return mapper.toPregnancyResponseVO(pregnancyPersistencePort.findByIdAndFarmIdAndGoatId(pregnancyId, farmId, goatId).orElseThrow(() -> new ResourceNotFoundException("Gestação não encontrada para o identificador informado: " + pregnancyId))); }
    @Override public PageResult<PregnancyResponseVO> getPregnancies(Long farmId, String goatId, PageQuery pageQuery) { goatGenderValidator.requireFemale(farmId, goatId); return pregnancyPersistencePort.findAllByFarmIdAndGoatId(farmId, goatId, withStableSort(pageQuery, new SortSpec("breedingDate", SortDirection.DESC))).map(mapper::toPregnancyResponseVO); }
    @Override public PageResult<ReproductiveEventResponseVO> getReproductiveEvents(Long farmId, String goatId, PageQuery pageQuery) { goatGenderValidator.requireFemale(farmId, goatId); return eventPersistencePort.findAllByFarmIdAndGoatId(farmId, goatId, withStableSort(pageQuery, new SortSpec("eventDate", SortDirection.DESC))).map(mapper::toReproductiveEventResponseVO); }
    @Override @Transactional(readOnly = true)
    public DiagnosisRecommendationResponseVO getDiagnosisRecommendation(Long farmId, String goatId, LocalDate referenceDate) {
        goatGenderValidator.requireFemale(farmId, goatId); LocalDate reference = referenceDate != null ? referenceDate : LocalDate.now(clock);
        Optional<ReproductiveEvent> latestCoverage = eventPersistencePort.findLatestEffectiveCoverageByFarmIdAndGoatIdOnOrBefore(farmId, goatId, reference);
        Optional<ReproductiveEvent> latestCheck = eventPersistencePort.findLatestPregnancyCheckByFarmIdAndGoatIdOnOrBefore(farmId, goatId, reference);
        DiagnosisRecommendationStatus status = DiagnosisRecommendationStatus.NOT_ELIGIBLE; LocalDate eligibleDate = null; DiagnosisRecommendationCoverageVO lastCoverage = null;
        if (latestCoverage.isPresent()) { ReproductiveEvent coverage = latestCoverage.get(); LocalDate effective = coverageDateResolver.resolve(farmId, goatId, coverage); eligibleDate = effective.plusDays(ReproductionRules.MIN_CONFIRMATION_DAYS); lastCoverage = toCoverageVO(coverage, effective); if (!reference.isBefore(eligibleDate)) status = isValidCheck(latestCheck.orElse(null), effective, eligibleDate) ? DiagnosisRecommendationStatus.RESOLVED : DiagnosisRecommendationStatus.ELIGIBLE_PENDING; }
        DiagnosisRecommendationCheckVO lastCheck = latestCheck.map(this::toCheckVO).orElse(null); List<String> warnings = new ArrayList<>(); Optional<Pregnancy> active = pregnancyPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId);
        if (active.isPresent()) { LocalDate effective = lastCoverage != null ? lastCoverage.getEffectiveDate() : null; if (!isValidPositiveCheck(latestCheck.orElse(null), effective, eligibleDate)) { warnings.add(WARNING_ACTIVE_PREGNANCY_WITHOUT_VALID_CHECK); status = DiagnosisRecommendationStatus.ELIGIBLE_PENDING; } }
        return DiagnosisRecommendationResponseVO.builder().status(status).eligibleDate(eligibleDate).lastCoverage(lastCoverage).lastCheck(lastCheck).warnings(warnings).build();
    }
    @Override @Transactional(readOnly = true)
    public PageResult<PregnancyDiagnosisAlertVO> getPendingPregnancyDiagnosisAlerts(Long farmId, LocalDate referenceDate, PageQuery pageQuery) { LocalDate reference = referenceDate != null ? referenceDate : LocalDate.now(clock); PageQuery page = normalizeAlertsPageQuery(pageQuery); return eventPersistencePort.findPendingPregnancyDiagnosisAlerts(farmId, reference, ReproductionRules.MIN_CONFIRMATION_DAYS, page).map(p -> toPregnancyDiagnosisAlertVO(p, reference)); }
    @Override @Transactional(readOnly = true)
    public PageResult<PregnancyDueAlertVO> getPendingBirthAlerts(Long farmId, LocalDate referenceDate, PageQuery pageQuery) { LocalDate reference = referenceDate != null ? referenceDate : LocalDate.now(clock); return pregnancyPersistencePort.findActiveWithDueDateOnOrBefore(farmId, reference, normalizeAlertsPageQuery(pageQuery)).map(p -> toPregnancyDueAlertVO(p, reference)); }
    private DiagnosisRecommendationCoverageVO toCoverageVO(ReproductiveEvent c, LocalDate date) { return DiagnosisRecommendationCoverageVO.builder().id(c.getId()).eventDate(c.getEventDate()).effectiveDate(date).breedingType(c.getBreedingType()).breederRef(c.getBreederRef()).notes(c.getNotes()).build(); }
    private DiagnosisRecommendationCheckVO toCheckVO(ReproductiveEvent c) { return DiagnosisRecommendationCheckVO.builder().id(c.getId()).checkDate(c.getEventDate()).checkResult(c.getCheckResult()).notes(c.getNotes()).build(); }
    private boolean isValidCheck(ReproductiveEvent c, LocalDate effective, LocalDate eligible) { if (c == null || c.getEventDate() == null || c.getCheckResult() == null || c.getCheckResult() == PregnancyCheckResult.PENDING) return false; return (effective == null || !c.getEventDate().isBefore(effective)) && (eligible == null || !c.getEventDate().isBefore(eligible)); }
    private boolean isValidPositiveCheck(ReproductiveEvent c, LocalDate effective, LocalDate eligible) { if (c == null || c.getEventDate() == null || c.getCheckResult() != PregnancyCheckResult.POSITIVE) return false; return (effective == null || !c.getEventDate().isBefore(effective)) && (eligible == null || !c.getEventDate().isBefore(eligible)); }
    private PregnancyDiagnosisAlertVO toPregnancyDiagnosisAlertVO(PregnancyDiagnosisAlertSnapshot p, LocalDate reference) { LocalDate eligible = p.eligibleDate() != null ? p.eligibleDate() : p.lastCoverageDate().plusDays(ReproductionRules.MIN_CONFIRMATION_DAYS); long overdue = Math.max(0L, ChronoUnit.DAYS.between(eligible, reference)); return PregnancyDiagnosisAlertVO.builder().goatTechnicalId(p.goatTechnicalId()).goatId(p.goatId()).eligibleDate(eligible).daysOverdue((int) overdue).lastCoverageDate(p.lastCoverageDate()).lastCheckDate(p.lastCheckDate()).build(); }
    private PregnancyDueAlertVO toPregnancyDueAlertVO(Pregnancy p, LocalDate reference) { long overdue = Math.max(0L, ChronoUnit.DAYS.between(p.getExpectedDueDate(), reference)); return PregnancyDueAlertVO.builder().pregnancyId(p.getId()).goatId(p.getGoatId()).goatTechnicalId(p.getGoatTechnicalId()).expectedDueDate(p.getExpectedDueDate()).daysOverdue((int) overdue).build(); }
    private PageQuery normalizeAlertsPageQuery(PageQuery query) { return query == null ? new PageQuery(0, ReproductionRules.DEFAULT_ALERT_PAGE_SIZE, List.of()) : query; }
    private PageQuery withStableSort(PageQuery query, SortSpec defaultSort) {
        if (query == null) {
            return new PageQuery(0, 10, List.of(defaultSort, new SortSpec("id", SortDirection.DESC)));
        }
        List<SortSpec> sort = query.sort().isEmpty() ? List.of(defaultSort) : query.sort();
        if (sort.stream().noneMatch(spec -> "id".equals(spec.field()))) {
            sort = new ArrayList<>(sort);
            sort.add(new SortSpec("id", SortDirection.DESC));
        }
        return new PageQuery(query.page(), query.size(), sort);
    }
}
