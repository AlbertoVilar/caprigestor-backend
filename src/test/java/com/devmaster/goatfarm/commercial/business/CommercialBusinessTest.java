package com.devmaster.goatfarm.commercial.business;

import com.devmaster.goatfarm.application.core.business.common.EntityFinder;
import com.devmaster.goatfarm.audit.application.ports.in.OperationalAuditUseCase;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.authority.application.ports.in.CurrentPrincipalQueryUseCase;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.commercial.application.model.*;
import com.devmaster.goatfarm.commercial.application.ports.out.AnimalSalePersistencePort;
import com.devmaster.goatfarm.commercial.application.ports.out.CustomerPersistencePort;
import com.devmaster.goatfarm.commercial.application.ports.out.MilkSalePersistencePort;
import com.devmaster.goatfarm.commercial.application.ports.out.AnimalSaleReversalPersistencePort;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatOwnershipQueryPort;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatOwnershipPeriodPersistencePort;
import com.devmaster.goatfarm.goatownership.application.ports.out.OwnershipTransferPersistencePort;
import com.devmaster.goatfarm.commercial.business.bo.*;
import com.devmaster.goatfarm.commercial.enums.SalePaymentStatus;
import com.devmaster.goatfarm.farm.application.model.FarmRecord;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.goat.application.ports.in.GoatManagementUseCase;
import com.devmaster.goatfarm.goat.business.bo.GoatExitResponseVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goatownership.application.ports.in.GoatOwnershipSaleUseCase;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferKind;
import com.devmaster.goatfarm.goatownership.domain.GoatOwnershipPeriod;
import com.devmaster.goatfarm.goatownership.domain.OwnershipEntryType;
import com.devmaster.goatfarm.goatownership.domain.OwnershipExitType;
import com.devmaster.goatfarm.goat.domain.GoatId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Clock;
import java.time.ZoneId;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommercialBusinessTest {
    @Mock CustomerPersistencePort customers;
    @Mock AnimalSalePersistencePort animalSales;
    @Mock MilkSalePersistencePort milkSales;
    @Mock GoatFarmPersistencePort farms;
    @Mock GoatManagementUseCase goats;
    @Mock FarmAuthorizationUseCase authorization;
    @Mock EntityFinder finder;
    @Mock OperationalAuditUseCase audit;
    @Mock GoatOwnershipSaleUseCase ownershipTransfers;
    @Mock AnimalSaleReversalPersistencePort reversals;
    @Mock GoatOwnershipQueryPort ownershipQuery;
    @Mock GoatOwnershipPeriodPersistencePort ownershipPeriods;
    @Mock OwnershipTransferPersistencePort ownershipTransferPersistence;
    @Mock CurrentPrincipalQueryUseCase currentPrincipalQuery;
    private CommercialBusiness business;

    @BeforeEach void setUp() {
        business = new CommercialBusiness(customers, animalSales, milkSales, farms, goats, authorization, finder, audit,
                ownershipTransfers, reversals, ownershipQuery, ownershipPeriods, ownershipTransferPersistence,
                currentPrincipalQuery,
                Clock.system(ZoneId.of("America/Sao_Paulo")));
        lenient().when(authorization.canManageFarm(anyLong())).thenReturn(true);
        lenient().when(farms.findById(anyLong())).thenReturn(Optional.of(farmRecord(1L)));
        lenient().when(finder.findOrThrow(any(), anyString())).thenAnswer(i -> ((Optional<?>) ((java.util.function.Supplier<?>) i.getArgument(0)).get()).orElseThrow());
    }

    @Test void createsCustomerWithoutEntityLeak() {
        when(customers.save(any())).thenAnswer(i -> i.getArgument(0));
        CustomerResponseVO result = business.createCustomer(1L, new CustomerRequestVO("Cliente", "DOC", null, null, null));
        assertEquals("Cliente", result.name());
        verify(customers).save(any(CustomerRecord.class));
    }

    @Test void preservesCurrentAcceptanceOfDuplicateCustomerDocumentAndEmail() {
        when(customers.save(any())).thenAnswer(i -> i.getArgument(0));
        CustomerRequestVO request = new CustomerRequestVO("Cliente", "DOC-1", null, "same@example.com", null);
        assertDoesNotThrow(() -> business.createCustomer(1L, request));
        assertDoesNotThrow(() -> business.createCustomer(1L, request));
        verify(customers, times(2)).save(any(CustomerRecord.class));
    }

    @Test void createsAnimalSaleAndExitsActiveGoat() {
        CustomerRecord customer = customer(10L); GoatResponseVO goat = goat(5L, "G1", GoatStatus.ATIVO);
        when(customers.findCustomerByIdAndFarmId(10L, 1L)).thenReturn(Optional.of(customer));
        when(goats.findGoatById(1L, "G1")).thenReturn(goat); when(goats.exitGoat(anyLong(), anyString(), any())).thenReturn(new GoatExitResponseVO());
        lenient().when(animalSales.existsExternalSaleByGoatTechnicalId(5L)).thenReturn(false); when(animalSales.save(any())).thenAnswer(i -> animalRecord((AnimalSaleCommand) i.getArgument(0)));
        AnimalSaleResponseVO result = business.createAnimalSale(1L, new AnimalSaleRequestVO("G1", 10L, LocalDate.now().minusDays(1), new BigDecimal("100"), LocalDate.now(), null, null));
        assertEquals(5L, result.goatTechnicalId()); verify(goats).exitGoat(eq(1L), eq("G1"), any()); verify(animalSales).save(any(AnimalSaleCommand.class));
    }

    @Test void createsMilkSaleAndCalculatesTotal() {
        when(customers.findCustomerByIdAndFarmId(10L, 1L)).thenReturn(Optional.of(customer(10L)));
        when(milkSales.save(any())).thenAnswer(i -> milkRecord((MilkSaleCommand) i.getArgument(0)));
        MilkSaleResponseVO result = business.createMilkSale(1L, new MilkSaleRequestVO(10L, LocalDate.now().minusDays(1), new BigDecimal("2.5"), new BigDecimal("4"), LocalDate.now(), null, null));
        assertEquals(new BigDecimal("10.00"), result.totalAmount()); verify(milkSales).save(any(MilkSaleCommand.class));
    }

    @Test void externalSaleIsBlockedWhenInternalSaleIsRequested() {
        CustomerRecord customer = customer(10L); GoatResponseVO goat = goat(5L, "G-INTERNAL-REQUESTED", GoatStatus.ATIVO);
        when(customers.findCustomerByIdAndFarmId(10L, 1L)).thenReturn(Optional.of(customer));
        when(goats.findGoatById(1L, "G-INTERNAL-REQUESTED")).thenReturn(goat);
        when(ownershipTransfers.hasActiveInternalSale(1L, com.devmaster.goatfarm.goat.domain.GoatId.of(5L))).thenReturn(true);

        assertThrows(com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException.class, () -> business.createAnimalSale(1L,
                new AnimalSaleRequestVO("G-INTERNAL-REQUESTED", 10L, LocalDate.now().minusDays(1), new BigDecimal("100"), LocalDate.now(), null, null)));
        verify(goats, never()).exitGoat(anyLong(), anyString(), any());
        verify(animalSales, never()).save(any(AnimalSaleCommand.class));
    }

    @Test void externalSaleIsBlockedWhenInternalSaleIsAccepted() {
        CustomerRecord customer = customer(10L); GoatResponseVO goat = goat(5L, "G-INTERNAL-ACCEPTED", GoatStatus.ATIVO);
        when(customers.findCustomerByIdAndFarmId(10L, 1L)).thenReturn(Optional.of(customer));
        when(goats.findGoatById(1L, "G-INTERNAL-ACCEPTED")).thenReturn(goat);
        when(ownershipTransfers.hasActiveInternalSale(1L, com.devmaster.goatfarm.goat.domain.GoatId.of(5L))).thenReturn(true);

        assertThrows(com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException.class, () -> business.createAnimalSale(1L,
                new AnimalSaleRequestVO("G-INTERNAL-ACCEPTED", 10L, LocalDate.now().minusDays(1), new BigDecimal("100"), LocalDate.now(), null, null)));
        verify(goats, never()).exitGoat(anyLong(), anyString(), any());
        verify(animalSales, never()).save(any(AnimalSaleCommand.class));
    }

    @Test void sensitiveMutationsAuthorizeBeforePersistence() {
        doThrow(new AccessDeniedException("denied")).when(authorization).verifyFarmOwnership(1L);
        assertThrows(AccessDeniedException.class, () -> business.createMilkSale(1L, null));
        verifyNoInteractions(customers, animalSales, milkSales, farms, goats);
    }

    @Test void salePersistenceFailurePropagatesAfterGoatExitForOuterTransactionRollback() {
        CustomerRecord customer = customer(10L);
        GoatResponseVO goat = goat(5L, "G-ROLLBACK", GoatStatus.ATIVO);
        when(customers.findCustomerByIdAndFarmId(10L, 1L)).thenReturn(Optional.of(customer));
        when(goats.findGoatById(1L, "G-ROLLBACK")).thenReturn(goat);
        when(goats.exitGoat(anyLong(), anyString(), any())).thenReturn(new GoatExitResponseVO());
        lenient().when(animalSales.existsExternalSaleByGoatTechnicalId(5L)).thenReturn(false);
        when(animalSales.save(any())).thenThrow(new IllegalStateException("sale persistence failure"));

        assertThrows(IllegalStateException.class, () -> business.createAnimalSale(1L,
                new AnimalSaleRequestVO("G-ROLLBACK", 10L, LocalDate.now().minusDays(1), new BigDecimal("100"), LocalDate.now(), null, null)));
        verify(goats).exitGoat(eq(1L), eq("G-ROLLBACK"), any());
        verify(animalSales).save(any(AnimalSaleCommand.class));
    }

    @Test void pendingOrRejectedOwnershipSalesDoNotEnterReceivablesOrSummary() {
        AnimalSaleRecord pending = animalRecord(new AnimalSaleCommand(2L, 1L, 10L, 5L, "G1", "Cabra", LocalDate.now().minusDays(2),
                new BigDecimal("100.00"), LocalDate.now().plusDays(5), SalePaymentStatus.OPEN, null, null, 20L));
        when(animalSales.findAnimalSalesByFarmId(1L)).thenReturn(List.of(pending));
        var transfer = mock(com.devmaster.goatfarm.goatownership.domain.OwnershipTransfer.class);
        when(transfer.status()).thenReturn(OwnershipTransferStatus.REQUESTED);
        when(ownershipTransfers.findSaleTransfer(2L)).thenReturn(transfer);

        assertTrue(business.listReceivables(1L).isEmpty());
        assertEquals(0, business.getSummary(1L).animalSalesCount());
        assertEquals(BigDecimal.ZERO.setScale(2), business.getSummary(1L).animalSalesTotal());
    }

    @Test void reversedAnimalSaleRemainsInHistoryButHasNoEffectiveFinancialImpact() {
        AnimalSaleRecord reversed = animalRecord(new AnimalSaleCommand(9L, 1L, 10L, 79L, "1400819006", "ZÉLIA DA BOCAÍNA",
                LocalDate.of(2026, 9, 20), new BigDecimal("5000.00"), LocalDate.of(2026, 9, 20), SalePaymentStatus.PAID,
                LocalDate.of(2026, 9, 20), null));
        AnimalSaleRecord valid = animalRecord(new AnimalSaleCommand(10L, 1L, 10L, 80L, "1400819007", "Substituta",
                LocalDate.of(2026, 9, 21), new BigDecimal("5000.00"), LocalDate.of(2026, 9, 21), SalePaymentStatus.PAID,
                LocalDate.of(2026, 9, 21), null));
        AnimalSaleReversalRecord reversal = new AnimalSaleReversalRecord(1L, 9L, "Correção da venda", java.time.LocalDateTime.of(2026, 9, 22, 10, 0), 42L);
        when(animalSales.findAnimalSalesByFarmId(1L)).thenReturn(List.of(reversed, valid));
        when(reversals.findBySaleIds(anyCollection())).thenReturn(Map.of(9L, reversal));

        List<AnimalSaleResponseVO> history = business.listAnimalSales(1L);
        AnimalSaleResponseVO reversedResponse = history.stream().filter(item -> item.id().equals(9L)).findFirst().orElseThrow();
        AnimalSaleResponseVO validResponse = history.stream().filter(item -> item.id().equals(10L)).findFirst().orElseThrow();
        assertTrue(reversedResponse.reversed());
        assertEquals(reversal.reversedAt(), reversedResponse.reversedAt());
        assertEquals(reversal.reason(), reversedResponse.reversalReason());
        assertFalse(validResponse.reversed());
        assertNull(validResponse.reversedAt());
        assertNull(validResponse.reversalReason());

        assertTrue(business.listReceivables(1L).stream().noneMatch(item -> item.sourceId().equals(9L)));
        CommercialSummaryVO summary = business.getSummary(1L);
        assertEquals(1, summary.animalSalesCount());
        assertEquals(new BigDecimal("5000.00"), summary.animalSalesTotal());
        assertEquals(1, summary.paidReceivablesCount());
        assertEquals(new BigDecimal("5000.00"), summary.paidReceivablesTotal());
    }

    @Test void completedInternalSaleRemainsEffectiveWhenNotReversed() {
        AnimalSaleRecord completed = animalRecord(new AnimalSaleCommand(77L, 1L, 10L, 5L, "G-INTERNAL-COMPLETED", "Cabra interna",
                LocalDate.of(2026, 9, 20), new BigDecimal("750.00"), LocalDate.of(2026, 9, 20), SalePaymentStatus.PAID,
                LocalDate.of(2026, 9, 20), null, 20L));
        var transfer = mock(com.devmaster.goatfarm.goatownership.domain.OwnershipTransfer.class);
        when(transfer.status()).thenReturn(OwnershipTransferStatus.COMPLETED);
        when(animalSales.findAnimalSalesByFarmId(1L)).thenReturn(List.of(completed));
        when(ownershipTransfers.findSaleTransfer(77L)).thenReturn(transfer);
        when(reversals.findBySaleIds(anyCollection())).thenReturn(Map.of());

        CommercialSummaryVO summary = business.getSummary(1L);

        assertEquals(1, summary.animalSalesCount());
        assertEquals(new BigDecimal("750.00"), summary.animalSalesTotal());
        assertEquals(1, summary.paidReceivablesCount());
    }

    @Test void legacyAnimalSalePaymentCannotBypassOwnershipSaleWorkflow() {
        AnimalSaleRecord ownershipSale = animalRecord(new AnimalSaleCommand(77L, 1L, 10L, 5L, "G1", "Cabra",
                LocalDate.now().minusDays(2), new BigDecimal("100.00"), LocalDate.now().plusDays(5),
                SalePaymentStatus.OPEN, null, null, 20L));
        when(animalSales.findAnimalSaleByIdAndFarmId(77L, 1L)).thenReturn(Optional.of(ownershipSale));

        assertThrows(com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException.class,
                () -> business.registerAnimalSalePayment(1L, 77L,
                        new SalePaymentRequestVO(LocalDate.now().minusDays(1))));
        verify(animalSales, never()).save(any(AnimalSaleCommand.class));
    }

    @Test void reversesOnlyExternalSaleAndCreatesAuditableCorrectionReentry() {
        Instant ended = Instant.parse("2026-09-20T23:27:21Z");
        AnimalSaleRecord sale = animalRecord(new AnimalSaleCommand(9L, 1L, 10L, 79L, "1400819006", "ZÉLIA DA BOCAÍNA",
                LocalDate.of(2026, 9, 20), new BigDecimal("100.00"), LocalDate.of(2026, 9, 30), SalePaymentStatus.PAID,
                LocalDate.of(2026, 9, 20), null));
        GoatResponseVO goat = goat(79L, "1400819006", GoatStatus.VENDIDO); goat.setExitType(com.devmaster.goatfarm.goat.enums.GoatExitType.VENDA);
        GoatOwnershipPeriod closed = GoatOwnershipPeriod.rehydrate(82L, GoatId.of(79L), 1L,
                ended.minusSeconds(3600), ended, OwnershipEntryType.ABCC_IMPORT, OwnershipExitType.EXTERNAL_SALE, "GOAT_CREATE:ABCC_IMPORT");
        when(animalSales.findAnimalSaleByIdAndFarmId(9L, 1L)).thenReturn(Optional.of(sale));
        when(ownershipQuery.findOwnershipHistory(GoatId.of(79L))).thenReturn(List.of(closed));
        when(goats.findGoatById(1L, "technical-79")).thenReturn(goat);
        when(reversals.findBySaleId(9L)).thenReturn(Optional.empty());
        when(currentPrincipalQuery.requireCurrent()).thenReturn(new AuthenticatedPrincipal(42L, "admin@test", "Admin", Set.of("ROLE_ADMIN")));
        when(ownershipPeriods.save(any())).thenAnswer(i -> i.getArgument(0));

        AnimalSaleReversalRecord reversal = new AnimalSaleReversalRecord(1L, 9L, "Destino correto: Capril Vilar", java.time.LocalDateTime.of(2026, 9, 21, 10, 0), 42L);
        when(reversals.save(eq(9L), eq("Destino correto: Capril Vilar"), any(), eq(42L))).thenReturn(reversal);
        AnimalSaleResponseVO response = business.reverseExternalAnimalSale(1L, 9L, "Destino correto: Capril Vilar");
        assertTrue(response.reversed());
        assertEquals(reversal.reversedAt(), response.reversedAt());
        assertEquals(reversal.reason(), response.reversalReason());
        verify(ownershipPeriods).save(argThat(p -> p.entryType() == OwnershipEntryType.CORRECTION_REENTRY
                && p.source().equals("ANIMAL_SALE_REVERSAL:9") && p.farmId() == 1L));
        verify(goats).restoreAfterSaleReversal(1L, "technical-79");
        verify(reversals).save(eq(9L), eq("Destino correto: Capril Vilar"), any(), eq(42L));
    }

    @Test void refusesReversalWhenAnyOwnershipTransferAlreadyExists() {
        AnimalSaleRecord sale = animalRecord(new AnimalSaleCommand(9L, 1L, 10L, 79L, "1400819006", "ZÉLIA DA BOCAÍNA",
                LocalDate.of(2026, 9, 20), new BigDecimal("100.00"), LocalDate.of(2026, 9, 30), SalePaymentStatus.PAID,
                LocalDate.of(2026, 9, 20), null));
        when(animalSales.findAnimalSaleByIdAndFarmId(9L, 1L)).thenReturn(Optional.of(sale));
        when(reversals.findBySaleId(9L)).thenReturn(Optional.empty());
        when(currentPrincipalQuery.requireCurrent()).thenReturn(new AuthenticatedPrincipal(42L, "admin@test", "Admin", Set.of("ROLE_ADMIN")));
        when(ownershipTransferPersistence.existsByGoatId(GoatId.of(79L))).thenReturn(true);

        assertThrows(com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException.class,
                () -> business.reverseExternalAnimalSale(1L, 9L, "correção"));
        verifyNoInteractions(ownershipQuery, ownershipPeriods);
        verify(reversals).findBySaleId(9L);
    }

    @Test void refusesDuplicateReversal() {
        AnimalSaleRecord sale = animalRecord(new AnimalSaleCommand(9L, 1L, 10L, 79L, "1400819006", "ZÉLIA DA BOCAÍNA",
                LocalDate.of(2026, 9, 20), new BigDecimal("100.00"), LocalDate.of(2026, 9, 30), SalePaymentStatus.PAID,
                LocalDate.of(2026, 9, 20), null));
        when(currentPrincipalQuery.requireCurrent()).thenReturn(new AuthenticatedPrincipal(42L, "admin@test", "Admin", Set.of("ROLE_ADMIN")));
        when(animalSales.findAnimalSaleByIdAndFarmId(9L, 1L)).thenReturn(Optional.of(sale));
        when(reversals.findBySaleId(9L)).thenReturn(Optional.of(new com.devmaster.goatfarm.commercial.application.model.AnimalSaleReversalRecord(1L, 9L, "already", java.time.LocalDateTime.now(), 42L)));

        assertThrows(com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException.class,
                () -> business.reverseExternalAnimalSale(1L, 9L, "correção"));
        verifyNoInteractions(ownershipQuery, ownershipPeriods, goats);
    }

    @Test void farmOwnerCannotReverseCompletedSale() {
        when(currentPrincipalQuery.requireCurrent()).thenReturn(new AuthenticatedPrincipal(7L, "owner@test", "Owner", Set.of("ROLE_FARM_OWNER")));
        assertThrows(com.devmaster.goatfarm.application.exception.AuthorizationDeniedException.class,
                () -> business.reverseExternalAnimalSale(1L, 9L, "correção"));
        verifyNoInteractions(animalSales, reversals, ownershipQuery, ownershipPeriods);
    }

    @Test void operatorCannotReverseCompletedSale() {
        when(currentPrincipalQuery.requireCurrent()).thenReturn(new AuthenticatedPrincipal(8L, "operator@test", "Operator", Set.of("ROLE_OPERATOR")));
        assertThrows(com.devmaster.goatfarm.application.exception.AuthorizationDeniedException.class,
                () -> business.reverseExternalAnimalSale(1L, 9L, "correção"));
        verifyNoInteractions(animalSales, reversals, ownershipQuery, ownershipPeriods);
    }

    @Test void refusesReversalWhenLaterOwnershipExists() {
        Instant ended = Instant.parse("2026-09-20T23:27:21Z");
        AnimalSaleRecord sale = animalRecord(new AnimalSaleCommand(9L, 1L, 10L, 79L, "1400819006", "ZÉLIA DA BOCAÍNA",
                LocalDate.of(2026, 9, 20), new BigDecimal("100.00"), LocalDate.of(2026, 9, 30), SalePaymentStatus.PAID,
                LocalDate.of(2026, 9, 20), null));
        GoatOwnershipPeriod historical = GoatOwnershipPeriod.rehydrate(82L, GoatId.of(79L), 1L, ended.minusSeconds(3600), ended,
                OwnershipEntryType.ABCC_IMPORT, OwnershipExitType.EXTERNAL_SALE, "GOAT_CREATE:ABCC_IMPORT");
        GoatOwnershipPeriod later = GoatOwnershipPeriod.open(GoatId.of(79L), 2L, ended.plusSeconds(1), OwnershipEntryType.TRANSFER_IN, "later");
        when(currentPrincipalQuery.requireCurrent()).thenReturn(new AuthenticatedPrincipal(42L, "admin@test", "Admin", Set.of("ROLE_ADMIN")));
        when(animalSales.findAnimalSaleByIdAndFarmId(9L, 1L)).thenReturn(Optional.of(sale));
        when(reversals.findBySaleId(9L)).thenReturn(Optional.empty());
        when(ownershipQuery.findOwnershipHistory(GoatId.of(79L))).thenReturn(List.of(historical, later));

        assertThrows(com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException.class,
                () -> business.reverseExternalAnimalSale(1L, 9L, "correção"));
        verifyNoInteractions(ownershipPeriods, goats);
    }

    @Test void refusesReversalWhenGoatProjectionIsIncompatible() {
        Instant ended = Instant.parse("2026-09-20T23:27:21Z");
        AnimalSaleRecord sale = animalRecord(new AnimalSaleCommand(9L, 1L, 10L, 79L, "1400819006", "ZÉLIA DA BOCAÍNA",
                LocalDate.of(2026, 9, 20), new BigDecimal("100.00"), LocalDate.of(2026, 9, 30), SalePaymentStatus.PAID,
                LocalDate.of(2026, 9, 20), null));
        GoatOwnershipPeriod closed = GoatOwnershipPeriod.rehydrate(82L, GoatId.of(79L), 1L, ended.minusSeconds(3600), ended,
                OwnershipEntryType.ABCC_IMPORT, OwnershipExitType.EXTERNAL_SALE, "GOAT_CREATE:ABCC_IMPORT");
        GoatResponseVO activeWithWrongExit = goat(79L, "1400819006", GoatStatus.ATIVO);
        when(currentPrincipalQuery.requireCurrent()).thenReturn(new AuthenticatedPrincipal(42L, "admin@test", "Admin", Set.of("ROLE_ADMIN")));
        when(animalSales.findAnimalSaleByIdAndFarmId(9L, 1L)).thenReturn(Optional.of(sale));
        when(reversals.findBySaleId(9L)).thenReturn(Optional.empty());
        when(ownershipQuery.findOwnershipHistory(GoatId.of(79L))).thenReturn(List.of(closed));
        when(goats.findGoatById(1L, "technical-79")).thenReturn(activeWithWrongExit);

        assertThrows(com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException.class,
                () -> business.reverseExternalAnimalSale(1L, 9L, "correção"));
        verifyNoInteractions(ownershipPeriods);
    }

    private CustomerRecord customer(Long id) { return new CustomerRecord(id, 1L, "Cliente", null, null, null, null, true, null, null); }
    private FarmRecord farmRecord(Long id) { return new FarmRecord(id, "Fazenda", null, null, null, null, List.of(), null, null, null); }
    private GoatResponseVO goat(Long id, String rg, GoatStatus status) { GoatResponseVO g = new GoatResponseVO(); g.setTechnicalId(id); g.setRegistrationNumber(rg); g.setName("Cabra"); g.setStatus(status); g.setBirthDate(LocalDate.now().minusYears(2)); return g; }
    private AnimalSaleRecord animalRecord(AnimalSaleCommand c) { return new AnimalSaleRecord(c.id() == null ? 1L : c.id(), c.farmId(), c.customerId(), new CustomerReference(c.customerId(), "Cliente", true), c.goatTechnicalId(), c.goatRegistrationNumber(), c.goatName(), c.saleDate(), c.amount(), c.dueDate(), c.paymentStatus(), c.paymentDate(), c.notes(), null, null, c.targetFarmId()); }
    private MilkSaleRecord milkRecord(MilkSaleCommand c) { return new MilkSaleRecord(1L, c.farmId(), c.customerId(), new CustomerReference(c.customerId(), "Cliente", true), c.saleDate(), c.quantityLiters(), c.unitPrice(), c.totalAmount(), c.dueDate(), c.paymentStatus(), c.paymentDate(), c.notes(), null, null); }
}
