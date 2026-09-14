package com.devmaster.goatfarm.integration;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.persistence.adapter.GoatOwnershipLockPersistenceAdapter;
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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class GoatOwnershipLockPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private GoatOwnershipLockPersistenceAdapter lockAdapter;

    @Test
    void secondTransactionWaitsForCanonicalGoatLockUntilFirstCommits() throws Exception {
        long goatId = createGoatWithOpenOwnershipPeriod();
        var firstLocked = new CountDownLatch(1);
        var releaseFirst = new CountDownLatch(1);
        var secondStarted = new CountDownLatch(1);
        var secondAcquired = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> transactionTemplate.execute(status -> {
                assertThat(lockAdapter.lockGoatOwnership(GoatId.of(goatId))).isPresent();
                firstLocked.countDown();
                await(releaseFirst);
                return null;
            }));

            assertThat(firstLocked.await(10, TimeUnit.SECONDS)).isTrue();
            var second = executor.submit(() -> transactionTemplate.execute(status -> {
                secondStarted.countDown();
                var result = lockAdapter.lockGoatOwnership(GoatId.of(goatId));
                secondAcquired.countDown();
                return result;
            }));

            assertThat(secondStarted.await(10, TimeUnit.SECONDS)).isTrue();
            assertThat(secondAcquired.await(300, TimeUnit.MILLISECONDS)).isFalse();
            releaseFirst.countDown();
            assertThat(secondAcquired.await(10, TimeUnit.SECONDS)).isTrue();
            first.get(10, TimeUnit.SECONDS);
            assertThat(second.get(10, TimeUnit.SECONDS)).isPresent();
        }
    }

    @Test
    void lockDistinguishesMissingGoatAndExistingGoatWithoutOpenPeriod() {
        long missingGoatId = Long.MAX_VALUE;
        java.util.Optional<?> missing = transactionTemplate.execute(
                status -> lockAdapter.lockGoatOwnership(GoatId.of(missingGoatId)));
        assertThat(missing).isEmpty();

        long goatId = createGoatWithOpenOwnershipPeriod();
        jdbcTemplate.update("delete from goat_ownership_period where goat_id = ?", goatId);
        var result = transactionTemplate.execute(status -> lockAdapter.lockGoatOwnership(GoatId.of(goatId)));
        assertThat(result).isPresent();
        assertThat(result.orElseThrow().openPeriod()).isEmpty();
    }

    private long createGoatWithOpenOwnershipPeriod() {
        long userId = jdbcTemplate.queryForObject("""
                insert into users (name, email, password, cpf)
                values ('Lock User', 'lock-%s@example.com', 'password', '%s') returning id
                """.formatted(System.nanoTime(), String.valueOf(System.nanoTime()).substring(0, 11)), Long.class);
        String farmName = "Lock Farm " + System.nanoTime();
        long farmId = jdbcTemplate.queryForObject("""
                insert into capril (name, user_id, tod) values ('%s', %d, 'LCK%02d') returning id
                """.formatted(farmName, userId, userId % 100), Long.class);
        long goatId = jdbcTemplate.queryForObject("""
                insert into cabras (num_registro, nome, sexo, data_nascimento, status, usuario_id, capril_id)
                values ('LOCK-%d', 'Lock Goat', 'FEMEA', date '2020-01-01', 'ATIVO', %d, %d) returning id
                """.formatted(System.nanoTime(), userId, farmId), Long.class);
        jdbcTemplate.update("""
                insert into goat_ownership_period (goat_id, farm_id, started_at, entry_type, source, version)
                values (?, ?, current_timestamp, 'MANUAL_IMPORT', 'lock-test', 0)
                """, goatId, farmId);
        return goatId;
    }

    private void await(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("timed out waiting for lock release");
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted while waiting for lock release", ex);
        }
    }
}
