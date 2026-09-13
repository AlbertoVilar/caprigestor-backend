package com.devmaster.goatfarm.integration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers(disabledWithoutDocker = true)
class GoatOwnershipSchemaFlywayPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @BeforeEach
    void resetDatabase() throws SQLException {
        try (Connection connection = openConnection()) {
            execute(connection, "DROP SCHEMA public CASCADE");
            execute(connection, "CREATE SCHEMA public");
        }
    }

    @Test
    void freshInstallCreatesOwnershipSchemaAtV46() throws SQLException {
        flyway().migrate();

        try (Connection connection = openConnection()) {
            assertThat(queryString(connection,
                    "select version from flyway_schema_history order by installed_rank desc limit 1"))
                    .isEqualTo("46");
            assertThat(tableExists(connection, "goat_creator_reference")).isTrue();
            assertThat(tableExists(connection, "goat_ownership_period")).isTrue();
            assertThat(tableExists(connection, "ownership_transfer")).isTrue();
            assertThat(indexDefinition(connection, "ux_goat_ownership_period_open_goat"))
                    .containsIgnoringCase("unique")
                    .containsIgnoringCase("where")
                    .containsIgnoringCase("ended_at IS NULL");
            assertThat(indexDefinition(connection, "ux_ownership_transfer_pending_goat"))
                    .containsIgnoringCase("unique")
                    .containsIgnoringCase("REQUESTED")
                    .containsIgnoringCase("ACCEPTED");
            assertThat(uniqueConstraintExists(connection, "uk_ownership_transfer_request_idempotency"))
                    .isTrue();
            assertThat(uniqueConstraintExists(connection, "uk_cabras_farm_id")).isTrue();
            for (String constraint : new String[]{
                    "fk_pregnancy_goat_technical_direct",
                    "fk_reproductive_event_goat_technical_direct",
                    "fk_health_events_goat_technical_direct",
                    "fk_lactation_goat_technical_direct",
                    "fk_milk_production_goat_technical_direct",
                    "fk_animal_sale_goat_technical_direct",
                    "fk_operational_audit_entry_goat_technical_direct",
                    "fk_pregnancy_farm_goat_technical",
                    "fk_reproductive_event_farm_goat_technical",
                    "fk_health_events_farm_goat_technical",
                    "fk_lactation_farm_goat_technical",
                    "fk_milk_production_farm_goat_technical_lactation",
                    "fk_animal_sale_farm_goat_technical",
                    "fk_operational_audit_entry_farm_goat_technical"}) {
                assertThat(foreignKeyExists(connection, constraint)).as(constraint).isTrue();
            }
        }
    }

    @Test
    void upgradeFromV44PreservesExistingRowsAndLeavesOwnershipTablesEmpty() throws SQLException {
        flyway("44").migrate();

        try (Connection connection = openConnection()) {
            seedUsersAndFarms(connection);
            insertGoat(connection, "W2-UPGRADE-GOAT", 1, 101);
            assertThat(queryLong(connection, "select count(*) from cabras")).isEqualTo(1L);
        }

        flyway().migrate();

        try (Connection connection = openConnection()) {
            assertThat(queryLong(connection, "select count(*) from cabras")).isEqualTo(1L);
            assertThat(queryLong(connection, "select count(*) from goat_creator_reference")).isZero();
            assertThat(queryLong(connection, "select count(*) from goat_ownership_period")).isZero();
            assertThat(queryLong(connection, "select count(*) from ownership_transfer")).isZero();
            assertThat(queryString(connection,
                    "select version from flyway_schema_history order by installed_rank desc limit 1"))
                    .isEqualTo("46");
        }
    }

    @Test
    void postgresqlEnforcesOwnershipStructuralConstraints() throws SQLException {
        flyway().migrate();

        try (Connection connection = openConnection()) {
            seedUsersAndFarms(connection);
            insertGoat(connection, "W2-GOAT-1", 1, 101);
            insertGoat(connection, "W2-GOAT-2", 1, 102);
            insertGoat(connection, "W2-GOAT-3", 2, 101);
            insertGoat(connection, "W2-GOAT-4", 2, 102);
            insertGoat(connection, "W2-GOAT-5", 1, 101);
            long goat1 = goatId(connection, "W2-GOAT-1");
            long goat2 = goatId(connection, "W2-GOAT-2");
            long goat3 = goatId(connection, "W2-GOAT-3");
            long goat4 = goatId(connection, "W2-GOAT-4");
            long goat5 = goatId(connection, "W2-GOAT-5");

            execute(connection, """
                    insert into goat_creator_reference
                        (goat_id, creator_tod, creator_farm_id, source, recorded_at)
                    values (%d, '12345', 101, 'BIRTH', current_timestamp),
                           (%d, '12345', null, 'ABCC', current_timestamp),
                           (%d, null, null, 'UNKNOWN', current_timestamp)
                    """.formatted(goat1, goat2, goat3));
            assertSqlFails(connection, """
                    insert into goat_creator_reference
                        (goat_id, creator_tod, source, recorded_at)
                    values (%d, '12345', 'UNKNOWN', current_timestamp)
                    """.formatted(goat4));
            assertSqlFails(connection, """
                    insert into goat_creator_reference
                        (goat_id, creator_tod, creator_farm_id, source, recorded_at)
                    values (999999, '12345', 101, 'BIRTH', current_timestamp)
                    """);
            assertSqlFails(connection, """
                    insert into goat_creator_reference
                        (goat_id, creator_farm_id, source, recorded_at)
                    values (%d, 101, 'BIRTH', current_timestamp)
                    """.formatted(goat4));
            assertSqlFails(connection, """
                    insert into goat_creator_reference
                        (goat_id, creator_tod, creator_farm_id, source, recorded_at)
                    values (%d, '12345', 999999, 'BIRTH', current_timestamp)
                    """.formatted(goat4));

            execute(connection, """
                    insert into goat_ownership_period
                        (goat_id, farm_id, started_at, entry_type, source)
                    values (%d, 101, timestamptz '2026-01-01 00:00:00+00', 'BIRTH', 'test'),
                           (%d, 102, timestamptz '2026-01-01 00:00:00+00', 'PURCHASE', 'test')
                    """.formatted(goat1, goat2));
            assertSqlFails(connection, """
                    insert into goat_ownership_period
                        (goat_id, farm_id, started_at, entry_type, source)
                    values (%d, 101, timestamptz '2026-02-01 00:00:00+00', 'RETURN', 'test')
                    """.formatted(goat1));
            execute(connection, """
                    insert into goat_ownership_period
                        (goat_id, farm_id, started_at, ended_at, entry_type, exit_type, source)
                    values (%d, 101, timestamptz '2025-01-01 00:00:00+00',
                            timestamptz '2025-02-01 00:00:00+00', 'BIRTH', 'TRANSFER_OUT', 'test'),
                           (%d, 101, timestamptz '2025-02-01 00:00:00+00',
                            timestamptz '2025-03-01 00:00:00+00', 'TRANSFER_IN', 'EXTERNAL_SALE', 'test')
                    """.formatted(goat3, goat3));
            assertSqlFails(connection, """
                    insert into goat_ownership_period
                        (goat_id, farm_id, started_at, ended_at, entry_type, exit_type, source)
                    values (%d, 101, timestamptz '2026-04-01 00:00:00+00',
                            timestamptz '2026-04-01 00:00:00+00', 'BIRTH', 'DEATH', 'test')
                    """.formatted(goat3));
            assertSqlFails(connection, """
                    insert into goat_ownership_period
                        (goat_id, farm_id, started_at, entry_type, exit_type, source)
                    values (%d, 101, timestamptz '2026-04-01 00:00:00+00', 'BIRTH', 'DEATH', 'test')
                    """.formatted(goat4));
            assertSqlFails(connection, """
                    insert into goat_ownership_period
                        (goat_id, farm_id, started_at, ended_at, entry_type, source)
                    values (%d, 101, timestamptz '2026-04-01 00:00:00+00',
                            timestamptz '2026-05-01 00:00:00+00', 'BIRTH', 'test')
                    """.formatted(goat4));
            assertSqlFails(connection, """
                    insert into goat_ownership_period
                        (goat_id, farm_id, started_at, entry_type, source)
                    values (%d, 999999, current_timestamp, 'BIRTH', 'test')
                    """.formatted(goat4));
            assertSqlFails(connection, """
                    insert into goat_ownership_period
                        (goat_id, farm_id, started_at, entry_type, source)
                    values (999999, 101, current_timestamp, 'BIRTH', 'test')
                    """);

            insertTransfer(connection, goat1, 101, 102, "INTERNAL_TRANSFER", "REQUESTED", 1, "pending-1");
            assertSqlFails(connection, """
                    insert into ownership_transfer
                        (goat_id, source_farm_id, target_farm_id, kind, state, reason,
                         idempotency_key, requested_at, requested_by)
                    values (%d, 101, 102, 'INTERNAL_TRANSFER', 'REQUESTED', 'duplicate',
                            'pending-2', current_timestamp, 1)
                    """.formatted(goat1));
            insertTransfer(connection, goat2, 102, 101, "INTERNAL_TRANSFER", "REJECTED", 1, "rejected-1");
            insertTransfer(connection, goat2, 102, 101, "RETURN", "REQUESTED", 1, "return-1");
            insertTransfer(connection, goat3, 101, 102, "INTERNAL_TRANSFER", "REQUESTED", 1, "same-key");
            assertSqlFails(connection, """
                    insert into ownership_transfer
                        (goat_id, source_farm_id, target_farm_id, kind, state, reason,
                         idempotency_key, requested_at, requested_by)
                    values (%d, 101, 102, 'INTERNAL_TRANSFER', 'REQUESTED', 'duplicate key',
                            'same-key', current_timestamp, 1)
                    """.formatted(goat4));
            insertTransfer(connection, goat4, 101, 102, "INTERNAL_TRANSFER", "REQUESTED", 2, "same-key");

            assertSqlFails(connection, """
                    insert into ownership_transfer
                        (goat_id, target_farm_id, kind, state, reason,
                         idempotency_key, requested_at, requested_by)
                    values (%d, 102, 'INTERNAL_TRANSFER', 'REQUESTED', 'missing source',
                            'bad-kind-source-null', current_timestamp, 1)
                    """.formatted(goat5));
            execute(connection, """
                    insert into ownership_transfer
                        (goat_id, target_farm_id, kind, state, reason,
                         idempotency_key, requested_at, requested_by)
                    values (%d, 102, 'EXTERNAL_CLAIM', 'REJECTED', 'external claim',
                            'external-claim-valid', current_timestamp, 1)
                    """.formatted(goat5));

            assertSqlFails(connection, """
                    insert into ownership_transfer
                        (goat_id, source_farm_id, target_farm_id, kind, state, reason,
                         idempotency_key, requested_at, requested_by)
                    values (%d, 101, 102, 'EXTERNAL_CLAIM', 'REQUESTED', 'bad source',
                            'bad-kind-1', current_timestamp, 1)
                    """.formatted(goat4));
            assertSqlFails(connection, """
                    insert into ownership_transfer
                        (goat_id, source_farm_id, target_farm_id, kind, state, reason,
                         idempotency_key, requested_at, requested_by)
                    values (%d, 101, 101, 'INTERNAL_TRANSFER', 'REQUESTED', 'same farm',
                            'bad-kind-2', current_timestamp, 1)
                    """.formatted(goat4));
            assertSqlFails(connection, """
                    insert into ownership_transfer
                        (goat_id, source_farm_id, target_farm_id, kind, state, reason,
                         idempotency_key, requested_at, requested_by, accepted_at, accepted_by)
                    values (%d, 101, 102, 'INTERNAL_TRANSFER', 'ACCEPTED', 'missing acceptance',
                            'bad-state-1', current_timestamp, 1, null, null)
                    """.formatted(goat4));
            assertSqlFails(connection, """
                    insert into ownership_transfer
                        (goat_id, source_farm_id, target_farm_id, kind, state, reason,
                         idempotency_key, requested_at, accepted_at, effective_at, completed_at,
                         requested_by, accepted_by, completed_by)
                    values (%d, 101, 102, 'INTERNAL_TRANSFER', 'COMPLETED', 'mismatched effective',
                            'bad-state-2', timestamptz '2026-01-02 00:00:00+00',
                            timestamptz '2026-01-03 00:00:00+00', timestamptz '2026-01-04 00:00:00+00',
                            timestamptz '2026-01-05 00:00:00+00', 1, 1, 1)
                    """.formatted(goat4));
            assertSqlFails(connection, """
                    insert into ownership_transfer
                        (goat_id, source_farm_id, target_farm_id, kind, state, reason,
                         idempotency_key, requested_at, requested_by, sale_id)
                    values (%d, 101, 102, 'INTERNAL_TRANSFER', 'REQUESTED', 'invalid sale',
                            'bad-fk-sale', current_timestamp, 1, 999999)
                    """.formatted(goat4));
            assertSqlFails(connection, """
                    insert into ownership_transfer
                        (goat_id, source_farm_id, target_farm_id, kind, state, reason,
                         idempotency_key, requested_at, requested_by)
                    values (999999, 101, 102, 'INTERNAL_TRANSFER', 'REQUESTED', 'invalid goat',
                            'bad-fk-goat', current_timestamp, 1)
                    """);
            assertSqlFails(connection, """
                    insert into ownership_transfer
                        (goat_id, source_farm_id, target_farm_id, kind, state, reason,
                         idempotency_key, requested_at, requested_by)
                    values (%d, 999999, 102, 'INTERNAL_TRANSFER', 'REQUESTED', 'invalid source farm',
                            'bad-fk-source-farm', current_timestamp, 1)
                    """.formatted(goat5));
            assertSqlFails(connection, """
                    insert into ownership_transfer
                        (goat_id, source_farm_id, target_farm_id, kind, state, reason,
                         idempotency_key, requested_at, requested_by)
                    values (%d, 101, 999999, 'INTERNAL_TRANSFER', 'REQUESTED', 'invalid target farm',
                            'bad-fk-target-farm', current_timestamp, 1)
                    """.formatted(goat5));
            assertSqlFails(connection, """
                    insert into ownership_transfer
                        (goat_id, source_farm_id, target_farm_id, kind, state, reason,
                         idempotency_key, requested_at, requested_by)
                    values (%d, 101, 102, 'INTERNAL_TRANSFER', 'REQUESTED', 'invalid requester',
                            'bad-fk-requested-by', current_timestamp, 999999)
                    """.formatted(goat5));
            assertSqlFails(connection, """
                    insert into ownership_transfer
                        (goat_id, source_farm_id, target_farm_id, kind, state, reason,
                         idempotency_key, requested_at, accepted_at, accepted_by, requested_by)
                    values (%d, 101, 102, 'INTERNAL_TRANSFER', 'ACCEPTED', 'invalid accepter',
                            'bad-fk-accepted-by', timestamptz '2026-01-01 00:00:00+00',
                            timestamptz '2026-01-02 00:00:00+00', 999999, 1)
                    """.formatted(goat5));
            assertSqlFails(connection, """
                    insert into ownership_transfer
                        (goat_id, source_farm_id, target_farm_id, kind, state, reason,
                         idempotency_key, requested_at, accepted_at, effective_at, completed_at,
                         requested_by, accepted_by, completed_by)
                    values (%d, 101, 102, 'INTERNAL_TRANSFER', 'COMPLETED', 'invalid completer',
                            'bad-fk-completed-by', timestamptz '2026-01-01 00:00:00+00',
                            timestamptz '2026-01-02 00:00:00+00', timestamptz '2026-01-03 00:00:00+00',
                            timestamptz '2026-01-03 00:00:00+00', 1, 1, 999999)
                    """.formatted(goat5));
            assertSqlFails(connection, """
                    insert into ownership_transfer
                        (goat_id, source_farm_id, target_farm_id, kind, state, reason,
                         idempotency_key, requested_at, accepted_at, accepted_by, requested_by)
                    values (%d, 101, 102, 'INTERNAL_TRANSFER', 'REQUESTED', 'invalid requested lifecycle',
                            'bad-state-requested', timestamptz '2026-01-01 00:00:00+00',
                            timestamptz '2026-01-02 00:00:00+00', 1, 1)
                    """.formatted(goat5));
            assertSqlFails(connection, """
                    insert into ownership_transfer
                        (goat_id, source_farm_id, target_farm_id, kind, state, reason,
                         idempotency_key, requested_at, accepted_at, accepted_by, requested_by)
                    values (%d, 101, 102, 'INTERNAL_TRANSFER', 'REJECTED', 'invalid rejected lifecycle',
                            'bad-state-rejected', timestamptz '2026-01-01 00:00:00+00',
                            timestamptz '2026-01-02 00:00:00+00', 1, 1)
                    """.formatted(goat5));
            assertSqlFails(connection, """
                    insert into ownership_transfer
                        (goat_id, source_farm_id, target_farm_id, kind, state, reason,
                         idempotency_key, requested_at, requested_by)
                    values (%d, 101, 102, 'INTERNAL_TRANSFER', 'CANCELLED', 'invalid cancelled lifecycle',
                            'bad-state-cancelled', timestamptz '2026-01-01 00:00:00+00', 1)
                    """.formatted(goat5));
            execute(connection, """
                    insert into ownership_transfer
                        (goat_id, source_farm_id, target_farm_id, kind, state, reason,
                         idempotency_key, requested_at, accepted_at, effective_at, completed_at,
                         requested_by, accepted_by, completed_by)
                    values (%d, 101, 102, 'INTERNAL_TRANSFER', 'COMPLETED', 'valid completed transfer',
                            'completed-valid', timestamptz '2026-01-01 00:00:00+00',
                            timestamptz '2026-01-02 00:00:00+00', timestamptz '2026-01-03 00:00:00+00',
                            timestamptz '2026-01-03 00:00:00+00', 1, 1, 2)
                    """.formatted(goat5));
        }
    }

    private void insertTransfer(Connection connection, long goatId, long sourceFarmId, long targetFarmId,
                                String kind, String state, long requestedBy, String idempotencyKey)
            throws SQLException {
        execute(connection, """
                insert into ownership_transfer
                    (goat_id, source_farm_id, target_farm_id, kind, state, reason,
                     idempotency_key, requested_at, requested_by)
                values (%d, %d, %d, '%s', '%s', 'test transfer', '%s', current_timestamp, %d)
                """.formatted(goatId, sourceFarmId, targetFarmId, kind, state, idempotencyKey, requestedBy));
    }

    private void seedUsersAndFarms(Connection connection) throws SQLException {
        execute(connection, """
                insert into users (id, name, email, password, cpf)
                values (1, 'W2 User One', 'w2-one@example.com', 'password', '00000000001'),
                       (2, 'W2 User Two', 'w2-two@example.com', 'password', '00000000002')
                """);
        execute(connection, """
                insert into capril (id, name, user_id, tod)
                values (101, 'W2 Farm One', 1, '11111'),
                       (102, 'W2 Farm Two', 2, '22222')
                """);
    }

    private void insertGoat(Connection connection, String registration, long userId, long farmId)
            throws SQLException {
        execute(connection, """
                insert into cabras (num_registro, nome, sexo, data_nascimento, status, usuario_id, capril_id)
                values ('%s', 'W2 Goat', 'FEMEA', date '2020-01-01', 'ATIVO', %d, %d)
                """.formatted(registration, userId, farmId));
    }

    private long goatId(Connection connection, String registration) throws SQLException {
        return queryLong(connection, "select id from cabras where num_registro = '%s'".formatted(registration));
    }

    private Flyway flyway() {
        return flyway(null);
    }

    private Flyway flyway(String target) {
        var configuration = Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .baselineOnMigrate(true);
        if (target != null) {
            configuration.target(target);
        }
        return configuration.load();
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    private void assertSqlFails(Connection connection, String sql) {
        assertThatThrownBy(() -> execute(connection, sql)).isInstanceOf(SQLException.class);
    }

    private void execute(Connection connection, String sql) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private long queryLong(Connection connection, String sql) throws SQLException {
        try (var statement = connection.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
            if (!rs.next()) {
                throw new SQLException("query returned no rows: " + sql);
            }
            return rs.getLong(1);
        }
    }

    private String queryString(Connection connection, String sql) throws SQLException {
        try (var statement = connection.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
            if (!rs.next()) {
                throw new SQLException("query returned no rows: " + sql);
            }
            return rs.getString(1);
        }
    }

    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        try (var statement = connection.prepareStatement("""
                select 1 from information_schema.tables
                where table_schema = 'public' and table_name = ?
                """)) {
            statement.setString(1, tableName);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    private String indexDefinition(Connection connection, String indexName) throws SQLException {
        try (var statement = connection.prepareStatement("""
                select indexdef from pg_indexes
                where schemaname = 'public' and indexname = ?
                """)) {
            statement.setString(1, indexName);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getString(1) : "";
            }
        }
    }

    private boolean uniqueConstraintExists(Connection connection, String constraintName) throws SQLException {
        try (var statement = connection.prepareStatement("""
                select 1 from information_schema.table_constraints
                where table_schema = 'public' and constraint_name = ? and constraint_type = 'UNIQUE'
                """)) {
            statement.setString(1, constraintName);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean foreignKeyExists(Connection connection, String constraintName) throws SQLException {
        try (var statement = connection.prepareStatement("""
                select 1 from information_schema.table_constraints
                where table_schema = 'public' and constraint_name = ? and constraint_type = 'FOREIGN KEY'
                """)) {
            statement.setString(1, constraintName);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }
}
