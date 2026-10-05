package com.devmaster.goatfarm.integration;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.domain.CreatorReference;
import com.devmaster.goatfarm.goatownership.domain.CreatorSource;
import com.devmaster.goatfarm.goatownership.domain.GoatOwnershipPeriod;
import com.devmaster.goatfarm.goatownership.domain.OwnershipEntryType;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransfer;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferKind;
import com.devmaster.goatfarm.goatownership.persistence.adapter.CreatorReferencePersistenceAdapter;
import com.devmaster.goatfarm.goatownership.persistence.adapter.GoatOwnershipPeriodPersistenceAdapter;
import com.devmaster.goatfarm.goatownership.persistence.adapter.GoatOwnershipQueryPersistenceAdapter;
import com.devmaster.goatfarm.goatownership.persistence.adapter.OwnershipTransferPersistenceAdapter;
import com.devmaster.goatfarm.goatownership.persistence.entity.GoatOwnershipPeriodEntity;
import com.devmaster.goatfarm.goatownership.persistence.entity.OwnershipTransferEntity;
import com.devmaster.goatfarm.goatownership.persistence.repository.GoatOwnershipPeriodRepository;
import com.devmaster.goatfarm.goatownership.persistence.repository.OwnershipTransferRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class GoatOwnershipPersistencePostgresIntegrationTest {

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
    private CreatorReferencePersistenceAdapter creatorAdapter;
    @Autowired
    private GoatOwnershipPeriodPersistenceAdapter periodAdapter;
    @Autowired
    private GoatOwnershipQueryPersistenceAdapter queryAdapter;
    @Autowired
    private OwnershipTransferPersistenceAdapter transferAdapter;
    @Autowired
    private GoatOwnershipPeriodRepository periodRepository;
    @Autowired
    private OwnershipTransferRepository transferRepository;

    @Test
    void realAdaptersPersistCreatorPeriodsQueriesTransfersAndVersions() {
        long farmId = createFarm();
        GoatId farmGoat = GoatId.of(createGoat(farmId));
        GoatId externalGoat = GoatId.of(createGoat(farmId));
        GoatId unknownGoat = GoatId.of(createGoat(farmId));

        var farmCreator = creatorAdapter.create(farmGoat,
                CreatorReference.farm(" 12345 ", farmId, " Capril ", CreatorSource.BIRTH, " DOC ", Instant.now()));
        assertCreatorRead(farmGoat, farmCreator);
        assertThat(farmCreator.creatorTod()).isEqualTo("12345");
        assertThat(farmCreator.creatorNameSnapshot()).isEqualTo("Capril");
        var externalCreator = creatorAdapter.create(externalGoat,
                CreatorReference.external("98765", "External", CreatorSource.ABCC, "ABCC-1", Instant.now()));
        assertCreatorRead(externalGoat, externalCreator);
        var unknownCreator = creatorAdapter.create(unknownGoat, CreatorReference.unknown(Instant.now()));
        assertCreatorRead(unknownGoat, unknownCreator);
        assertThatThrownBy(() -> creatorAdapter.create(farmGoat, CreatorReference.unknown(Instant.now())))
                .isInstanceOf(DataIntegrityViolationException.class);

        Instant start = Instant.parse("2026-01-01T00:00:00Z");
        Instant end = Instant.parse("2026-02-01T00:00:00Z");
        var open = periodAdapter.save(GoatOwnershipPeriod.open(farmGoat, farmId, start,
                OwnershipEntryType.MANUAL_IMPORT, "integration"));
        assertThat(open.id()).isNotNull();
        assertThat(periodAdapter.findOpenByGoatId(farmGoat)).isPresent().get()
                .usingRecursiveComparison().isEqualTo(open);
        assertThat(queryAdapter.findCurrentOwnerFarmId(farmGoat)).contains(farmId);
        assertThat(queryAdapter.isOwnedByFarmAt(farmGoat, farmId, start)).isTrue();

        open.close(end, com.devmaster.goatfarm.goatownership.domain.OwnershipExitType.TRANSFER_OUT);
        var closed = periodAdapter.save(open);
        assertThat(periodAdapter.findOpenByGoatId(farmGoat)).isEmpty();
        assertThat(queryAdapter.isOwnedByFarmAt(farmGoat, farmId, end)).isFalse();
        assertThat(queryAdapter.findOwnershipHistory(farmGoat)).extracting(GoatOwnershipPeriod::id)
                .containsExactly(closed.id());
        var reopened = periodAdapter.save(GoatOwnershipPeriod.open(farmGoat, farmId, end,
                OwnershipEntryType.RETURN, "integration"));
        assertThat(queryAdapter.isOwnedByFarmAt(farmGoat, farmId, end)).isTrue();
        assertThat(reopened.id()).isNotEqualTo(closed.id());

        var transfer = transferAdapter.save(OwnershipTransfer.request(farmGoat, farmId, farmId + 1,
                OwnershipTransferKind.INTERNAL_TRANSFER, "test", "idempotency-1", start, 1L, null));
        assertThat(transferAdapter.findById(transfer.id())).isPresent().get()
                .usingRecursiveComparison().isEqualTo(transfer);
        assertThat(transferAdapter.findPendingByGoatId(farmGoat)).isPresent().get()
                .usingRecursiveComparison().isEqualTo(transfer);
        assertThat(transferAdapter.findByRequesterAndIdempotencyKey(1L, "idempotency-1")).isPresent().get()
                .usingRecursiveComparison().isEqualTo(transfer);

        GoatOwnershipPeriodEntity periodEntity = periodRepository.findById(closed.id()).orElseThrow();
        Long periodVersion = periodEntity.getVersion();
        periodEntity.setSource("updated");
        transactionTemplate.executeWithoutResult(status -> periodRepository.saveAndFlush(periodEntity));
        assertThat(periodRepository.findById(closed.id()).orElseThrow().getVersion()).isEqualTo(periodVersion + 1);

        OwnershipTransferEntity transferEntity = transferRepository.findById(transfer.id()).orElseThrow();
        Long transferVersion = transferEntity.getVersion();
        transferEntity.setReason("updated");
        transactionTemplate.executeWithoutResult(status -> transferRepository.saveAndFlush(transferEntity));
        assertThat(transferRepository.findById(transfer.id()).orElseThrow().getVersion()).isEqualTo(transferVersion + 1);
    }

    private long createFarm() {
        long userId = jdbcTemplate.queryForObject("""
                insert into users (name, email, password, cpf)
                values ('Persistence User', 'persistence-%d@example.com', 'password', '%011d') returning id
                """.formatted(System.nanoTime(), System.nanoTime() % 100000000000L), Long.class);
        return jdbcTemplate.queryForObject("""
                insert into capril (name, user_id, tod)
                values ('Persistence Farm %d', %d, 'PST%02d') returning id
                """.formatted(System.nanoTime(), userId, userId % 100), Long.class);
    }

    private void assertCreatorRead(GoatId goatId, CreatorReference expected) {
        var actual = creatorAdapter.findByGoatId(goatId).orElseThrow();
        assertThat(actual).usingRecursiveComparison().ignoringFields("recordedAt").isEqualTo(expected);
        assertThat(actual.recordedAt()).isNotNull();
    }

    private long createGoat(long farmId) {
        return jdbcTemplate.queryForObject("""
                insert into cabras (num_registro, nome, sexo, data_nascimento, status, usuario_id, capril_id)
                values ('P-%d', 'Persistence Goat', 'FEMEA', date '2020-01-01', 'ATIVO',
                        (select user_id from capril where id = %d), %d)
                returning id
                """.formatted(Math.abs(System.nanoTime() % 1_000_000_000_000L), farmId, farmId), Long.class);
    }
}
