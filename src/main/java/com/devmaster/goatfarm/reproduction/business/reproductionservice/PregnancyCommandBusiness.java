package com.devmaster.goatfarm.reproduction.business.reproductionservice;

import com.devmaster.goatfarm.application.core.business.validation.GoatGenderValidator;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.reproduction.application.ports.in.PregnancyCommandUseCase;
import com.devmaster.goatfarm.reproduction.application.ports.out.PregnancyPersistencePort;
import com.devmaster.goatfarm.reproduction.application.ports.out.ReproductiveEventPersistencePort;
import com.devmaster.goatfarm.reproduction.business.bo.*;
import com.devmaster.goatfarm.reproduction.business.mapper.ReproductionBusinessMapper;
import com.devmaster.goatfarm.reproduction.enums.PregnancyCheckResult;
import com.devmaster.goatfarm.reproduction.enums.PregnancyCloseReason;
import com.devmaster.goatfarm.reproduction.enums.PregnancyStatus;
import com.devmaster.goatfarm.reproduction.enums.ReproductiveEventType;
import com.devmaster.goatfarm.reproduction.persistence.entity.Pregnancy;
import com.devmaster.goatfarm.reproduction.persistence.entity.ReproductiveEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class PregnancyCommandBusiness implements PregnancyCommandUseCase {
    private static final String CONFIRMATION_MIN_DAYS_MESSAGE = "Diagnostico de prenhez so pode ser registrado a partir de 60 dias apos a ultima cobertura.";
    private static final String COVERAGE_ALREADY_CONSUMED_MESSAGE = "A cobertura selecionada ja foi utilizada em uma gestacao e nao pode ser reutilizada.";
    private static final String CLOSE_REASON_BIRTH_FORBIDDEN_MESSAGE = "Encerramento com motivo BIRTH e exclusivo do endpoint de parto.";
    private final PregnancyPersistencePort pregnancyPersistencePort;
    private final ReproductiveEventPersistencePort eventPersistencePort;
    private final GoatGenderValidator goatGenderValidator;
    private final ReproductionBusinessMapper mapper;
    private final EffectiveCoverageDateResolver coverageDateResolver;
    private final Clock clock;

    public PregnancyCommandBusiness(PregnancyPersistencePort pregnancyPersistencePort,
                                    ReproductiveEventPersistencePort eventPersistencePort,
                                    GoatGenderValidator goatGenderValidator,
                                    ReproductionBusinessMapper mapper,
                                    EffectiveCoverageDateResolver coverageDateResolver,
                                    Clock clock) {
        this.pregnancyPersistencePort = pregnancyPersistencePort;
        this.eventPersistencePort = eventPersistencePort;
        this.goatGenderValidator = goatGenderValidator;
        this.mapper = mapper;
        this.coverageDateResolver = coverageDateResolver;
        this.clock = clock;
    }

    @Override
    @Transactional(noRollbackFor = {InvalidArgumentException.class, DuplicateEntityException.class})
    public PregnancyResponseVO confirmPregnancy(Long farmId, String goatId, PregnancyConfirmRequestVO vo) {
        goatGenderValidator.requireFemaleAndActive(farmId, goatId);
        if (vo.getCheckDate() == null) throw new InvalidArgumentException("checkDate", "Data do exame de gestação é obrigatória");
        if (vo.getCheckDate().isAfter(LocalDate.now(clock))) throw new InvalidArgumentException("checkDate", "Data do exame de gestação não pode ser futura");
        if (vo.getCheckResult() == null) throw new InvalidArgumentException("checkResult", "Resultado do exame de gestação é obrigatório");
        if (vo.getCheckResult() == PregnancyCheckResult.POSITIVE) {
            var activeList = pregnancyPersistencePort.findAllActiveByFarmIdAndGoatIdOrdered(farmId, goatId);
            if (activeList.size() > 1) throw new DuplicateEntityException("status", "Foram encontradas múltiplas gestações ativas para a mesma cabra na fazenda");
        }
        Optional<ReproductiveEvent> latest = eventPersistencePort.findLatestEffectiveCoverageByFarmIdAndGoatIdOnOrBefore(farmId, goatId, vo.getCheckDate());
        if (latest.isEmpty()) throw new InvalidArgumentException("checkDate", "Não foi encontrada cobertura anterior à data do exame de gestação");
        ReproductiveEvent coverage = latest.get();
        LocalDate breedingDate = coverageDateResolver.resolve(farmId, goatId, coverage);
        if (vo.getCheckResult() == PregnancyCheckResult.POSITIVE) {
            if (pregnancyPersistencePort.existsByFarmIdAndCoverageEventId(farmId, coverage.getId())) throw new BusinessRuleException("checkDate", COVERAGE_ALREADY_CONSUMED_MESSAGE);
            if (vo.getCheckDate().isBefore(breedingDate.plusDays(ReproductionRules.MIN_CONFIRMATION_DAYS))) throw new BusinessRuleException("checkDate", CONFIRMATION_MIN_DAYS_MESSAGE);
            if (pregnancyPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId).isPresent()) throw new InvalidArgumentException("checkResult", "Já existe uma gestação ativa para esta cabra nesta fazenda");
        }
        if (vo.getCheckResult() == PregnancyCheckResult.NEGATIVE) throw new InvalidArgumentException("checkResult", "Resultado NEGATIVE não é permitido neste endpoint de confirmação. Utilize o endpoint de diagnóstico negativo.");
        ReproductiveEvent check = ReproductiveEvent.builder().farmId(farmId).goatId(goatId).eventType(ReproductiveEventType.PREGNANCY_CHECK)
                .eventDate(vo.getCheckDate()).checkResult(vo.getCheckResult()).notes(vo.getNotes()).build();
        eventPersistencePort.save(check);
        Pregnancy pregnancy = Pregnancy.builder().farmId(farmId).goatId(goatId).status(PregnancyStatus.ACTIVE)
                .breedingDate(breedingDate).confirmDate(vo.getCheckDate()).expectedDueDate(breedingDate.plusDays(ReproductionRules.GESTATION_DAYS))
                .closedAt(null).closeReason(null).notes(vo.getNotes()).coverageEventId(coverage.getId()).build();
        return mapper.toPregnancyResponseVO(pregnancyPersistencePort.save(pregnancy));
    }

    @Override
    @Transactional(noRollbackFor = {InvalidArgumentException.class, DuplicateEntityException.class})
    public ReproductiveEventResponseVO registerPregnancyCheck(Long farmId, String goatId, PregnancyCheckRequestVO vo) {
        goatGenderValidator.requireFemaleAndActive(farmId, goatId);
        if (vo.getCheckDate() == null) throw new InvalidArgumentException("checkDate", "Data do diagnóstico de prenhez é obrigatória");
        if (vo.getCheckDate().isAfter(LocalDate.now(clock))) throw new InvalidArgumentException("checkDate", "Data do diagnóstico de prenhez não pode ser futura");
        if (vo.getCheckResult() == null) throw new InvalidArgumentException("checkResult", "Resultado do diagnóstico de prenhez é obrigatório");
        if (vo.getCheckResult() != PregnancyCheckResult.NEGATIVE) throw new InvalidArgumentException("checkResult", "Resultado deve ser NEGATIVE neste endpoint de diagnóstico.");
        Optional<ReproductiveEvent> latest = eventPersistencePort.findLatestEffectiveCoverageByFarmIdAndGoatIdOnOrBefore(farmId, goatId, vo.getCheckDate());
        if (latest.isEmpty()) throw new BusinessRuleException("checkDate", "Não foi encontrada cobertura anterior à data do diagnóstico de prenhez");
        ReproductiveEvent coverage = latest.get();
        LocalDate breedingDate = coverageDateResolver.resolve(farmId, goatId, coverage);
        if (vo.getCheckDate().isBefore(breedingDate.plusDays(ReproductionRules.MIN_CONFIRMATION_DAYS))) throw new BusinessRuleException("checkDate", CONFIRMATION_MIN_DAYS_MESSAGE);
        ReproductiveEvent check = ReproductiveEvent.builder().farmId(farmId).goatId(goatId).eventType(ReproductiveEventType.PREGNANCY_CHECK)
                .eventDate(vo.getCheckDate()).checkResult(PregnancyCheckResult.NEGATIVE).notes(vo.getNotes()).build();
        ReproductiveEvent savedCheck = eventPersistencePort.save(check);
        Optional<Pregnancy> active = pregnancyPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId);
        if (active.isPresent()) {
            Pregnancy pregnancy = active.get(); pregnancy.setStatus(PregnancyStatus.CLOSED); pregnancy.setClosedAt(vo.getCheckDate()); pregnancy.setCloseReason(PregnancyCloseReason.FALSE_POSITIVE);
            if (vo.getNotes() != null && !vo.getNotes().isBlank()) pregnancy.setNotes(vo.getNotes());
            Pregnancy saved = pregnancyPersistencePort.save(pregnancy);
            eventPersistencePort.save(ReproductiveEvent.builder().farmId(farmId).goatId(goatId).pregnancyId(saved.getId())
                    .eventType(ReproductiveEventType.PREGNANCY_CLOSE).eventDate(vo.getCheckDate()).notes(vo.getNotes()).build());
        }
        return mapper.toReproductiveEventResponseVO(savedCheck);
    }

    @Override
    @Transactional(noRollbackFor = {InvalidArgumentException.class, DuplicateEntityException.class})
    public PregnancyResponseVO closePregnancy(Long farmId, String goatId, Long pregnancyId, PregnancyCloseRequestVO vo) {
        goatGenderValidator.requireFemaleAndActive(farmId, goatId);
        Pregnancy pregnancy = pregnancyPersistencePort.findByIdAndFarmIdAndGoatId(pregnancyId, farmId, goatId)
                .orElseThrow(() -> new ResourceNotFoundException("Gestação não encontrada para o identificador informado: " + pregnancyId));
        if (pregnancy.getStatus() != PregnancyStatus.ACTIVE) throw new InvalidArgumentException("status", "Gestação não está ativa");
        if (vo.getCloseDate() == null) throw new InvalidArgumentException("closeDate", "Data de encerramento é obrigatória");
        if (vo.getCloseDate().isAfter(LocalDate.now(clock))) throw new InvalidArgumentException("closeDate", "Data de encerramento nao pode ser futura");
        if (vo.getCloseReason() == null) throw new InvalidArgumentException("closeReason", "Motivo de encerramento é obrigatório");
        if (vo.getCloseReason() == PregnancyCloseReason.BIRTH) throw new BusinessRuleException("closeReason", CLOSE_REASON_BIRTH_FORBIDDEN_MESSAGE);
        if (pregnancy.getBreedingDate() != null && vo.getCloseDate().isBefore(pregnancy.getBreedingDate())) throw new InvalidArgumentException("closeDate", "Data de encerramento não pode ser anterior à data de cobertura");
        pregnancy.setStatus(PregnancyStatus.CLOSED); pregnancy.setClosedAt(vo.getCloseDate()); pregnancy.setCloseReason(vo.getCloseReason());
        Pregnancy saved = pregnancyPersistencePort.save(pregnancy);
        eventPersistencePort.save(ReproductiveEvent.builder().farmId(farmId).goatId(goatId).pregnancyId(pregnancy.getId())
                .eventType(ReproductiveEventType.PREGNANCY_CLOSE).eventDate(vo.getCloseDate()).notes(vo.getNotes()).build());
        return mapper.toPregnancyResponseVO(saved);
    }
}
