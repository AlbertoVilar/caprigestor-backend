package com.devmaster.goatfarm.milk.business.lactationservice;

import com.devmaster.goatfarm.application.core.business.validation.GoatGenderValidator;
import com.devmaster.goatfarm.application.exception.AuthorizationDeniedException;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.goat.application.ports.out.GoatBirthDateQueryPort;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.goat.application.routing.GoatReferenceResolver;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.ports.in.GoatOwnershipGuardUseCase;
import com.devmaster.goatfarm.milk.application.ports.out.LactationPersistencePort;
import com.devmaster.goatfarm.milk.application.ports.out.MilkProductionSummaryQueryPort;
import com.devmaster.goatfarm.milk.business.bo.LactationRequestVO;
import com.devmaster.goatfarm.milk.business.mapper.LactationBusinessMapper;
import com.devmaster.goatfarm.milk.domain.Lactation;
import com.devmaster.goatfarm.reproduction.application.ports.in.PregnancyDryOffQueryUseCase;
import com.devmaster.goatfarm.reproduction.application.ports.in.PregnancySnapshotQueryUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LactationAgeValidationTest {

    private static final GoatId GOAT_ID = GoatId.of(42L);
    private static final long FARM_ID = 2L;
    private static final String RG = "RG-42";
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 28);

    @Mock private LactationPersistencePort lactations;
    @Mock private MilkProductionSummaryQueryPort milkSummary;
    @Mock private PregnancySnapshotQueryUseCase pregnancies;
    @Mock private PregnancyDryOffQueryUseCase dryOff;
    @Mock private GoatGenderValidator genderValidator;
    @Mock private LactationBusinessMapper mapper;
    @Mock private GoatReferenceResolver references;
    @Mock private GoatOwnershipGuardUseCase ownership;
    @Mock private GoatBirthDateQueryPort birthDates;

    private LactationBusiness business;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(TODAY.atStartOfDay(ZoneId.of("America/Sao_Paulo")).toInstant(),
                ZoneId.of("America/Sao_Paulo"));
        business = new LactationBusiness(lactations, milkSummary, pregnancies, dryOff,
                genderValidator, mapper, references, ownership, birthDates, clock);
        lenient().when(references.resolveGlobal(RG))
                .thenReturn(Optional.of(new GoatReference(GOAT_ID, FARM_ID, RG, "Matriz")));
        lenient().when(lactations.findActiveByGoatTechnicalId(GOAT_ID)).thenReturn(Optional.empty());
        lenient().when(lactations.findLatestByGoatTechnicalId(GOAT_ID)).thenReturn(Optional.empty());
        lenient().when(lactations.save(any(Lactation.class))).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void preBirthIsRejectedEvenWithConfirmation() {
        when(birthDates.findBirthDate(GOAT_ID)).thenReturn(Optional.of(TODAY));
        BusinessRuleException error = assertThrows(BusinessRuleException.class,
                () -> business.openLactation(FARM_ID, RG, request(TODAY.minusDays(1), true)));
        assertEquals("startDate", error.getFieldName());
        verify(lactations, never()).save(any());
    }

    @Test
    void newbornRequiresExplicitConfirmation() {
        when(birthDates.findBirthDate(GOAT_ID)).thenReturn(Optional.of(TODAY));
        BusinessRuleException error = assertThrows(BusinessRuleException.class,
                () -> business.openLactation(FARM_ID, RG, request(TODAY, false)));
        assertEquals("confirmYoungAge", error.getFieldName());
        verify(lactations, never()).save(any());
    }

    @Test
    void confirmedNewbornCanBeRecorded() {
        when(birthDates.findBirthDate(GOAT_ID)).thenReturn(Optional.of(TODAY));
        assertDoesNotThrow(() -> business.openLactation(FARM_ID, RG, request(TODAY, true)));
        verify(lactations).save(any(Lactation.class));
    }

    @Test
    void dayBeforeCalendarAnniversaryRequiresConfirmation() {
        LocalDate birthDate = LocalDate.of(2025, 9, 29);
        when(birthDates.findBirthDate(GOAT_ID)).thenReturn(Optional.of(birthDate));
        assertThrows(BusinessRuleException.class,
                () -> business.openLactation(FARM_ID, RG, request(TODAY, false)));
        assertDoesNotThrow(() -> business.openLactation(FARM_ID, RG, request(TODAY, true)));
    }

    @Test
    void calendarAnniversaryAndAdultNeedNoConfirmationOrReproductionHistory() {
        when(birthDates.findBirthDate(GOAT_ID)).thenReturn(Optional.of(LocalDate.of(2025, 9, 28)));
        assertDoesNotThrow(() -> business.openLactation(FARM_ID, RG, request(TODAY, false)));
        verify(lactations).save(any(Lactation.class));
    }

    @Test
    void missingCanonicalBirthDateFailsClosed() {
        when(birthDates.findBirthDate(GOAT_ID)).thenReturn(Optional.empty());
        BusinessRuleException error = assertThrows(BusinessRuleException.class,
                () -> business.openLactation(FARM_ID, RG, request(TODAY, false)));
        assertEquals("birthDate", error.getFieldName());
        verify(lactations, never()).save(any());
    }

    @Test
    void ownershipDenialPrecedesBirthDateLookup() {
        doThrow(new AuthorizationDeniedException("denied"))
                .when(ownership).requireCurrentFarm(GOAT_ID, FARM_ID);
        assertThrows(AuthorizationDeniedException.class,
                () -> business.openLactation(FARM_ID, RG, request(TODAY, false)));
        verifyNoInteractions(birthDates);
    }

    private LactationRequestVO request(LocalDate startDate, boolean confirmYoungAge) {
        return LactationRequestVO.builder().startDate(startDate).confirmYoungAge(confirmYoungAge).build();
    }
}
