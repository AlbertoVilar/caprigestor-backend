package com.devmaster.goatfarm.milk.business.milkproductionservice;

import com.devmaster.goatfarm.application.core.business.validation.GoatGenderValidator;
import com.devmaster.goatfarm.application.exception.AuthorizationDeniedException;
import com.devmaster.goatfarm.config.exceptions.DuplicateMilkProductionException;
import com.devmaster.goatfarm.config.exceptions.NoActiveLactationException;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.goat.application.routing.GoatReferenceResolver;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.ports.in.GoatOwnershipGuardUseCase;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.health.application.ports.in.HealthWithdrawalQueryUseCase;
import com.devmaster.goatfarm.health.business.bo.GoatWithdrawalStatusVO;
import com.devmaster.goatfarm.health.business.bo.HealthWithdrawalOriginVO;
import com.devmaster.goatfarm.milk.application.ports.out.LactationPersistencePort;
import com.devmaster.goatfarm.milk.application.ports.out.MilkProductionPersistencePort;
import com.devmaster.goatfarm.milk.business.bo.MilkProductionRequestVO;
import com.devmaster.goatfarm.milk.business.bo.MilkProductionResponseVO;
import com.devmaster.goatfarm.milk.business.mapper.MilkProductionBusinessMapper;
import com.devmaster.goatfarm.milk.domain.Lactation;
import com.devmaster.goatfarm.milk.domain.MilkProduction;
import com.devmaster.goatfarm.milk.enums.LactationStatus;
import com.devmaster.goatfarm.milk.enums.MilkingShift;
import com.devmaster.goatfarm.milk.enums.MilkProductionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MilkProductionCanonicalOwnershipTest {

    private static final Long FARM_A = 1L;
    private static final Long FARM_B = 2L;
    private static final GoatId GOAT_ID = new GoatId(42L);
    private static final String CURRENT_RG = "NEW-RG";
    private static final LocalDate RECORD_DATE = LocalDate.of(2026, 9, 10);

    @Mock private MilkProductionPersistencePort milkPersistence;
    @Mock private LactationPersistencePort lactationPersistence;
    @Mock private GoatGenderValidator genderValidator;
    @Mock private HealthWithdrawalQueryUseCase withdrawalQuery;
    @Mock private MilkProductionBusinessMapper mapper;
    @Mock private GoatReferenceResolver resolver;
    @Mock private GoatOwnershipGuardUseCase ownershipGuard;

    private MilkProductionBusiness business;

    @BeforeEach
    void setUp() {
        business = new MilkProductionBusiness(milkPersistence, lactationPersistence, genderValidator,
                withdrawalQuery, mapper, resolver, ownershipGuard);
        lenient().when(resolver.resolveGlobal(anyString()))
                .thenReturn(Optional.of(new GoatReference(GOAT_ID, FARM_B, CURRENT_RG, "Goat", Gender.FEMEA)));
        lenient().doNothing().when(ownershipGuard).requireCurrentFarm(any(GoatId.class), anyLong());
        lenient().doNothing().when(ownershipGuard).requireUnambiguousOwnershipOnDate(any(GoatId.class), anyLong(), any(LocalDate.class));
        lenient().doNothing().when(genderValidator).requireFemaleAndActive(any(GoatId.class));
        lenient().when(milkPersistence.existsActiveByGoatTechnicalIdAndDateAndShift(any(GoatId.class), any(LocalDate.class), any(MilkingShift.class)))
                .thenReturn(false);
        lenient().when(lactationPersistence.findActiveByFarmIdAndGoatId(FARM_B, CURRENT_RG))
                .thenReturn(Optional.of(activeLactation(FARM_B, CURRENT_RG)));
        lenient().when(withdrawalQuery.getGoatWithdrawalStatus(eq(GOAT_ID), any(LocalDate.class)))
                .thenReturn(noWithdrawal());
        lenient().when(milkPersistence.save(any(MilkProduction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(mapper.toResponseVO(any(MilkProduction.class))).thenReturn(MilkProductionResponseVO.builder().build());
    }

    @Test
    void currentOwnerCanRecordAgainstCurrentFarmLactation() {
        MilkProductionResponseVO response = business.createMilkProduction(FARM_B, "NEW-RG", request());

        assertNotNull(response);
        ArgumentCaptor<MilkProduction> captor = ArgumentCaptor.forClass(MilkProduction.class);
        verify(milkPersistence).save(captor.capture());
        MilkProduction saved = captor.getValue();
        assertEquals(FARM_B, saved.getFarmId());
        assertEquals(CURRENT_RG, saved.getGoatId());
        assertEquals(GOAT_ID.value(), saved.getGoatTechnicalId());
        assertEquals(10L, saved.getLactationId());
        assertEquals(FARM_B, activeLactation(FARM_B, CURRENT_RG).getFarmId());
    }

    @Test
    void formerFarmLactationCannotBeReusedByCurrentOwner() {
        Lactation inherited = activeLactation(FARM_A, "OLD-RG");
        when(lactationPersistence.findActiveByFarmIdAndGoatId(FARM_B, CURRENT_RG))
                .thenReturn(Optional.empty());

        assertThrows(NoActiveLactationException.class,
                () -> business.createMilkProduction(FARM_B, CURRENT_RG, request()));

        assertEquals(FARM_A, inherited.getFarmId());
        assertEquals("OLD-RG", inherited.getGoatId());
        verify(milkPersistence, never()).save(any());
    }

    @Test
    void formerOwnerIsRejectedBeforeOperationalLookups() {
        doThrow(new AuthorizationDeniedException("former owner")).when(ownershipGuard).requireCurrentFarm(GOAT_ID, FARM_A);

        assertThrows(AuthorizationDeniedException.class,
                () -> business.createMilkProduction(FARM_A, CURRENT_RG, request()));

        InOrder order = inOrder(resolver, ownershipGuard, genderValidator, milkPersistence, lactationPersistence, withdrawalQuery);
        order.verify(resolver).resolveGlobal(CURRENT_RG);
        order.verify(ownershipGuard).requireCurrentFarm(GOAT_ID, FARM_A);
        verifyNoInteractions(genderValidator, milkPersistence, lactationPersistence, withdrawalQuery, mapper);
    }

    @Test
    void projectionDriftFailsClosedBeforeOperationalDataAccess() {
        when(resolver.resolveGlobal(CURRENT_RG)).thenReturn(Optional.of(new GoatReference(GOAT_ID, FARM_A, CURRENT_RG, "Goat", Gender.FEMEA)));

        assertThrows(BusinessRuleException.class,
                () -> business.createMilkProduction(FARM_B, CURRENT_RG, request()));

        verify(ownershipGuard).requireCurrentFarm(GOAT_ID, FARM_B);
        verifyNoInteractions(genderValidator, milkPersistence, lactationPersistence, withdrawalQuery, mapper);
    }

    @Test
    void transferDayIsRejectedWithoutSaving() {
        doThrow(new AuthorizationDeniedException("ambiguous transfer day")).when(ownershipGuard)
                .requireUnambiguousOwnershipOnDate(GOAT_ID, FARM_B, RECORD_DATE);

        assertThrows(AuthorizationDeniedException.class,
                () -> business.createMilkProduction(FARM_B, CURRENT_RG, request()));
        verify(milkPersistence, never()).existsActiveByGoatTechnicalIdAndDateAndShift(any(), any(), any());
        verifyNoInteractions(lactationPersistence, withdrawalQuery, mapper);
        verify(milkPersistence, never()).save(any());
    }

    @Test
    void currentOwnerCannotBackdateToFormerOwnersDay() {
        doThrow(new AuthorizationDeniedException("date owned by former farm")).when(ownershipGuard)
                .requireUnambiguousOwnershipOnDate(GOAT_ID, FARM_B, RECORD_DATE);
        assertThrows(AuthorizationDeniedException.class,
                () -> business.createMilkProduction(FARM_B, CURRENT_RG, request()));
        verify(milkPersistence, never()).save(any());
    }

    @Test
    void futureDateAndMissingDateAreRejectedBeforeProductionQueries() {
        MilkProductionRequestVO future = request();
        future.setDate(LocalDate.now().plusDays(1));
        assertThrows(InvalidArgumentException.class, () -> business.createMilkProduction(FARM_B, CURRENT_RG, future));
        MilkProductionRequestVO missing = request();
        missing.setDate(null);
        assertThrows(InvalidArgumentException.class, () -> business.createMilkProduction(FARM_B, CURRENT_RG, missing));
        verify(milkPersistence, never()).existsActiveByGoatTechnicalIdAndDateAndShift(any(), any(), any());
        verifyNoInteractions(lactationPersistence, withdrawalQuery, mapper);
    }

    @Test
    void globalDuplicateUsesGoatIdAndBlocksSave() {
        when(milkPersistence.existsActiveByGoatTechnicalIdAndDateAndShift(GOAT_ID, RECORD_DATE, MilkingShift.MORNING))
                .thenReturn(true);
        assertThrows(DuplicateMilkProductionException.class,
                () -> business.createMilkProduction(FARM_B, CURRENT_RG, request()));
        verify(lactationPersistence, never()).findActiveByGoatTechnicalId(any());
        verify(withdrawalQuery, never()).getGoatWithdrawalStatus(any(GoatId.class), any(LocalDate.class));
        verify(milkPersistence, never()).save(any());
    }

    @Test
    void missingCurrentFarmActiveLactationPreservesExistingException() {
        when(lactationPersistence.findActiveByFarmIdAndGoatId(FARM_B, CURRENT_RG)).thenReturn(Optional.empty());
        assertThrows(NoActiveLactationException.class,
                () -> business.createMilkProduction(FARM_B, CURRENT_RG, request()));
        verify(withdrawalQuery, never()).getGoatWithdrawalStatus(any(GoatId.class), any(LocalDate.class));
        verify(milkPersistence, never()).save(any());
    }

    @Test
    void withdrawalFromOriginFarmIsSnapshottedByGlobalGoatIdentity() {
        when(withdrawalQuery.getGoatWithdrawalStatus(GOAT_ID, RECORD_DATE)).thenReturn(
                GoatWithdrawalStatusVO.builder().goatTechnicalId(GOAT_ID.value()).goatId("OLD-RG")
                        .referenceDate(RECORD_DATE).hasActiveMilkWithdrawal(true)
                        .milkWithdrawal(HealthWithdrawalOriginVO.builder().eventId(88L)
                                .performedDate(RECORD_DATE.minusDays(1)).withdrawalEndDate(RECORD_DATE.plusDays(3))
                                .productName("Antibiotico").build()).build());

        business.createMilkProduction(FARM_B, CURRENT_RG, request());

        ArgumentCaptor<MilkProduction> captor = ArgumentCaptor.forClass(MilkProduction.class);
        verify(milkPersistence).save(captor.capture());
        assertTrue(captor.getValue().isRecordedDuringMilkWithdrawal());
        assertEquals(88L, captor.getValue().getMilkWithdrawalEventId());
        assertEquals(RECORD_DATE.plusDays(3), captor.getValue().getMilkWithdrawalEndDate());
        assertEquals("Antibiotico", captor.getValue().getMilkWithdrawalSource());
    }

    @Test
    void authorizationAndDateChecksPrecedeDuplicateLactationAndWithdrawal() {
        InOrder order = inOrder(resolver, ownershipGuard, genderValidator, milkPersistence, lactationPersistence, withdrawalQuery);
        business.createMilkProduction(FARM_B, CURRENT_RG, request());
        order.verify(resolver).resolveGlobal(CURRENT_RG);
        order.verify(ownershipGuard).requireCurrentFarm(GOAT_ID, FARM_B);
        order.verify(genderValidator).requireFemaleAndActive(GOAT_ID);
        order.verify(ownershipGuard).requireUnambiguousOwnershipOnDate(GOAT_ID, FARM_B, RECORD_DATE);
        order.verify(milkPersistence).existsActiveByGoatTechnicalIdAndDateAndShift(GOAT_ID, RECORD_DATE, MilkingShift.MORNING);
        order.verify(lactationPersistence).findActiveByFarmIdAndGoatId(FARM_B, CURRENT_RG);
        order.verify(withdrawalQuery).getGoatWithdrawalStatus(GOAT_ID, RECORD_DATE);
    }

    private MilkProductionRequestVO request() {
        return MilkProductionRequestVO.builder().date(RECORD_DATE).shift(MilkingShift.MORNING)
                .volumeLiters(new BigDecimal("2.50")).notes("morning").build();
    }

    private Lactation activeLactation(Long farmId) {
        return activeLactation(farmId, "OLD-RG");
    }

    private Lactation activeLactation(Long farmId, String registration) {
        return Lactation.rehydrate(10L, farmId, registration, GOAT_ID.value(), LactationStatus.ACTIVE,
                RECORD_DATE.minusDays(10), null, null, null, 90, 60, null, null);
    }

    private GoatWithdrawalStatusVO noWithdrawal() {
        return GoatWithdrawalStatusVO.builder().goatTechnicalId(GOAT_ID.value()).goatId(CURRENT_RG)
                .referenceDate(RECORD_DATE).hasActiveMilkWithdrawal(false).hasActiveMeatWithdrawal(false).build();
    }
}
