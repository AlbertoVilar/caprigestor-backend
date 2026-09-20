package com.devmaster.goatfarm.commercial.business;

import com.devmaster.goatfarm.audit.application.ports.in.OperationalAuditUseCase;
import com.devmaster.goatfarm.authority.application.ports.in.CurrentPrincipalQueryUseCase;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.commercial.application.model.AnimalSaleCommand;
import com.devmaster.goatfarm.commercial.application.model.AnimalSaleRecord;
import com.devmaster.goatfarm.commercial.application.model.CustomerRecord;
import com.devmaster.goatfarm.commercial.application.model.CustomerReference;
import com.devmaster.goatfarm.commercial.application.ports.out.AnimalSalePersistencePort;
import com.devmaster.goatfarm.commercial.application.ports.out.CustomerPersistencePort;
import com.devmaster.goatfarm.commercial.business.bo.OwnershipSaleRequestVO;
import com.devmaster.goatfarm.commercial.business.bo.SalePaymentRequestVO;
import com.devmaster.goatfarm.commercial.enums.SalePaymentStatus;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.farm.application.model.FarmRecord;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.goat.application.ports.in.GoatManagementUseCase;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.GoatOwnershipLockState;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatCurrentOwnerProjectionPort;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatOwnershipLockPort;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatOwnershipPeriodPersistencePort;
import com.devmaster.goatfarm.goatownership.application.ports.out.OwnershipTransferPersistencePort;
import com.devmaster.goatfarm.goatownership.domain.GoatOwnershipPeriod;
import com.devmaster.goatfarm.goatownership.domain.OwnershipEntryType;
import com.devmaster.goatfarm.goatownership.domain.OwnershipExitType;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransfer;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferKind;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OwnershipSaleBusinessTest {
    private static final long SOURCE = 10L;
    private static final long TARGET = 20L;
    private static final GoatId GOAT = GoatId.of(42L);

    @Mock CustomerPersistencePort customers;
    @Mock AnimalSalePersistencePort sales;
    @Mock GoatFarmPersistencePort farms;
    @Mock GoatManagementUseCase goats;
    @Mock FarmAuthorizationUseCase authorization;
    @Mock CurrentPrincipalQueryUseCase principals;
    @Mock GoatOwnershipLockPort ownershipLock;
    @Mock GoatOwnershipPeriodPersistencePort periods;
    @Mock OwnershipTransferPersistencePort transfers;
    @Mock GoatCurrentOwnerProjectionPort projection;
    @Mock OperationalAuditUseCase audit;
    private OwnershipSaleBusiness business;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-09-19T12:00:00Z"), ZoneOffset.UTC);
        business = new OwnershipSaleBusiness(customers, sales, farms, goats, authorization, principals,
                ownershipLock, periods, transfers, projection, audit, clock);
        when(farms.findById(SOURCE)).thenReturn(Optional.of(farm(SOURCE)));
        when(farms.findById(TARGET)).thenReturn(Optional.of(farm(TARGET)));
        when(customers.findCustomerByIdAndFarmId(7L, SOURCE)).thenReturn(Optional.of(customer()));
        when(customers.findCustomerByIdAndFarmId(8L, SOURCE)).thenReturn(Optional.of(new CustomerRecord(8L, SOURCE, "Other buyer", null, null, null, null, true, null, null)));
        when(principals.requireCurrent()).thenReturn(new AuthenticatedPrincipal(99L, "seller@test", "Seller", Set.of("ROLE_FARM_OWNER")));
        when(goats.findGoatById(SOURCE, "technical-42")).thenReturn(goat());
        when(ownershipLock.lockGoatOwnership(GOAT)).thenAnswer(invocation -> Optional.of(lockState()));
        when(transfers.findPendingByGoatId(GOAT)).thenReturn(Optional.empty());
        when(sales.existsByFarmIdAndGoatTechnicalId(SOURCE, GOAT.value())).thenReturn(false);
        when(sales.save(any(AnimalSaleCommand.class))).thenAnswer(invocation -> sale((AnimalSaleCommand) invocation.getArgument(0)));
        when(transfers.save(any(OwnershipTransfer.class))).thenAnswer(invocation -> withId((OwnershipTransfer) invocation.getArgument(0)));
    }

    @Test
    void requestCreatesPendingInternalSaleWithoutGoatExitOrOwnershipHandoff() {
        var result = business.requestOwnershipSale(SOURCE, request());

        assertThat(result.ownershipTransferStatus()).isEqualTo(OwnershipTransferStatus.REQUESTED);
        assertThat(result.paymentStatus()).isEqualTo(SalePaymentStatus.OPEN);
        assertThat(result.targetFarmId()).isEqualTo(TARGET);
        ArgumentCaptor<OwnershipTransfer> transfer = ArgumentCaptor.forClass(OwnershipTransfer.class);
        verify(transfers).save(transfer.capture());
        assertThat(transfer.getValue().kind()).isEqualTo(OwnershipTransferKind.INTERNAL_SALE);
        assertThat(transfer.getValue().saleId()).isEqualTo(501L);
        verify(periods, never()).handoff(any(), any());
        verify(goats, never()).exitGoat(anyLong(), any(), any());
    }

    @Test
    void requestResolvesNumericCompatibilityTokenAsStructuralTechnicalGoatId() {
        business.requestOwnershipSale(SOURCE, request());

        verify(goats).findGoatById(SOURCE, "technical-42");
        verify(sales).save(argThat(command -> command.goatTechnicalId().equals(GOAT.value())));
    }

    @Test
    void requestRechecksIdempotencyAfterGoatLockAndRejectsMaterialPayloadChange() {
        OwnershipTransfer duplicate = transfer(700L, OwnershipTransferStatus.REQUESTED);
        when(transfers.findByRequesterAndIdempotencyKey(99L, "sale-42"))
                .thenReturn(Optional.empty(), Optional.of(duplicate));
        when(sales.findAnimalSaleById(501L)).thenReturn(Optional.of(sale(new AnimalSaleCommand(501L, SOURCE, 7L,
                GOAT.value(), "RG-42", "Goat", date(), amount(), date().plusDays(2), SalePaymentStatus.OPEN, null, "sale", TARGET))));
        OwnershipSaleRequestVO changed = new OwnershipSaleRequestVO("42", 8L, TARGET, date(), amount(), date().plusDays(2), "sale", "sale-42");

        assertThatThrownBy(() -> business.requestOwnershipSale(SOURCE, changed))
                .isInstanceOf(BusinessRuleException.class);

        verify(ownershipLock).lockGoatOwnership(GOAT);
        verify(sales, never()).save(any());
    }

    @Test
    void requestRejectsAnotherPendingOwnershipProcessBeforePersistingSale() {
        when(transfers.findPendingByGoatId(GOAT)).thenReturn(Optional.of(transfer(800L, OwnershipTransferStatus.REQUESTED)));

        assertThatThrownBy(() -> business.requestOwnershipSale(SOURCE, request()))
                .isInstanceOf(BusinessRuleException.class);

        verify(sales, never()).save(any());
        verify(periods, never()).handoff(any(), any());
    }

    @Test
    void acceptMarksPaymentAndHandsOffCanonicalOwnershipInOneBusinessTransaction() {
        AnimalSaleRecord openSale = sale(new AnimalSaleCommand(501L, SOURCE, 7L, GOAT.value(), "RG-42", "Goat", date(), amount(), date().plusDays(2), SalePaymentStatus.OPEN, null, null, TARGET));
        OwnershipTransfer requested = transfer(700L, OwnershipTransferStatus.REQUESTED);
        when(sales.findAnimalSaleByIdAndFarmId(501L, SOURCE)).thenReturn(Optional.of(openSale));
        when(transfers.findBySaleId(501L)).thenReturn(Optional.of(requested));
        when(authorization.canAdministerFarm(TARGET)).thenReturn(true);
        when(projection.moveFromTo(GOAT, SOURCE, TARGET)).thenReturn(true);
        GoatOwnershipPeriod source = lockState().openPeriod().orElseThrow();
        when(periods.findByGoatIdOrderByStartedAt(GOAT)).thenReturn(List.of(source));

        var result = business.acceptOwnershipSale(SOURCE, 501L, new SalePaymentRequestVO(date().plusDays(1)));

        assertThat(result.ownershipTransferStatus()).isEqualTo(OwnershipTransferStatus.COMPLETED);
        assertThat(result.paymentStatus()).isEqualTo(SalePaymentStatus.PAID);
        ArgumentCaptor<GoatOwnershipPeriod> closed = ArgumentCaptor.forClass(GoatOwnershipPeriod.class);
        ArgumentCaptor<GoatOwnershipPeriod> opened = ArgumentCaptor.forClass(GoatOwnershipPeriod.class);
        verify(periods).handoff(closed.capture(), opened.capture());
        assertThat(closed.getValue().exitType()).isEqualTo(OwnershipExitType.EXTERNAL_SALE);
        assertThat(opened.getValue().entryType()).isEqualTo(OwnershipEntryType.PURCHASE);
        verify(projection).moveFromTo(GOAT, SOURCE, TARGET);
        verify(transfers, times(1)).save(argThat(value -> value.status() == OwnershipTransferStatus.COMPLETED));
    }

    @Test
    void acceptFailsClosedWithoutProjectionMutationWhenSellerNoLongerOwnsGoat() {
        AnimalSaleRecord openSale = sale(new AnimalSaleCommand(501L, SOURCE, 7L, GOAT.value(), "RG-42", "Goat", date(), amount(), date().plusDays(2), SalePaymentStatus.OPEN, null, null, TARGET));
        when(sales.findAnimalSaleByIdAndFarmId(501L, SOURCE)).thenReturn(Optional.of(openSale));
        when(transfers.findBySaleId(501L)).thenReturn(Optional.of(transfer(700L, OwnershipTransferStatus.REQUESTED)));
        when(authorization.canAdministerFarm(TARGET)).thenReturn(true);
        GoatOwnershipPeriod foreign = GoatOwnershipPeriod.open(GOAT, 30L, Instant.parse("2026-01-01T00:00:00Z"), OwnershipEntryType.MANUAL_IMPORT, "baseline");
        when(ownershipLock.lockGoatOwnership(GOAT)).thenReturn(Optional.of(new GoatOwnershipLockState(GOAT, Optional.of(foreign))));

        assertThatThrownBy(() -> business.acceptOwnershipSale(SOURCE, 501L, new SalePaymentRequestVO(date().plusDays(1))))
                .isInstanceOf(BusinessRuleException.class);

        verify(projection, never()).moveFromTo(any(), anyLong(), anyLong());
        verify(periods, never()).handoff(any(), any());
    }

    @Test
    void acceptReloadsTerminalStateAfterLockInsteadOfRegressingIt() {
        AnimalSaleRecord openSale = sale(new AnimalSaleCommand(501L, SOURCE, 7L, GOAT.value(), "RG-42", "Goat", date(), amount(), date().plusDays(2), SalePaymentStatus.OPEN, null, null, TARGET));
        AnimalSaleRecord paidSale = sale(new AnimalSaleCommand(501L, SOURCE, 7L, GOAT.value(), "RG-42", "Goat", date(), amount(), date().plusDays(2), SalePaymentStatus.PAID, date().plusDays(1), null, TARGET));
        OwnershipTransfer requested = transfer(700L, OwnershipTransferStatus.REQUESTED);
        OwnershipTransfer completed = transfer(700L, OwnershipTransferStatus.COMPLETED);
        when(sales.findAnimalSaleByIdAndFarmId(501L, SOURCE)).thenReturn(Optional.of(openSale), Optional.of(paidSale));
        when(transfers.findBySaleId(501L)).thenReturn(Optional.of(requested), Optional.of(completed));
        when(authorization.canAdministerFarm(TARGET)).thenReturn(true);
        when(ownershipLock.lockGoatOwnership(GOAT)).thenReturn(Optional.of(new GoatOwnershipLockState(GOAT, Optional.of(
                GoatOwnershipPeriod.rehydrate(101L, GOAT, TARGET, Instant.parse("2026-09-19T12:00:00Z"), null,
                        OwnershipEntryType.PURCHASE, null, "OWNERSHIP_SALE:501")))));

        var result = business.acceptOwnershipSale(SOURCE, 501L, new SalePaymentRequestVO(date().plusDays(1)));

        assertThat(result.ownershipTransferStatus()).isEqualTo(OwnershipTransferStatus.COMPLETED);
        verify(periods, never()).handoff(any(), any());
        verify(projection, never()).moveFromTo(any(), anyLong(), anyLong());
        verify(transfers, never()).save(any());
    }

    private OwnershipSaleRequestVO request() {
        return new OwnershipSaleRequestVO("42", 7L, TARGET, date(), amount(), date().plusDays(2), "sale", "sale-42");
    }

    private AnimalSaleRecord sale(AnimalSaleCommand command) {
        return new AnimalSaleRecord(command.id() == null ? 501L : command.id(), command.farmId(), command.customerId(),
                new CustomerReference(command.customerId(), "Buyer", true), command.goatTechnicalId(), command.goatRegistrationNumber(),
                command.goatName(), command.saleDate(), command.amount(), command.dueDate(), command.paymentStatus(),
                command.paymentDate(), command.notes(), null, null, command.targetFarmId());
    }

    private OwnershipTransfer transfer(long id, OwnershipTransferStatus status) {
        Instant requested = Instant.parse("2026-09-18T12:00:00Z");
        return OwnershipTransfer.rehydrate(id, GOAT, SOURCE, TARGET, OwnershipTransferKind.INTERNAL_SALE, status,
                "OWNERSHIP_SALE:501", "sale-42", requested,
                status == OwnershipTransferStatus.COMPLETED ? requested.plusSeconds(1) : null,
                status == OwnershipTransferStatus.COMPLETED ? requested.plusSeconds(1) : null,
                status == OwnershipTransferStatus.COMPLETED ? requested.plusSeconds(1) : null,
                null, 99L, status == OwnershipTransferStatus.COMPLETED ? 77L : null,
                status == OwnershipTransferStatus.COMPLETED ? 77L : null, 501L);
    }

    private OwnershipTransfer withId(OwnershipTransfer transfer) {
        return OwnershipTransfer.rehydrate(700L, transfer.goatId(), transfer.sourceFarmId(), transfer.targetFarmId(),
                transfer.kind(), transfer.status(), transfer.reason(), transfer.idempotencyKey(), transfer.requestedAt(),
                transfer.acceptedAt(), transfer.effectiveAt(), transfer.completedAt(), transfer.cancelledAt(), transfer.requestedBy(),
                transfer.acceptedBy(), transfer.completedBy(), transfer.saleId());
    }

    private GoatOwnershipLockState lockState() {
        return new GoatOwnershipLockState(GOAT, Optional.of(GoatOwnershipPeriod.rehydrate(101L, GOAT, SOURCE,
                Instant.parse("2026-01-01T00:00:00Z"), null, OwnershipEntryType.MANUAL_IMPORT, null, "baseline")));
    }

    private CustomerRecord customer() { return new CustomerRecord(7L, SOURCE, "Buyer", null, null, null, null, true, null, null); }
    private FarmRecord farm(long id) { return new FarmRecord(id, "Farm", null, null, null, null, List.of(), null, null, null); }
    private GoatResponseVO goat() { GoatResponseVO goat = new GoatResponseVO(); goat.setTechnicalId(GOAT.value()); goat.setRegistrationNumber("RG-42"); goat.setName("Goat"); return goat; }
    private LocalDate date() { return LocalDate.of(2026, 9, 18); }
    private BigDecimal amount() { return new BigDecimal("100.00"); }
}
