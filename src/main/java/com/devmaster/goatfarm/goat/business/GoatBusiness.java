package com.devmaster.goatfarm.goat.business;

import com.devmaster.goatfarm.application.core.business.common.EntityFinder;
import com.devmaster.goatfarm.audit.application.ports.in.OperationalAuditUseCase;
import com.devmaster.goatfarm.audit.business.bo.OperationalAuditRecordVO;
import com.devmaster.goatfarm.audit.enums.OperationalAuditActionType;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.goat.business.bo.GoatExitRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatExitResponseVO;
import com.devmaster.goatfarm.goat.business.bo.GoatBreedSummaryVO;
import com.devmaster.goatfarm.goat.business.bo.GoatHerdSummaryVO;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.goat.business.mapper.GoatBusinessMapper;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatExitType;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goat.persistence.entity.Goat;
import com.devmaster.goatfarm.goat.persistence.repository.GoatBreedCountProjection;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.goat.application.ports.in.GoatManagementUseCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class GoatBusiness implements GoatManagementUseCase {
    private final GoatPersistencePort goatPort;
    private final GoatFarmPersistencePort goatFarmPort;
    private final OwnershipService ownershipService;
    private final GoatBusinessMapper goatBusinessMapper;
    private final EntityFinder entityFinder;
    private final OperationalAuditUseCase operationalAuditUseCase;

    public GoatBusiness(GoatPersistencePort goatPort, GoatFarmPersistencePort goatFarmPort,
                        OwnershipService ownershipService, GoatBusinessMapper goatBusinessMapper, EntityFinder entityFinder,
                        OperationalAuditUseCase operationalAuditUseCase) {
        this.goatPort = goatPort;
        this.goatFarmPort = goatFarmPort;
        this.ownershipService = ownershipService;
        this.goatBusinessMapper = goatBusinessMapper;
        this.entityFinder = entityFinder;
        this.operationalAuditUseCase = operationalAuditUseCase;
    }

    @Transactional
    public GoatResponseVO createGoat(Long farmId, GoatRequestVO requestVO) {
        ownershipService.verifyFarmOwnership(farmId);

        if (requestVO.getRegistrationNumber() != null && goatPort.existsByRegistrationNumber(requestVO.getRegistrationNumber())) {
            throw new DuplicateEntityException("Número de registro já existe.");
        }

        GoatFarm farm = entityFinder.findOrThrow(
                () -> goatFarmPort.findById(farmId),
                "Fazenda não encontrada."
        );
        Goat father = findOptionalGoat(requestVO.getFatherRegistrationNumber()).orElse(null);
        Goat mother = findOptionalGoat(requestVO.getMotherRegistrationNumber()).orElse(null);

        Goat goat = goatBusinessMapper.toEntity(requestVO);
        User user = ownershipService.getCurrentUser();
        goat.setUser(user);
        goat.setFarm(farm);
        goat.setFather(father);
        goat.setMother(mother);
        
        Goat savedGoat = goatPort.save(goat);

        return goatBusinessMapper.toResponseVO(savedGoat);
    }

    @Transactional
    public GoatResponseVO updateGoat(Long farmId, String goatId, GoatRequestVO requestVO) {
        ownershipService.verifyFarmOwnership(farmId);

        Goat goatToUpdate = entityFinder.findOrThrow(
                () -> goatPort.findByIdAndFarmId(goatId, farmId),
                "Cabra não encontrada nesta fazenda."
        );

        Goat father = findOptionalGoat(requestVO.getFatherRegistrationNumber()).orElse(null);
        Goat mother = findOptionalGoat(requestVO.getMotherRegistrationNumber()).orElse(null);

        goatBusinessMapper.updateEntity(goatToUpdate, requestVO, father, mother);
        
        Goat updatedGoat = goatPort.save(goatToUpdate);
        return goatBusinessMapper.toResponseVO(updatedGoat);
    }

    @Transactional
    public GoatExitResponseVO exitGoat(Long farmId, String goatId, GoatExitRequestVO requestVO) {
        ownershipService.verifyFarmOwnership(farmId);

        Goat goat = entityFinder.findOrThrow(
                () -> goatPort.findByIdAndFarmId(goatId, farmId),
                "Cabra nao encontrada nesta fazenda."
        );

        if (requestVO.getExitType() == null) {
            throw new InvalidArgumentException("exitType", "Tipo de saida e obrigatorio");
        }
        if (requestVO.getExitDate() == null) {
            throw new InvalidArgumentException("exitDate", "Data de saida e obrigatoria");
        }
        if (requestVO.getExitDate().isAfter(LocalDate.now())) {
            throw new InvalidArgumentException("exitDate", "Data de saida nao pode ser futura");
        }
        if (goat.getBirthDate() != null && requestVO.getExitDate().isBefore(goat.getBirthDate())) {
            throw new InvalidArgumentException("exitDate", "Data de saida nao pode ser anterior a data de nascimento");
        }
        if (goat.getStatus() != GoatStatus.ATIVO) {
            throw new BusinessRuleException(
                    "status",
                    "A saida controlada so e permitida para animais com status ATIVO. Status atual: " + goat.getStatus()
            );
        }
        if (goat.getExitType() != null || goat.getExitDate() != null) {
            throw new BusinessRuleException("exitDate", "Ja existe saida registrada para este animal");
        }

        GoatStatus previousStatus = goat.getStatus();
        GoatStatus currentStatus = mapExitStatus(requestVO.getExitType());

        goat.setStatus(currentStatus);
        goat.setExitType(requestVO.getExitType());
        goat.setExitDate(requestVO.getExitDate());
        goat.setExitNotes(normalizeNotes(requestVO.getNotes()));

        Goat savedGoat = goatPort.save(goat);
        operationalAuditUseCase.record(new OperationalAuditRecordVO(
                farmId,
                savedGoat.getRegistrationNumber(),
                OperationalAuditActionType.GOAT_EXIT,
                savedGoat.getRegistrationNumber(),
                "Saida do rebanho registrada como " + requestVO.getExitType().getPortugueseValue()
                        + " com status final " + savedGoat.getStatus() + "."
        ));

        return GoatExitResponseVO.builder()
                .goatId(savedGoat.getRegistrationNumber())
                .exitType(savedGoat.getExitType())
                .exitDate(savedGoat.getExitDate())
                .notes(savedGoat.getExitNotes())
                .previousStatus(previousStatus)
                .currentStatus(savedGoat.getStatus())
                .build();
    }

    @Transactional
    public void deleteGoat(Long farmId, String goatId) {
        ownershipService.verifyGoatOwnership(farmId, goatId);
        entityFinder.findOrThrow(
                () -> goatPort.findByIdAndFarmId(goatId, farmId),
                "Cabra não encontrada nesta fazenda."
        );
        goatPort.deleteById(goatId);
    }

    @Transactional(readOnly = true)
    public GoatResponseVO findGoatById(Long farmId, String goatId) {
        Goat goat = entityFinder.findOrThrow(
                () -> goatPort.findByIdAndFarmId(goatId, farmId),
                "Cabra não encontrada nesta fazenda."
        );
        return goatBusinessMapper.toResponseVO(goat);
    }

    @Transactional(readOnly = true)
    public Page<GoatResponseVO> findAllGoatsByFarm(Long farmId, Pageable pageable) {
        return goatPort.findAllByFarmId(farmId, pageable).map(goatBusinessMapper::toResponseVO);
    }

    @Transactional(readOnly = true)
    public Page<GoatResponseVO> findAllGoatsByFarm(Long farmId, GoatBreed breed, Pageable pageable) {
        if (breed == null) {
            return findAllGoatsByFarm(farmId, pageable);
        }

        return goatPort.findAllByFarmIdAndBreed(farmId, breed, pageable).map(goatBusinessMapper::toResponseVO);
    }

    @Transactional(readOnly = true)
    public Page<GoatResponseVO> findGoatsByNameAndFarm(Long farmId, String name, Pageable pageable) {
        return goatPort.findByNameAndFarmId(farmId, name, pageable).map(goatBusinessMapper::toResponseVO);
    }

    @Transactional(readOnly = true)
    public Page<GoatResponseVO> findGoatsByNameAndFarm(Long farmId, String name, GoatBreed breed, Pageable pageable) {
        if (breed == null) {
            return findGoatsByNameAndFarm(farmId, name, pageable);
        }

        return goatPort.findByNameAndFarmIdAndBreed(farmId, name, breed, pageable).map(goatBusinessMapper::toResponseVO);
    }

    @Transactional(readOnly = true)
    public List<GoatResponseVO> listOffspring(Long farmId, String goatId) {
        Goat goat = entityFinder.findOrThrow(
                () -> goatPort.findByIdAndFarmId(goatId, farmId),
                "Cabra nÃ£o encontrada nesta fazenda."
        );

        return goatPort.findOffspringByParentRegistration(farmId, goat.getRegistrationNumber()).stream()
                .map(goatBusinessMapper::toResponseVO)
                .toList();
    }

    @Transactional(readOnly = true)
    public GoatHerdSummaryVO getGoatHerdSummary(Long farmId) {
        long total = goatPort.countByFarmId(farmId);
        long males = goatPort.countByFarmIdAndGender(farmId, Gender.MACHO);
        long females = goatPort.countByFarmIdAndGender(farmId, Gender.FEMEA);
        long active = goatPort.countByFarmIdAndStatus(farmId, GoatStatus.ATIVO);
        long inactive = goatPort.countByFarmIdAndStatus(farmId, GoatStatus.INATIVO);
        long sold = goatPort.countByFarmIdAndStatus(farmId, GoatStatus.VENDIDO);
        long deceased = goatPort.countByFarmIdAndStatus(farmId, GoatStatus.FALECIDO);

        List<GoatBreedSummaryVO> breeds = new ArrayList<>(
                goatPort.countBreedsByFarmId(farmId).stream()
                        .map(this::toBreedSummary)
                        .toList()
        );

        long withoutBreed = goatPort.countByFarmIdWithoutBreed(farmId);
        if (withoutBreed > 0) {
            breeds.add(GoatBreedSummaryVO.builder()
                    .breed(null)
                    .label("Não informada")
                    .count(withoutBreed)
                    .build());
        }

        breeds.sort(Comparator
                .comparingLong(GoatBreedSummaryVO::getCount)
                .reversed()
                .thenComparing(GoatBreedSummaryVO::getLabel, String.CASE_INSENSITIVE_ORDER));

        return GoatHerdSummaryVO.builder()
                .total(total)
                .males(males)
                .females(females)
                .active(active)
                .inactive(inactive)
                .sold(sold)
                .deceased(deceased)
                .breeds(breeds)
                .build();
    }

    private Optional<Goat> findOptionalGoat(String registrationNumber) {
        if (registrationNumber == null) return Optional.empty();
        return goatPort.findByRegistrationNumber(registrationNumber);
    }

    private GoatBreedSummaryVO toBreedSummary(GoatBreedCountProjection projection) {
        return GoatBreedSummaryVO.builder()
                .breed(projection.getBreed())
                .label(projection.getBreed().getLabel())
                .count(projection.getTotal())
                .build();
    }

    private GoatStatus mapExitStatus(GoatExitType exitType) {
        return switch (exitType) {
            case VENDA -> GoatStatus.VENDIDO;
            case MORTE -> GoatStatus.FALECIDO;
            case DESCARTE, DOACAO, TRANSFERENCIA -> GoatStatus.INATIVO;
        };
    }

    private String normalizeNotes(String notes) {
        if (notes == null) {
            return null;
        }
        String normalized = notes.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}


