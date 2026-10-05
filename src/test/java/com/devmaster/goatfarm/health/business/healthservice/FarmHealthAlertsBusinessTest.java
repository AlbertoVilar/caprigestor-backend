package com.devmaster.goatfarm.health.business.healthservice;

import com.devmaster.goatfarm.health.application.ports.in.HealthWithdrawalQueryUseCase;
import com.devmaster.goatfarm.health.application.ports.out.HealthEventPersistencePort;
import com.devmaster.goatfarm.health.application.model.HealthEventWindow;
import com.devmaster.goatfarm.health.application.model.HealthEventRecord;
import com.devmaster.goatfarm.health.business.bo.HealthEventResponseVO;
import com.devmaster.goatfarm.health.business.bo.FarmHealthAlertsResponseVO;
import com.devmaster.goatfarm.health.business.bo.GoatWithdrawalStatusVO;
import com.devmaster.goatfarm.health.business.bo.HealthWithdrawalOriginVO;
import com.devmaster.goatfarm.health.business.mapper.HealthEventBusinessMapper;
import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class FarmHealthAlertsBusinessTest {

    @Mock
    private HealthEventPersistencePort persistencePort;

    @Mock
    private HealthWithdrawalQueryUseCase withdrawalQueryUseCase;

    @Mock
    private HealthEventBusinessMapper mapper;

    @Test
    void getAlerts_shouldIncludeActiveWithdrawalCountsAndTopLists() {
        Long farmId = 17L;
        FarmHealthAlertsBusiness business = new FarmHealthAlertsBusiness(persistencePort, withdrawalQueryUseCase, mapper);

        when(persistencePort.findNextScheduledEvents(eq(farmId), any(), any(), any(), any(), eq(5)))
                .thenReturn(new HealthEventWindow(List.of(), 0));

        when(withdrawalQueryUseCase.listActiveWithdrawalStatuses(eq(farmId), any()))
                .thenReturn(List.of(
                        GoatWithdrawalStatusVO.builder()
                                .goatId("QA-MILK-001")
                                .referenceDate(LocalDate.of(2026, 3, 29))
                                .hasActiveMilkWithdrawal(true)
                                .hasActiveMeatWithdrawal(false)
                                .milkWithdrawal(HealthWithdrawalOriginVO.builder()
                                        .eventId(50L)
                                        .title("Antibiotico leite")
                                        .productName("Produto leite")
                                        .performedDate(LocalDate.of(2026, 3, 28))
                                        .withdrawalEndDate(LocalDate.of(2026, 3, 31))
                                        .build())
                                .build(),
                        GoatWithdrawalStatusVO.builder()
                                .goatId("QA-MEAT-001")
                                .referenceDate(LocalDate.of(2026, 3, 29))
                                .hasActiveMilkWithdrawal(false)
                                .hasActiveMeatWithdrawal(true)
                                .meatWithdrawal(HealthWithdrawalOriginVO.builder()
                                        .eventId(51L)
                                        .title("Antibiotico carne")
                                        .productName("Produto carne")
                                        .performedDate(LocalDate.of(2026, 3, 27))
                                        .withdrawalEndDate(LocalDate.of(2026, 4, 2))
                                        .build())
                                .build()
                ));

        FarmHealthAlertsResponseVO response = business.getAlerts(farmId, 7);

        assertEquals(1, response.activeMilkWithdrawalCount());
        assertEquals(1, response.activeMeatWithdrawalCount());
        assertEquals(1, response.milkWithdrawalTop().size());
        assertEquals(1, response.meatWithdrawalTop().size());
        assertEquals("QA-MILK-001", response.milkWithdrawalTop().getFirst().goatId());
        assertEquals("QA-MEAT-001", response.meatWithdrawalTop().getFirst().goatId());
        assertNotNull(response.milkWithdrawalTop().getFirst().withdrawalEndDate());
    }

    @Test
    void getAlerts_shouldUseBoundedScheduledWindowAndRetainTotalCounts() {
        Long farmId = 18L;
        FarmHealthAlertsBusiness business = new FarmHealthAlertsBusiness(persistencePort, withdrawalQueryUseCase, mapper);

        var events = java.util.stream.IntStream.rangeClosed(1, 5)
                .mapToObj(index -> {
                    HealthEventRecord record = new HealthEventRecord();
                    record.setId((long) index);
                    record.setFarmId(farmId);
                    record.setGoatId("GOAT-" + index);
                    record.setStatus(HealthEventStatus.AGENDADO);
                    record.setScheduledDate(LocalDate.now().plusDays(index));
                    return record;
                })
                .toList();
        when(persistencePort.findNextScheduledEvents(eq(farmId), any(), any(), any(), eq(HealthEventStatus.AGENDADO), eq(5)))
                .thenReturn(new HealthEventWindow(events, 6), new HealthEventWindow(List.of(), 0), new HealthEventWindow(List.of(), 0));
        when(mapper.toResponseVO(any(HealthEventRecord.class)))
                .thenReturn(HealthEventResponseVO.builder().status(HealthEventStatus.AGENDADO).build());
        when(withdrawalQueryUseCase.listActiveWithdrawalStatuses(eq(farmId), any())).thenReturn(List.of());

        FarmHealthAlertsResponseVO response = business.getAlerts(farmId, 7);

        assertEquals(6, response.dueTodayCount());
        assertEquals(5, response.dueTodayTop().size());
        verify(persistencePort, times(3)).findNextScheduledEvents(eq(farmId), any(), any(), any(), eq(HealthEventStatus.AGENDADO), eq(5));
    }
}
