package com.devmaster.goatfarm.milk.business.farmmilkproductionservice;

import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.milk.application.ports.in.FarmMilkProductionUseCase;
import com.devmaster.goatfarm.milk.application.ports.out.FarmMilkProductionPersistencePort;
import com.devmaster.goatfarm.milk.business.bo.*;
import com.devmaster.goatfarm.milk.persistence.entity.FarmMilkProduction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class FarmMilkProductionBusiness implements FarmMilkProductionUseCase {

    private final FarmMilkProductionPersistencePort persistencePort;
    private final GoatFarmPersistencePort goatFarmPersistencePort;

    public FarmMilkProductionBusiness(
            FarmMilkProductionPersistencePort persistencePort,
            GoatFarmPersistencePort goatFarmPersistencePort
    ) {
        this.persistencePort = persistencePort;
        this.goatFarmPersistencePort = goatFarmPersistencePort;
    }

    @Override
    @Transactional
    public FarmMilkProductionDailySummaryVO upsertDailyProduction(
            Long farmId,
            LocalDate productionDate,
            FarmMilkProductionUpsertRequestVO requestVO
    ) {
        resolveFarm(farmId);
        LocalDate safeDate = validateProductionDate(productionDate);
        if (requestVO == null) {
            throw new InvalidArgumentException("request", "Payload da requisicao e obrigatorio.");
        }

        NormalizedVolumes normalizedVolumes = normalizeVolumes(requestVO);
        String normalizedNotes = normalizeText(requestVO.notes());
        FarmMilkProduction saved = persistencePort.upsertDaily(
                farmId,
                safeDate,
                normalizedVolumes.totalProduced(),
                normalizedVolumes.withdrawalProduced(),
                normalizedVolumes.marketableProduced(),
                normalizedNotes
        );
        return toDailySummary(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public FarmMilkProductionDailySummaryVO getDailySummary(Long farmId, LocalDate productionDate) {
        resolveFarm(farmId);
        LocalDate safeDate = validateProductionDate(productionDate);

        return persistencePort.findByFarmIdAndProductionDate(farmId, safeDate)
                .map(this::toDailySummary)
                .orElseGet(() -> new FarmMilkProductionDailySummaryVO(
                        safeDate,
                        false,
                        zero(),
                        zero(),
                        zero(),
                        null,
                        null
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public FarmMilkProductionMonthlySummaryVO getMonthlySummary(Long farmId, int year, int month) {
        resolveFarm(farmId);

        YearMonth yearMonth = resolveYearMonth(year, month);
        LocalDate from = yearMonth.atDay(1);
        LocalDate to = yearMonth.atEndOfMonth();

        List<FarmMilkProduction> records = persistencePort.findByFarmIdAndProductionDateBetween(farmId, from, to);
        List<FarmMilkProductionMonthlyDayItemVO> dailyRecords = records.stream()
                .map(record -> new FarmMilkProductionMonthlyDayItemVO(
                        record.getProductionDate(),
                        normalizeMoney(record.getTotalProduced()),
                        normalizeMoney(record.getWithdrawalProduced()),
                        normalizeMoney(record.getMarketableProduced()),
                        record.getNotes()
                ))
                .toList();

        BigDecimal totalProduced = dailyRecords.stream()
                .map(FarmMilkProductionMonthlyDayItemVO::totalProduced)
                .reduce(zero(), BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal withdrawalProduced = dailyRecords.stream()
                .map(FarmMilkProductionMonthlyDayItemVO::withdrawalProduced)
                .reduce(zero(), BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal marketableProduced = dailyRecords.stream()
                .map(FarmMilkProductionMonthlyDayItemVO::marketableProduced)
                .reduce(zero(), BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        return new FarmMilkProductionMonthlySummaryVO(
                yearMonth.getYear(),
                yearMonth.getMonthValue(),
                totalProduced,
                withdrawalProduced,
                marketableProduced,
                dailyRecords.size(),
                dailyRecords
        );
    }

    @Override
    @Transactional(readOnly = true)
    public FarmMilkProductionAnnualSummaryVO getAnnualSummary(Long farmId, int year) {
        resolveFarm(farmId);

        YearMonth january = resolveYearMonth(year, 1);
        LocalDate from = january.atDay(1);
        LocalDate to = january.plusMonths(11).atEndOfMonth();

        List<FarmMilkProduction> records = persistencePort.findByFarmIdAndProductionDateBetween(farmId, from, to);
        Map<Integer, List<FarmMilkProduction>> recordsByMonth = new TreeMap<>();
        records.forEach(record -> recordsByMonth.computeIfAbsent(record.getProductionDate().getMonthValue(), ignored -> new ArrayList<>()).add(record));

        List<FarmMilkProductionAnnualMonthItemVO> monthlyRecords = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            List<FarmMilkProduction> monthRecords = recordsByMonth.getOrDefault(month, List.of());
            BigDecimal totalProduced = monthRecords.stream()
                    .map(FarmMilkProduction::getTotalProduced)
                    .map(this::normalizeMoney)
                    .reduce(zero(), BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal withdrawalProduced = monthRecords.stream()
                    .map(FarmMilkProduction::getWithdrawalProduced)
                    .map(this::normalizeMoney)
                    .reduce(zero(), BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal marketableProduced = monthRecords.stream()
                    .map(FarmMilkProduction::getMarketableProduced)
                    .map(this::normalizeMoney)
                    .reduce(zero(), BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);

            monthlyRecords.add(new FarmMilkProductionAnnualMonthItemVO(
                    month,
                    totalProduced,
                    withdrawalProduced,
                    marketableProduced,
                    monthRecords.size()
            ));
        }

        BigDecimal totalProduced = monthlyRecords.stream()
                .map(FarmMilkProductionAnnualMonthItemVO::totalProduced)
                .reduce(zero(), BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal withdrawalProduced = monthlyRecords.stream()
                .map(FarmMilkProductionAnnualMonthItemVO::withdrawalProduced)
                .reduce(zero(), BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal marketableProduced = monthlyRecords.stream()
                .map(FarmMilkProductionAnnualMonthItemVO::marketableProduced)
                .reduce(zero(), BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        int daysRegistered = monthlyRecords.stream()
                .mapToInt(FarmMilkProductionAnnualMonthItemVO::daysRegistered)
                .sum();

        return new FarmMilkProductionAnnualSummaryVO(
                year,
                totalProduced,
                withdrawalProduced,
                marketableProduced,
                daysRegistered,
                monthlyRecords
        );
    }

    private void resolveFarm(Long farmId) {
        if (farmId == null) {
            throw new InvalidArgumentException("farmId", "farmId e obrigatorio.");
        }

        goatFarmPersistencePort.findById(farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Fazenda nao encontrada."));
    }

    private LocalDate validateProductionDate(LocalDate productionDate) {
        if (productionDate == null) {
            throw new InvalidArgumentException("productionDate", "productionDate e obrigatoria.");
        }
        if (productionDate.isAfter(LocalDate.now())) {
            throw new InvalidArgumentException("productionDate", "Data da producao nao pode ser futura.");
        }
        return productionDate;
    }

    private YearMonth resolveYearMonth(int year, int month) {
        try {
            return YearMonth.of(year, month);
        } catch (RuntimeException exception) {
            throw new InvalidArgumentException("month", "Periodo mensal invalido.");
        }
    }

    private NormalizedVolumes normalizeVolumes(FarmMilkProductionUpsertRequestVO requestVO) {
        if (requestVO.totalProduced() == null) {
            throw new InvalidArgumentException("totalProduced", "totalProduced e obrigatorio.");
        }

        validateScale(requestVO.totalProduced(), "totalProduced");
        BigDecimal totalProduced = normalizeMoney(requestVO.totalProduced());
        if (totalProduced.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidArgumentException("totalProduced", "totalProduced nao pode ser negativo.");
        }

        BigDecimal withdrawalProduced = requestVO.withdrawalProduced() == null
                ? null
                : normalizeMoney(validatedValue(requestVO.withdrawalProduced(), "withdrawalProduced"));
        BigDecimal marketableProduced = requestVO.marketableProduced() == null
                ? null
                : normalizeMoney(validatedValue(requestVO.marketableProduced(), "marketableProduced"));

        if (withdrawalProduced != null && withdrawalProduced.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidArgumentException("withdrawalProduced", "withdrawalProduced nao pode ser negativo.");
        }
        if (marketableProduced != null && marketableProduced.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidArgumentException("marketableProduced", "marketableProduced nao pode ser negativo.");
        }

        if (withdrawalProduced == null && marketableProduced == null) {
            withdrawalProduced = zero();
            marketableProduced = totalProduced;
        } else if (withdrawalProduced == null) {
            if (marketableProduced.compareTo(totalProduced) > 0) {
                throw new InvalidArgumentException("marketableProduced", "marketableProduced nao pode ser maior que totalProduced.");
            }
            withdrawalProduced = totalProduced.subtract(marketableProduced).setScale(2, RoundingMode.HALF_UP);
        } else if (marketableProduced == null) {
            if (withdrawalProduced.compareTo(totalProduced) > 0) {
                throw new InvalidArgumentException("withdrawalProduced", "withdrawalProduced nao pode ser maior que totalProduced.");
            }
            marketableProduced = totalProduced.subtract(withdrawalProduced).setScale(2, RoundingMode.HALF_UP);
        } else if (withdrawalProduced.add(marketableProduced).compareTo(totalProduced) != 0) {
            throw new InvalidArgumentException(
                    "marketableProduced",
                    "withdrawalProduced + marketableProduced deve ser igual a totalProduced."
            );
        }

        return new NormalizedVolumes(totalProduced, withdrawalProduced, marketableProduced);
    }

    private BigDecimal validatedValue(BigDecimal value, String fieldName) {
        validateScale(value, fieldName);
        return value;
    }

    private void validateScale(BigDecimal value, String fieldName) {
        if (value != null && value.scale() > 2) {
            throw new InvalidArgumentException(fieldName, fieldName + " deve ter no maximo 2 casas decimais.");
        }
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal zero() {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replaceAll("\\s+", " ");
        return normalized.isBlank() ? null : normalized;
    }

    private FarmMilkProductionDailySummaryVO toDailySummary(FarmMilkProduction entity) {
        return new FarmMilkProductionDailySummaryVO(
                entity.getProductionDate(),
                true,
                normalizeMoney(entity.getTotalProduced()),
                normalizeMoney(entity.getWithdrawalProduced()),
                normalizeMoney(entity.getMarketableProduced()),
                entity.getNotes(),
                entity.getUpdatedAt()
        );
    }

    private record NormalizedVolumes(
            BigDecimal totalProduced,
            BigDecimal withdrawalProduced,
            BigDecimal marketableProduced
    ) {
    }
}
