package com.devmaster.goatfarm.milk.business.lactationservice;

import com.devmaster.goatfarm.milk.application.ports.in.LactationCommandUseCase;
import com.devmaster.goatfarm.milk.application.ports.in.LactationQueryUseCase;
import com.devmaster.goatfarm.milk.application.ports.out.LactationPersistencePort;
import com.devmaster.goatfarm.milk.application.ports.out.MilkProductionPersistencePort;
import com.devmaster.goatfarm.milk.application.ports.out.PregnancySnapshotQueryPort;
import com.devmaster.goatfarm.application.core.business.validation.GoatGenderValidator;
import com.devmaster.goatfarm.milk.business.bo.LactationRequestVO;
import com.devmaster.goatfarm.milk.business.bo.LactationResponseVO;
import com.devmaster.goatfarm.milk.business.bo.LactationDryRequestVO;
import com.devmaster.goatfarm.milk.business.bo.LactationDryOffAlertVO;
import com.devmaster.goatfarm.milk.business.bo.LactationSummaryResponseVO;
import com.devmaster.goatfarm.milk.business.mapper.LactationBusinessMapper;
import com.devmaster.goatfarm.sharedkernel.pregnancy.PregnancySnapshot;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.milk.enums.LactationStatus;
import com.devmaster.goatfarm.milk.persistence.entity.Lactation;
import com.devmaster.goatfarm.milk.persistence.entity.MilkProduction;
import com.devmaster.goatfarm.milk.persistence.projection.LactationDryOffAlertProjection;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class LactationBusiness implements LactationCommandUseCase, LactationQueryUseCase {

    private static final int DEFAULT_DRY_OFF_BEFORE_DUE_DAYS = 90;

    private final LactationPersistencePort lactationPersistencePort;
    private final MilkProductionPersistencePort milkProductionPersistencePort;
    private final PregnancySnapshotQueryPort pregnancySnapshotQueryPort;
    private final GoatGenderValidator goatGenderValidator;
    private final LactationBusinessMapper lactationMapper;

    public LactationBusiness(LactationPersistencePort lactationPersistencePort,
                             MilkProductionPersistencePort milkProductionPersistencePort,
                             PregnancySnapshotQueryPort pregnancySnapshotQueryPort,
                             GoatGenderValidator goatGenderValidator,
                             LactationBusinessMapper lactationMapper) {
        this.lactationPersistencePort = lactationPersistencePort;
        this.milkProductionPersistencePort = milkProductionPersistencePort;
        this.pregnancySnapshotQueryPort = pregnancySnapshotQueryPort;
        this.goatGenderValidator = goatGenderValidator;
        this.lactationMapper = lactationMapper;
    }

    @Override
    public LactationResponseVO openLactation(Long farmId, String goatId, LactationRequestVO vo) {
        goatGenderValidator.requireFemaleAndActive(farmId, goatId);
        if (vo.getStartDate() != null && vo.getStartDate().isAfter(LocalDate.now())) {
             throw new InvalidArgumentException("startDate", "Data de inÃ­cio da lactaÃ§Ã£o nÃ£o pode ser futura.");
        }

        Optional<Lactation> activeLactation = lactationPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId);
        if (activeLactation.isPresent()) {
            throw new BusinessRuleException("JÃ¡ existe uma lactaÃ§Ã£o ativa para esta cabra.");
        }
        
        Optional<Lactation> latestLactation = lactationPersistencePort.findAllByFarmIdAndGoatId(
                farmId,
                goatId,
                PageRequest.of(0, 1, Sort.by(Sort.Order.desc("startDate"), Sort.Order.desc("id")))
        ).stream().findFirst();

        Optional<PregnancySnapshot> pregnancySnapshot = pregnancySnapshotQueryPort
                .findLatestByFarmIdAndGoatId(farmId, goatId, vo.getStartDate());

        if (latestLactation.isPresent()
                && latestLactation.get().getStatus() == LactationStatus.DRY
                && pregnancySnapshot.map(PregnancySnapshot::active).orElse(false)) {
            throw new BusinessRuleException("Nao e permitido abrir nova lactacao enquanto houver prenhez ativa apos secagem confirmada.");
        }

        Lactation entity = new Lactation();
        entity.setFarmId(farmId);
        entity.setGoatId(goatId);
        entity.setStartDate(vo.getStartDate());
        entity.setStatus(LactationStatus.ACTIVE);
        entity.setEndDate(null);

        Lactation saved = lactationPersistencePort.save(entity);
        return lactationMapper.toResponseVO(saved);
    }

    @Override
    public LactationResponseVO dryLactation(Long farmId, String goatId, Long lactationId, LactationDryRequestVO vo) {
        goatGenderValidator.requireFemaleAndActive(farmId, goatId);
        Lactation lactation = lactationPersistencePort.findByIdAndFarmIdAndGoatId(lactationId, farmId, goatId)
                .orElseThrow(() -> new ResourceNotFoundException("LactaÃ§Ã£o nÃ£o encontrada para esta cabra"));

        if (lactation.getStatus() != LactationStatus.ACTIVE) {
            throw new BusinessRuleException("LactaÃ§Ã£o nÃ£o estÃ¡ ativa.");
        }

        if (vo.getEndDate() == null) {
            throw new BusinessRuleException("Data de fim da lactaÃ§Ã£o Ã© obrigatÃ³ria.");
        }

        if (vo.getEndDate().isBefore(lactation.getStartDate())) {
            throw new BusinessRuleException("Data de fim da lactaÃ§Ã£o nÃ£o pode ser anterior Ã  data de inÃ­cio.");
        }

        lactation.setStatus(LactationStatus.DRY);
        lactation.setEndDate(vo.getEndDate());
        lactation.setDryStartDate(vo.getEndDate()); // Assumindo dryStartDate = endDate da lactaÃ§Ã£o

        Lactation saved = lactationPersistencePort.save(lactation);
        return lactationMapper.toResponseVO(saved);
    }

    @Override
    public LactationResponseVO resumeLactation(Long farmId, String goatId, Long lactationId) {
        goatGenderValidator.requireFemaleAndActive(farmId, goatId);

        Lactation lactation = lactationPersistencePort.findByIdAndFarmIdAndGoatId(lactationId, farmId, goatId)
                .orElseThrow(() -> new ResourceNotFoundException("Lactacao nao encontrada para esta cabra"));

        if (lactation.getStatus() != LactationStatus.DRY) {
            throw new BusinessRuleException("Apenas lactacoes secadas podem ser retomadas.");
        }

        Optional<Lactation> activeLactation = lactationPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId);
        if (activeLactation.isPresent() && !activeLactation.get().getId().equals(lactationId)) {
            throw new BusinessRuleException("Ja existe uma lactacao ativa para esta cabra.");
        }

        Optional<PregnancySnapshot> pregnancySnapshot = pregnancySnapshotQueryPort
                .findLatestByFarmIdAndGoatId(farmId, goatId, LocalDate.now());

        if (pregnancySnapshot.map(PregnancySnapshot::active).orElse(false)) {
            throw new BusinessRuleException("Nao e permitido retomar lactacao com prenhez ativa.");
        }

        if (pregnancySnapshot.isPresent()
                && "BIRTH".equalsIgnoreCase(pregnancySnapshot.get().closeReason())) {
            throw new BusinessRuleException("Nao e permitido retomar lactacao apos parto. Inicie uma nova lactacao para o novo ciclo.");
        }

        lactation.setStatus(LactationStatus.ACTIVE);
        lactation.setEndDate(null);
        lactation.setDryStartDate(null);

        Lactation saved = lactationPersistencePort.save(lactation);
        return lactationMapper.toResponseVO(saved);
    }

    @Override
    public LactationResponseVO getActiveLactation(Long farmId, String goatId) {
        goatGenderValidator.requireFemale(farmId, goatId);
        Lactation lactation = lactationPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId)
                .orElseThrow(() -> new ResourceNotFoundException("Nenhuma lactaÃ§Ã£o ativa encontrada para esta cabra"));
        return lactationMapper.toResponseVO(lactation);
    }

    @Override
    public LactationSummaryResponseVO getActiveLactationSummary(Long farmId, String goatId) {
        goatGenderValidator.requireFemale(farmId, goatId);
        Lactation lactation = lactationPersistencePort.findActiveByFarmIdAndGoatId(farmId, goatId)
                .orElseThrow(() -> new ResourceNotFoundException("Nenhuma lactaÃ§Ã£o ativa encontrada para esta cabra"));
        return buildSummary(farmId, goatId, lactation);
    }

    @Override
    public LactationResponseVO getLactationById(Long farmId, String goatId, Long lactationId) {
        goatGenderValidator.requireFemale(farmId, goatId);
        Lactation lactation = lactationPersistencePort.findByIdAndFarmIdAndGoatId(lactationId, farmId, goatId)
                .orElseThrow(() -> new ResourceNotFoundException("LactaÃ§Ã£o nÃ£o encontrada"));
        return lactationMapper.toResponseVO(lactation);
    }

    @Override
    public LactationSummaryResponseVO getLactationSummary(Long farmId, String goatId, Long lactationId) {
        Lactation lactation = lactationPersistencePort.findByIdAndFarmIdAndGoatId(lactationId, farmId, goatId)
                .orElseThrow(() -> new ResourceNotFoundException("LactaÃ§Ã£o nÃ£o encontrada para esta cabra/fazenda."));
        goatGenderValidator.requireFemale(farmId, goatId);
        return buildSummary(farmId, goatId, lactation);
    }

    @Override
    public Page<LactationResponseVO> getAllLactations(Long farmId, String goatId, Pageable pageable) {
        goatGenderValidator.requireFemale(farmId, goatId);
        Page<Lactation> page = lactationPersistencePort.findAllByFarmIdAndGoatId(farmId, goatId, pageable);
        return page.map(lactationMapper::toResponseVO);
    }

    @Override
    public Page<LactationDryOffAlertVO> getDryOffAlerts(Long farmId, LocalDate referenceDate, Pageable pageable) {
        LocalDate reference = referenceDate != null ? referenceDate : LocalDate.now();
        return lactationPersistencePort.findDryOffAlerts(farmId, reference, 90, pageable)
                .map(alert -> toDryOffAlertVO(alert, reference));
    }

    private LactationSummaryResponseVO buildSummary(Long farmId, String goatId, Lactation lactation) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = lactation.getStartDate();
        LocalDate endDate = lactation.getEndDate() != null ? lactation.getEndDate() : today;

        List<MilkProduction> productions = milkProductionPersistencePort.findByFarmIdAndGoatIdAndDateBetween(
                farmId,
                goatId,
                startDate,
                endDate
        );

        BigDecimal totalLiters = productions.stream()
                .map(MilkProduction::getVolumeLiters)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long daysMeasured = productions.stream()
                .map(MilkProduction::getDate)
                .distinct()
                .count();

        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        int daysInLactation = (int) Math.max(daysBetween, 0) + 1;

        BigDecimal averagePerDay = daysMeasured > 0
                ? totalLiters.divide(BigDecimal.valueOf(daysMeasured), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<LocalDate, BigDecimal> totalsByDate = new HashMap<>();
        for (MilkProduction production : productions) {
            if (production.getDate() == null || production.getVolumeLiters() == null) {
                continue;
            }
            totalsByDate.merge(production.getDate(), production.getVolumeLiters(), BigDecimal::add);
        }

        BigDecimal peakLiters = BigDecimal.ZERO;
        LocalDate peakDate = null;
        for (Map.Entry<LocalDate, BigDecimal> entry : totalsByDate.entrySet()) {
            if (peakDate == null || entry.getValue().compareTo(peakLiters) > 0) {
                peakDate = entry.getKey();
                peakLiters = entry.getValue();
            }
        }

        LactationSummaryResponseVO.LactationSummaryPregnancyVO pregnancyVO = buildPregnancySnapshot(
                farmId,
                goatId,
                lactation,
                today
        );

        LactationSummaryResponseVO.LactationSummaryLactationVO lactationVO = LactationSummaryResponseVO.LactationSummaryLactationVO.builder()
                .lactationId(lactation.getId())
                .goatId(lactation.getGoatId())
                .startDate(lactation.getStartDate())
                .endDate(lactation.getEndDate())
                .status(lactation.getStatus())
                .build();

        LactationSummaryResponseVO.LactationSummaryProductionVO productionVO = LactationSummaryResponseVO.LactationSummaryProductionVO.builder()
                .totalLiters(totalLiters)
                .daysInLactation(daysInLactation)
                .daysMeasured((int) daysMeasured)
                .averagePerDay(averagePerDay)
                .peakLiters(peakLiters)
                .peakDate(peakDate)
                .build();

        return LactationSummaryResponseVO.builder()
                .lactation(lactationVO)
                .production(productionVO)
                .pregnancy(pregnancyVO)
                .build();
    }

    private LactationSummaryResponseVO.LactationSummaryPregnancyVO buildPregnancySnapshot(Long farmId,
                                                                                           String goatId,
                                                                                           Lactation lactation,
                                                                                           LocalDate referenceDate) {
        Optional<PregnancySnapshot> snapshot = pregnancySnapshotQueryPort
                .findLatestByFarmIdAndGoatId(farmId, goatId, referenceDate);
        if (snapshot.isEmpty()) {
            return null;
        }

        PregnancySnapshot pregnancySnapshot = snapshot.get();

        LocalDate gestationReferenceDate = pregnancySnapshot.breedingDate() != null
                ? pregnancySnapshot.breedingDate()
                : pregnancySnapshot.confirmDate();
        if (gestationReferenceDate == null) {
            return null;
        }

        int gestationDays = (int) ChronoUnit.DAYS.between(gestationReferenceDate, referenceDate);
        boolean lactationActive = lactation.getStatus() == LactationStatus.ACTIVE;
        int dryAtDaysBeforeDue = lactation.getDryAtPregnancyDays() != null
                ? lactation.getDryAtPregnancyDays()
                : DEFAULT_DRY_OFF_BEFORE_DUE_DAYS;
        boolean recommendDryOff = pregnancySnapshot.active() && lactationActive && gestationDays >= dryAtDaysBeforeDue;
        LocalDate recommendedDryOffDate = pregnancySnapshot.active() ? gestationReferenceDate.plusDays(dryAtDaysBeforeDue) : null;

        String message;
        if (!pregnancySnapshot.active()) {
            message = "NÃ£o hÃ¡ prenhez ativa para recomendar secagem.";
        } else if (recommendDryOff) {
            message = "Prenhez confirmada com " + gestationDays + " dias. Recomenda-se secagem.";
        } else if (!lactationActive) {
            message = "Prenhez confirmada com " + gestationDays + " dias. LactaÃ§Ã£o nÃ£o estÃ¡ ativa.";
        } else {
            message = "Prenhez confirmada com " + gestationDays + " dias. Secagem ainda nÃ£o recomendada.";
        }

        return LactationSummaryResponseVO.LactationSummaryPregnancyVO.builder()
                .gestationDays(gestationDays)
                .dryOffRecommendation(recommendDryOff)
                .recommendedDryOffDate(recommendedDryOffDate)
                .message(message)
                .build();
    }

    private LactationDryOffAlertVO toDryOffAlertVO(LactationDryOffAlertProjection alert, LocalDate referenceDate) {
        LocalDate gestationStartDate = alert.getStartDatePregnancy();
        if (gestationStartDate == null) {
            throw new IllegalStateException("startDatePregnancy deve estar preenchida para alerta de secagem");
        }
        LocalDate dryOffDate = alert.getDryOffDate();
        if (dryOffDate == null) {
            throw new IllegalStateException("dryOffDate deve estar preenchida para alerta de secagem");
        }
        int dryAtPregnancyDays = alert.getDryAtPregnancyDays() != null
                ? alert.getDryAtPregnancyDays()
                : DEFAULT_DRY_OFF_BEFORE_DUE_DAYS;
        int gestationDays = (int) Math.max(0L, ChronoUnit.DAYS.between(gestationStartDate, referenceDate));
        int daysOverdue = Math.max(0, gestationDays - dryAtPregnancyDays);
        boolean dryOffRecommendation = gestationDays >= dryAtPregnancyDays;

        return LactationDryOffAlertVO.builder()
                .lactationId(alert.getLactationId())
                .goatId(alert.getGoatId())
                .startDatePregnancy(gestationStartDate)
                .breedingDate(alert.getBreedingDate())
                .confirmDate(alert.getConfirmDate())
                .dryOffDate(dryOffDate)
                .dryAtPregnancyDays(dryAtPregnancyDays)
                .gestationDays(gestationDays)
                .daysOverdue(daysOverdue)
                .dryOffRecommendation(dryOffRecommendation)
                .build();
    }
}