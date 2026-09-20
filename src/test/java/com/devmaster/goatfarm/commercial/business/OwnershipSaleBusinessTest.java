package com.devmaster.goatfarm.commercial.business;

import com.devmaster.goatfarm.audit.application.ports.in.OperationalAuditUseCase;
import com.devmaster.goatfarm.authority.application.ports.in.CurrentPrincipalQueryUseCase;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.commercial.application.model.*;
import com.devmaster.goatfarm.commercial.application.ports.out.*;
import com.devmaster.goatfarm.commercial.business.bo.*;
import com.devmaster.goatfarm.commercial.enums.SalePaymentStatus;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.farm.application.model.FarmRecord;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.goat.application.ports.in.GoatManagementUseCase;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.ports.in.GoatOwnershipSaleUseCase;
import com.devmaster.goatfarm.goatownership.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OwnershipSaleBusinessTest {
    private static final long SOURCE = 10L;
    private static final long TARGET = 20L;
    @Mock CustomerPersistencePort customers;
    @Mock AnimalSalePersistencePort sales;
    @Mock GoatFarmPersistencePort farms;
    @Mock GoatManagementUseCase goats;
    @Mock FarmAuthorizationUseCase authorization;
    @Mock CurrentPrincipalQueryUseCase principals;
    @Mock GoatOwnershipSaleUseCase ownership;
    @Mock OperationalAuditUseCase audit;
    private OwnershipSaleBusiness business;

    @BeforeEach
    void setUp() {
        business = new OwnershipSaleBusiness(customers, sales, farms, goats, authorization, principals, ownership, audit,
                Clock.fixed(Instant.parse("2026-09-19T12:00:00Z"), ZoneOffset.UTC));
        lenient().when(farms.findById(SOURCE)).thenReturn(Optional.of(farm(SOURCE)));
        lenient().when(farms.findById(TARGET)).thenReturn(Optional.of(farm(TARGET)));
        lenient().when(customers.findCustomerByIdAndFarmId(7L, SOURCE)).thenReturn(Optional.of(new CustomerRecord(7L, SOURCE, "Buyer", null, null, null, null, true, null, null)));
        lenient().when(principals.requireCurrent()).thenReturn(new AuthenticatedPrincipal(99L, "seller@test", "Seller", Set.of("ROLE_FARM_OWNER")));
        lenient().when(authorization.canAdministerFarm(TARGET)).thenReturn(true);
        lenient().when(goats.findGoatById(SOURCE, "technical-42")).thenReturn(goat(42L, "42"));
        lenient().when(sales.save(any(AnimalSaleCommand.class))).thenAnswer(invocation -> sale((AnimalSaleCommand) invocation.getArgument(0)));
        lenient().when(ownership.requestInternalSale(any())).thenAnswer(invocation -> transfer(700L, OwnershipTransferStatus.REQUESTED));
        lenient().when(ownership.findSaleTransfer(501L)).thenAnswer(invocation -> transfer(700L, OwnershipTransferStatus.REQUESTED));
    }

    @Test
    void requestRequiresUnambiguousStructuralTechnicalToken() {
        assertThatThrownBy(() -> business.requestOwnershipSale(SOURCE, new OwnershipSaleRequestVO("42", 7L, TARGET, date(), amount(), date().plusDays(2), "sale", "sale-42")))
                .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    void sameRegistrationDifferentTechnicalIdsRemainStructurallyAddressable() {
        when(goats.findGoatById(SOURCE, "technical-84")).thenReturn(goat(84L, "42"));
        business.requestOwnershipSale(SOURCE, new OwnershipSaleRequestVO("technical-84", 7L, TARGET, date(), amount(), date().plusDays(2), "sale", "sale-84"));
        verify(goats).findGoatById(SOURCE, "technical-84");
        verify(ownership).requestInternalSale(any());
    }

    @Test
    void paymentAndAcceptanceAreIndependentPrerequisites() {
        OwnershipTransfer accepted = transfer(700L, OwnershipTransferStatus.ACCEPTED);
        when(ownership.acceptInternalSale(501L, false)).thenReturn(accepted);
        AnimalSaleRecord open = sale(new AnimalSaleCommand(501L, SOURCE, 7L, 42L, "42", "Goat", date(), amount(), date().plusDays(2), SalePaymentStatus.OPEN, null, null, TARGET));
        when(sales.findAnimalSaleByIdAndFarmId(501L, SOURCE)).thenReturn(Optional.of(open));
        var result = business.acceptOwnershipSale(SOURCE, 501L);
        assertThat(result.ownershipTransferStatus()).isEqualTo(OwnershipTransferStatus.ACCEPTED);
        assertThat(result.paymentStatus()).isEqualTo(SalePaymentStatus.OPEN);
        verify(ownership).acceptInternalSale(501L, false);
    }

    @Test
    void paymentFirstDoesNotCompleteUntilAcceptance() {
        OwnershipTransfer requested = transfer(700L, OwnershipTransferStatus.REQUESTED);
        when(ownership.completeInternalSaleAfterPayment(501L)).thenReturn(requested);
        AnimalSaleRecord open = sale(new AnimalSaleCommand(501L, SOURCE, 7L, 42L, "42", "Goat", date(), amount(), date().plusDays(2), SalePaymentStatus.OPEN, null, null, TARGET));
        when(sales.findAnimalSaleByIdAndFarmId(501L, SOURCE)).thenReturn(Optional.of(open));
        var result = business.registerOwnershipSalePayment(SOURCE, 501L, new SalePaymentRequestVO(date().plusDays(1)));
        assertThat(result.paymentStatus()).isEqualTo(SalePaymentStatus.PAID);
        assertThat(result.ownershipTransferStatus()).isEqualTo(OwnershipTransferStatus.REQUESTED);
        verify(ownership).completeInternalSaleAfterPayment(501L);
    }

    @Test
    void paidSaleCannotBeRejectedOrCancelled() {
        AnimalSaleRecord paid = sale(new AnimalSaleCommand(501L, SOURCE, 7L, 42L, "42", "Goat", date(), amount(),
                date().plusDays(2), SalePaymentStatus.PAID, date().plusDays(1), null, TARGET));
        when(sales.findAnimalSaleByIdAndFarmId(501L, SOURCE)).thenReturn(Optional.of(paid));

        assertThatThrownBy(() -> business.rejectOwnershipSale(SOURCE, 501L))
                .isInstanceOf(com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException.class);
        assertThatThrownBy(() -> business.cancelOwnershipSale(SOURCE, 501L))
                .isInstanceOf(com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException.class);
        verify(ownership, never()).rejectInternalSale(501L);
        verify(ownership, never()).cancelInternalSale(501L);
    }

    @Test
    void paymentCannotBeRecordedAfterRejectedOrCancelledTransfer() {
        AnimalSaleRecord open = sale(new AnimalSaleCommand(501L, SOURCE, 7L, 42L, "42", "Goat", date(), amount(),
                date().plusDays(2), SalePaymentStatus.OPEN, null, null, TARGET));
        when(sales.findAnimalSaleByIdAndFarmId(501L, SOURCE)).thenReturn(Optional.of(open));
        when(ownership.findSaleTransfer(501L)).thenReturn(transfer(700L, OwnershipTransferStatus.REJECTED),
                transfer(700L, OwnershipTransferStatus.CANCELLED));

        assertThatThrownBy(() -> business.registerOwnershipSalePayment(SOURCE, 501L,
                new SalePaymentRequestVO(date().plusDays(1))))
                .isInstanceOf(com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException.class);
        assertThatThrownBy(() -> business.registerOwnershipSalePayment(SOURCE, 501L,
                new SalePaymentRequestVO(date().plusDays(1))))
                .isInstanceOf(com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException.class);
        verify(sales, never()).save(any(AnimalSaleCommand.class));
    }

    @Test
    void concurrentEquivalentRetryReturnsOriginalSale() {
        when(ownership.requestInternalSale(any())).thenReturn(transfer(900L, OwnershipTransferStatus.REQUESTED, 900L));
        AnimalSaleRecord original = sale(new AnimalSaleCommand(900L, SOURCE, 7L, 42L, "42", "Goat", date(), amount(),
                date().plusDays(2), SalePaymentStatus.OPEN, null, "sale", TARGET));
        when(sales.findAnimalSaleById(900L)).thenReturn(Optional.of(original));

        var result = business.requestOwnershipSale(SOURCE, new OwnershipSaleRequestVO(
                "technical-42", 7L, TARGET, date(), amount(), date().plusDays(2), "sale", "sale-42"));

        assertThat(result.saleId()).isEqualTo(900L);
        assertThat(result.ownershipTransferId()).isEqualTo(900L);
    }

    private OwnershipTransfer transfer(long id, OwnershipTransferStatus status) {
        return transfer(id, status, 501L);
    }
    private OwnershipTransfer transfer(long id, OwnershipTransferStatus status, long saleId) {
        Instant requested = Instant.parse("2026-09-18T12:00:00Z");
        boolean accepted = status == OwnershipTransferStatus.ACCEPTED || status == OwnershipTransferStatus.COMPLETED;
        Instant cancelled = status == OwnershipTransferStatus.CANCELLED ? requested.plusSeconds(1) : null;
        return OwnershipTransfer.rehydrate(id, GoatId.of(42L), SOURCE, TARGET, OwnershipTransferKind.INTERNAL_SALE, status,
                "OWNERSHIP_SALE:501", "sale-42", requested, accepted ? requested.plusSeconds(1) : null,
                status == OwnershipTransferStatus.COMPLETED ? requested.plusSeconds(1) : null,
                status == OwnershipTransferStatus.COMPLETED ? requested.plusSeconds(1) : null, cancelled, 99L,
                accepted ? 77L : null, status == OwnershipTransferStatus.COMPLETED ? 77L : null, saleId);
    }
    private AnimalSaleRecord sale(AnimalSaleCommand c) { return new AnimalSaleRecord(c.id() == null ? 501L : c.id(), c.farmId(), c.customerId(), new CustomerReference(c.customerId(), "Buyer", true), c.goatTechnicalId(), c.goatRegistrationNumber(), c.goatName(), c.saleDate(), c.amount(), c.dueDate(), c.paymentStatus(), c.paymentDate(), c.notes(), null, null, c.targetFarmId()); }
    private GoatResponseVO goat(long id, String rg) { GoatResponseVO goat = new GoatResponseVO(); goat.setTechnicalId(id); goat.setRegistrationNumber(rg); goat.setName("Goat"); return goat; }
    private FarmRecord farm(long id) { return new FarmRecord(id, "Farm", null, null, null, null, List.of(), null, null, null); }
    private LocalDate date() { return LocalDate.of(2026, 9, 18); }
    private BigDecimal amount() { return new BigDecimal("100.00"); }
}
