package com.devmaster.goatfarm.reproduction.business.reproductionservice;

import com.devmaster.goatfarm.application.core.business.validation.GoatGenderValidator;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.goat.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.goat.application.routing.GoatReferenceResolver;
import com.devmaster.goatfarm.goat.domain.Goat;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.reproduction.application.ports.in.WeaningCommandUseCase;
import com.devmaster.goatfarm.reproduction.application.ports.out.ReproductiveEventPersistencePort;
import com.devmaster.goatfarm.reproduction.business.bo.WeaningRequestVO;
import com.devmaster.goatfarm.reproduction.business.bo.WeaningResponseVO;
import com.devmaster.goatfarm.reproduction.business.mapper.ReproductionBusinessMapper;
import com.devmaster.goatfarm.reproduction.enums.ReproductiveEventType;
import com.devmaster.goatfarm.reproduction.persistence.entity.ReproductiveEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class WeaningCommandBusiness implements WeaningCommandUseCase {
    private final ReproductiveEventPersistencePort eventPersistencePort;
    private final GoatPersistencePort goatPersistencePort;
    private final GoatReferenceResolver goatReferenceResolver;
    private final GoatGenderValidator goatGenderValidator;
    private final ReproductionBusinessMapper mapper;
    private final Clock clock;

    public WeaningCommandBusiness(ReproductiveEventPersistencePort eventPersistencePort, GoatPersistencePort goatPersistencePort,
                                  GoatReferenceResolver goatReferenceResolver, GoatGenderValidator goatGenderValidator,
                                  ReproductionBusinessMapper mapper, Clock clock) {
        this.eventPersistencePort = eventPersistencePort; this.goatPersistencePort = goatPersistencePort; this.goatReferenceResolver = goatReferenceResolver;
        this.goatGenderValidator = goatGenderValidator; this.mapper = mapper; this.clock = clock;
    }

    @Override @Transactional
    public WeaningResponseVO registerWeaning(Long farmId, String goatId, WeaningRequestVO vo) {
        goatGenderValidator.requireActive(farmId, goatId); Goat kid = requireGoat(farmId, goatId);
        if (vo.getWeaningDate() == null) throw new InvalidArgumentException("weaningDate", "Data de desmame e obrigatoria");
        if (vo.getWeaningDate().isAfter(LocalDate.now(clock))) throw new InvalidArgumentException("weaningDate", "Data de desmame nao pode ser futura");
        if (kid.birthDate() != null && vo.getWeaningDate().isBefore(kid.birthDate())) throw new InvalidArgumentException("weaningDate", "Data de desmame nao pode ser anterior a data de nascimento");
        if (kid.mother() == null) throw new BusinessRuleException("goatId", "Desmame so pode ser registrado para animal vinculado a uma matriz");
        Optional<ReproductiveEvent> existing = eventPersistencePort.findLatestByFarmIdAndGoatIdAndEventType(farmId, goatId, ReproductiveEventType.WEANING);
        if (existing.isPresent()) throw new BusinessRuleException("weaningDate", "Ja existe desmame registrado para este animal");
        GoatStatus previous = kid.status(); kid.activateAfterWeaning(); Goat saved = goatPersistencePort.save(kid);
        ReproductiveEvent event = eventPersistencePort.save(ReproductiveEvent.builder().farmId(farmId).goatId(goatId).eventType(ReproductiveEventType.WEANING).eventDate(vo.getWeaningDate()).notes(vo.getNotes()).build());
        return WeaningResponseVO.builder().goatId(saved.registrationNumber()).goatTechnicalId(saved.id() == null ? null : saved.id().value())
                .weaningDate(vo.getWeaningDate()).previousStatus(previous).currentStatus(saved.status()).event(mapper.toReproductiveEventResponseVO(event)).build();
    }

    private Goat requireGoat(Long farmId, String routeToken) {
        GoatReference reference = goatReferenceResolver.resolve(routeToken, farmId).orElseThrow(() -> new ResourceNotFoundException("Cabra não encontrada para a fazenda informada."));
        return goatPersistencePort.findByIdAndFarmId(reference.id(), farmId).orElseThrow(() -> new ResourceNotFoundException("Cabra não encontrada para a fazenda informada."));
    }
}
