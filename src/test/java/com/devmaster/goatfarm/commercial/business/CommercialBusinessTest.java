package com.devmaster.goatfarm.commercial.business;

import com.devmaster.goatfarm.application.core.business.common.EntityFinder;
import com.devmaster.goatfarm.audit.application.ports.in.OperationalAuditUseCase;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.commercial.application.model.*;
import com.devmaster.goatfarm.commercial.application.ports.out.AnimalSalePersistencePort;
import com.devmaster.goatfarm.commercial.application.ports.out.CustomerPersistencePort;
import com.devmaster.goatfarm.commercial.application.ports.out.MilkSalePersistencePort;
import com.devmaster.goatfarm.commercial.business.bo.*;
import com.devmaster.goatfarm.commercial.enums.SalePaymentStatus;
import com.devmaster.goatfarm.farm.application.model.FarmRecord;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.goat.application.ports.in.GoatManagementUseCase;
import com.devmaster.goatfarm.goat.business.bo.GoatExitResponseVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goatownership.application.ports.out.OwnershipTransferPersistencePort;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferKind;
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
import java.util.List;
import java.util.Optional;

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
    @Mock OwnershipTransferPersistencePort ownershipTransfers;
    private CommercialBusiness business;

    @BeforeEach void setUp() {
        business = new CommercialBusiness(customers, animalSales, milkSales, farms, goats, authorization, finder, audit,
                ownershipTransfers, Clock.system(ZoneId.of("America/Sao_Paulo")));
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
        when(animalSales.existsByFarmIdAndGoatTechnicalId(1L, 5L)).thenReturn(false); when(animalSales.save(any())).thenAnswer(i -> animalRecord((AnimalSaleCommand) i.getArgument(0)));
        AnimalSaleResponseVO result = business.createAnimalSale(1L, new AnimalSaleRequestVO("G1", 10L, LocalDate.now().minusDays(1), new BigDecimal("100"), LocalDate.now(), null, null));
        assertEquals(5L, result.goatTechnicalId()); verify(goats).exitGoat(eq(1L), eq("G1"), any()); verify(animalSales).save(any(AnimalSaleCommand.class));
    }

    @Test void createsMilkSaleAndCalculatesTotal() {
        when(customers.findCustomerByIdAndFarmId(10L, 1L)).thenReturn(Optional.of(customer(10L)));
        when(milkSales.save(any())).thenAnswer(i -> milkRecord((MilkSaleCommand) i.getArgument(0)));
        MilkSaleResponseVO result = business.createMilkSale(1L, new MilkSaleRequestVO(10L, LocalDate.now().minusDays(1), new BigDecimal("2.5"), new BigDecimal("4"), LocalDate.now(), null, null));
        assertEquals(new BigDecimal("10.00"), result.totalAmount()); verify(milkSales).save(any(MilkSaleCommand.class));
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
        when(animalSales.existsByFarmIdAndGoatTechnicalId(1L, 5L)).thenReturn(false);
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
        when(transfer.kind()).thenReturn(OwnershipTransferKind.INTERNAL_SALE);
        when(transfer.status()).thenReturn(OwnershipTransferStatus.REQUESTED);
        when(ownershipTransfers.findBySaleId(2L)).thenReturn(Optional.of(transfer));

        assertTrue(business.listReceivables(1L).isEmpty());
        assertEquals(0, business.getSummary(1L).animalSalesCount());
        assertEquals(BigDecimal.ZERO.setScale(2), business.getSummary(1L).animalSalesTotal());
    }

    private CustomerRecord customer(Long id) { return new CustomerRecord(id, 1L, "Cliente", null, null, null, null, true, null, null); }
    private FarmRecord farmRecord(Long id) { return new FarmRecord(id, "Fazenda", null, null, null, null, List.of(), null, null, null); }
    private GoatResponseVO goat(Long id, String rg, GoatStatus status) { GoatResponseVO g = new GoatResponseVO(); g.setTechnicalId(id); g.setRegistrationNumber(rg); g.setName("Cabra"); g.setStatus(status); g.setBirthDate(LocalDate.now().minusYears(2)); return g; }
    private AnimalSaleRecord animalRecord(AnimalSaleCommand c) { return new AnimalSaleRecord(c.id() == null ? 1L : c.id(), c.farmId(), c.customerId(), new CustomerReference(c.customerId(), "Cliente", true), c.goatTechnicalId(), c.goatRegistrationNumber(), c.goatName(), c.saleDate(), c.amount(), c.dueDate(), c.paymentStatus(), c.paymentDate(), c.notes(), null, null, c.targetFarmId()); }
    private MilkSaleRecord milkRecord(MilkSaleCommand c) { return new MilkSaleRecord(1L, c.farmId(), c.customerId(), new CustomerReference(c.customerId(), "Cliente", true), c.saleDate(), c.quantityLiters(), c.unitPrice(), c.totalAmount(), c.dueDate(), c.paymentStatus(), c.paymentDate(), c.notes(), null, null); }
}
