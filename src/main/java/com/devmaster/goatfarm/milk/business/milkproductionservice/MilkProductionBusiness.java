package com.devmaster.goatfarm.milk.business.milkproductionservice;

import com.devmaster.goatfarm.milk.application.ports.in.MilkProductionUseCase;
import com.devmaster.goatfarm.milk.application.ports.out.LactationPersistencePort;
import com.devmaster.goatfarm.milk.application.ports.out.MilkProductionPersistencePort;
import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;
import com.devmaster.goatfarm.application.core.business.validation.GoatGenderValidator;
import com.devmaster.goatfarm.config.exceptions.NoActiveLactationException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.health.application.ports.in.HealthWithdrawalQueryUseCase;
import com.devmaster.goatfarm.health.business.bo.GoatWithdrawalStatusVO;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.goat.application.routing.GoatReferenceResolver;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.ports.in.GoatOwnershipGuardUseCase;
import com.devmaster.goatfarm.milk.business.bo.MilkProductionRequestVO;
import com.devmaster.goatfarm.milk.business.bo.MilkProductionResponseVO;
import com.devmaster.goatfarm.milk.business.bo.MilkProductionUpdateRequestVO;
import com.devmaster.goatfarm.config.exceptions.DuplicateMilkProductionException;
import com.devmaster.goatfarm.milk.enums.MilkingShift;
import com.devmaster.goatfarm.milk.enums.MilkProductionStatus;
import com.devmaster.goatfarm.milk.business.mapper.MilkProductionBusinessMapper;
import com.devmaster.goatfarm.milk.domain.Lactation;
import com.devmaster.goatfarm.milk.domain.MilkProduction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class MilkProductionBusiness implements MilkProductionUseCase {

    /** Ports (infra abstraída) */
    private final MilkProductionPersistencePort milkProductionPersistencePort;
    private final LactationPersistencePort lactationPersistencePort;
    private final GoatGenderValidator goatGenderValidator;
    private final HealthWithdrawalQueryUseCase healthWithdrawalQueryUseCase;
    private final GoatReferenceResolver goatReferenceResolver;
    private final GoatOwnershipGuardUseCase goatOwnershipGuard;

    /** Mapper de domínio */
    private final MilkProductionBusinessMapper milkProductionMapper;

    public MilkProductionBusiness(MilkProductionPersistencePort milkProductionPersistencePort,
                                  LactationPersistencePort lactationPersistencePort,
                                  GoatGenderValidator goatGenderValidator,
                                  HealthWithdrawalQueryUseCase healthWithdrawalQueryUseCase,
                                  MilkProductionBusinessMapper milkProductionMapper,
                                  GoatReferenceResolver goatReferenceResolver,
                                  GoatOwnershipGuardUseCase goatOwnershipGuard) {
        this.milkProductionPersistencePort = milkProductionPersistencePort;
        this.lactationPersistencePort = lactationPersistencePort;
        this.goatGenderValidator = goatGenderValidator;
        this.healthWithdrawalQueryUseCase = healthWithdrawalQueryUseCase;
        this.milkProductionMapper = milkProductionMapper;
        this.goatReferenceResolver = goatReferenceResolver;
        this.goatOwnershipGuard = goatOwnershipGuard;
    }

    /**
     * Criação de produção diária de leite
     */
    @Override
    public MilkProductionResponseVO createMilkProduction(
            Long farmId,
            String goatId,
            MilkProductionRequestVO requestVO
    ) {
        GoatReference goat = goatReferenceResolver.resolveGlobal(goatId)
                .orElseThrow(() -> new ResourceNotFoundException("Cabra não encontrada."));
        GoatId technicalId = goat.id();
        goatOwnershipGuard.requireCurrentFarm(technicalId, requireFarmId(farmId));
        if (!Objects.equals(goat.farmId(), farmId)) {
            throw new BusinessRuleException("ownership",
                    "A projeção de fazenda do animal diverge do ownership canônico.");
        }
        goatGenderValidator.requireFemaleAndActive(technicalId);
        //=======================
        // *** VALIDAÇÃO *** //
        //=======================
        
        if (requestVO == null) {
            throw new InvalidArgumentException("request", "Dados da produção são obrigatórios");
        }
        if (requestVO.getDate() == null) {
            throw new InvalidArgumentException("date", "Data da ordenha é obrigatória");
        }
        if (requestVO.getDate().isAfter(LocalDate.now())) {
            throw new InvalidArgumentException("date", "Data da ordenha não pode ser futura");
        }

        goatOwnershipGuard.requireUnambiguousOwnershipOnDate(technicalId, farmId, requestVO.getDate());

        validateNoDuplicateProduction(technicalId, requestVO.getDate(), requestVO.getShift());
        Lactation lactation = getRequiredActiveLactation(technicalId);
        GoatWithdrawalStatusVO withdrawalStatus = healthWithdrawalQueryUseCase.getGoatWithdrawalStatus(
                technicalId, requestVO.getDate());

        MilkProduction milkProduction = MilkProduction.record(
                farmId,
                goat.registrationNumber(),
                technicalId.value(),
                lactation.getId(),
                requestVO.getDate(),
                requestVO.getShift(),
                requestVO.getVolumeLiters(),
                requestVO.getNotes()
        );
        applyMilkWithdrawalSnapshot(milkProduction, withdrawalStatus);
        MilkProduction saved = milkProductionPersistencePort.save(milkProduction);
        return milkProductionMapper.toResponseVO(saved);
    }

    @Override
    public MilkProductionResponseVO update(
            Long farmId,
            String goatId,
            Long id,
            MilkProductionUpdateRequestVO request
    ) {
        goatGenderValidator.requireFemaleAndActive(farmId, goatId);
        MilkProduction milkProduction = milkProductionPersistencePort.findById(farmId, goatId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Produção de leite não encontrada com o ID: " + id));
        if (milkProduction.getStatus() == MilkProductionStatus.CANCELED) {
            throw new BusinessRuleException("status", "Registro cancelado não pode ser alterado.");
        }
        milkProduction.updateDetails(request.getVolumeLiters(), request.getNotes());

        MilkProduction saved = milkProductionPersistencePort.save(milkProduction);
        return milkProductionMapper.toResponseVO(saved);
    }

    @Override
    public MilkProductionResponseVO findById(Long farmId, String goatId, Long id) {
        goatGenderValidator.requireFemale(farmId, goatId);
        MilkProduction milkProduction = milkProductionPersistencePort.findById(farmId, goatId, id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Produção de leite não encontrada com o ID: " + id
                        )
                );

        return milkProductionMapper.toResponseVO(milkProduction);
    }


    @Override
    public void delete(Long farmId, String goatId, Long id) {
        goatGenderValidator.requireFemaleAndActive(farmId, goatId);
        MilkProduction milkProduction = milkProductionPersistencePort.findById(farmId, goatId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Produção de leite não encontrada com o ID: " + id));

        if (milkProduction.getStatus() == MilkProductionStatus.CANCELED) {
            return;
        }

        milkProduction.cancel(LocalDateTime.now(), null);
        milkProductionPersistencePort.save(milkProduction);

    }

    /**
     * Consulta de produções por período
     */
    @Override
    public PageResult<MilkProductionResponseVO> getMilkProductions(
            Long farmId,
            String goatId,
            LocalDate from,
            LocalDate to,
            PageQuery pageQuery,
            boolean includeCanceled
    ) {
        goatGenderValidator.requireFemale(farmId, goatId);
        PageResult<MilkProduction> productions =
                milkProductionPersistencePort.search(
                        farmId,
                        goatId,
                        from,
                        to,
                        pageQuery,
                        includeCanceled
                );
        return productions.map(milkProductionMapper::toResponseVO);
    }


    /* ==========================================================
       Regras de domínio (assinaturas internas)
       ========================================================== */

    /**
     * Regra 1:
     * Não permitir produção duplicada para a mesma data e turno
     */
    private void validateNoDuplicateProduction(
            GoatId goatId,
            LocalDate date,
            MilkingShift shift
    ) {
        if (milkProductionPersistencePort.existsActiveByGoatTechnicalIdAndDateAndShift(goatId, date, shift)) {
            throw new DuplicateMilkProductionException();
        }
    }

    /**
     * Regra 2:
     * Produção só pode existir se houver lactação ativa
     */
    private Lactation getRequiredActiveLactation(
            GoatId goatId
    ) {
        return lactationPersistencePort
                .findActiveByGoatTechnicalId(goatId)
                .orElseThrow(NoActiveLactationException::new);
    }

    private long requireFarmId(Long farmId) {
        if (farmId == null || farmId <= 0) {
            throw new InvalidArgumentException("farmId", "Identificador de fazenda inválido.");
        }
        return farmId;
    }

    private void applyMilkWithdrawalSnapshot(MilkProduction milkProduction, GoatWithdrawalStatusVO status) {
        if (status == null || !status.hasActiveMilkWithdrawal() || status.milkWithdrawal() == null) {
            milkProduction.applyWithdrawalSnapshot(null, null, null);
            return;
        }

        String productName = status.milkWithdrawal().productName() != null && !status.milkWithdrawal().productName().isBlank()
                ? status.milkWithdrawal().productName()
                : status.milkWithdrawal().title();
        milkProduction.applyWithdrawalSnapshot(status.milkWithdrawal().eventId(),
                status.milkWithdrawal().withdrawalEndDate(), productName);
    }

}

