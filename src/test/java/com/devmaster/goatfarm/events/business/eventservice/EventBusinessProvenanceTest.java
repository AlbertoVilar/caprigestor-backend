package com.devmaster.goatfarm.events.business.eventservice;

import com.devmaster.goatfarm.application.exception.AuthorizationDeniedException;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.events.application.ports.out.EventPersistencePort;
import com.devmaster.goatfarm.events.application.ports.out.EventPublisher;
import com.devmaster.goatfarm.events.business.bo.EventRequestVO;
import com.devmaster.goatfarm.events.domain.OperationalEvent;
import com.devmaster.goatfarm.events.enums.EventType;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReferenceQueryPort;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.ports.in.GoatOwnershipGuardUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventBusinessProvenanceTest {

    private static final Long FARM_A = 10L;
    private static final Long FARM_B = 20L;
    private static final Long EVENT_ID = 100L;
    private static final GoatId GOAT_ID = GoatId.of(42L);
    private static final String REGISTRATION_NUMBER = "GOAT-42";
    private static final LocalDate VALID_DATE = LocalDate.of(2026, 9, 10);

    @Mock private EventPersistencePort eventPersistence;
    @Mock private GoatReferenceQueryPort goatReferences;
    @Mock private FarmAuthorizationUseCase farmAuthorization;
    @Mock private EventPublisher eventPublisher;
    @Mock private GoatOwnershipGuardUseCase ownershipGuard;

    private EventBusiness business;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-09-19T12:00:00Z"), ZoneOffset.UTC);
        business = new EventBusiness(eventPersistence, goatReferences, farmAuthorization,
                eventPublisher, ownershipGuard, clock);
        givenGoatForFarm(FARM_B);
        lenient().when(eventPersistence.save(any(OperationalEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createPersistsCallingFarmAsRecordingProvenance() {
        when(goatReferences.findReferenceByRegistrationNumber(REGISTRATION_NUMBER))
                .thenReturn(Optional.of(goat(FARM_B)));

        business.createEvent(FARM_B, REGISTRATION_NUMBER, request(VALID_DATE));

        ArgumentCaptor<OperationalEvent> captor = ArgumentCaptor.forClass(OperationalEvent.class);
        verify(eventPersistence).save(captor.capture());
        assertThat(captor.getValue().recordingFarmId()).isEqualTo(FARM_B);
        verify(ownershipGuard).requireCurrentFarm(GOAT_ID, FARM_B);
        verify(ownershipGuard).requireUnambiguousOwnershipOnDate(GOAT_ID, FARM_B, VALID_DATE);
    }

    @Test
    void createRejectsFutureDateBeforePersisting() {
        when(goatReferences.findReferenceByRegistrationNumber(REGISTRATION_NUMBER))
                .thenReturn(Optional.of(goat(FARM_B)));

        assertThatThrownBy(() -> business.createEvent(FARM_B, REGISTRATION_NUMBER, request(VALID_DATE.plusDays(20))))
                .isInstanceOf(InvalidArgumentException.class);

        verify(eventPersistence, never()).save(any());
    }

    @Test
    void createRejectsOwnershipTransferDay() {
        when(goatReferences.findReferenceByRegistrationNumber(REGISTRATION_NUMBER))
                .thenReturn(Optional.of(goat(FARM_B)));
        doThrow(new AuthorizationDeniedException("ambiguous transfer day")).when(ownershipGuard)
                .requireUnambiguousOwnershipOnDate(GOAT_ID, FARM_B, VALID_DATE);

        assertThatThrownBy(() -> business.createEvent(FARM_B, REGISTRATION_NUMBER, request(VALID_DATE)))
                .isInstanceOf(AuthorizationDeniedException.class);

        verify(eventPersistence, never()).save(any());
    }

    @Test
    void createRejectsDateOwnedByAnotherFarm() {
        when(goatReferences.findReferenceByRegistrationNumber(REGISTRATION_NUMBER))
                .thenReturn(Optional.of(goat(FARM_B)));
        doThrow(new AuthorizationDeniedException("date belongs to former farm")).when(ownershipGuard)
                .requireUnambiguousOwnershipOnDate(GOAT_ID, FARM_B, VALID_DATE);

        assertThatThrownBy(() -> business.createEvent(FARM_B, REGISTRATION_NUMBER, request(VALID_DATE)))
                .isInstanceOf(AuthorizationDeniedException.class);

        verify(eventPersistence, never()).save(any());
    }

    @Test
    void currentOwnerCanUpdateOwnProvenanceAndItIsPreserved() {
        givenEvent(FARM_B, VALID_DATE.minusDays(1));

        business.updateEvent(FARM_B, REGISTRATION_NUMBER, EVENT_ID, request(VALID_DATE));

        ArgumentCaptor<OperationalEvent> captor = ArgumentCaptor.forClass(OperationalEvent.class);
        verify(eventPersistence).save(captor.capture());
        assertThat(captor.getValue().recordingFarmId()).isEqualTo(FARM_B);
        verify(ownershipGuard).requireCurrentFarm(GOAT_ID, FARM_B);
        verify(ownershipGuard).requireUnambiguousOwnershipOnDate(GOAT_ID, FARM_B, VALID_DATE);
    }

    @Test
    void currentOwnerCannotUpdateFormerFarmProvenance() {
        givenEvent(FARM_A, VALID_DATE.minusDays(1));

        assertThatThrownBy(() -> business.updateEvent(FARM_B, REGISTRATION_NUMBER, EVENT_ID, request(VALID_DATE)))
                .isInstanceOf(AuthorizationDeniedException.class);

        verify(eventPersistence, never()).save(any());
    }

    @Test
    void formerOwnerCannotUpdateAfterTransfer() {
        givenGoatForFarm(FARM_A);
        doThrow(new AuthorizationDeniedException("former owner")).when(ownershipGuard)
                .requireCurrentFarm(GOAT_ID, FARM_A);

        assertThatThrownBy(() -> business.updateEvent(FARM_A, REGISTRATION_NUMBER, EVENT_ID, request(VALID_DATE)))
                .isInstanceOf(AuthorizationDeniedException.class);

        verify(eventPersistence, never()).findByIdAndGoatId(anyLong(), any());
        verify(eventPersistence, never()).save(any());
    }

    @Test
    void legacyEventWithoutProvenanceCannotBeUpdated() {
        givenEvent(null, VALID_DATE.minusDays(1));

        assertThatThrownBy(() -> business.updateEvent(FARM_B, REGISTRATION_NUMBER, EVENT_ID, request(VALID_DATE)))
                .isInstanceOf(AuthorizationDeniedException.class);

        verify(eventPersistence, never()).save(any());
    }

    @Test
    void updateRejectsDateOwnedByAnotherFarm() {
        givenEvent(FARM_B, VALID_DATE.minusDays(1));
        doThrow(new AuthorizationDeniedException("wrong ownership date")).when(ownershipGuard)
                .requireUnambiguousOwnershipOnDate(GOAT_ID, FARM_B, VALID_DATE);

        assertThatThrownBy(() -> business.updateEvent(FARM_B, REGISTRATION_NUMBER, EVENT_ID, request(VALID_DATE)))
                .isInstanceOf(AuthorizationDeniedException.class);

        verify(eventPersistence, never()).save(any());
    }

    @Test
    void updateRejectsTransferDay() {
        givenEvent(FARM_B, VALID_DATE.minusDays(1));
        doThrow(new AuthorizationDeniedException("ambiguous transfer day")).when(ownershipGuard)
                .requireUnambiguousOwnershipOnDate(GOAT_ID, FARM_B, VALID_DATE);

        assertThatThrownBy(() -> business.updateEvent(FARM_B, REGISTRATION_NUMBER, EVENT_ID, request(VALID_DATE)))
                .isInstanceOf(AuthorizationDeniedException.class);

        verify(eventPersistence, never()).save(any());
    }

    @Test
    void updateRejectsFutureDate() {
        givenEvent(FARM_B, VALID_DATE.minusDays(1));

        assertThatThrownBy(() -> business.updateEvent(FARM_B, REGISTRATION_NUMBER, EVENT_ID,
                request(VALID_DATE.plusDays(20))))
                .isInstanceOf(InvalidArgumentException.class);

        verify(eventPersistence, never()).save(any());
    }

    @Test
    void currentOwnerCanDeleteOwnProvenance() {
        givenEvent(FARM_B, VALID_DATE);

        business.deleteEvent(FARM_B, REGISTRATION_NUMBER, EVENT_ID);

        verify(eventPersistence).deleteById(EVENT_ID);
    }

    @Test
    void currentOwnerCannotDeleteFormerFarmProvenance() {
        givenEvent(FARM_A, VALID_DATE);

        assertThatThrownBy(() -> business.deleteEvent(FARM_B, REGISTRATION_NUMBER, EVENT_ID))
                .isInstanceOf(AuthorizationDeniedException.class);

        verify(eventPersistence, never()).deleteById(anyLong());
    }

    @Test
    void formerOwnerCannotDeleteAfterTransfer() {
        givenGoatForFarm(FARM_A);
        doThrow(new AuthorizationDeniedException("former owner")).when(ownershipGuard)
                .requireCurrentFarm(GOAT_ID, FARM_A);

        assertThatThrownBy(() -> business.deleteEvent(FARM_A, REGISTRATION_NUMBER, EVENT_ID))
                .isInstanceOf(AuthorizationDeniedException.class);

        verify(eventPersistence, never()).deleteById(anyLong());
    }

    @Test
    void legacyEventWithoutProvenanceCannotBeDeleted() {
        givenEvent(null, VALID_DATE);

        assertThatThrownBy(() -> business.deleteEvent(FARM_B, REGISTRATION_NUMBER, EVENT_ID))
                .isInstanceOf(AuthorizationDeniedException.class);

        verify(eventPersistence, never()).deleteById(anyLong());
    }

    private void givenGoatForFarm(Long farmId) {
        lenient().when(goatReferences.findReferenceByRegistrationNumberAndFarmId(REGISTRATION_NUMBER, farmId))
                .thenReturn(Optional.of(goat(farmId)));
    }

    private void givenEvent(Long recordingFarmId, LocalDate date) {
        when(eventPersistence.findByIdAndGoatId(EVENT_ID, GOAT_ID))
                .thenReturn(Optional.of(new OperationalEvent(EVENT_ID, GOAT_ID, recordingFarmId,
                        REGISTRATION_NUMBER, "Goat", EventType.PESAGEM, date,
                        "before", "farm", "vet", "done")));
    }

    private GoatReference goat(Long farmId) {
        return new GoatReference(GOAT_ID, farmId, REGISTRATION_NUMBER, "Goat");
    }

    private EventRequestVO request(LocalDate date) {
        return new EventRequestVO(REGISTRATION_NUMBER, EventType.PESAGEM, date,
                "after", "farm", "vet", "done");
    }
}
