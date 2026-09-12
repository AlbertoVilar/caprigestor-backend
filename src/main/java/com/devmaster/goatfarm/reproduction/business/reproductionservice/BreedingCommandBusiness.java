package com.devmaster.goatfarm.reproduction.business.reproductionservice;

import com.devmaster.goatfarm.application.core.business.validation.GoatGenderValidator;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.reproduction.application.ports.in.BreedingCommandUseCase;
import com.devmaster.goatfarm.reproduction.application.ports.out.PregnancyPersistencePort;
import com.devmaster.goatfarm.reproduction.application.ports.out.ReproductiveEventPersistencePort;
import com.devmaster.goatfarm.reproduction.business.bo.BreedingRequestVO;
import com.devmaster.goatfarm.reproduction.business.bo.CoverageCorrectionRequestVO;
import com.devmaster.goatfarm.reproduction.business.bo.ReproductiveEventResponseVO;
import com.devmaster.goatfarm.reproduction.business.mapper.ReproductionBusinessMapper;
import com.devmaster.goatfarm.reproduction.enums.ReproductiveEventType;
import com.devmaster.goatfarm.reproduction.persistence.entity.Pregnancy;
import com.devmaster.goatfarm.reproduction.persistence.entity.ReproductiveEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class BreedingCommandBusiness implements BreedingCommandUseCase {
    private static final String ACTIVE_PREGNANCY_BLOCKS_OPERATION_MESSAGE =
            "Nao e permitido registrar nova cobertura quando existe gestacao ativa para esta cabra.";
    private static final String BREEDING_DATE_ON_OR_BEFORE_BIRTH_MESSAGE =
            "Nao e permitido registrar cobertura na mesma data ou antes de um parto ja registrado para esta cabra.";
    private static final String SAME_DAY_BREEDING_ALREADY_EXISTS_MESSAGE =
            "Já existe uma cobertura registrada para esta cabra hoje. Use a correção de cobertura se precisar ajustar o lançamento.";

    private final PregnancyPersistencePort pregnancyPersistencePort;
    private final ReproductiveEventPersistencePort eventPersistencePort;
    private final GoatGenderValidator goatGenderValidator;
    private final ReproductionBusinessMapper mapper;
    private final EffectiveCoverageDateResolver coverageDateResolver;
    private final Clock clock;

    public BreedingCommandBusiness(PregnancyPersistencePort pregnancyPersistencePort,
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
    @Transactional
    public ReproductiveEventResponseVO registerBreeding(Long farmId, String goatId, BreedingRequestVO vo) {
        goatGenderValidator.requireFemaleAndActive(farmId, goatId);
        if (vo.getEventDate() == null) throw new InvalidArgumentException("eventDate", "Data do evento é obrigatória");
        if (vo.getBreedingType() == null) throw new InvalidArgumentException("breedingType", "Tipo de cobertura é obrigatório");
        if (vo.getEventDate().isAfter(LocalDate.now(clock))) throw new InvalidArgumentException("eventDate", "Data do evento não pode ser futura");
        if (pregnancyPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId).isPresent())
            throw new BusinessRuleException("status", ACTIVE_PREGNANCY_BLOCKS_OPERATION_MESSAGE);
        Optional<LocalDate> latestBirth = pregnancyPersistencePort.findLatestBirthCloseDate(farmId, goatId);
        if (latestBirth.isPresent() && !vo.getEventDate().isAfter(latestBirth.get()))
            throw new BusinessRuleException("eventDate", BREEDING_DATE_ON_OR_BEFORE_BIRTH_MESSAGE);
        Optional<ReproductiveEvent> latest = eventPersistencePort.findLatestEffectiveCoverageByFarmIdAndGoatIdOnOrBefore(farmId, goatId, vo.getEventDate());
        if (latest.isPresent() && vo.getEventDate().equals(coverageDateResolver.resolve(farmId, goatId, latest.get())))
            throw new BusinessRuleException("eventDate", SAME_DAY_BREEDING_ALREADY_EXISTS_MESSAGE);
        ReproductiveEvent event = ReproductiveEvent.builder().farmId(farmId).goatId(goatId)
                .eventType(ReproductiveEventType.COVERAGE).eventDate(vo.getEventDate())
                .breedingType(vo.getBreedingType()).breederRef(vo.getBreederRef()).notes(vo.getNotes()).build();
        return mapper.toReproductiveEventResponseVO(eventPersistencePort.save(event));
    }

    @Override
    @Transactional
    public ReproductiveEventResponseVO correctCoverage(Long farmId, String goatId, Long coverageEventId, CoverageCorrectionRequestVO vo) {
        goatGenderValidator.requireFemaleAndActive(farmId, goatId);
        if (coverageEventId == null || coverageEventId <= 0) throw new InvalidArgumentException("coverageEventId", "Identificador de cobertura inválido");
        if (vo.getCorrectedDate() == null) throw new InvalidArgumentException("correctedDate", "Data corrigida é obrigatória");
        if (vo.getCorrectedDate().isAfter(LocalDate.now(clock))) throw new InvalidArgumentException("correctedDate", "Data corrigida não pode ser futura");
        ReproductiveEvent coverage = eventPersistencePort.findByIdAndFarmIdAndGoatId(coverageEventId, farmId, goatId)
                .orElseThrow(() -> new ResourceNotFoundException("Cobertura não encontrada para o identificador informado: " + coverageEventId));
        if (coverage.getEventType() != ReproductiveEventType.COVERAGE) throw new BusinessRuleException("coverageEventId", "Evento informado não é uma cobertura");
        if (eventPersistencePort.findCoverageCorrectionByRelatedEventId(farmId, goatId, coverageEventId).isPresent())
            throw new BusinessRuleException("coverageEventId", "Cobertura já possui correção registrada");
        Optional<Pregnancy> linked = pregnancyPersistencePort.findByFarmIdAndCoverageEventId(farmId, coverageEventId);
        if (linked.isPresent()) throw new BusinessRuleException("coverageEventId", "Não é possível corrigir uma cobertura associada a uma gestação");
        ReproductiveEvent correction = ReproductiveEvent.builder().farmId(farmId).goatId(goatId)
                .eventType(ReproductiveEventType.COVERAGE_CORRECTION).eventDate(LocalDate.now(clock))
                .relatedEventId(coverageEventId).correctedEventDate(vo.getCorrectedDate()).notes(vo.getNotes()).build();
        return mapper.toReproductiveEventResponseVO(eventPersistencePort.save(correction));
    }
}
