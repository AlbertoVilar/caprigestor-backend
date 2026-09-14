package com.devmaster.goatfarm.integration;

import com.devmaster.goatfarm.authority.application.ports.in.CurrentPrincipalQueryUseCase;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.InternalOwnershipTransferRequest;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatCurrentOwnerProjectionPort;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatOwnershipLockPort;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatOwnershipPeriodPersistencePort;
import com.devmaster.goatfarm.goatownership.application.ports.out.OwnershipTransferPersistencePort;
import com.devmaster.goatfarm.goatownership.business.GoatOwnershipTransferBusiness;
import com.devmaster.goatfarm.goatownership.domain.CreatorReference;
import com.devmaster.goatfarm.goatownership.domain.CreatorSource;
import com.devmaster.goatfarm.goatownership.domain.GoatOwnershipPeriod;
import com.devmaster.goatfarm.goatownership.domain.OwnershipEntryType;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus;
import com.devmaster.goatfarm.goatownership.persistence.adapter.CreatorReferencePersistenceAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.sql.Timestamp;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class GoatOwnershipTransferPostgresIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    private static final Instant START = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant ACCEPTED = Instant.parse("2026-02-01T00:00:00Z");
    private static final Instant ACCEPTED_AGAIN = Instant.parse("2026-03-01T00:00:00Z");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired TransactionTemplate transactionTemplate;
    @Autowired GoatFarmPersistencePort farmPersistence;
    @Autowired GoatOwnershipLockPort ownershipLock;
    @Autowired GoatOwnershipPeriodPersistencePort periodPersistence;
    @Autowired OwnershipTransferPersistencePort transferPersistence;
    @Autowired GoatCurrentOwnerProjectionPort projection;
    @Autowired CreatorReferencePersistenceAdapter creatorReference;

    @Test
    void internalTransferCompletesAtomicallyAndPreservesIdentityAndHistory() {
        long sourceFarm = createFarm("W6 Source");
        long targetFarm = createFarm("W6 Target");
        long goatRow = createGoat(sourceFarm, "W6-GOAT-" + System.nanoTime());
        GoatId goatId = GoatId.of(goatRow);
        jdbcTemplate.update("insert into goat_ownership_period (goat_id, farm_id, started_at, entry_type, source, version) values (?, ?, ?, 'MANUAL_IMPORT', 'w6-test', 0)", goatRow, sourceFarm, Timestamp.from(START));
        creatorReference.create(goatId, CreatorReference.farm("SRC01", sourceFarm, "Source", CreatorSource.MANUAL_DECLARATION, "W6", START));
        String registration = jdbcTemplate.queryForObject("select num_registro from cabras where id = ?", String.class, goatRow);

        var business = businessAt(ACCEPTED);
        var requested = transactionTemplate.execute(status -> business.requestInternalTransfer(
                new InternalOwnershipTransferRequest(goatId, targetFarm, "w6 move", "w6-key-" + goatRow)));
        assertThat(requested).isNotNull();
        assertThat(requested.status()).isEqualTo(OwnershipTransferStatus.REQUESTED);
        assertThat(jdbcTemplate.queryForObject("select farm_id from goat_ownership_period where goat_id = ? and ended_at is null", Long.class, goatRow)).isEqualTo(sourceFarm);

        var completed = transactionTemplate.execute(status -> business.acceptTransfer(requested.id()));
        assertThat(completed).isNotNull();
        assertThat(completed.status()).isEqualTo(OwnershipTransferStatus.COMPLETED);
        assertThat(completed.acceptedAt()).isEqualTo(ACCEPTED);
        assertThat(completed.effectiveAt()).isEqualTo(ACCEPTED);
        assertThat(completed.completedAt()).isEqualTo(ACCEPTED);

        assertThat(jdbcTemplate.queryForObject("select capril_id from cabras where id = ?", Long.class, goatRow)).isEqualTo(targetFarm);
        assertThat(jdbcTemplate.queryForObject("select farm_id from goat_ownership_period where goat_id = ? and ended_at is null", Long.class, goatRow)).isEqualTo(targetFarm);
        assertThat(jdbcTemplate.queryForObject("select count(*) from goat_ownership_period where goat_id = ? and ended_at is null", Integer.class, goatRow)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select ended_at from goat_ownership_period where goat_id = ? and farm_id = ?", Instant.class, goatRow, sourceFarm)).isEqualTo(ACCEPTED);
        assertThat(jdbcTemplate.queryForObject("select started_at from goat_ownership_period where goat_id = ? and farm_id = ?", Instant.class, goatRow, targetFarm)).isEqualTo(ACCEPTED);
        assertThat(jdbcTemplate.queryForObject("select num_registro from cabras where id = ?", String.class, goatRow)).isEqualTo(registration);
        assertThat(creatorReference.findByGoatId(goatId)).isPresent();

        var secondBusiness = businessAt(ACCEPTED_AGAIN);
        var second = transactionTemplate.execute(status -> secondBusiness.requestInternalTransfer(
                new InternalOwnershipTransferRequest(goatId, sourceFarm, "w6 return", "w6-key-second-" + goatRow)));
        transactionTemplate.executeWithoutResult(status -> secondBusiness.acceptTransfer(second.id()));
        assertThat(jdbcTemplate.queryForObject("select count(*) from goat_ownership_period where goat_id = ?", Integer.class, goatRow)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("select count(*) from goat_ownership_period where goat_id = ? and ended_at is null", Integer.class, goatRow)).isEqualTo(1);
    }

    @Test
    void projectionDriftRollsBackTheWholeAcceptance() {
        long sourceFarm = createFarm("W6 Drift Source");
        long targetFarm = createFarm("W6 Drift Target");
        long goatRow = createGoat(sourceFarm, "W6-DRIFT-" + System.nanoTime());
        GoatId goatId = GoatId.of(goatRow);
        jdbcTemplate.update("insert into goat_ownership_period (goat_id, farm_id, started_at, entry_type, source, version) values (?, ?, ?, 'MANUAL_IMPORT', 'w6-drift', 0)", goatRow, sourceFarm, Timestamp.from(START));
        var business = businessAt(ACCEPTED);
        var requested = transactionTemplate.execute(status -> business.requestInternalTransfer(
                new InternalOwnershipTransferRequest(goatId, targetFarm, "drift", "w6-drift-key-" + goatRow)));
        jdbcTemplate.update("update cabras set capril_id = ? where id = ?", targetFarm, goatRow);

        assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(status -> business.acceptTransfer(requested.id())))
                .hasMessageContaining("projection drift");
        assertThat(transferPersistence.findById(requested.id()).orElseThrow().status()).isEqualTo(OwnershipTransferStatus.REQUESTED);
        assertThat(jdbcTemplate.queryForObject("select count(*) from goat_ownership_period where goat_id = ?", Integer.class, goatRow)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select farm_id from goat_ownership_period where goat_id = ? and ended_at is null", Long.class, goatRow)).isEqualTo(sourceFarm);
    }

    @Test
    void rejectionAndCancellationLeaveTheCanonicalOwnerUnchanged() {
        long sourceFarm = createFarm("W6 Reject Source");
        long targetFarm = createFarm("W6 Reject Target");
        long rejectGoat = createGoat(sourceFarm, "W6-REJECT-" + System.nanoTime());
        long cancelGoat = createGoat(sourceFarm, "W6-CANCEL-" + System.nanoTime());
        seedOpenPeriod(rejectGoat, sourceFarm, "w6-reject");
        seedOpenPeriod(cancelGoat, sourceFarm, "w6-cancel");
        var business = businessAt(ACCEPTED);
        var rejected = transactionTemplate.execute(status -> business.requestInternalTransfer(new InternalOwnershipTransferRequest(GoatId.of(rejectGoat), targetFarm, "reject", "w6-reject-key")));
        var cancelled = transactionTemplate.execute(status -> business.requestInternalTransfer(new InternalOwnershipTransferRequest(GoatId.of(cancelGoat), targetFarm, "cancel", "w6-cancel-key")));
        transactionTemplate.executeWithoutResult(status -> business.rejectTransfer(rejected.id()));
        transactionTemplate.executeWithoutResult(status -> business.cancelTransfer(cancelled.id()));
        assertThat(jdbcTemplate.queryForObject("select count(*) from goat_ownership_period where goat_id = ?", Integer.class, rejectGoat)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from goat_ownership_period where goat_id = ?", Integer.class, cancelGoat)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select capril_id from cabras where id = ?", Long.class, rejectGoat)).isEqualTo(sourceFarm);
        assertThat(jdbcTemplate.queryForObject("select capril_id from cabras where id = ?", Long.class, cancelGoat)).isEqualTo(sourceFarm);
    }

    @Test
    void concurrentRequestsForOneGoatCreateOnlyOnePendingTransfer() throws Exception {
        long sourceFarm = createFarm("W6 Concurrent Source");
        long targetFarm = createFarm("W6 Concurrent Target");
        long goatRow = createGoat(sourceFarm, "W6-CONCURRENT-" + System.nanoTime());
        seedOpenPeriod(goatRow, sourceFarm, "w6-concurrent");
        var business = businessAt(ACCEPTED);
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> concurrentRequest(business, GoatId.of(goatRow), targetFarm, "w6-concurrent-a", ready, start));
            var second = executor.submit(() -> concurrentRequest(business, GoatId.of(goatRow), targetFarm, "w6-concurrent-b", ready, start));
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            int successes = 0;
            int failures = 0;
            for (var future : List.of(first, second)) {
                try {
                    future.get(30, TimeUnit.SECONDS);
                    successes++;
                } catch (ExecutionException expected) {
                    failures++;
                }
            }
            assertThat(successes).isEqualTo(1);
            assertThat(failures).isEqualTo(1);
        }
        assertThat(jdbcTemplate.queryForObject("select count(*) from ownership_transfer where goat_id = ? and state in ('REQUESTED', 'ACCEPTED')", Integer.class, goatRow)).isEqualTo(1);
    }

    @Test
    void concurrentAcceptanceRetriesReturnOneCompletedTransferAndOneOpenPeriod() throws Exception {
        long sourceFarm = createFarm("W6 Accept Source");
        long targetFarm = createFarm("W6 Accept Target");
        long goatRow = createGoat(sourceFarm, "W6-ACCEPT-" + System.nanoTime());
        seedOpenPeriod(goatRow, sourceFarm, "w6-accept");
        var business = businessAt(ACCEPTED);
        var requested = transactionTemplate.execute(status -> business.requestInternalTransfer(
                new InternalOwnershipTransferRequest(GoatId.of(goatRow), targetFarm, "accept", "w6-accept-key")));
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> concurrentAccept(business, requested.id(), ready, start));
            var second = executor.submit(() -> concurrentAccept(business, requested.id(), ready, start));
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            int completed = 0;
            for (var future : List.of(first, second)) {
                try {
                    assertThat(future.get(30, TimeUnit.SECONDS).status()).isEqualTo(OwnershipTransferStatus.COMPLETED);
                    completed++;
                } catch (ExecutionException expectedConcurrentOutcome) {
                    // A racing retry may observe the completed handoff boundary and
                    // fail closed; the invariants below prove no duplicate period.
                    assertThat(expectedConcurrentOutcome.getCause()).isInstanceOf(RuntimeException.class);
                }
            }
            assertThat(completed).isGreaterThanOrEqualTo(1);
        }
        assertThat(jdbcTemplate.queryForObject("select count(*) from goat_ownership_period where goat_id = ? and ended_at is null", Integer.class, goatRow)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("select count(*) from goat_ownership_period where goat_id = ?", Integer.class, goatRow)).isEqualTo(2);
    }

    private Object concurrentRequest(GoatOwnershipTransferBusiness business, GoatId goatId, long targetFarm,
                                     String key, CountDownLatch ready, CountDownLatch start) {
        ready.countDown();
        await(start);
        return transactionTemplate.execute(status -> business.requestInternalTransfer(
                new InternalOwnershipTransferRequest(goatId, targetFarm, "concurrent", key)));
    }

    private com.devmaster.goatfarm.goatownership.domain.OwnershipTransfer concurrentAccept(
            GoatOwnershipTransferBusiness business, long transferId, CountDownLatch ready, CountDownLatch start) {
        ready.countDown();
        await(start);
        return transactionTemplate.execute(status -> business.acceptTransfer(transferId));
    }

    private void await(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("timed out waiting for concurrent test start");
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted while waiting for concurrent test start", ex);
        }
    }

    private GoatOwnershipTransferBusiness businessAt(Instant now) {
        var principal = mock(CurrentPrincipalQueryUseCase.class);
        var authorization = mock(FarmAuthorizationUseCase.class);
        when(principal.requireCurrent()).thenReturn(new AuthenticatedPrincipal(1L, "w6@example.com", "W6", Set.of("ROLE_FARM_OWNER")));
        when(authorization.canAdministerFarm(anyLong())).thenReturn(true);
        return new GoatOwnershipTransferBusiness(principal, authorization, farmPersistence, ownershipLock,
                periodPersistence, transferPersistence, projection, Clock.fixed(now, ZoneOffset.UTC));
    }

    private void seedOpenPeriod(long goatId, long farmId, String source) {
        jdbcTemplate.update("insert into goat_ownership_period (goat_id, farm_id, started_at, entry_type, source, version) values (?, ?, ?, 'MANUAL_IMPORT', ?, 0)", goatId, farmId, Timestamp.from(START), source);
    }

    private long createFarm(String prefix) {
        long userId = jdbcTemplate.queryForObject("insert into users (name, email, password, cpf) values (?, ?, 'password', ?) returning id", Long.class,
                prefix + " User", prefix.toLowerCase().replace(' ', '-') + "-" + System.nanoTime() + "@example.com", String.valueOf(Math.abs(System.nanoTime())).substring(0, 11));
        return jdbcTemplate.queryForObject("insert into capril (name, user_id, tod) values (?, ?, ?) returning id", Long.class,
                prefix + " " + System.nanoTime(), userId, prefix.substring(0, 3).toUpperCase() + (userId % 100));
    }

    private long createGoat(long farmId, String registration) {
        Long userId = jdbcTemplate.queryForObject("select user_id from capril where id = ?", Long.class, farmId);
        String compactRegistration = registration.length() > 18 ? registration.substring(0, 18) : registration;
        return jdbcTemplate.queryForObject("insert into cabras (num_registro, nome, sexo, data_nascimento, status, usuario_id, capril_id) values (?, 'W6 Goat', 'FEMEA', date '2020-01-01', 'ATIVO', ?, ?) returning id", Long.class,
                compactRegistration, userId, farmId);
    }
}
