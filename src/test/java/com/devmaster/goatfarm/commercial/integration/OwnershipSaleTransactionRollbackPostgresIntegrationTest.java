package com.devmaster.goatfarm.commercial.integration;

import com.devmaster.goatfarm.authority.application.ports.in.CurrentPrincipalQueryUseCase;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.authority.persistence.repository.UserRepository;
import com.devmaster.goatfarm.commercial.api.dto.SalePaymentRequestDTO;
import com.devmaster.goatfarm.commercial.application.ports.in.OwnershipSaleUseCase;
import com.devmaster.goatfarm.commercial.business.bo.OwnershipSaleRequestVO;
import com.devmaster.goatfarm.commercial.persistence.entity.Customer;
import com.devmaster.goatfarm.commercial.persistence.repository.AnimalSaleRepository;
import com.devmaster.goatfarm.commercial.persistence.repository.CustomerRepository;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.farm.persistence.repository.GoatFarmRepository;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goat.persistence.entity.GoatEntity;
import com.devmaster.goatfarm.goat.persistence.repository.GoatRepository;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatCurrentOwnerProjectionPort;
import com.devmaster.goatfarm.goatownership.persistence.entity.GoatOwnershipPeriodEntity;
import com.devmaster.goatfarm.goatownership.persistence.entity.OwnershipTransferEntity;
import com.devmaster.goatfarm.goatownership.persistence.repository.GoatOwnershipPeriodRepository;
import com.devmaster.goatfarm.goatownership.persistence.repository.GoatCurrentOwnerProjectionRepository;
import com.devmaster.goatfarm.goatownership.persistence.repository.OwnershipTransferRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Proves ownership sale writes roll back together after the projection write point. */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@Import(OwnershipSaleTransactionRollbackPostgresIntegrationTest.FailureConfiguration.class)
class OwnershipSaleTransactionRollbackPostgresIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            System.getProperty("caprigestor.test.postgres.image", "postgres:16-alpine"));

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired private OwnershipSaleUseCase ownershipSales;
    @Autowired private UserRepository users;
    @Autowired private GoatFarmRepository farms;
    @Autowired private GoatRepository goats;
    @Autowired private CustomerRepository customers;
    @Autowired private AnimalSaleRepository animalSales;
    @Autowired private OwnershipTransferRepository transfers;
    @Autowired private GoatOwnershipPeriodRepository periods;

    @Test
    void projectionFailureRollsBackSalePaymentPeriodsProjectionAndTransfer() {
        FailureConfiguration.PROJECTION_FAILURE.set(true);
        try {
        User seller = user("sale-rollback-seller");
        User buyer = user("sale-rollback-buyer");
        GoatFarm sourceFarm = farm("Sale rollback source", "SRS01", seller);
        GoatFarm targetFarm = farm("Sale rollback target", "SRT01", buyer);
        GoatEntity goat = new GoatEntity();
        goat.setRegistrationNumber("SRS010001");
        goat.setName("Rollback sale goat");
        goat.setGender(Gender.FEMEA);
        goat.setBirthDate(LocalDate.of(2024, 1, 1));
        goat.setStatus(GoatStatus.ATIVO);
        goat.setTod("SRS01");
        goat.setToe("0001");
        goat.setFarm(sourceFarm);
        goat.setUser(seller);
        goat = goats.saveAndFlush(goat);
        Customer customer = customers.saveAndFlush(Customer.builder().farm(sourceFarm).name("Rollback buyer").active(true).build());
        GoatOwnershipPeriodEntity sourcePeriod = new GoatOwnershipPeriodEntity();
        sourcePeriod.setGoatId(goat.getTechnicalId());
        sourcePeriod.setFarmId(sourceFarm.getId());
        sourcePeriod.setStartedAt(Instant.parse("2024-01-01T00:00:00Z"));
        sourcePeriod.setEntryType(com.devmaster.goatfarm.goatownership.domain.OwnershipEntryType.MANUAL_IMPORT);
        sourcePeriod.setSource("TEST_FIXTURE");
        periods.saveAndFlush(sourcePeriod);

        FailureConfiguration.PRINCIPAL_ID.set(seller.getId());
        var request = new OwnershipSaleRequestVO("technical-" + goat.getTechnicalId(), customer.getId(), targetFarm.getId(),
                LocalDate.of(2026, 9, 19), new BigDecimal("100.00"), LocalDate.of(2026, 9, 25), "rollback", "rollback-sale-1");
        var pending = ownershipSales.requestOwnershipSale(sourceFarm.getId(), request);
        assertThat(pending.ownershipTransferStatus()).isEqualTo(com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus.REQUESTED);

        assertThatThrownBy(() -> ownershipSales.acceptOwnershipSale(sourceFarm.getId(), pending.saleId(),
                new com.devmaster.goatfarm.commercial.business.bo.SalePaymentRequestVO(LocalDate.of(2026, 9, 19))))
                .isInstanceOf(RuntimeException.class);

        var sale = animalSales.findById(pending.saleId()).orElseThrow();
        assertThat(sale.getPaymentStatus()).isEqualTo(com.devmaster.goatfarm.commercial.enums.SalePaymentStatus.OPEN);
        OwnershipTransferEntity transfer = transfers.findBySaleId(pending.saleId()).orElseThrow();
        assertThat(transfer.getStatus()).isEqualTo(com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus.REQUESTED);
        assertThat(periods.findByGoatIdOrderByStartedAtAscIdAsc(goat.getTechnicalId())).singleElement()
                .satisfies(period -> assertThat(period.getEndedAt()).isNull());
        assertThat(goats.findById(goat.getTechnicalId()).orElseThrow().getFarm().getId()).isEqualTo(sourceFarm.getId());
        } finally {
            FailureConfiguration.PROJECTION_FAILURE.set(false);
        }
    }

    @Test
    void concurrentEquivalentRequestLeavesExactlyOneSaleAndTransfer() throws Exception {
        User seller = user("sale-concurrency-seller");
        User buyer = user("sale-concurrency-buyer");
        GoatFarm sourceFarm = farm("Sale concurrency source", "SCS01", seller);
        GoatFarm targetFarm = farm("Sale concurrency target", "SCT01", buyer);
        GoatEntity goat = new GoatEntity();
        goat.setRegistrationNumber("SCS010001");
        goat.setName("Concurrent sale goat");
        goat.setGender(Gender.FEMEA);
        goat.setBirthDate(LocalDate.of(2024, 1, 1));
        goat.setStatus(GoatStatus.ATIVO);
        goat.setTod("SCS01");
        goat.setToe("0001");
        goat.setFarm(sourceFarm);
        goat.setUser(seller);
        goat = goats.saveAndFlush(goat);
        Customer customer = customers.saveAndFlush(Customer.builder().farm(sourceFarm).name("Concurrent buyer").active(true).build());
        GoatOwnershipPeriodEntity sourcePeriod = new GoatOwnershipPeriodEntity();
        sourcePeriod.setGoatId(goat.getTechnicalId());
        sourcePeriod.setFarmId(sourceFarm.getId());
        sourcePeriod.setStartedAt(Instant.parse("2024-01-01T00:00:00Z"));
        sourcePeriod.setEntryType(com.devmaster.goatfarm.goatownership.domain.OwnershipEntryType.MANUAL_IMPORT);
        sourcePeriod.setSource("TEST_FIXTURE");
        periods.saveAndFlush(sourcePeriod);
        FailureConfiguration.PRINCIPAL_ID.set(seller.getId());
        OwnershipSaleRequestVO request = new OwnershipSaleRequestVO("technical-" + goat.getTechnicalId(), customer.getId(),
                targetFarm.getId(), LocalDate.of(2026, 9, 19), new BigDecimal("100.00"), LocalDate.of(2026, 9, 25),
                "concurrent", "concurrent-sale-1");

        ExecutorService executor = Executors.newFixedThreadPool(2);
        Long technicalId = goat.getTechnicalId();
        try {
            List<Future<Long>> results = executor.invokeAll(List.of(
                    () -> ownershipSales.requestOwnershipSale(sourceFarm.getId(), request).saleId(),
                    () -> ownershipSales.requestOwnershipSale(sourceFarm.getId(), request).saleId()));
            assertThat(results.stream().map(this::getUnchecked).distinct()).hasSize(1);
        } finally {
            executor.shutdownNow();
        }

        assertThat(animalSales.findByFarm_IdOrderBySaleDateDescIdDesc(sourceFarm.getId()).stream()
                .filter(sale -> technicalId.equals(sale.getGoatTechnicalId())).toList()).hasSize(1);
        assertThat(transfers.findByRequestedByAndIdempotencyKey(seller.getId(), "concurrent-sale-1")).isPresent();
    }

    @Test
    void concurrentPaymentAndAcceptanceNeverLeavesPaidTerminalTransfer() throws Exception {
        SaleFixture fixture = fixture("payment-accept");
        var pending = request(fixture, "payment-accept-key");
        CyclicBarrier start = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            List<Future<Throwable>> results = executor.invokeAll(List.of(
                    () -> attempt(start, () -> ownershipSales.registerOwnershipSalePayment(fixture.source().getId(), pending.saleId(),
                            new com.devmaster.goatfarm.commercial.business.bo.SalePaymentRequestVO(LocalDate.of(2026, 9, 19)))),
                    () -> attempt(start, () -> ownershipSales.acceptOwnershipSale(fixture.source().getId(), pending.saleId()))));
            results.forEach(this::getUnchecked);
        } finally {
            executor.shutdownNow();
        }
        assertPaidCompletedTransfer(pending.saleId());
    }

    @Test
    void paymentAndAcceptanceCompleteInEitherDeterministicOrder() {
        SaleFixture acceptanceFirst = fixture("accept-first");
        var first = request(acceptanceFirst, "accept-first-key");
        FailureConfiguration.PRINCIPAL_ID.set(acceptanceFirst.target().getUser().getId());
        ownershipSales.acceptOwnershipSale(acceptanceFirst.source().getId(), first.saleId());
        ownershipSales.registerOwnershipSalePayment(acceptanceFirst.source().getId(), first.saleId(),
                new com.devmaster.goatfarm.commercial.business.bo.SalePaymentRequestVO(LocalDate.of(2026, 9, 19)));
        assertPaidCompletedTransfer(first.saleId());

        SaleFixture paymentFirst = fixture("payment-first");
        var second = request(paymentFirst, "payment-first-key");
        FailureConfiguration.PRINCIPAL_ID.set(paymentFirst.target().getUser().getId());
        ownershipSales.registerOwnershipSalePayment(paymentFirst.source().getId(), second.saleId(),
                new com.devmaster.goatfarm.commercial.business.bo.SalePaymentRequestVO(LocalDate.of(2026, 9, 19)));
        ownershipSales.acceptOwnershipSale(paymentFirst.source().getId(), second.saleId());
        assertPaidCompletedTransfer(second.saleId());
    }

    @Test
    void rejectionOrCancellationBeforePaymentRemainsTerminalAndRejectsPayment() {
        SaleFixture rejected = fixture("reject-first");
        var rejectedSale = request(rejected, "reject-first-key");
        FailureConfiguration.PRINCIPAL_ID.set(rejected.target().getUser().getId());
        ownershipSales.rejectOwnershipSale(rejected.source().getId(), rejectedSale.saleId());
        assertThatThrownBy(() -> ownershipSales.registerOwnershipSalePayment(rejected.source().getId(), rejectedSale.saleId(),
                new com.devmaster.goatfarm.commercial.business.bo.SalePaymentRequestVO(LocalDate.of(2026, 9, 19))))
                .isInstanceOf(RuntimeException.class);
        assertOpenTerminalTransfer(rejectedSale.saleId(), com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus.REJECTED);

        SaleFixture cancelled = fixture("cancel-first");
        var cancelledSale = request(cancelled, "cancel-first-key");
        FailureConfiguration.PRINCIPAL_ID.set(cancelled.source().getUser().getId());
        ownershipSales.cancelOwnershipSale(cancelled.source().getId(), cancelledSale.saleId());
        FailureConfiguration.PRINCIPAL_ID.set(cancelled.target().getUser().getId());
        assertThatThrownBy(() -> ownershipSales.registerOwnershipSalePayment(cancelled.source().getId(), cancelledSale.saleId(),
                new com.devmaster.goatfarm.commercial.business.bo.SalePaymentRequestVO(LocalDate.of(2026, 9, 19))))
                .isInstanceOf(RuntimeException.class);
        assertOpenTerminalTransfer(cancelledSale.saleId(), com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus.CANCELLED);
    }

    @Test
    void paymentFirstKeepsSalePaidAndRejectCancelFailClosed() {
        SaleFixture fixture = fixture("paid-terminal");
        var pending = request(fixture, "paid-terminal-key");
        FailureConfiguration.PRINCIPAL_ID.set(fixture.target().getUser().getId());
        ownershipSales.registerOwnershipSalePayment(fixture.source().getId(), pending.saleId(),
                new com.devmaster.goatfarm.commercial.business.bo.SalePaymentRequestVO(LocalDate.of(2026, 9, 19)));
        assertThatThrownBy(() -> ownershipSales.rejectOwnershipSale(fixture.source().getId(), pending.saleId()))
                .isInstanceOf(RuntimeException.class);
        FailureConfiguration.PRINCIPAL_ID.set(fixture.source().getUser().getId());
        assertThatThrownBy(() -> ownershipSales.cancelOwnershipSale(fixture.source().getId(), pending.saleId()))
                .isInstanceOf(RuntimeException.class);
        var sale = animalSales.findById(pending.saleId()).orElseThrow();
        var transfer = transfers.findBySaleId(pending.saleId()).orElseThrow();
        assertThat(sale.getPaymentStatus()).isEqualTo(com.devmaster.goatfarm.commercial.enums.SalePaymentStatus.PAID);
        assertThat(transfer.getStatus()).isEqualTo(com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus.REQUESTED);
    }

    @Test
    void concurrentPaymentAndRejectionNeverLeavesPaidRejectedTransfer() throws Exception {
        SaleFixture fixture = fixture("payment-reject");
        var pending = request(fixture, "payment-reject-key");
        runConcurrentPaymentAndTerminalAction(fixture, pending.saleId(), false);
        assertNoPaidTerminalTransfer(pending.saleId());
    }

    @Test
    void concurrentPaymentAndCancellationNeverLeavesPaidCancelledTransfer() throws Exception {
        SaleFixture fixture = fixture("payment-cancel");
        var pending = request(fixture, "payment-cancel-key");
        runConcurrentPaymentAndTerminalAction(fixture, pending.saleId(), true);
        assertNoPaidTerminalTransfer(pending.saleId());
    }

    private void runConcurrentPaymentAndTerminalAction(SaleFixture fixture, Long saleId, boolean cancel) throws Exception {
        CyclicBarrier start = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            List<Future<Throwable>> results = executor.invokeAll(List.of(
                    () -> attempt(start, () -> ownershipSales.registerOwnershipSalePayment(fixture.source().getId(), saleId,
                            new com.devmaster.goatfarm.commercial.business.bo.SalePaymentRequestVO(LocalDate.of(2026, 9, 19)))),
                    () -> attempt(start, () -> {
                        if (cancel) {
                            ownershipSales.cancelOwnershipSale(fixture.source().getId(), saleId);
                        } else {
                            ownershipSales.rejectOwnershipSale(fixture.source().getId(), saleId);
                        }
                    })));
            results.forEach(this::getUnchecked);
        } finally {
            executor.shutdownNow();
        }
    }

    private Throwable attempt(CyclicBarrier start, Runnable action) {
        try {
            start.await();
            action.run();
            return null;
        } catch (Throwable exception) {
            return exception;
        }
    }

    private void assertNoPaidTerminalTransfer(Long saleId) {
        var sale = animalSales.findById(saleId).orElseThrow();
        var transfer = transfers.findBySaleId(saleId).orElseThrow();
        assertThat(sale.getPaymentStatus() == com.devmaster.goatfarm.commercial.enums.SalePaymentStatus.PAID
                && (transfer.getStatus() == com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus.REJECTED
                || transfer.getStatus() == com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus.CANCELLED))
                .as("payment and rejection/cancellation must serialize").isFalse();
    }

    private void assertPaidCompletedTransfer(Long saleId) {
        var sale = animalSales.findById(saleId).orElseThrow();
        var transfer = transfers.findBySaleId(saleId).orElseThrow();
        assertThat(sale.getPaymentStatus()).isEqualTo(com.devmaster.goatfarm.commercial.enums.SalePaymentStatus.PAID);
        assertThat(transfer.getStatus()).isEqualTo(com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus.COMPLETED);
    }

    private void assertOpenTerminalTransfer(Long saleId,
                                            com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus status) {
        var sale = animalSales.findById(saleId).orElseThrow();
        var transfer = transfers.findBySaleId(saleId).orElseThrow();
        assertThat(sale.getPaymentStatus()).isEqualTo(com.devmaster.goatfarm.commercial.enums.SalePaymentStatus.OPEN);
        assertThat(transfer.getStatus()).isEqualTo(status);
    }

    private SaleFixture fixture(String suffix) {
        User seller = user("sale-" + suffix + "-seller");
        User buyer = user("sale-" + suffix + "-buyer");
        String code = Integer.toHexString(Math.abs(suffix.hashCode())).toUpperCase().substring(0, 2);
        GoatFarm sourceFarm = farm("Sale " + suffix + " source", ("S" + code + "01"), seller);
        GoatFarm targetFarm = farm("Sale " + suffix + " target", ("T" + code + "01"), buyer);
        GoatEntity goat = new GoatEntity();
        goat.setRegistrationNumber("S" + code + "0001");
        goat.setName("Concurrent " + suffix + " goat");
        goat.setGender(Gender.FEMEA);
        goat.setBirthDate(LocalDate.of(2024, 1, 1));
        goat.setStatus(GoatStatus.ATIVO);
        goat.setTod("S" + code);
        goat.setToe("0001");
        goat.setFarm(sourceFarm);
        goat.setUser(seller);
        goat = goats.saveAndFlush(goat);
        Customer customer = customers.saveAndFlush(Customer.builder().farm(sourceFarm).name("Buyer " + suffix).active(true).build());
        GoatOwnershipPeriodEntity sourcePeriod = new GoatOwnershipPeriodEntity();
        sourcePeriod.setGoatId(goat.getTechnicalId());
        sourcePeriod.setFarmId(sourceFarm.getId());
        sourcePeriod.setStartedAt(Instant.parse("2024-01-01T00:00:00Z"));
        sourcePeriod.setEntryType(com.devmaster.goatfarm.goatownership.domain.OwnershipEntryType.MANUAL_IMPORT);
        sourcePeriod.setSource("TEST_FIXTURE");
        periods.saveAndFlush(sourcePeriod);
        FailureConfiguration.PRINCIPAL_ID.set(seller.getId());
        return new SaleFixture(sourceFarm, targetFarm, customer, goat.getTechnicalId());
    }

    private com.devmaster.goatfarm.commercial.business.bo.OwnershipSaleResponseVO request(SaleFixture fixture, String key) {
        return ownershipSales.requestOwnershipSale(fixture.source().getId(), new OwnershipSaleRequestVO(
                "technical-" + fixture.goatId(), fixture.customer().getId(), fixture.target().getId(),
                LocalDate.of(2026, 9, 19), new BigDecimal("100.00"), LocalDate.of(2026, 9, 25), "concurrent", key));
    }

    private record SaleFixture(GoatFarm source, GoatFarm target, Customer customer, Long goatId) { }

    private <T> T getUnchecked(Future<T> future) {
        try {
            return future.get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError(exception);
        } catch (ExecutionException exception) {
            throw new AssertionError(exception.getCause());
        }
    }

    private User user(String suffix) {
        User user = new User();
        user.setName(suffix);
        user.setEmail(suffix + "@example.com");
        user.setCpf(String.format("%011d", Math.abs((long) suffix.hashCode())));
        user.setPassword("password");
        return users.saveAndFlush(user);
    }

    private GoatFarm farm(String name, String tod, User user) {
        GoatFarm farm = new GoatFarm();
        farm.setName(name);
        farm.setTod(tod);
        farm.setUser(user);
        return farms.saveAndFlush(farm);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class FailureConfiguration {
        static final AtomicLong PRINCIPAL_ID = new AtomicLong(1L);
        static final java.util.concurrent.atomic.AtomicBoolean PROJECTION_FAILURE = new java.util.concurrent.atomic.AtomicBoolean();

        @Bean
        @Primary
        FarmAuthorizationUseCase authorization() {
            return new FarmAuthorizationUseCase() {
                public void verifyFarmOwnership(Long farmId) { }
                public void verifyFarmManagement(Long farmId) { }
                public boolean isFarmOwner(Long farmId) { return true; }
                public boolean canAdministerFarm(Long farmId) { return true; }
                public boolean canManageFarm(Long farmId) { return true; }
            };
        }

        @Bean
        @Primary
        CurrentPrincipalQueryUseCase currentPrincipal() {
            return new CurrentPrincipalQueryUseCase() {
                public java.util.Optional<AuthenticatedPrincipal> findCurrent() { return java.util.Optional.of(requireCurrent()); }
                public AuthenticatedPrincipal requireCurrent() {
                    return new AuthenticatedPrincipal(PRINCIPAL_ID.get(), "rollback@example.com", "Rollback", Set.of("ROLE_FARM_OWNER"));
                }
            };
        }

        @Bean
        @Primary
        GoatCurrentOwnerProjectionPort failingProjection(GoatCurrentOwnerProjectionRepository repository) {
            return (goatId, expectedSourceFarmId, targetFarmId) -> {
                int updated = repository.moveFromTo(goatId.value(), expectedSourceFarmId, targetFarmId);
                if (updated != 1) {
                    return false;
                }
                if (PROJECTION_FAILURE.get()) {
                    throw new IllegalStateException("projection failure after write");
                }
                return true;
            };
        }
    }
}
