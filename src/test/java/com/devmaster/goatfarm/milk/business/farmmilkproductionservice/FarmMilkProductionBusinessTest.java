package com.devmaster.goatfarm.milk.business.farmmilkproductionservice;

import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.milk.application.ports.out.FarmMilkProductionPersistencePort;
import com.devmaster.goatfarm.milk.business.bo.FarmMilkProductionAnnualSummaryVO;
import com.devmaster.goatfarm.milk.business.bo.FarmMilkProductionDailySummaryVO;
import com.devmaster.goatfarm.milk.business.bo.FarmMilkProductionMonthlySummaryVO;
import com.devmaster.goatfarm.milk.business.bo.FarmMilkProductionUpsertRequestVO;
import com.devmaster.goatfarm.milk.persistence.entity.FarmMilkProduction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FarmMilkProductionBusinessTest {

    @Mock
    private FarmMilkProductionPersistencePort persistencePort;

    @Mock
    private GoatFarmPersistencePort goatFarmPersistencePort;

    @InjectMocks
    private FarmMilkProductionBusiness business;

    @BeforeEach
    void setUp() {
        lenient().when(goatFarmPersistencePort.findById(anyLong())).thenReturn(Optional.of(new GoatFarm()));
    }

    @Test
    void shouldUpsertDailyProductionDerivingMarketableVolume() {
        LocalDate date = LocalDate.of(2026, 3, 30);
        FarmMilkProductionUpsertRequestVO request = new FarmMilkProductionUpsertRequestVO(
                new BigDecimal("180.00"),
                new BigDecimal("12.50"),
                null,
                "Ordenha total do dia"
        );

        when(persistencePort.upsertDaily(eq(17L), eq(date), any(), any(), any(), eq("Ordenha total do dia")))
                .thenReturn(buildRecord(date, "180.00", "12.50", "167.50"));

        FarmMilkProductionDailySummaryVO response = business.upsertDailyProduction(17L, date, request);

        assertTrue(response.registered());
        assertEquals(new BigDecimal("180.00"), response.totalProduced());
        assertEquals(new BigDecimal("12.50"), response.withdrawalProduced());
        assertEquals(new BigDecimal("167.50"), response.marketableProduced());
        verify(persistencePort).upsertDaily(eq(17L), eq(date), any(), any(), any(), eq("Ordenha total do dia"));
    }

    @Test
    void shouldThrowWhenVolumesAreIncoherent() {
        LocalDate date = LocalDate.of(2026, 3, 30);
        FarmMilkProductionUpsertRequestVO request = new FarmMilkProductionUpsertRequestVO(
                new BigDecimal("100.00"),
                new BigDecimal("10.00"),
                new BigDecimal("95.00"),
                null
        );

        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> business.upsertDailyProduction(17L, date, request)
        );

        assertEquals("marketableProduced", exception.getFieldName());
        verify(persistencePort, never()).upsertDaily(anyLong(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldDeriveWithdrawalWhenOnlyMarketableVolumeIsProvided() {
        LocalDate date = LocalDate.of(2026, 3, 30);
        FarmMilkProductionUpsertRequestVO request = new FarmMilkProductionUpsertRequestVO(
                new BigDecimal("180.00"),
                null,
                new BigDecimal("150.00"),
                "Ajuste QA"
        );

        when(persistencePort.upsertDaily(eq(17L), eq(date), any(), any(), any(), eq("Ajuste QA")))
                .thenAnswer(invocation -> buildRecord(
                        date,
                        invocation.getArgument(2).toString(),
                        invocation.getArgument(3).toString(),
                        invocation.getArgument(4).toString()
                ));

        FarmMilkProductionDailySummaryVO response = business.upsertDailyProduction(17L, date, request);

        assertEquals(new BigDecimal("180.00"), response.totalProduced());
        assertEquals(new BigDecimal("30.00"), response.withdrawalProduced());
        assertEquals(new BigDecimal("150.00"), response.marketableProduced());
    }

    @Test
    void shouldRejectFutureProductionDate() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        FarmMilkProductionUpsertRequestVO request = new FarmMilkProductionUpsertRequestVO(
                new BigDecimal("100.00"),
                new BigDecimal("10.00"),
                null,
                null
        );

        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> business.upsertDailyProduction(17L, tomorrow, request)
        );

        assertEquals("productionDate", exception.getFieldName());
        verify(persistencePort, never()).upsertDaily(anyLong(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldRejectNegativeWithdrawalVolume() {
        LocalDate date = LocalDate.of(2026, 3, 30);
        FarmMilkProductionUpsertRequestVO request = new FarmMilkProductionUpsertRequestVO(
                new BigDecimal("100.00"),
                new BigDecimal("-1.00"),
                null,
                null
        );

        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> business.upsertDailyProduction(17L, date, request)
        );

        assertEquals("withdrawalProduced", exception.getFieldName());
        verify(persistencePort, never()).upsertDaily(anyLong(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldRejectValuesWithMoreThanTwoDecimalPlaces() {
        LocalDate date = LocalDate.of(2026, 3, 30);
        FarmMilkProductionUpsertRequestVO request = new FarmMilkProductionUpsertRequestVO(
                new BigDecimal("100.001"),
                null,
                null,
                null
        );

        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> business.upsertDailyProduction(17L, date, request)
        );

        assertEquals("totalProduced", exception.getFieldName());
        verify(persistencePort, never()).upsertDaily(anyLong(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldReturnEmptyDailySummaryWhenRecordDoesNotExist() {
        LocalDate date = LocalDate.of(2026, 3, 30);
        when(persistencePort.findByFarmIdAndProductionDate(17L, date)).thenReturn(Optional.empty());

        FarmMilkProductionDailySummaryVO response = business.getDailySummary(17L, date);

        assertFalse(response.registered());
        assertEquals(new BigDecimal("0.00"), response.totalProduced());
        assertEquals(new BigDecimal("0.00"), response.withdrawalProduced());
        assertEquals(new BigDecimal("0.00"), response.marketableProduced());
    }

    @Test
    void shouldAggregateMonthlySummary() {
        when(persistencePort.findByFarmIdAndProductionDateBetween(
                eq(17L),
                eq(LocalDate.of(2026, 3, 1)),
                eq(LocalDate.of(2026, 3, 31))
        )).thenReturn(List.of(
                buildRecord(LocalDate.of(2026, 3, 28), "150.00", "10.00", "140.00"),
                buildRecord(LocalDate.of(2026, 3, 29), "165.50", "5.50", "160.00")
        ));

        FarmMilkProductionMonthlySummaryVO response = business.getMonthlySummary(17L, 2026, 3);

        assertEquals(2, response.daysRegistered());
        assertEquals(new BigDecimal("315.50"), response.totalProduced());
        assertEquals(new BigDecimal("15.50"), response.withdrawalProduced());
        assertEquals(new BigDecimal("300.00"), response.marketableProduced());
        assertEquals(2, response.dailyRecords().size());
    }

    @Test
    void shouldAggregateAnnualSummaryByMonth() {
        when(persistencePort.findByFarmIdAndProductionDateBetween(
                eq(17L),
                eq(LocalDate.of(2026, 1, 1)),
                eq(LocalDate.of(2026, 12, 31))
        )).thenReturn(List.of(
                buildRecord(LocalDate.of(2026, 1, 10), "100.00", "0.00", "100.00"),
                buildRecord(LocalDate.of(2026, 3, 28), "150.00", "10.00", "140.00"),
                buildRecord(LocalDate.of(2026, 3, 29), "165.50", "5.50", "160.00")
        ));

        FarmMilkProductionAnnualSummaryVO response = business.getAnnualSummary(17L, 2026);

        assertEquals(3, response.daysRegistered());
        assertEquals(new BigDecimal("415.50"), response.totalProduced());
        assertEquals(new BigDecimal("15.50"), response.withdrawalProduced());
        assertEquals(new BigDecimal("400.00"), response.marketableProduced());
        assertEquals(12, response.monthlyRecords().size());
        assertEquals(new BigDecimal("315.50"), response.monthlyRecords().get(2).totalProduced());
    }

    private FarmMilkProduction buildRecord(LocalDate date, String total, String withdrawal, String marketable) {
        return FarmMilkProduction.builder()
                .id(Math.abs(date.toEpochDay()))
                .farmId(17L)
                .productionDate(date)
                .totalProduced(new BigDecimal(total))
                .withdrawalProduced(new BigDecimal(withdrawal))
                .marketableProduced(new BigDecimal(marketable))
                .notes("QA")
                .updatedAt(LocalDateTime.of(2026, 3, 30, 8, 0))
                .build();
    }
}
