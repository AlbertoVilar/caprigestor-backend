package com.devmaster.goatfarm.milk.business.milkproductionservice;

import com.devmaster.goatfarm.milk.application.ports.in.MilkProductionUseCase;
import com.devmaster.goatfarm.milk.application.ports.out.LactationPersistencePort;
import com.devmaster.goatfarm.milk.application.ports.out.MilkProductionPersistencePort;
import com.devmaster.goatfarm.application.core.business.validation.GoatGenderValidator;
import com.devmaster.goatfarm.config.exceptions.NoActiveLactationException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.health.application.ports.in.HealthWithdrawalQueryUseCase;
import com.devmaster.goatfarm.health.business.bo.GoatWithdrawalStatusVO;
import com.devmaster.goatfarm.milk.business.bo.MilkProductionRequestVO;
import com.devmaster.goatfarm.milk.business.bo.MilkProductionResponseVO;
import com.devmaster.goatfarm.milk.business.bo.MilkProductionUpdateRequestVO;
import com.devmaster.goatfarm.config.exceptions.DuplicateMilkProductionException;
import com.devmaster.goatfarm.milk.enums.MilkProductionStatus;
import com.devmaster.goatfarm.milk.enums.MilkingShift;
import com.devmaster.goatfarm.milk.business.mapper.MilkProductionBusinessMapper;
import com.devmaster.goatfarm.milk.persistence.entity.Lactation;
import com.devmaster.goatfarm.milk.persistence.entity.MilkProduction;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MilkProductionBusiness implements MilkProductionUseCase {

    /** Ports (infra abstraÃ­da) */
    private final MilkProductionPersistencePort milkProductionPersistencePort;
    private final LactationPersistencePort lactationPersistencePort;
    private final GoatGenderValidator goatGenderValidator;
    private final HealthWithdrawalQueryUseCase healthWithdrawalQueryUseCase;

    /** Mapper de domÃ­nio */
    private final MilkProductionBusinessMapper milkProductionMapper;

    public MilkProductionBusiness(MilkProductionPersistencePort milkProductionPersistencePort,
                                  LactationPersistencePort lactationPersistencePort,
                                  GoatGenderValidator goatGenderValidator,
                                  HealthWithdrawalQueryUseCase healthWithdrawalQueryUseCase,
                                  MilkProductionBusinessMapper milkProductionMapper) {
        this.milkProductionPersistencePort = milkProductionPersistencePort;
        this.lactationPersistencePort = lactationPersistencePort;
        this.goatGenderValidator = goatGenderValidator;
        this.healthWithdrawalQueryUseCase = healthWithdrawalQueryUseCase;
        this.milkProductionMapper = milkProductionMapper;
    }

    /**
     * CriaÃ§Ã£o de produÃ§Ã£o diÃ¡ria de leite
     */
    @Override
    public MilkProductionResponseVO createMilkProduction(
            Long farmId,
            String goatId,
            MilkProductionRequestVO requestVO
    ) {
        goatGenderValidator.requireFemaleAndActive(farmId, goatId);
        //=======================
        // *** VALIDAÃ‡ÃƒO *** //
        //=======================
        
        if (requestVO.getDate() == null) {
            throw new InvalidArgumentException("date", "Data da ordenha Ã© obrigatÃ³ria");
        }
        if (requestVO.getDate().isAfter(LocalDate.now())) {
            throw new InvalidArgumentException("date", "Data da ordenha nÃ£o pode ser futura");
        }

        // Regra 1: NÃ£o permitir produÃ§Ã£o duplicada para a mesma data e turno
        validateNoDuplicateProduction(farmId, goatId, requestVO.getDate(), requestVO.getShift());
        GoatWithdrawalStatusVO withdrawalStatus = healthWithdrawalQueryUseCase.getGoatWithdrawalStatus(
                farmId,
                goatId,
                requestVO.getDate()
        );
        Lactation lactation = getRequiredActiveLactation(farmId, goatId, requestVO.getDate());

        MilkProduction milkProduction = milkProductionMapper.toEntity(requestVO);
        milkProduction.setFarmId(farmId);
        milkProduction.setGoatId(goatId);
        milkProduction.setLactation(lactation);
        milkProduction.setStatus(MilkProductionStatus.ACTIVE);
        milkProduction.setCanceledAt(null);
        milkProduction.setCanceledReason(null);
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
                .orElseThrow(() -> new ResourceNotFoundException("ProduÃ§Ã£o de leite nÃ£o encontrada com o ID: " + id));
        if (milkProduction.getStatus() == MilkProductionStatus.CANCELED) {
            throw new BusinessRuleException("status", "Registro cancelado nao pode ser alterado.");
        }


        if (request.getVolumeLiters() != null) {
            milkProduction.setVolumeLiters(request.getVolumeLiters());
        }
        if (request.getNotes() != null) {
            milkProduction.setNotes(request.getNotes());
        }

        MilkProduction saved = milkProductionPersistencePort.save(milkProduction);
        return milkProductionMapper.toResponseVO(saved);
    }

    @Override
    public MilkProductionResponseVO findById(Long farmId, String goatId, Long id) {
        goatGenderValidator.requireFemale(farmId, goatId);
        MilkProduction milkProduction = milkProductionPersistencePort.findById(farmId, goatId, id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "ProduÃ§Ã£o de leite nÃ£o encontrada com o ID: " + id
                        )
                );

        return milkProductionMapper.toResponseVO(milkProduction);
    }


    @Override
    public void delete(Long farmId, String goatId, Long id) {
        goatGenderValidator.requireFemaleAndActive(farmId, goatId);
        MilkProduction milkProduction = milkProductionPersistencePort.findById(farmId, goatId, id)
                .orElseThrow(() -> new ResourceNotFoundException("ProduÃ§Ã£o de leite nÃ£o encontrada com o ID: " + id));

        if (milkProduction.getStatus() == MilkProductionStatus.CANCELED) {
            return;
        }

        milkProduction.setStatus(MilkProductionStatus.CANCELED);
        milkProduction.setCanceledAt(LocalDateTime.now());
        milkProduction.setCanceledReason(null);
        milkProductionPersistencePort.save(milkProduction);

    }

    /**
     * Consulta de produÃ§Ãµes por perÃ­odo
     */
    @Override
    public Page<MilkProductionResponseVO> getMilkProductions(
            Long farmId,
            String goatId,
            LocalDate from,
            LocalDate to,
            Pageable pageable,
            boolean includeCanceled
    ) {
        goatGenderValidator.requireFemale(farmId, goatId);
        Page<MilkProduction> productions =
                milkProductionPersistencePort.search(
                        farmId,
                        goatId,
                        from,
                        to,
                        pageable,
                        includeCanceled
                );
        return productions.map(milkProductionMapper::toResponseVO);
    }


    /* ==========================================================
       Regras de domÃ­nio (assinaturas internas)
       ========================================================== */

    /**
     * Regra 1:
     * NÃ£o permitir produÃ§Ã£o duplicada para a mesma data e turno
     */
    private void validateNoDuplicateProduction(
            Long farmId,
            String goatId,
            LocalDate date,
            MilkingShift shift
    ) {
        // implementaÃ§Ã£o futura
        if (milkProductionPersistencePort.existsByFarmIdAndGoatIdAndDateAndShift(farmId, goatId, date, shift)) {
            throw new DuplicateMilkProductionException();
        }
    }

    /**
     * Regra 2:
     * ProduÃ§Ã£o sÃ³ pode existir se houver lactaÃ§Ã£o ativa
     */
    private Lactation getRequiredActiveLactation(
            Long farmId,
            String goatId,
            LocalDate productionDate
    ) {
        return lactationPersistencePort
                .findActiveByFarmIdAndGoatId(farmId, goatId)
                .orElseThrow(NoActiveLactationException::new);
    }

    private void applyMilkWithdrawalSnapshot(MilkProduction milkProduction, GoatWithdrawalStatusVO status) {
        if (status == null || !status.hasActiveMilkWithdrawal() || status.milkWithdrawal() == null) {
            milkProduction.setRecordedDuringMilkWithdrawal(false);
            milkProduction.setMilkWithdrawalEventId(null);
            milkProduction.setMilkWithdrawalEndDate(null);
            milkProduction.setMilkWithdrawalSource(null);
            return;
        }

        String productName = status.milkWithdrawal().productName() != null && !status.milkWithdrawal().productName().isBlank()
                ? status.milkWithdrawal().productName()
                : status.milkWithdrawal().title();
        milkProduction.setRecordedDuringMilkWithdrawal(true);
        milkProduction.setMilkWithdrawalEventId(status.milkWithdrawal().eventId());
        milkProduction.setMilkWithdrawalEndDate(status.milkWithdrawal().withdrawalEndDate());
        milkProduction.setMilkWithdrawalSource(productName);
    }

}

