package com.devmaster.goatfarm.goatownership.business;

import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.goat.application.model.GoatCreationOrigin;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.GoatOwnershipInitializationCommand;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatOwnershipPeriodPersistencePort;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatOwnershipQueryPort;
import com.devmaster.goatfarm.goatownership.domain.GoatOwnershipPeriod;
import com.devmaster.goatfarm.goatownership.domain.OwnershipEntryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoatOwnershipInitializationBusinessTest {
    private static final Instant NOW = Instant.parse("2026-09-14T12:00:00Z");

    @Mock private GoatOwnershipQueryPort ownershipQuery;
    @Mock private GoatOwnershipPeriodPersistencePort periodPersistence;

    private GoatOwnershipInitializationBusiness business;

    @BeforeEach
    void setUp() {
        business = new GoatOwnershipInitializationBusiness(
                ownershipQuery, periodPersistence, Clock.fixed(NOW, ZoneOffset.UTC));
        org.mockito.Mockito.lenient().when(periodPersistence.save(any(GoatOwnershipPeriod.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(ownershipQuery.findOwnershipHistory(any())).thenReturn(List.of());
    }

    @Test
    void mapsEachCreationOriginAndUsesTheStructuralGoatIdAndClock() {
        assertOrigin(GoatCreationOrigin.MANUAL, OwnershipEntryType.MANUAL_IMPORT, "GOAT_CREATE:MANUAL");
        assertOrigin(GoatCreationOrigin.ABCC_IMPORT, OwnershipEntryType.ABCC_IMPORT, "GOAT_CREATE:ABCC_IMPORT");
        assertOrigin(GoatCreationOrigin.BIRTH, OwnershipEntryType.BIRTH, "GOAT_CREATE:BIRTH");
    }

    @Test
    void rejectsExistingHistoryWithoutCreatingAnotherPeriod() {
        GoatOwnershipPeriod existing = GoatOwnershipPeriod.open(
                new GoatId(7), 3, NOW.minusSeconds(1), OwnershipEntryType.MANUAL_IMPORT, "LEGACY");
        when(ownershipQuery.findOwnershipHistory(new GoatId(7))).thenReturn(List.of(existing));

        assertThatThrownBy(() -> business.initialize(new GoatOwnershipInitializationCommand(
                new GoatId(7), 3, GoatCreationOrigin.MANUAL)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already has canonical ownership history");
    }

    private void assertOrigin(GoatCreationOrigin origin, OwnershipEntryType expectedType, String expectedSource) {
        GoatId goatId = new GoatId(origin.ordinal() + 1L);
        business.initialize(new GoatOwnershipInitializationCommand(goatId, 3, origin));

        ArgumentCaptor<GoatOwnershipPeriod> captor = ArgumentCaptor.forClass(GoatOwnershipPeriod.class);
        verify(periodPersistence, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        GoatOwnershipPeriod saved = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertThat(saved.goatId()).isEqualTo(goatId);
        assertThat(saved.farmId()).isEqualTo(3);
        assertThat(saved.entryType()).isEqualTo(expectedType);
        assertThat(saved.startedAt()).isEqualTo(NOW);
        assertThat(saved.source()).isEqualTo(expectedSource);
        assertThat(saved.isOpen()).isTrue();
    }
}
