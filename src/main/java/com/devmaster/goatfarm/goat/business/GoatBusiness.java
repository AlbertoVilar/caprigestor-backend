package com.devmaster.goatfarm.goat.business;

import com.devmaster.goatfarm.application.core.business.common.EntityFinder;
import com.devmaster.goatfarm.audit.application.ports.in.OperationalAuditUseCase;
import com.devmaster.goatfarm.audit.business.bo.OperationalAuditRecordVO;
import com.devmaster.goatfarm.audit.enums.OperationalAuditActionType;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.goat.application.ports.in.GoatManagementUseCase;
import com.devmaster.goatfarm.goat.application.ports.out.GoatHerdSnapshot;
import com.devmaster.goatfarm.goat.application.ports.out.GoatPage;
import com.devmaster.goatfarm.goat.application.ports.out.GoatPageQuery;
import com.devmaster.goatfarm.goat.application.ports.out.GoatParentagePort;
import com.devmaster.goatfarm.goat.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.goat.business.bo.GoatBreedSummaryVO;
import com.devmaster.goatfarm.goat.business.bo.GoatExitRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatExitResponseVO;
import com.devmaster.goatfarm.goat.business.bo.GoatHerdSummaryVO;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.domain.Goat;
import com.devmaster.goatfarm.goat.domain.RegistrationIdentity;
import com.devmaster.goatfarm.goat.application.routing.GoatRouteIdentifier;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatExitType;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Application service for Goat, depending on the domain-facing persistence boundary. */
@Service
public class GoatBusiness implements GoatManagementUseCase {
    private final GoatPersistencePort goatPort;
    private final GoatFarmPersistencePort goatFarmPort;
    private final OwnershipService ownershipService;
    private final EntityFinder entityFinder;
    private final OperationalAuditUseCase operationalAuditUseCase;
    private final GoatParentagePort parentagePort;

    public GoatBusiness(GoatPersistencePort goatPort, GoatFarmPersistencePort goatFarmPort,
                        OwnershipService ownershipService, EntityFinder entityFinder,
                        OperationalAuditUseCase operationalAuditUseCase, GoatParentagePort parentagePort) {
        this.goatPort = goatPort;
        this.goatFarmPort = goatFarmPort;
        this.ownershipService = ownershipService;
        this.entityFinder = entityFinder;
        this.operationalAuditUseCase = operationalAuditUseCase;
        this.parentagePort = parentagePort;
    }

    @Transactional
    @Override
    public GoatResponseVO createGoat(Long farmId, GoatRequestVO requestVO) {
        ownershipService.verifyFarmManagement(farmId);
        String registration = normalize(requestVO.getRegistrationNumber());
        if (registration == null) throw new InvalidArgumentException("registrationNumber", "Número de registro é obrigatório");
        if (goatPort.existsByRegistrationNumber(registration)) throw new DuplicateEntityException("Número de registro já existe.");
        entityFinder.findOrThrow(() -> goatFarmPort.findById(farmId), "Fazenda não encontrada.");
        GoatParentagePort.ResolvedParentage parents = parentagePort.resolve(requestVO.getCategory(), registration,
                requestVO.getFatherRegistrationNumber(), requestVO.getMotherRegistrationNumber());
        Goat goat = Goat.register(RegistrationIdentity.of(registration, requestVO.getTod(), requestVO.getToe()),
                requestVO.getName(), requestVO.getGender(), requestVO.getBreed(), requestVO.getColor(), requestVO.getBirthDate(),
                requestVO.getStatus(), requestVO.getCategory(), parents.father(), parents.mother(), farmId,
                ownershipService.getCurrentUser().getId());
        return toResponse(goatPort.save(goat));
    }

    @Transactional
    @Override
    public GoatResponseVO updateGoat(Long farmId, String goatId, GoatRequestVO requestVO) {
        ownershipService.verifyFarmOwnership(farmId);
        Goat goat = findOrThrow(farmId, goatId);
        RegistrationIdentity submittedIdentity;
        try {
            submittedIdentity = RegistrationIdentity.of(requestVO.getRegistrationNumber(), requestVO.getTod(), requestVO.getToe());
        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleException("registrationNumber",
                    "TOD, TOE e número de registro devem formar uma identidade consistente. "
                            + "Use o endpoint explícito de retificação para corrigir a identidade.");
        }
        if (!goat.registrationIdentity().equals(submittedIdentity)) {
            throw new BusinessRuleException("registrationNumber",
                    "A identidade registral não pode ser alterada na atualização cadastral comum. "
                            + "Use o endpoint explícito de retificação.");
        }
        GoatParentagePort.ResolvedParentage parents = parentagePort.resolve(requestVO.getCategory(), goat.registrationNumber(),
                requestVO.getFatherRegistrationNumber(), requestVO.getMotherRegistrationNumber());
        goat.updateProfile(requestVO.getName(), requestVO.getGender(), requestVO.getBreed(), requestVO.getColor(),
                requestVO.getBirthDate(), requestVO.getStatus(), requestVO.getCategory(), parents.father(), parents.mother());
        return toResponse(goatPort.save(goat));
    }

    @Transactional
    @Override
    public GoatExitResponseVO exitGoat(Long farmId, String goatId, GoatExitRequestVO requestVO) {
        ownershipService.verifyFarmOwnership(farmId);
        Goat goat = findOrThrow(farmId, goatId);
        validateExit(goat, requestVO);
        GoatStatus previousStatus = goat.status();
        GoatStatus currentStatus = mapExitStatus(requestVO.getExitType());
        goat.markExit(requestVO.getExitType(), requestVO.getExitDate(), normalizeNotes(requestVO.getNotes()), currentStatus);
        Goat saved = goatPort.save(goat);
        operationalAuditUseCase.record(new OperationalAuditRecordVO(farmId,
                saved.id() == null ? null : saved.id().value(), saved.registrationNumber(),
                OperationalAuditActionType.GOAT_EXIT, saved.registrationNumber(), "Saída do rebanho registrada como "
                + requestVO.getExitType().getPortugueseValue() + " com status final " + saved.status() + "."));
        return GoatExitResponseVO.builder().goatId(saved.registrationNumber())
                .goatTechnicalId(saved.id() == null ? null : saved.id().value()).exitType(saved.exitType())
                .exitDate(saved.exitDate()).notes(saved.exitNotes()).previousStatus(previousStatus)
                .currentStatus(saved.status()).build();
    }

    @Transactional
    @Override
    public void deleteGoat(Long farmId, String goatId) {
        ownershipService.verifyGoatOwnership(farmId, goatId);
        goatPort.deleteById(findOrThrow(farmId, goatId).id());
    }

    @Transactional(readOnly = true)
    @Override
    public GoatResponseVO findGoatById(Long farmId, String goatId) { return toResponse(findOrThrow(farmId, goatId)); }

    @Transactional(readOnly = true)
    @Override
    public Page<GoatResponseVO> findAllGoatsByFarm(Long farmId, Pageable pageable) {
        return toSpringPage(goatPort.findAllByFarmId(farmId, toQuery(pageable)));
    }

    @Transactional(readOnly = true)
    @Override
    public Page<GoatResponseVO> findAllGoatsByFarm(Long farmId, GoatBreed breed, Pageable pageable) {
        return breed == null ? findAllGoatsByFarm(farmId, pageable)
                : toSpringPage(goatPort.findAllByFarmIdAndBreed(farmId, breed, toQuery(pageable)));
    }

    @Transactional(readOnly = true)
    @Override
    public Page<GoatResponseVO> findGoatsByNameAndFarm(Long farmId, String name, Pageable pageable) {
        return toSpringPage(goatPort.findByNameAndFarmId(farmId, name, toQuery(pageable)));
    }

    @Transactional(readOnly = true)
    @Override
    public Page<GoatResponseVO> findGoatsByNameAndFarm(Long farmId, String name, GoatBreed breed, Pageable pageable) {
        return breed == null ? findGoatsByNameAndFarm(farmId, name, pageable)
                : toSpringPage(goatPort.findByNameAndFarmIdAndBreed(farmId, name, breed, toQuery(pageable)));
    }

    @Transactional(readOnly = true)
    @Override
    public List<GoatResponseVO> listOffspring(Long farmId, String goatId) {
        return goatPort.findOffspringByParentId(farmId, findOrThrow(farmId, goatId).id()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public GoatHerdSummaryVO getGoatHerdSummary(Long farmId) {
        GoatHerdSnapshot summary = goatPort.getHerdSummary(farmId);
        List<GoatBreedSummaryVO> breeds = new ArrayList<>(summary.breeds().stream()
                .map(b -> GoatBreedSummaryVO.builder().breed(b.breed()).label(b.breed().getLabel()).count(b.total()).build()).toList());
        if (summary.withoutBreed() > 0) breeds.add(GoatBreedSummaryVO.builder().breed(null).label("Não informada").count(summary.withoutBreed()).build());
        breeds.sort(Comparator.comparingLong(GoatBreedSummaryVO::getCount).reversed().thenComparing(GoatBreedSummaryVO::getLabel, String.CASE_INSENSITIVE_ORDER));
        return GoatHerdSummaryVO.builder().total(summary.total()).males(summary.males()).females(summary.females())
                .active(summary.active()).inactive(summary.inactive()).sold(summary.sold()).deceased(summary.deceased()).breeds(breeds).build();
    }

    private Goat findOrThrow(Long farmId, String token) {
        // Explicit technical tokens are unambiguous even when an RG happens
        // to contain only digits. Every other route token is an RG; a bare
        // numeric token must never be guessed to be a technical id.
        var explicitTechnicalId = GoatRouteIdentifier.technicalId(token);
        if (explicitTechnicalId.isPresent()) {
            return goatPort.findByIdAndFarmId(explicitTechnicalId.get(), farmId)
                    .orElseThrow(() -> new com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException("Cabra não encontrada nesta fazenda."));
        }

        Goat found = goatPort.findByRegistrationNumberAndFarmId(token, farmId).orElse(null);
        if (found == null) throw new com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException("Cabra não encontrada nesta fazenda.");
        return found;
    }

    private Page<GoatResponseVO> toSpringPage(GoatPage<Goat> page) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(page.page(), page.size());
        return new PageImpl<>(page.content().stream().map(this::toResponse).toList(), pageable, page.totalElements());
    }

    private GoatPageQuery toQuery(Pageable pageable) {
        String sort = pageable.getSort().stream().findFirst().map(o -> o.getProperty() + "," + o.getDirection().name()).orElse("");
        return new GoatPageQuery(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private GoatResponseVO toResponse(Goat goat) {
        GoatResponseVO response = new GoatResponseVO();
        response.setTechnicalId(goat.id() == null ? null : goat.id().value());
        response.setRegistrationNumber(goat.registrationNumber()); response.setName(goat.name()); response.setGender(goat.gender());
        response.setBreed(goat.breed()); response.setColor(goat.color()); response.setBirthDate(goat.birthDate()); response.setStatus(goat.status());
        response.setExitType(goat.exitType()); response.setExitDate(goat.exitDate()); response.setExitNotes(goat.exitNotes()); response.setTod(goat.tod());
        response.setToe(goat.toe()); response.setCategory(goat.category()); response.setFarmId(goat.farmId()); response.setFarmName(goat.farmName()); response.setUserName(goat.userName());
        if (goat.father() != null) { response.setFatherRegistrationNumber(goat.father().registrationNumber()); response.setFatherName(goat.father().name()); }
        if (goat.mother() != null) { response.setMotherRegistrationNumber(goat.mother().registrationNumber()); response.setMotherName(goat.mother().name()); }
        return response;
    }

    private void validateExit(Goat goat, GoatExitRequestVO requestVO) {
        if (requestVO.getExitType() == null) throw new InvalidArgumentException("exitType", "Tipo de saída é obrigatório");
        if (requestVO.getExitDate() == null) throw new InvalidArgumentException("exitDate", "Data de saída é obrigatória");
        if (requestVO.getExitDate().isAfter(LocalDate.now())) throw new InvalidArgumentException("exitDate", "Data de saída não pode ser futura");
        if (goat.birthDate() != null && requestVO.getExitDate().isBefore(goat.birthDate())) throw new InvalidArgumentException("exitDate", "Data de saída não pode ser anterior à data de nascimento");
        if (goat.status() != GoatStatus.ATIVO) throw new BusinessRuleException("status", "A saída controlada só é permitida para animais com status ATIVO. Status atual: " + goat.status());
        if (goat.exitType() != null || goat.exitDate() != null) throw new BusinessRuleException("exitDate", "Já existe saída registrada para este animal");
    }

    private GoatStatus mapExitStatus(GoatExitType type) {
        return switch (type) { case VENDA -> GoatStatus.VENDIDO; case MORTE -> GoatStatus.FALECIDO; case DESCARTE, DOACAO, TRANSFERENCIA -> GoatStatus.INATIVO; };
    }
    private String normalize(String value) { return value == null ? null : value.trim().replaceAll("\\s+", "").toUpperCase(Locale.ROOT); }
    private String normalizeNotes(String value) { if (value == null) return null; String normalized = value.trim(); return normalized.isEmpty() ? null : normalized; }
}
