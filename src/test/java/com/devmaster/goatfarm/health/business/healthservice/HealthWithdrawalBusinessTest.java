package com.devmaster.goatfarm.health.business.healthservice;

import com.devmaster.goatfarm.application.core.business.common.EntityFinder;
import com.devmaster.goatfarm.goat.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.goat.persistence.entity.Goat;
import com.devmaster.goatfarm.health.application.ports.out.HealthEventPersistencePort;
import com.devmaster.goatfarm.health.business.bo.GoatWithdrawalStatusVO;
import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;
import com.devmaster.goatfarm.health.persistence.entity.HealthEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthWithdrawalBusinessTest {

    @Mock
    private HealthEventPersistencePort healthEventPersistencePort;

    @Mock
    private GoatPersistencePort goatPersistencePort;

    @Test
    void getGoatWithdrawalStatus_shouldDeriveActiveWithdrawals() {
        HealthWithdrawalBusiness business = new HealthWithdrawalBusiness(
                healthEventPersistencePort,
                goatPersistencePort,
                new EntityFinder()
        );

        Long farmId = 17L;
        String goatId = "QA-WD-001";
        LocalDate referenceDate = LocalDate.of(2026, 3, 29);

        when(goatPersistencePort.findByIdAndFarmId(goatId, farmId)).thenReturn(Optional.of(new Goat()));
        when(healthEventPersistencePort.findPerformedWithWithdrawalByFarmIdAndGoatId(farmId, goatId))
                .thenReturn(List.of(
                        buildPerformedEvent(10L, farmId, goatId, "Antibiotico A", LocalDate.of(2026, 3, 28), 4, 0),
                        buildPerformedEvent(11L, farmId, goatId, "Antibiotico B", LocalDate.of(2026, 3, 27), 0, 7)
                ));

        GoatWithdrawalStatusVO status = business.getGoatWithdrawalStatus(farmId, goatId, referenceDate);

        assertTrue(status.hasActiveMilkWithdrawal());
        assertTrue(status.hasActiveMeatWithdrawal());
        assertNotNull(status.milkWithdrawal());
        assertNotNull(status.meatWithdrawal());
        assertEquals(LocalDate.of(2026, 4, 1), status.milkWithdrawal().withdrawalEndDate());
        assertEquals(LocalDate.of(2026, 4, 3), status.meatWithdrawal().withdrawalEndDate());
        assertEquals("Antibiotico A", status.milkWithdrawal().productName());
        assertEquals("Antibiotico B", status.meatWithdrawal().productName());
    }

    @Test
    void getGoatWithdrawalStatus_shouldIgnoreExpiredWithdrawal() {
        HealthWithdrawalBusiness business = new HealthWithdrawalBusiness(
                healthEventPersistencePort,
                goatPersistencePort,
                new EntityFinder()
        );

        Long farmId = 17L;
        String goatId = "QA-WD-002";
        LocalDate referenceDate = LocalDate.of(2026, 3, 29);

        when(goatPersistencePort.findByIdAndFarmId(goatId, farmId)).thenReturn(Optional.of(new Goat()));
        when(healthEventPersistencePort.findPerformedWithWithdrawalByFarmIdAndGoatId(farmId, goatId))
                .thenReturn(List.of(buildPerformedEvent(12L, farmId, goatId, "Anti-inflamatorio", LocalDate.of(2026, 3, 10), 3, 5)));

        GoatWithdrawalStatusVO status = business.getGoatWithdrawalStatus(farmId, goatId, referenceDate);

        assertFalse(status.hasActiveMilkWithdrawal());
        assertFalse(status.hasActiveMeatWithdrawal());
    }

    @Test
    void listActiveWithdrawalStatuses_shouldReturnOnlyActiveGoats() {
        HealthWithdrawalBusiness business = new HealthWithdrawalBusiness(
                healthEventPersistencePort,
                goatPersistencePort,
                new EntityFinder()
        );

        Long farmId = 17L;
        LocalDate referenceDate = LocalDate.of(2026, 3, 29);

        when(healthEventPersistencePort.findPerformedWithWithdrawalByFarmId(farmId))
                .thenReturn(List.of(
                        buildPerformedEvent(20L, farmId, "QA-WD-ACTIVE", "Produto ativo", LocalDate.of(2026, 3, 28), 2, 0),
                        buildPerformedEvent(21L, farmId, "QA-WD-EXPIRED", "Produto expirado", LocalDate.of(2026, 3, 10), 2, 0)
                ));

        List<GoatWithdrawalStatusVO> statuses = business.listActiveWithdrawalStatuses(farmId, referenceDate);

        assertEquals(1, statuses.size());
        assertEquals("QA-WD-ACTIVE", statuses.getFirst().goatId());
        assertTrue(statuses.getFirst().hasActiveMilkWithdrawal());
    }

    private HealthEvent buildPerformedEvent(
            Long eventId,
            Long farmId,
            String goatId,
            String productName,
            LocalDate performedDate,
            Integer milkWithdrawalDays,
            Integer meatWithdrawalDays
    ) {
        HealthEvent event = new HealthEvent();
        event.setId(eventId);
        event.setFarmId(farmId);
        event.setGoatId(goatId);
        event.setType(HealthEventType.MEDICACAO);
        event.setStatus(HealthEventStatus.REALIZADO);
        event.setTitle("Tratamento sanitario");
        event.setProductName(productName);
        event.setPerformedAt(LocalDateTime.of(performedDate, java.time.LocalTime.of(8, 0)));
        event.setWithdrawalMilkDays(milkWithdrawalDays);
        event.setWithdrawalMeatDays(meatWithdrawalDays);
        return event;
    }
}
