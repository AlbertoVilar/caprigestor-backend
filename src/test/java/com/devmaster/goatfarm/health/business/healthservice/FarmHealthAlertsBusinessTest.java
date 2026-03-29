package com.devmaster.goatfarm.health.business.healthservice;

import com.devmaster.goatfarm.health.application.ports.in.HealthWithdrawalQueryUseCase;
import com.devmaster.goatfarm.health.application.ports.out.HealthEventPersistencePort;
import com.devmaster.goatfarm.health.business.bo.FarmHealthAlertsResponseVO;
import com.devmaster.goatfarm.health.business.bo.GoatWithdrawalStatusVO;
import com.devmaster.goatfarm.health.business.bo.HealthWithdrawalOriginVO;
import com.devmaster.goatfarm.health.business.mapper.HealthEventBusinessMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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

        when(persistencePort.findByFarmIdAndPeriod(eq(farmId), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());

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
}
