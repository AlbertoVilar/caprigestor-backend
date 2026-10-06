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
import com.devmaster.goatfarm.commercial.persistence.entity.AnimalSale;
import com.devmaster.goatfarm.commercial.persistence.entity.AnimalSaleReversal;
import com.devmaster.goatfarm.commercial.enums.SalePaymentStatus;
import com.devmaster.goatfarm.commercial.persistence.repository.AnimalSaleRepository;
import com.devmaster.goatfarm.commercial.persistence.repository.AnimalSaleReversalRepository;
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
import com.devmaster.goatfarm.goat.application.ports.out.HistoricalAnimalSaleQueryPort;
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
import java.time.LocalDateTime;
import java.util.Set;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Proves ownership sale writes roll back together after the projection write point. */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@Import(OwnershipSaleTransactionRollbackPostgresIntegrationTest.FailureConfiguration.class)
class OwnershipSaleTransactionRollbackPostgresIntegrationTest {
    private static final AtomicInteger FIXTURE_SEQUENCE = new AtomicInteger();

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
    @Autowired private AnimalSaleReversalRepository reversals;
    @Autowired private OwnershipTransferRepository transfers;
    @Autowired private GoatOwnershipPeriodRepository periods;
    @Autowired private HistoricalAnimalSaleQueryPort historicalAnimalSales;

    @Test
    void historicalSoldCountUsesDistinctCompletedNonReversedSalesBySeller() {
        SaleFixture fixture = fixture("history-query-" + System.nanoTime());
        User seller = fixture.source().getUser();
        GoatEntity externalPaidGoat = goats.findById(fixture.goatId()).orElseThrow();
        GoatEntity externalOpenGoat = goat(fixture.source(), seller, registration(fixture.source(), 2), "External open");
        GoatEntity externalReversedGoat = goat(fixture.source(), seller, registration(fixture.source(), 3), "External reversed");
        GoatEntity internalCompletedGoat = goat(fixture.source(), seller, registration(fixture.source(), 4), "Internal completed");
        GoatEntity internalPendingGoat = goat(fixture.source(), seller, registration(fixture.source(), 5), "Internal pending");
        GoatEntity internalCancelledGoat = goat(fixture.source(), seller, registration(fixture.source(), 6), "Internal cancelled");
        GoatEntity resoldGoat = goat(fixture.source(), seller, registration(fixture.source(), 7), "Resold");
        GoatEntity secondInternalGoat = goat(fixture.source(), seller, registration(fixture.source(), 8), "Second internal");
        GoatEntity simpleTransferGoat = goat(fixture.source(), seller, registration(fixture.source(), 9), "Simple transfer");
        GoatEntity internalRejectedGoat = goat(fixture.source(), seller, registration(fixture.source(), 10), "Internal rejected");

        AnimalSale externalPaid = saveSale(fixture.source(), null, externalPaidGoat, SalePaymentStatus.PAID);
        saveSale(fixture.source(), null, externalOpenGoat, SalePaymentStatus.OPEN);
        AnimalSale externalReversed = saveSale(fixture.source(), null, externalReversedGoat, SalePaymentStatus.PAID);
        AnimalSaleReversal reversal = new AnimalSaleReversal();
        reversal.setSale(externalReversed);
        reversal.setReason("integration fixture reversal");
        reversal.setReversedAt(LocalDateTime.now());
        reversals.saveAndFlush(reversal);

        AnimalSale internalCompleted = saveSale(fixture.source(), fixture.target(), internalCompletedGoat, SalePaymentStatus.PAID);
        saveTransfer(internalCompleted, internalCompletedGoat, fixture, com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus.COMPLETED);
        AnimalSale internalPending = saveSale(fixture.source(), fixture.target(), internalPendingGoat, SalePaymentStatus.OPEN);
        saveTransfer(internalPending, internalPendingGoat, fixture, com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus.REQUESTED);
        AnimalSale internalCancelled = saveSale(fixture.source(), fixture.target(), internalCancelledGoat, SalePaymentStatus.OPEN);
        saveTransfer(internalCancelled, internalCancelledGoat, fixture, com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus.CANCELLED);
        AnimalSale internalRejected = saveSale(fixture.source(), fixture.target(), internalRejectedGoat, SalePaymentStatus.OPEN);
        saveTransfer(internalRejected, internalRejectedGoat, fixture, com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus.REJECTED);

        AnimalSale resoldFirst = saveSale(fixture.source(), fixture.target(), resoldGoat, SalePaymentStatus.PAID);
        saveTransfer(resoldFirst, resoldGoat, fixture, com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus.COMPLETED);
        AnimalSale resoldAgain = saveSale(fixture.source(), fixture.target(), resoldGoat, SalePaymentStatus.PAID);
        saveTransfer(resoldAgain, resoldGoat, fixture, com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus.COMPLETED);
        AnimalSale secondInternal = saveSale(fixture.source(), fixture.target(), secondInternalGoat, SalePaymentStatus.PAID);
        saveTransfer(secondInternal, secondInternalGoat, fixture, com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus.COMPLETED);
        saveTransfer(null, simpleTransferGoat, fixture, com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus.COMPLETED,
                com.devmaster.goatfarm.goatownership.domain.OwnershipTransferKind.INTERNAL_TRANSFER);

        internalCompletedGoat.setFarm(fixture.target());
        goats.saveAndFlush(internalCompletedGoat);

        assertThat(historicalAnimalSales.countDistinctSoldGoatsByFarmId(fixture.source().getId())).isEqualTo(4L);
        assertThat(historicalAnimalSales.countDistinctSoldGoatsByFarmId(fixture.target().getId())).isZero();
    }

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
        var request = new OwnershipSaleRequestVO("technical-" + goat.getTechnicalId(), targetFarm.getId(),
                LocalDate.of(2026, 9, 19), new BigDecimal("100.00"), LocalDate.of(2026, 9, 25), null, "rollback", "rollback-sale-1");
        var pending = ownershipSales.requestOwnershipSale(sourceFarm.getId(), request);
        assertThat(pending.ownershipTransferStatus()).isEqualTo(com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus.REQUESTED);

        assertThatThrownBy(() -> ownershipSales.registerOwnershipSalePayment(sourceFarm.getId(), pending.saleId(),
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
    void paidAtCreationProjectionFailureRollsBackSalePaymentAndOwnership() {
        SaleFixture fixture = fixture("paid-creation-rollback");
        FailureConfiguration.PROJECTION_FAILURE.set(true);
        try {
            assertThatThrownBy(() -> ownershipSales.requestOwnershipSale(fixture.source().getId(),
                    new OwnershipSaleRequestVO("technical-" + fixture.goatId(), fixture.target().getId(),
                            LocalDate.of(2026, 9, 19), new BigDecimal("100.00"), LocalDate.of(2026, 9, 25),
                            LocalDate.of(2026, 9, 19), "paid at creation rollback", "paid-creation-rollback")))
                    .isInstanceOf(RuntimeException.class);
        } finally {
            FailureConfiguration.PROJECTION_FAILURE.set(false);
        }

        assertThat(animalSales.findByFarm_IdOrderBySaleDateDescIdDesc(fixture.source().getId()).stream()
                .filter(sale -> fixture.goatId().equals(sale.getGoatTechnicalId())).toList()).isEmpty();
        assertThat(transfers.findByGoatIdAndStatusIn(fixture.goatId(),
                java.util.List.of(com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus.REQUESTED,
                        com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus.ACCEPTED,
                        com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus.COMPLETED))).isEmpty();
        assertThat(periods.findByGoatIdOrderByStartedAtAscIdAsc(fixture.goatId())).singleElement()
                .satisfies(period -> {
                    assertThat(period.getFarmId()).isEqualTo(fixture.source().getId());
                    assertThat(period.getEndedAt()).isNull();
                });
        assertThat(goats.findById(fixture.goatId()).orElseThrow().getFarm().getId()).isEqualTo(fixture.source().getId());
    }

    @Test
    void paidAnimalRevenueExcludesReversedSaleAtRepositoryBoundary() {
        User seller = user("reversal-revenue-seller");
        GoatFarm farm = farm("Reversal revenue farm", "RVR01", seller);
        Customer customer = customers.saveAndFlush(Customer.builder().farm(farm).name("Revenue customer").active(true).build());
        LocalDate paymentDate = LocalDate.of(2026, 9, 20);
        GoatEntity validGoat = goat(farm, seller, "RVR0001", "Valid sale");
        GoatEntity reversedGoat = goat(farm, seller, "RVR0002", "Reversed sale");

        AnimalSale valid = animalSale(farm, customer, validGoat.getTechnicalId(), "RVR0001", "Valid sale", new BigDecimal("5000.00"), paymentDate);
        AnimalSale reversed = animalSale(farm, customer, reversedGoat.getTechnicalId(), "RVR0002", "Reversed sale", new BigDecimal("5000.00"), paymentDate);
        valid = animalSales.saveAndFlush(valid);
        reversed = animalSales.saveAndFlush(reversed);

        AnimalSaleReversal reversal = new AnimalSaleReversal();
        reversal.setSale(reversed);
        reversal.setReason("Correction");
        reversal.setReversedAt(java.time.LocalDateTime.of(2026, 9, 21, 10, 0));
        reversal.setReversedBy(seller.getId());
        reversals.saveAndFlush(reversal);

        BigDecimal effectiveRevenue = animalSales.sumPaidAmountByFarmIdAndPaymentDateBetween(
                farm.getId(), SalePaymentStatus.PAID, paymentDate, paymentDate);

        assertThat(effectiveRevenue).isEqualByComparingTo("5000.00");
        assertThat(animalSales.findById(valid.getId())).isPresent();
        assertThat(animalSales.findById(reversed.getId())).isPresent();
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
        OwnershipSaleRequestVO request = new OwnershipSaleRequestVO("technical-" + goat.getTechnicalId(),
                targetFarm.getId(), LocalDate.of(2026, 9, 19), new BigDecimal("100.00"), LocalDate.of(2026, 9, 25),
                null, "concurrent", "concurrent-sale-1");

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
    void concurrentPaymentRetriesNeverDuplicateHandoff() throws Exception {
        SaleFixture fixture = fixture("payment-retry");
        var pending = request(fixture, "payment-retry-key");
        CyclicBarrier start = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            List<Future<Throwable>> results = executor.invokeAll(List.of(
                    () -> attempt(start, () -> ownershipSales.registerOwnershipSalePayment(fixture.source().getId(), pending.saleId(),
                            new com.devmaster.goatfarm.commercial.business.bo.SalePaymentRequestVO(LocalDate.of(2026, 9, 19)))),
                    () -> attempt(start, () -> ownershipSales.registerOwnershipSalePayment(fixture.source().getId(), pending.saleId(),
                            new com.devmaster.goatfarm.commercial.business.bo.SalePaymentRequestVO(LocalDate.of(2026, 9, 19))))));
            results.forEach(this::getUnchecked);
        } finally {
            executor.shutdownNow();
        }
        assertPaidCompletedTransfer(pending.saleId());
    }

    @Test
    void paymentCompletesWithoutTargetAcceptanceAndRetryIsIdempotent() {
        SaleFixture fixture = fixture("payment-first");
        var sale = request(fixture, "payment-first-key");
        FailureConfiguration.PRINCIPAL_ID.set(fixture.source().getUser().getId());
        ownershipSales.registerOwnershipSalePayment(fixture.source().getId(), sale.saleId(),
                new com.devmaster.goatfarm.commercial.business.bo.SalePaymentRequestVO(LocalDate.of(2026, 9, 19)));
        ownershipSales.registerOwnershipSalePayment(fixture.source().getId(), sale.saleId(),
                new com.devmaster.goatfarm.commercial.business.bo.SalePaymentRequestVO(LocalDate.of(2026, 9, 19)));
        assertPaidCompletedTransfer(sale.saleId());
    }

    @Test
    void cancellationBeforePaymentRemainsTerminalAndRejectsPayment() {
        SaleFixture cancelled = fixture("cancel-first");
        var cancelledSale = request(cancelled, "cancel-first-key");
        FailureConfiguration.PRINCIPAL_ID.set(cancelled.source().getUser().getId());
        ownershipSales.cancelOwnershipSale(cancelled.source().getId(), cancelledSale.saleId());
        FailureConfiguration.PRINCIPAL_ID.set(cancelled.source().getUser().getId());
        assertThatThrownBy(() -> ownershipSales.registerOwnershipSalePayment(cancelled.source().getId(), cancelledSale.saleId(),
                new com.devmaster.goatfarm.commercial.business.bo.SalePaymentRequestVO(LocalDate.of(2026, 9, 19))))
                .isInstanceOf(RuntimeException.class);
        assertOpenTerminalTransfer(cancelledSale.saleId(), com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus.CANCELLED);
    }

    @Test
    void paymentFirstKeepsSalePaidAndRejectCancelFailClosed() {
        SaleFixture fixture = fixture("paid-terminal");
        var pending = request(fixture, "paid-terminal-key");
        FailureConfiguration.PRINCIPAL_ID.set(fixture.source().getUser().getId());
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
        assertThat(transfer.getStatus()).isEqualTo(com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus.COMPLETED);
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
                    () -> attempt(start, () -> ownershipSales.cancelOwnershipSale(fixture.source().getId(), saleId))));
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
        int fixtureId = FIXTURE_SEQUENCE.incrementAndGet();
        String sourceTod = String.format("S%04d", fixtureId);
        String targetTod = String.format("T%04d", fixtureId);
        GoatFarm sourceFarm = farm("Sale " + suffix + " source", sourceTod, seller);
        GoatFarm targetFarm = farm("Sale " + suffix + " target", targetTod, buyer);
        GoatEntity goat = new GoatEntity();
        goat.setRegistrationNumber(sourceTod + "0001");
        goat.setName("Concurrent " + suffix + " goat");
        goat.setGender(Gender.FEMEA);
        goat.setBirthDate(LocalDate.of(2024, 1, 1));
        goat.setStatus(GoatStatus.ATIVO);
        goat.setTod(sourceTod);
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
                "technical-" + fixture.goatId(), fixture.target().getId(),
                LocalDate.of(2026, 9, 19), new BigDecimal("100.00"), LocalDate.of(2026, 9, 25), null, "concurrent", key));
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

    private GoatEntity goat(GoatFarm farm, User user, String registration, String name) {
        GoatEntity goat = new GoatEntity();
        goat.setRegistrationNumber(registration);
        goat.setName(name);
        goat.setGender(Gender.FEMEA);
        goat.setBirthDate(LocalDate.of(2024, 1, 1));
        goat.setStatus(GoatStatus.ATIVO);
        goat.setTod(farm.getTod());
        goat.setToe(registration.substring(Math.max(0, registration.length() - 4)));
        goat.setFarm(farm);
        goat.setUser(user);
        return goats.saveAndFlush(goat);
    }

    private AnimalSale animalSale(GoatFarm farm, Customer customer, Long goatTechnicalId, String registration, String name,
                                  BigDecimal amount, LocalDate paymentDate) {
        AnimalSale sale = new AnimalSale();
        sale.setFarm(farm);
        sale.setCustomer(customer);
        sale.setGoatTechnicalId(goatTechnicalId);
        sale.setGoatRegistrationNumber(registration);
        sale.setGoatName(name);
        sale.setSaleDate(paymentDate);
        sale.setAmount(amount);
        sale.setDueDate(paymentDate);
        sale.setPaymentStatus(SalePaymentStatus.PAID);
        sale.setPaymentDate(paymentDate);
        return sale;
    }

    private AnimalSale saveSale(GoatFarm seller, GoatFarm target, GoatEntity goat, SalePaymentStatus paymentStatus) {
        AnimalSale sale = new AnimalSale();
        sale.setFarm(seller);
        sale.setTargetFarm(target);
        sale.setGoatTechnicalId(goat.getTechnicalId());
        sale.setGoatRegistrationNumber(goat.getRegistrationNumber());
        sale.setGoatName(goat.getName());
        sale.setSaleDate(LocalDate.of(2026, 9, 19));
        sale.setAmount(new BigDecimal("100.00"));
        sale.setDueDate(LocalDate.of(2026, 9, 25));
        sale.setPaymentStatus(paymentStatus);
        sale.setPaymentDate(paymentStatus == SalePaymentStatus.PAID ? LocalDate.of(2026, 9, 19) : null);
        return animalSales.saveAndFlush(sale);
    }

    private void saveTransfer(AnimalSale sale, GoatEntity goat, SaleFixture fixture,
                              com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus status) {
        saveTransfer(sale, goat, fixture, status,
                com.devmaster.goatfarm.goatownership.domain.OwnershipTransferKind.INTERNAL_SALE);
    }

    private void saveTransfer(AnimalSale sale, GoatEntity goat, SaleFixture fixture,
                              com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus status,
                              com.devmaster.goatfarm.goatownership.domain.OwnershipTransferKind kind) {
        OwnershipTransferEntity transfer = new OwnershipTransferEntity();
        transfer.setGoatId(goat.getTechnicalId());
        transfer.setSourceFarmId(fixture.source().getId());
        transfer.setTargetFarmId(fixture.target().getId());
        transfer.setKind(kind);
        transfer.setStatus(status);
        transfer.setReason("historical sale query integration fixture");
        transfer.setIdempotencyKey(java.util.UUID.randomUUID().toString());
        transfer.setRequestedAt(Instant.now());
        transfer.setRequestedBy(fixture.source().getUser().getId());
        transfer.setSaleId(sale == null ? null : sale.getId());
        if (status == com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus.COMPLETED) {
            Instant completedAt = Instant.now();
            transfer.setEffectiveAt(completedAt);
            transfer.setCompletedAt(completedAt);
            transfer.setCompletedBy(fixture.source().getUser().getId());
            if (kind != com.devmaster.goatfarm.goatownership.domain.OwnershipTransferKind.INTERNAL_SALE) {
                transfer.setAcceptedAt(completedAt);
                transfer.setAcceptedBy(fixture.source().getUser().getId());
            }
        } else if (status == com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus.CANCELLED) {
            transfer.setCancelledAt(Instant.now());
        }
        transfers.saveAndFlush(transfer);
    }

    private String registration(GoatFarm farm, int suffix) {
        return farm.getTod() + String.format("%04d", suffix);
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
