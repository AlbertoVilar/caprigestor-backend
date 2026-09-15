package com.devmaster.goatfarm.milk.business.lactationservice;

import com.devmaster.goatfarm.application.core.business.validation.GoatGenderValidator;
import com.devmaster.goatfarm.application.exception.AuthorizationDeniedException;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.goat.application.routing.GoatReferenceResolver;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.ports.in.GoatOwnershipGuardUseCase;
import com.devmaster.goatfarm.milk.application.ports.out.LactationPersistencePort;
import com.devmaster.goatfarm.milk.application.ports.out.MilkProductionSummaryQueryPort;
import com.devmaster.goatfarm.milk.business.bo.LactationRequestVO;
import com.devmaster.goatfarm.milk.business.mapper.LactationBusinessMapper;
import com.devmaster.goatfarm.milk.domain.Lactation;
import com.devmaster.goatfarm.milk.enums.LactationStatus;
import com.devmaster.goatfarm.reproduction.application.ports.in.PregnancyDryOffQueryUseCase;
import com.devmaster.goatfarm.reproduction.application.ports.in.PregnancySnapshotQueryUseCase;
import com.devmaster.goatfarm.sharedkernel.pregnancy.PregnancySnapshot;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LactationOpenCanonicalOwnershipTest {

    private static final Long REQUESTED_FARM = 2L;
    private static final GoatId GOAT_ID = GoatId.of(42L);
    private static final String REGISTRATION = "RG-42";
    private static final LocalDate START_DATE = LocalDate.of(2026, 9, 10);

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
        lenient().when(referenceResolver.resolveGlobal(REGISTRATION))
                .thenReturn(Optional.of(new GoatReference(GOAT_ID, REQUESTED_FARM, REGISTRATION, "Matriz")));
        lenient().when(lactationPersistence.findActiveByGoatTechnicalId(GOAT_ID)).thenReturn(Optional.empty());
        lenient().when(lactationPersistence.findLatestByGoatTechnicalId(GOAT_ID)).thenReturn(Optional.empty());
        lenient().when(pregnancySnapshots.findLatestByGoatTechnicalId(GOAT_ID, START_DATE)).thenReturn(Optional.empty());
        lenient().when(lactationPersistence.save(any(Lactation.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void currentOwnerCanOpenNewLactationAfterTransferWhenNoActiveCycleExists() {
        business.openLactation(REQUESTED_FARM, REGISTRATION, request());

        ArgumentCaptor<Lactation> captor = ArgumentCaptor.forClass(Lactation.class);
        verify(lactationPersistence).save(captor.capture());
        Lactation created = captor.getValue();
        assertEquals(REQUESTED_FARM, created.getFarmId());
        assertEquals(GOAT_ID.value(), created.getGoatTechnicalId());
        assertEquals(REGISTRATION, created.getGoatId());
        verify(ownershipGuard).requireCurrentFarm(GOAT_ID, REQUESTED_FARM);
        verify(ownershipGuard).requireUnambiguousOwnershipOnDate(GOAT_ID, REQUESTED_FARM, START_DATE);
    }

    @Test
    void inheritedActiveLactationBlocksSecondCycleAtCurrentOwner() {
        when(lactationPersistence.findActiveByGoatTechnicalId(GOAT_ID))
                .thenReturn(Optional.of(lactation(LactationStatus.ACTIVE)));

        assertThrows(BusinessRuleException.class,
                () -> business.openLactation(REQUESTED_FARM, REGISTRATION, request()));
        verify(lactationPersistence, never()).save(any(Lactation.class));
    }

    @Test
    void formerOwnerCannotOpenAfterTransfer() {
        doThrow(new AuthorizationDeniedException("not current owner"))
                .when(ownershipGuard).requireCurrentFarm(GOAT_ID, 1L);

        assertThrows(AuthorizationDeniedException.class,
                () -> business.openLactation(1L, REGISTRATION, request()));
        verify(lactationPersistence, never()).save(any(Lactation.class));
    }

    @Test
    void ownershipIsRejectedBeforeOperationalGenderOrStatusValidation() {
        doThrow(new AuthorizationDeniedException("not current owner"))
                .when(ownershipGuard).requireCurrentFarm(GOAT_ID, 1L);

        assertThrows(AuthorizationDeniedException.class,
                () -> business.openLactation(1L, REGISTRATION, request()));
        verify(genderValidator, never()).requireFemaleAndActive(GOAT_ID);
        verify(lactationPersistence, never()).findActiveByGoatTechnicalId(any(GoatId.class));
        verify(lactationPersistence, never()).save(any(Lactation.class));
    }

    @Test
    void staleProjectionFailsClosedAfterCanonicalOwnershipSucceeds() {
        when(referenceResolver.resolveGlobal(REGISTRATION))
                .thenReturn(Optional.of(new GoatReference(GOAT_ID, 1L, REGISTRATION, "Matriz")));

        assertThrows(BusinessRuleException.class,
                () -> business.openLactation(REQUESTED_FARM, REGISTRATION, request()));
        verify(ownershipGuard).requireCurrentFarm(GOAT_ID, REQUESTED_FARM);
        verify(lactationPersistence, never()).save(any(Lactation.class));
    }

    @Test
    void pregnancyOriginatedInAnotherFarmRemainsVisibleAndBlocksNewCycle() {
        when(lactationPersistence.findLatestByGoatTechnicalId(GOAT_ID))
                .thenReturn(Optional.of(lactation(LactationStatus.DRY)));
        when(pregnancySnapshots.findLatestByGoatTechnicalId(GOAT_ID, START_DATE))
                .thenReturn(Optional.of(new PregnancySnapshot(true, START_DATE, null)));

        assertThrows(BusinessRuleException.class,
                () -> business.openLactation(REQUESTED_FARM, REGISTRATION, request()));
        verify(pregnancySnapshots).findLatestByGoatTechnicalId(GOAT_ID, START_DATE);
        verify(lactationPersistence, never()).save(any(Lactation.class));
    }

    @Test
    void transferDayFailsClosedThroughWholeCivilDayPolicy() {
        doThrow(new AuthorizationDeniedException("ambiguous ownership day"))
                .when(ownershipGuard).requireUnambiguousOwnershipOnDate(GOAT_ID, REQUESTED_FARM, START_DATE);

        assertThrows(AuthorizationDeniedException.class,
                () -> business.openLactation(REQUESTED_FARM, REGISTRATION, request()));
        verify(lactationPersistence, never()).save(any(Lactation.class));
    }

    @Test
    void unknownTechnicalRouteTokenCannotUseAnUnrelatedGoat() {
        when(referenceResolver.resolveGlobal("technical-999")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> business.openLactation(REQUESTED_FARM, "technical-999", request()));
        verify(lactationPersistence, never()).findActiveByGoatTechnicalId(any(GoatId.class));
        verify(lactationPersistence, never()).save(any(Lactation.class));
    }

    @Test
    void openingAfterTransferDoesNotRewriteInheritedLactationOrigin() {
        Lactation inheritedDry = lactation(LactationStatus.DRY);
        when(lactationPersistence.findLatestByGoatTechnicalId(GOAT_ID)).thenReturn(Optional.of(inheritedDry));

        business.openLactation(REQUESTED_FARM, REGISTRATION, request());

        ArgumentCaptor<Lactation> captor = ArgumentCaptor.forClass(Lactation.class);
        verify(lactationPersistence).save(captor.capture());
        assertEquals(1L, inheritedDry.getFarmId());
        assertEquals(REQUESTED_FARM, captor.getValue().getFarmId());
    }

    @Test
    void technicalRouteUsesRegistrationSnapshotAndNotRawRouteToken() {
        when(referenceResolver.resolveGlobal("technical-42"))
                .thenReturn(Optional.of(new GoatReference(GOAT_ID, REQUESTED_FARM, REGISTRATION, "Matriz")));

        business.openLactation(REQUESTED_FARM, "technical-42", request());

        ArgumentCaptor<Lactation> captor = ArgumentCaptor.forClass(Lactation.class);
        verify(lactationPersistence).save(captor.capture());
        assertEquals(GOAT_ID.value(), captor.getValue().getGoatTechnicalId());
        assertEquals(REGISTRATION, captor.getValue().getGoatId());
    }

    private LactationRequestVO request() {
        return LactationRequestVO.builder().startDate(START_DATE).build();
    }

    private Lactation lactation(LactationStatus status) {
        return Lactation.rehydrate(10L, 1L, REGISTRATION, GOAT_ID.value(), status,
                START_DATE.minusDays(30), status == LactationStatus.DRY ? START_DATE.minusDays(1) : null,
                null, status == LactationStatus.DRY ? START_DATE.minusDays(1) : null,
                90, 60, null, null);
    }
}
