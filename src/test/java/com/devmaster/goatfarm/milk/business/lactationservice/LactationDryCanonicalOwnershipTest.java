package com.devmaster.goatfarm.milk.business.lactationservice;

import com.devmaster.goatfarm.application.core.business.validation.GoatGenderValidator;
import com.devmaster.goatfarm.application.exception.AuthorizationDeniedException;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.goat.application.routing.GoatReferenceResolver;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.ports.in.GoatOwnershipGuardUseCase;
import com.devmaster.goatfarm.milk.application.ports.out.LactationPersistencePort;
import com.devmaster.goatfarm.milk.application.ports.out.MilkProductionSummaryQueryPort;
import com.devmaster.goatfarm.milk.business.bo.LactationDryRequestVO;
import com.devmaster.goatfarm.milk.business.mapper.LactationBusinessMapper;
import com.devmaster.goatfarm.milk.domain.Lactation;
import com.devmaster.goatfarm.milk.enums.LactationStatus;
import com.devmaster.goatfarm.reproduction.application.ports.in.PregnancyDryOffQueryUseCase;
import com.devmaster.goatfarm.reproduction.application.ports.in.PregnancySnapshotQueryUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LactationDryCanonicalOwnershipTest {

    private static final Long ORIGIN_FARM = 1L;
    private static final Long CURRENT_FARM = 2L;
    private static final GoatId GOAT_ID = GoatId.of(42L);
    private static final String ROUTE_REGISTRATION = "RG-CURRENT";
    private static final String HISTORICAL_REGISTRATION = "RG-AT-ORIGIN";
    private static final LocalDate START_DATE = LocalDate.of(2026, 9, 10);
    private static final LocalDate DRY_DATE = LocalDate.of(2026, 9, 16);

    @Mock private LactationPersistencePort lactationPersistence;
    @Mock private MilkProductionSummaryQueryPort milkSummary;
    @Mock private PregnancySnapshotQueryUseCase pregnancySnapshots;
    @Mock private PregnancyDryOffQueryUseCase pregnancyDryOff;
    @Mock private GoatGenderValidator genderValidator;
    @Mock private LactationBusinessMapper mapper;
    @Mock private GoatReferenceResolver referenceResolver;
    @Mock private GoatOwnershipGuardUseCase ownershipGuard;

    private LactationBusiness business;

    @BeforeEach
    void setUp() {
        business = new LactationBusiness(lactationPersistence, milkSummary, pregnancySnapshots,
                pregnancyDryOff, genderValidator, mapper, referenceResolver, ownershipGuard);
        lenient().when(referenceResolver.resolveGlobal(ROUTE_REGISTRATION))
                .thenReturn(Optional.of(new GoatReference(GOAT_ID, CURRENT_FARM, ROUTE_REGISTRATION, "Matriz")));
        lenient().doNothing().when(genderValidator).requireFemaleAndActive(GOAT_ID);
        lenient().doNothing().when(ownershipGuard).requireCurrentFarm(GOAT_ID, CURRENT_FARM);
        lenient().doNothing().when(ownershipGuard)
                .requireUnambiguousOwnershipOnDate(GOAT_ID, CURRENT_FARM, DRY_DATE);
        lenient().when(lactationPersistence.findByIdAndFarmIdAndGoatId(10L, CURRENT_FARM, ROUTE_REGISTRATION))
                .thenReturn(Optional.of(currentActiveLactation()));
        lenient().when(lactationPersistence.save(any(Lactation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void currentOwnerCanDryCurrentFarmLactationWithoutRewritingHistory() {
        Lactation current = currentActiveLactation();
        when(lactationPersistence.findByIdAndFarmIdAndGoatId(10L, CURRENT_FARM, ROUTE_REGISTRATION))
                .thenReturn(Optional.of(current));

        business.dryLactation(CURRENT_FARM, ROUTE_REGISTRATION, 10L, request(DRY_DATE));

        ArgumentCaptor<Lactation> captor = ArgumentCaptor.forClass(Lactation.class);
        verify(lactationPersistence).save(captor.capture());
        assertSame(current, captor.getValue());
        assertEquals(LactationStatus.DRY, current.getStatus());
        assertEquals(DRY_DATE, current.getEndDate());
        assertEquals(DRY_DATE, current.getDryStartDate());
        assertEquals(CURRENT_FARM, current.getFarmId());
        assertEquals(ROUTE_REGISTRATION, current.getGoatId());
        assertEquals(GOAT_ID.value(), current.getGoatTechnicalId());
        verify(lactationPersistence).findByIdAndFarmIdAndGoatId(10L, CURRENT_FARM, ROUTE_REGISTRATION);
        verify(ownershipGuard).requireCurrentFarm(GOAT_ID, CURRENT_FARM);
        verify(ownershipGuard).requireUnambiguousOwnershipOnDate(GOAT_ID, CURRENT_FARM, DRY_DATE);
    }

    @Test
    void formerOwnerIsRejectedBeforeGlobalLactationLookup() {
        doThrow(new AuthorizationDeniedException("not current owner"))
                .when(ownershipGuard).requireCurrentFarm(GOAT_ID, ORIGIN_FARM);

        assertThrows(AuthorizationDeniedException.class,
                () -> business.dryLactation(ORIGIN_FARM, ROUTE_REGISTRATION, 10L, request(DRY_DATE)));

        verify(lactationPersistence, never()).findByIdAndFarmIdAndGoatId(anyLong(), anyLong(), any());
        verify(genderValidator, never()).requireFemaleAndActive(any(GoatId.class));
        verify(lactationPersistence, never()).save(any(Lactation.class));
    }

    @Test
    void projectionDriftFailsClosedWithoutMutation() {
        assertThrows(BusinessRuleException.class,
                () -> business.dryLactation(ORIGIN_FARM, ROUTE_REGISTRATION, 10L, request(DRY_DATE)));

        verify(ownershipGuard).requireCurrentFarm(GOAT_ID, ORIGIN_FARM);
        verify(genderValidator, never()).requireFemaleAndActive(any(GoatId.class));
        verify(lactationPersistence, never()).findByIdAndFarmIdAndGoatId(anyLong(), anyLong(), any());
        verify(lactationPersistence, never()).save(any(Lactation.class));
    }

    @Test
    void wrongGoatTechnicalIdCannotResolveAnotherGoatLactation() {
        when(referenceResolver.resolveGlobal("technical-42"))
                .thenReturn(Optional.of(new GoatReference(GOAT_ID, CURRENT_FARM, ROUTE_REGISTRATION, "Matriz")));
        when(lactationPersistence.findByIdAndFarmIdAndGoatId(99L, CURRENT_FARM, ROUTE_REGISTRATION)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> business.dryLactation(CURRENT_FARM, "technical-42", 99L, request(DRY_DATE)));

        verify(lactationPersistence, never()).save(any(Lactation.class));
    }

    @Test
    void alreadyDryLactationIsRejected() {
        when(lactationPersistence.findByIdAndFarmIdAndGoatId(10L, CURRENT_FARM, ROUTE_REGISTRATION))
                .thenReturn(Optional.of(currentLactation(LactationStatus.DRY)));

        assertThrows(BusinessRuleException.class,
                () -> business.dryLactation(CURRENT_FARM, ROUTE_REGISTRATION, 10L, request(DRY_DATE)));
        verify(ownershipGuard, never()).requireUnambiguousOwnershipOnDate(any(GoatId.class), anyLong(), any(LocalDate.class));
        verify(lactationPersistence, never()).save(any(Lactation.class));
    }

    @Test
    void nullEndDateIsRejected() {
        assertThrows(BusinessRuleException.class,
                () -> business.dryLactation(CURRENT_FARM, ROUTE_REGISTRATION, 10L, request(null)));
        verify(ownershipGuard, never()).requireUnambiguousOwnershipOnDate(any(GoatId.class), anyLong(), any(LocalDate.class));
        verify(lactationPersistence, never()).save(any(Lactation.class));
    }

    @Test
    void endDateBeforeStartDateIsRejected() {
        assertThrows(BusinessRuleException.class,
                () -> business.dryLactation(CURRENT_FARM, ROUTE_REGISTRATION, 10L, request(START_DATE.minusDays(1))));
        verify(ownershipGuard, never()).requireUnambiguousOwnershipOnDate(any(GoatId.class), anyLong(), any(LocalDate.class));
        verify(lactationPersistence, never()).save(any(Lactation.class));
    }

    @Test
    void transferDayIsRejectedByWholeCivilDayOwnershipPolicy() {
        LocalDate transferDay = LocalDate.of(2026, 9, 15);
        doThrow(new AuthorizationDeniedException("ambiguous ownership day"))
                .when(ownershipGuard).requireUnambiguousOwnershipOnDate(GOAT_ID, CURRENT_FARM, transferDay);

        assertThrows(AuthorizationDeniedException.class,
                () -> business.dryLactation(CURRENT_FARM, ROUTE_REGISTRATION, 10L, request(transferDay)));
        verify(lactationPersistence, never()).save(any(Lactation.class));
    }

    @Test
    void currentOwnerCannotBackdateDryToFormerOwnersWholeDay() {
        LocalDate formerOwnerDate = LocalDate.of(2026, 9, 14);
        doThrow(new AuthorizationDeniedException("date belonged to former owner"))
                .when(ownershipGuard).requireUnambiguousOwnershipOnDate(GOAT_ID, CURRENT_FARM, formerOwnerDate);

        assertThrows(AuthorizationDeniedException.class,
                () -> business.dryLactation(CURRENT_FARM, ROUTE_REGISTRATION, 10L, request(formerOwnerDate)));
        verify(lactationPersistence, never()).save(any(Lactation.class));
    }

    @Test
    void currentRegistrationChangeDoesNotAlterHistoricalSnapshot() {
        Lactation current = currentActiveLactation();
        when(lactationPersistence.findByIdAndFarmIdAndGoatId(10L, CURRENT_FARM, ROUTE_REGISTRATION))
                .thenReturn(Optional.of(current));

        business.dryLactation(CURRENT_FARM, ROUTE_REGISTRATION, 10L, request(DRY_DATE));

        assertEquals(ROUTE_REGISTRATION, current.getGoatId());
        assertEquals(CURRENT_FARM, current.getFarmId());
    }

    private LactationDryRequestVO request(LocalDate endDate) {
        LactationDryRequestVO request = new LactationDryRequestVO();
        request.setEndDate(endDate);
        return request;
    }

    private Lactation inheritedActiveLactation() {
        return lactation(LactationStatus.ACTIVE);
    }

    private Lactation currentActiveLactation() {
        return currentLactation(LactationStatus.ACTIVE);
    }

    private Lactation currentLactation(LactationStatus status) {
        return Lactation.rehydrate(10L, CURRENT_FARM, ROUTE_REGISTRATION, GOAT_ID.value(), status,
                START_DATE, status == LactationStatus.DRY ? START_DATE.plusDays(1) : null,
                null, status == LactationStatus.DRY ? START_DATE.plusDays(1) : null,
                90, 60, null, null);
    }

    private Lactation lactation(LactationStatus status) {
        return Lactation.rehydrate(10L, ORIGIN_FARM, HISTORICAL_REGISTRATION, GOAT_ID.value(), status,
                START_DATE, status == LactationStatus.DRY ? START_DATE.plusDays(1) : null,
                null, status == LactationStatus.DRY ? START_DATE.plusDays(1) : null,
                90, 60, null, null);
    }
}
