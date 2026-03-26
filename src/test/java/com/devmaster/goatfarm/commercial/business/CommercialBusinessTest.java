package com.devmaster.goatfarm.commercial.business;

import com.devmaster.goatfarm.application.core.business.common.EntityFinder;
import com.devmaster.goatfarm.commercial.application.ports.out.CommercialPersistencePort;
import com.devmaster.goatfarm.commercial.business.bo.AnimalSaleRequestVO;
import com.devmaster.goatfarm.commercial.business.bo.AnimalSaleResponseVO;
import com.devmaster.goatfarm.commercial.business.bo.CommercialSummaryVO;
import com.devmaster.goatfarm.commercial.business.bo.MilkSaleRequestVO;
import com.devmaster.goatfarm.commercial.business.bo.SalePaymentRequestVO;
import com.devmaster.goatfarm.commercial.enums.SalePaymentStatus;
import com.devmaster.goatfarm.commercial.persistence.entity.AnimalSale;
import com.devmaster.goatfarm.commercial.persistence.entity.Customer;
import com.devmaster.goatfarm.commercial.persistence.entity.MilkSale;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.goat.application.ports.in.GoatManagementUseCase;
import com.devmaster.goatfarm.goat.business.bo.GoatExitResponseVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.enums.GoatExitType;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommercialBusinessTest {

    private CommercialBusiness commercialBusiness;

    @Mock
    private CommercialPersistencePort commercialPersistencePort;
    @Mock
    private GoatFarmPersistencePort goatFarmPersistencePort;
    @Mock
    private GoatManagementUseCase goatManagementUseCase;
    @Mock
    private OwnershipService ownershipService;
    @Mock
    private EntityFinder entityFinder;

    @BeforeEach
    void setUp() {
        commercialBusiness = new CommercialBusiness(
                commercialPersistencePort,
                goatFarmPersistencePort,
                goatManagementUseCase,
                ownershipService,
                entityFinder
        );

        lenient().when(entityFinder.findOrThrow(any(), anyString())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Supplier<Optional<Object>> supplier = invocation.getArgument(0);
            String message = invocation.getArgument(1);
            return supplier.get().orElseThrow(() -> new com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException(message));
        });
    }

    @Test
    void shouldCreateAnimalSaleAndTriggerGoatExitWhenGoatIsActive() {
        Long farmId = 1L;
        GoatFarm farm = farm(farmId);
        Customer customer = activeCustomer(10L, farm);
        GoatResponseVO goat = goat("G001", "Cabra Teste", GoatStatus.ATIVO, null, null);

        AnimalSaleRequestVO requestVO = new AnimalSaleRequestVO(
                "G001",
                10L,
                LocalDate.now().minusDays(2),
                new BigDecimal("1800"),
                LocalDate.now().plusDays(5),
                null,
                "Venda de teste"
        );

        when(goatFarmPersistencePort.findById(farmId)).thenReturn(Optional.of(farm));
        when(commercialPersistencePort.findCustomerByIdAndFarmId(10L, farmId)).thenReturn(Optional.of(customer));
        when(goatManagementUseCase.findGoatById(farmId, "G001")).thenReturn(goat);
        when(goatManagementUseCase.exitGoat(anyLong(), anyString(), any())).thenReturn(new GoatExitResponseVO());
        when(commercialPersistencePort.existsAnimalSaleByGoatRegistrationNumber("G001")).thenReturn(false);
        when(commercialPersistencePort.saveAnimalSale(any(AnimalSale.class))).thenAnswer(invocation -> {
            AnimalSale entity = invocation.getArgument(0);
            entity.setId(77L);
            return entity;
        });

        AnimalSaleResponseVO response = commercialBusiness.createAnimalSale(farmId, requestVO);

        assertEquals(77L, response.id());
        assertEquals("G001", response.goatRegistrationNumber());
        assertEquals(SalePaymentStatus.OPEN, response.paymentStatus());
        assertNull(response.paymentDate());

        ArgumentCaptor<AnimalSale> saleCaptor = ArgumentCaptor.forClass(AnimalSale.class);
        verify(commercialPersistencePort).saveAnimalSale(saleCaptor.capture());
        verify(goatManagementUseCase).exitGoat(anyLong(), anyString(), any());
        assertEquals("Cabra Teste", saleCaptor.getValue().getGoatName());
        assertEquals(new BigDecimal("1800.00"), saleCaptor.getValue().getAmount());
    }

    @Test
    void shouldAllowAnimalSaleForAlreadySoldGoatWhenExitIsCommercialAndCoherent() {
        Long farmId = 1L;
        GoatFarm farm = farm(farmId);
        Customer customer = activeCustomer(10L, farm);
        LocalDate saleDate = LocalDate.now().minusDays(3);
        GoatResponseVO goat = goat("G002", "Cabra Vendida", GoatStatus.VENDIDO, GoatExitType.VENDA, saleDate);

        when(goatFarmPersistencePort.findById(farmId)).thenReturn(Optional.of(farm));
        when(commercialPersistencePort.findCustomerByIdAndFarmId(10L, farmId)).thenReturn(Optional.of(customer));
        when(goatManagementUseCase.findGoatById(farmId, "G002")).thenReturn(goat);
        when(commercialPersistencePort.existsAnimalSaleByGoatRegistrationNumber("G002")).thenReturn(false);
        when(commercialPersistencePort.saveAnimalSale(any(AnimalSale.class))).thenAnswer(invocation -> {
            AnimalSale entity = invocation.getArgument(0);
            entity.setId(78L);
            return entity;
        });

        AnimalSaleResponseVO response = commercialBusiness.createAnimalSale(
                farmId,
                new AnimalSaleRequestVO("G002", 10L, saleDate, new BigDecimal("950"), saleDate.plusDays(2), null, null)
        );

        assertEquals(78L, response.id());
        verify(goatManagementUseCase, never()).exitGoat(anyLong(), anyString(), any());
    }

    @Test
    void shouldRejectAnimalSaleWhenGoatExitIsContradictory() {
        Long farmId = 1L;
        GoatFarm farm = farm(farmId);
        Customer customer = activeCustomer(10L, farm);
        LocalDate saleDate = LocalDate.now().minusDays(2);
        GoatResponseVO goat = goat("G003", "Cabra Transferida", GoatStatus.VENDIDO, GoatExitType.TRANSFERENCIA, saleDate);

        when(goatFarmPersistencePort.findById(farmId)).thenReturn(Optional.of(farm));
        when(commercialPersistencePort.findCustomerByIdAndFarmId(10L, farmId)).thenReturn(Optional.of(customer));
        when(goatManagementUseCase.findGoatById(farmId, "G003")).thenReturn(goat);

        assertThrows(
                BusinessRuleException.class,
                () -> commercialBusiness.createAnimalSale(
                        farmId,
                        new AnimalSaleRequestVO("G003", 10L, saleDate, new BigDecimal("800"), saleDate.plusDays(3), null, null)
                )
        );

        verify(commercialPersistencePort, never()).saveAnimalSale(any());
    }

    @Test
    void shouldCreateMilkSaleAndCalculateTotal() {
        Long farmId = 1L;
        GoatFarm farm = farm(farmId);
        Customer customer = activeCustomer(22L, farm);
        LocalDate saleDate = LocalDate.now().minusDays(1);

        when(goatFarmPersistencePort.findById(farmId)).thenReturn(Optional.of(farm));
        when(commercialPersistencePort.findCustomerByIdAndFarmId(22L, farmId)).thenReturn(Optional.of(customer));
        when(commercialPersistencePort.saveMilkSale(any(MilkSale.class))).thenAnswer(invocation -> {
            MilkSale entity = invocation.getArgument(0);
            entity.setId(99L);
            return entity;
        });

        var response = commercialBusiness.createMilkSale(
                farmId,
                new MilkSaleRequestVO(22L, saleDate, new BigDecimal("32.5"), new BigDecimal("4.80"), saleDate.plusDays(7), saleDate, "Leite fresco")
        );

        assertEquals(99L, response.id());
        assertEquals(new BigDecimal("32.50"), response.quantityLiters());
        assertEquals(new BigDecimal("4.80"), response.unitPrice());
        assertEquals(new BigDecimal("156.00"), response.totalAmount());
        assertEquals(SalePaymentStatus.PAID, response.paymentStatus());
    }

    @Test
    void shouldRegisterAnimalSalePayment() {
        Long farmId = 1L;
        GoatFarm farm = farm(farmId);
        Customer customer = activeCustomer(33L, farm);
        AnimalSale sale = AnimalSale.builder()
                .id(12L)
                .farm(farm)
                .customer(customer)
                .goatRegistrationNumber("G010")
                .goatName("Cabra Recebivel")
                .saleDate(LocalDate.now().minusDays(5))
                .amount(new BigDecimal("1200.00"))
                .dueDate(LocalDate.now().plusDays(5))
                .paymentStatus(SalePaymentStatus.OPEN)
                .build();

        when(goatFarmPersistencePort.findById(farmId)).thenReturn(Optional.of(farm));
        when(commercialPersistencePort.findAnimalSaleByIdAndFarmId(12L, farmId)).thenReturn(Optional.of(sale));
        when(commercialPersistencePort.saveAnimalSale(any(AnimalSale.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = commercialBusiness.registerAnimalSalePayment(
                farmId,
                12L,
                new SalePaymentRequestVO(LocalDate.now().minusDays(1))
        );

        assertEquals(SalePaymentStatus.PAID, response.paymentStatus());
        assertEquals(LocalDate.now().minusDays(1), response.paymentDate());
    }

    @Test
    void shouldBuildCommercialSummaryAndReceivablesFromSales() {
        Long farmId = 1L;
        GoatFarm farm = farm(farmId);
        Customer customer = activeCustomer(44L, farm);

        AnimalSale animalSale = AnimalSale.builder()
                .id(1L)
                .farm(farm)
                .customer(customer)
                .goatRegistrationNumber("G100")
                .goatName("Cabra 100")
                .saleDate(LocalDate.now().minusDays(10))
                .amount(new BigDecimal("1500.00"))
                .dueDate(LocalDate.now().plusDays(2))
                .paymentStatus(SalePaymentStatus.OPEN)
                .build();

        MilkSale milkSale = MilkSale.builder()
                .id(2L)
                .farm(farm)
                .customer(customer)
                .saleDate(LocalDate.now().minusDays(4))
                .quantityLiters(new BigDecimal("20.00"))
                .unitPrice(new BigDecimal("5.00"))
                .totalAmount(new BigDecimal("100.00"))
                .dueDate(LocalDate.now().minusDays(1))
                .paymentStatus(SalePaymentStatus.PAID)
                .paymentDate(LocalDate.now().minusDays(1))
                .build();

        when(goatFarmPersistencePort.findById(farmId)).thenReturn(Optional.of(farm));
        when(commercialPersistencePort.countCustomersByFarmId(farmId)).thenReturn(1L);
        when(commercialPersistencePort.findAnimalSalesByFarmId(farmId)).thenReturn(List.of(animalSale));
        when(commercialPersistencePort.findMilkSalesByFarmId(farmId)).thenReturn(List.of(milkSale));

        CommercialSummaryVO summary = commercialBusiness.getSummary(farmId);
        List<?> receivables = commercialBusiness.listReceivables(farmId);

        assertEquals(1L, summary.customerCount());
        assertEquals(1L, summary.animalSalesCount());
        assertEquals(new BigDecimal("1500.00"), summary.animalSalesTotal());
        assertEquals(1L, summary.milkSalesCount());
        assertEquals(new BigDecimal("20.00"), summary.milkSalesQuantityLiters());
        assertEquals(new BigDecimal("100.00"), summary.milkSalesTotal());
        assertEquals(1L, summary.openReceivablesCount());
        assertEquals(new BigDecimal("1500.00"), summary.openReceivablesTotal());
        assertEquals(1L, summary.paidReceivablesCount());
        assertEquals(new BigDecimal("100.00"), summary.paidReceivablesTotal());
        assertEquals(2, receivables.size());
    }

    @Test
    void shouldRejectPaymentDateInFuture() {
        Long farmId = 1L;
        GoatFarm farm = farm(farmId);
        Customer customer = activeCustomer(22L, farm);

        when(goatFarmPersistencePort.findById(farmId)).thenReturn(Optional.of(farm));
        when(commercialPersistencePort.findCustomerByIdAndFarmId(22L, farmId)).thenReturn(Optional.of(customer));

        assertThrows(
                InvalidArgumentException.class,
                () -> commercialBusiness.createMilkSale(
                        farmId,
                        new MilkSaleRequestVO(
                                22L,
                                LocalDate.now().minusDays(1),
                                new BigDecimal("10"),
                                new BigDecimal("4"),
                                LocalDate.now().plusDays(1),
                                LocalDate.now().plusDays(1),
                                null
                        )
                )
        );
    }

    private GoatFarm farm(Long id) {
        GoatFarm farm = new GoatFarm();
        farm.setId(id);
        farm.setName("Fazenda QA");
        return farm;
    }

    private Customer activeCustomer(Long id, GoatFarm farm) {
        return Customer.builder()
                .id(id)
                .farm(farm)
                .name("Cliente QA")
                .active(true)
                .build();
    }

    private GoatResponseVO goat(String registrationNumber, String name, GoatStatus status, GoatExitType exitType, LocalDate exitDate) {
        GoatResponseVO goat = new GoatResponseVO();
        goat.setRegistrationNumber(registrationNumber);
        goat.setName(name);
        goat.setStatus(status);
        goat.setExitType(exitType);
        goat.setExitDate(exitDate);
        goat.setBirthDate(LocalDate.now().minusYears(2));
        return goat;
    }
}
