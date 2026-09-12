package com.devmaster.goatfarm.reproduction.business.reproductionservice;

import com.devmaster.goatfarm.application.core.business.validation.GoatGenderValidator;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.farm.application.model.FarmRegistrationSnapshot;
import com.devmaster.goatfarm.farm.application.ports.in.FarmRegistrationQueryUseCase;
import com.devmaster.goatfarm.goat.application.ports.in.GoatManagementUseCase;
import com.devmaster.goatfarm.goat.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.goat.application.routing.GoatReferenceResolver;
import com.devmaster.goatfarm.goat.business.bo.*;
import com.devmaster.goatfarm.goat.domain.Goat;
import com.devmaster.goatfarm.goat.enums.*;
import com.devmaster.goatfarm.reproduction.application.ports.in.BirthCommandUseCase;
import com.devmaster.goatfarm.reproduction.application.ports.out.PregnancyPersistencePort;
import com.devmaster.goatfarm.reproduction.application.ports.out.ReproductiveEventPersistencePort;
import com.devmaster.goatfarm.reproduction.business.bo.*;
import com.devmaster.goatfarm.reproduction.business.mapper.ReproductionBusinessMapper;
import com.devmaster.goatfarm.reproduction.enums.PregnancyCloseReason;
import com.devmaster.goatfarm.reproduction.enums.PregnancyStatus;
import com.devmaster.goatfarm.reproduction.enums.ReproductiveEventType;
import com.devmaster.goatfarm.reproduction.domain.Pregnancy;
import com.devmaster.goatfarm.reproduction.domain.ReproductiveEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class BirthCommandBusiness implements BirthCommandUseCase {
    private static final Pattern BIRTH_REGISTRATION_PATTERN = Pattern.compile("^(?=.{10,12}$)[0-9]+[A-Z]?$" );
    private final PregnancyPersistencePort pregnancyPersistencePort;
    private final ReproductiveEventPersistencePort eventPersistencePort;
    private final GoatPersistencePort goatPersistencePort;
    private final GoatReferenceResolver goatReferenceResolver;
    private final FarmRegistrationQueryUseCase farmRegistrationQueryUseCase;
    private final GoatManagementUseCase goatManagementUseCase;
    private final GoatGenderValidator goatGenderValidator;
    private final ReproductionBusinessMapper mapper;
    private final Clock clock;

    public BirthCommandBusiness(PregnancyPersistencePort pregnancyPersistencePort, ReproductiveEventPersistencePort eventPersistencePort,
                                GoatPersistencePort goatPersistencePort, GoatReferenceResolver goatReferenceResolver,
                                FarmRegistrationQueryUseCase farmRegistrationQueryUseCase, GoatManagementUseCase goatManagementUseCase,
                                GoatGenderValidator goatGenderValidator, ReproductionBusinessMapper mapper, Clock clock) {
        this.pregnancyPersistencePort = pregnancyPersistencePort; this.eventPersistencePort = eventPersistencePort; this.goatPersistencePort = goatPersistencePort;
        this.goatReferenceResolver = goatReferenceResolver; this.farmRegistrationQueryUseCase = farmRegistrationQueryUseCase; this.goatManagementUseCase = goatManagementUseCase;
        this.goatGenderValidator = goatGenderValidator; this.mapper = mapper; this.clock = clock;
    }

    @Override @Transactional
    public BirthResponseVO registerBirth(Long farmId, String goatId, Long pregnancyId, BirthRequestVO vo) {
        goatGenderValidator.requireFemaleAndActive(farmId, goatId); Goat mother = requireGoat(farmId, goatId);
        if (pregnancyId == null || pregnancyId <= 0) throw new InvalidArgumentException("pregnancyId", "Identificador de gestacao invalido");
        if (vo.getBirthDate() == null) throw new InvalidArgumentException("birthDate", "Data do parto e obrigatoria");
        if (vo.getBirthDate().isAfter(LocalDate.now(clock))) throw new InvalidArgumentException("birthDate", "Data do parto nao pode ser futura");
        if (vo.getKids() == null || vo.getKids().isEmpty()) throw new InvalidArgumentException("kids", "E necessario informar ao menos uma cria");
        String birthFarmTod = resolveBirthFarmTod(farmId);
        Pregnancy pregnancy = pregnancyPersistencePort.findByIdAndFarmIdAndGoatId(pregnancyId, farmId, goatId)
                .orElseThrow(() -> new ResourceNotFoundException("Gestacao nao encontrada para o identificador informado: " + pregnancyId));
        if (pregnancy.getStatus() != PregnancyStatus.ACTIVE) throw new InvalidArgumentException("status", "Gestacao nao esta ativa");
        if (pregnancy.getCoverageEventId() != null) {
            Optional<Pregnancy> linked = pregnancyPersistencePort.findByFarmIdAndCoverageEventId(farmId, pregnancy.getCoverageEventId());
            if (linked.isPresent() && !linked.get().getId().equals(pregnancy.getId())) throw new BusinessRuleException("pregnancyId", "Cobertura da gestacao ja esta vinculada a outra gestacao.");
        }
        if (pregnancy.getBreedingDate() != null && vo.getBirthDate().isBefore(pregnancy.getBreedingDate())) throw new InvalidArgumentException("birthDate", "Data do parto nao pode ser anterior a data de cobertura");
        ensureDistinctKidRegistrations(vo.getKids());
        List<BirthKidResponseVO> createdKids = new ArrayList<>();
        for (BirthKidRequestVO kid : vo.getKids()) {
            GoatRequestVO request = buildKidRequestVO(farmId, goatId, mother, vo.getFatherRegistrationNumber(), birthFarmTod, vo.getBirthDate(), kid);
            GoatResponseVO saved = goatManagementUseCase.createGoat(farmId, request); createdKids.add(mapper.toBirthKidResponseVO(saved));
        }
        pregnancy.close(PregnancyCloseReason.BIRTH, vo.getBirthDate());
        if (vo.getNotes() != null && !vo.getNotes().isBlank()) pregnancy.updateNotes(vo.getNotes());
        Pregnancy savedPregnancy = pregnancyPersistencePort.save(pregnancy);
        ReproductiveEvent closeEvent = eventPersistencePort.save(ReproductiveEvent.builder().farmId(farmId).goatId(goatId).pregnancyId(savedPregnancy.getId())
                .eventType(ReproductiveEventType.PREGNANCY_CLOSE).eventDate(vo.getBirthDate()).notes(vo.getNotes()).build());
        return BirthResponseVO.builder().pregnancy(mapper.toPregnancyResponseVO(savedPregnancy)).closeEvent(mapper.toReproductiveEventResponseVO(closeEvent)).kids(createdKids).build();
    }

    private Goat requireGoat(Long farmId, String routeToken) {
        GoatReference reference = goatReferenceResolver.resolve(routeToken, farmId).orElseThrow(() -> new ResourceNotFoundException("Cabra não encontrada para a fazenda informada."));
        return goatPersistencePort.findByIdAndFarmId(reference.id(), farmId).orElseThrow(() -> new ResourceNotFoundException("Cabra não encontrada para a fazenda informada."));
    }
    private GoatRequestVO buildKidRequestVO(Long farmId, String motherGoatId, Goat mother, String fatherRegistrationNumber, String birthFarmTod, LocalDate defaultBirthDate, BirthKidRequestVO kid) {
        String registration = normalizeBirthRegistration(kid.getRegistrationNumber());
        if (registration == null) throw new InvalidArgumentException("kids.registrationNumber", "Registro da cria e obrigatorio");
        if (!BIRTH_REGISTRATION_PATTERN.matcher(registration).matches()) throw new InvalidArgumentException("kids.registrationNumber", "Registro da cria deve ter entre 10 e 12 caracteres: numeros e, opcionalmente, uma letra final");
        if (!registration.startsWith(birthFarmTod)) throw new BusinessRuleException("kids.registrationNumber", "O registro da cria deve iniciar com o TOD da fazenda de nascimento: " + birthFarmTod);
        String toe = registration.substring(birthFarmTod.length()); if (toe.isEmpty()) throw new InvalidArgumentException("kids.registrationNumber", "O registro da cria deve conter o TOE apos o TOD da fazenda");
        String name = normalizeText(kid.getName()); if (name == null) throw new InvalidArgumentException("kids.name", "Nome da cria e obrigatorio");
        if (kid.getGender() == null) throw new InvalidArgumentException("kids.gender", "Sexo da cria e obrigatorio");
        LocalDate birthDate = kid.getBirthDate() != null ? kid.getBirthDate() : defaultBirthDate;
        if (birthDate == null) throw new InvalidArgumentException("kids.birthDate", "Data de nascimento da cria e obrigatoria");
        if (birthDate.isAfter(LocalDate.now(clock))) throw new InvalidArgumentException("kids.birthDate", "Data de nascimento da cria nao pode ser futura");
        if (defaultBirthDate != null && !birthDate.equals(defaultBirthDate)) throw new InvalidArgumentException("kids.birthDate", "Data de nascimento da cria deve ser igual a data do parto");
        GoatBreed breed = kid.getBreed() != null ? kid.getBreed() : mother.breed();
        if (breed == null) throw new InvalidArgumentException("kids.breed", "Raca da cria e obrigatoria quando a matriz nao possui raca cadastrada");
        return GoatRequestVO.builder().registrationNumber(registration).name(name).gender(kid.getGender()).breed(breed).color(normalizeText(kid.getColor())).birthDate(birthDate)
                .status(GoatStatus.ATIVO).tod(birthFarmTod).toe(toe).category(kid.getCategory() != null ? kid.getCategory() : Category.PA)
                .fatherRegistrationNumber(normalizeRegistration(fatherRegistrationNumber)).motherRegistrationNumber(motherGoatId).farmId(farmId).build();
    }
    private String resolveBirthFarmTod(Long farmId) {
        String tod = farmRegistrationQueryUseCase.findRegistrationById(farmId).map(FarmRegistrationSnapshot::tod).map(this::normalizeRegistration).orElseThrow(() -> new BusinessRuleException("kids.registrationNumber", "Nao e possivel registrar cria: a fazenda de nascimento nao possui TOD cadastrado"));
        if (!tod.matches("[0-9]{5}")) throw new BusinessRuleException("kids.registrationNumber", "Nao e possivel registrar cria: o TOD da fazenda de nascimento deve conter 5 digitos"); return tod;
    }
    private void ensureDistinctKidRegistrations(List<BirthKidRequestVO> kids) { Set<String> seen = new HashSet<>(); for (BirthKidRequestVO kid : kids) { String registration = normalizeBirthRegistration(kid.getRegistrationNumber()); if (registration == null) throw new InvalidArgumentException("kids.registrationNumber", "Registro da cria e obrigatorio"); if (!seen.add(registration)) throw new BusinessRuleException("kids.registrationNumber", "Nao e permitido informar crias com registro duplicado no mesmo parto"); } }
    private String normalizeBirthRegistration(String value) { String normalized = normalizeRegistration(value); return normalized == null ? null : normalized.toUpperCase(Locale.ROOT); }
    private String normalizeRegistration(String value) { if (value == null) return null; String normalized = value.trim(); return normalized.isEmpty() ? null : normalized; }
    private String normalizeText(String value) { if (value == null) return null; String normalized = value.trim(); return normalized.isEmpty() ? null : normalized; }
}
