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
class GoatOwnershipBaselineFlywayPostgresIntegrationTest {

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
    void freshV1ToV48SucceedsWithEmptyLegacyDatabase() throws SQLException {
        flyway("48").migrate();

        try (Connection connection = openConnection()) {
            assertThat(queryString(connection,
                    "select version from flyway_schema_history order by installed_rank desc limit 1"))
                    .isEqualTo("48");
            assertThat(queryLong(connection, "select count(*) from goat_ownership_period")).isZero();
            assertThat(queryLong(connection, "select count(*) from ownership_transfer")).isZero();
        }
    }

    @Test
    void backfillsLegacyOwnershipWithoutInferringCreatorsOrTransfers() throws SQLException {
        flyway("47").migrate();

        try (Connection connection = openConnection()) {
            seedUsersAndFarms(connection);
            insertGoat(connection, "W5-ACTIVE", "ATIVO", null, null, 202L, "2020-01-02");
            insertGoat(connection, "W5-INACTIVE", "INATIVO", null, null, 101L, "2020-01-03");
            insertGoat(connection, "W5-SALE", "INATIVO", "VENDA", "2026-02-10", 101L, "2020-01-04");
            insertGoat(connection, "W5-DEATH", "INATIVO", "MORTE", "2026-02-11", 101L, "2020-01-05");
            insertGoat(connection, "W5-RETIREMENT", "INATIVO", "DESCARTE", "2026-02-12", 101L, "2020-01-06");
            insertGoat(connection, "W5-DONATION", "INATIVO", "DOACAO", "2026-02-13", 101L, "2020-01-07");
            insertGoat(connection, "W5-TRANSFER", "INATIVO", "TRANSFERENCIA", "2026-02-14", 101L, "2020-01-08");

            long activeId = goatId(connection, "W5-ACTIVE");
            insertCreatorReference(connection, activeId);
            insertHistoricalRows(connection, activeId);
            long pregnancyFarmBefore = queryLong(connection,
                    "select farm_id from pregnancy where goat_technical_id = " + activeId);
            long auditFarmBefore = queryLong(connection,
                    "select farm_id from operational_audit_entry where goat_technical_id = " + activeId);
        }

        flyway().migrate();

        try (Connection connection = openConnection()) {
            assertThat(queryLong(connection, "select count(*) from goat_ownership_period")).isEqualTo(7L);
            long activeId = goatId(connection, "W5-ACTIVE");
            assertBaseline(connection, "W5-ACTIVE", 202, true, null);
            assertBaseline(connection, "W5-INACTIVE", 101, true, null);
            assertBaseline(connection, "W5-SALE", 101, false, "EXTERNAL_SALE");
            assertBaseline(connection, "W5-DEATH", 101, false, "DEATH");
            assertBaseline(connection, "W5-RETIREMENT", 101, false, "RETIREMENT");
            assertBaseline(connection, "W5-DONATION", 101, false, "DONATION");
            assertBaseline(connection, "W5-TRANSFER", 101, false, "TRANSFER_OUT");
            assertThat(queryLong(connection, "select count(*) from ownership_transfer")).isZero();
            assertThat(queryLong(connection, "select count(*) from goat_creator_reference")).isEqualTo(1L);
            assertThat(queryString(connection, "select creator_tod from goat_creator_reference where goat_id = "
                    + activeId)).isEqualTo("11111");
            assertThat(queryLong(connection, "select creator_farm_id from goat_creator_reference where goat_id = "
                    + activeId)).isEqualTo(101L);
            assertThat(queryString(connection, "select source from goat_creator_reference where goat_id = "
                    + activeId)).isEqualTo("MANUAL_DECLARATION");

            assertThat(queryLong(connection,
                    "select farm_id from pregnancy where goat_technical_id = " + activeId)).isEqualTo(101L);
            assertThat(queryLong(connection,
                    "select farm_id from operational_audit_entry where goat_technical_id = " + activeId)).isEqualTo(101L);
            assertThat(queryString(connection,
                    "select started_at at time zone 'America/Sao_Paulo' from goat_ownership_period where goat_id = "
                            + activeId)).startsWith("2020-01-02 00:00:00");
            assertThat(queryString(connection,
                    "select ended_at at time zone 'America/Sao_Paulo' from goat_ownership_period where goat_id = "
                            + goatId(connection, "W5-SALE"))).startsWith("2026-02-11 00:00:00");
        }
    }

    @Test
    void rejectsAmbiguousLegacyExitBeforeInsertingAnyBaseline() throws SQLException {
        flyway("47").migrate();
        try (Connection connection = openConnection()) {
            seedUsersAndFarms(connection);
            insertGoat(connection, "W5-PARTIAL", "INATIVO", "VENDA", null, 101L, "2020-01-01");
        }

        assertThatThrownBy(() -> flyway().migrate())
                .hasMessageContaining("V48: legacy exit_type and exit_date must be supplied together");
        try (Connection connection = openConnection()) {
            assertThat(queryLong(connection, "select count(*) from goat_ownership_period")).isZero();
        }
    }

    @Test
    void rejectsMissingFarmAndDirtyOwnershipState() throws SQLException {
        flyway("47").migrate();
        try (Connection connection = openConnection()) {
            seedUsersAndFarms(connection);
            // Simulate a legacy schema drift so the V48 fail-closed precheck
            // is exercised even though the nominal column is NOT NULL.
            execute(connection, "alter table cabras alter column capril_id drop not null");
            insertGoat(connection, "W5-NO-FARM", "ATIVO", null, null, null, "2020-01-01");
        }
        assertThatThrownBy(() -> flyway().migrate())
                .hasMessageContaining("V48: legacy goat has no farm");

        resetDatabase();
        flyway("47").migrate();
        try (Connection connection = openConnection()) {
            seedUsersAndFarms(connection);
            insertGoat(connection, "W5-DIRTY", "ATIVO", null, null, 101L, "2020-01-01");
            long goatId = goatId(connection, "W5-DIRTY");
            execute(connection, "insert into goat_ownership_period (goat_id, farm_id, started_at, entry_type, source) "
                    + "values (" + goatId + ", 101, current_timestamp, 'BIRTH', 'test')");
        }
        assertThatThrownBy(() -> flyway().migrate())
                .hasMessageContaining("V48: goat_ownership_period must be empty");
    }

    @Test
    void rejectsTerminalLegacyStatusesWithoutExitMetadata() throws SQLException {
        flyway("47").migrate();
        try (Connection connection = openConnection()) {
            seedUsersAndFarms(connection);
            insertGoat(connection, "W5-SOLD-WITHOUT-EXIT", "VENDIDO", null, null, 101L, "2020-01-01");
        }
        assertThatThrownBy(() -> flyway().migrate())
                .hasMessageContaining("V48: terminal legacy status requires exit_type and exit_date");

        resetDatabase();
        flyway("47").migrate();
        try (Connection connection = openConnection()) {
            seedUsersAndFarms(connection);
            insertGoat(connection, "W5-DEAD-WITHOUT-EXIT", "FALECIDO", null, null, 101L, "2020-01-01");
        }
        assertThatThrownBy(() -> flyway().migrate())
                .hasMessageContaining("V48: terminal legacy status requires exit_type and exit_date");
    }

    @Test
    void rejectsContradictoryTerminalStatusAndExitType() throws SQLException {
        flyway("47").migrate();
        try (Connection connection = openConnection()) {
            seedUsersAndFarms(connection);
            insertGoat(connection, "W5-SOLD-DEATH", "VENDIDO", "MORTE", "2026-02-10", 101L, "2020-01-01");
        }
        assertThatThrownBy(() -> flyway().migrate())
                .hasMessageContaining("V48: legacy status and exit_type contradict each other");
    }

    private void insertCreatorReference(Connection connection, long goatId) throws SQLException {
        execute(connection, "insert into goat_creator_reference "
                + "(goat_id, creator_tod, creator_farm_id, creator_name_snapshot, source, evidence_reference, recorded_at) "
                + "values (" + goatId + ", '11111', 101, 'W5 Creator', 'MANUAL_DECLARATION', 'W5 evidence', "
                + "timestamp with time zone '2026-01-01 00:00:00+00')");
    }

    private void assertBaseline(Connection connection, String registration, long farmId,
                                boolean open, String exitType) throws SQLException {
        long goatId = goatId(connection, registration);
        assertThat(queryLong(connection, "select count(*) from goat_ownership_period where goat_id = " + goatId))
                .isEqualTo(1L);
        assertThat(queryLong(connection, "select farm_id from goat_ownership_period where goat_id = " + goatId))
                .isEqualTo(farmId);
        assertThat(queryString(connection, "select entry_type from goat_ownership_period where goat_id = " + goatId))
                .isEqualTo("MANUAL_IMPORT");
        assertThat(queryString(connection, "select source from goat_ownership_period where goat_id = " + goatId))
                .isEqualTo("LEGACY_BACKFILL");
        if (open) {
            assertThat(queryLong(connection, "select count(*) from goat_ownership_period where goat_id = " + goatId
                    + " and ended_at is null and exit_type is null")).isEqualTo(1L);
        } else {
            assertThat(queryString(connection, "select exit_type from goat_ownership_period where goat_id = " + goatId))
                    .isEqualTo(exitType);
            assertThat(queryLong(connection, "select count(*) from goat_ownership_period where goat_id = " + goatId
                    + " and ended_at is not null")).isEqualTo(1L);
        }
    }

    private void insertHistoricalRows(Connection connection, long goatId) throws SQLException {
        execute(connection, "insert into pregnancy (farm_id, goat_id, goat_technical_id, status, created_at, updated_at) "
                + "values (101, 'W5-ACTIVE', " + goatId + ", 'CLOSED', current_timestamp, current_timestamp)");
        execute(connection, "insert into operational_audit_entry "
                + "(farm_id, goat_registration_number, goat_technical_id, action_type, target_id, actor_user_id, actor_name, actor_email, description) "
                + "values (101, 'W5-ACTIVE', " + goatId + ", 'UPDATE', 'W5', 1, 'W5 User', 'w5@example.com', 'baseline test')");
    }

    private void seedUsersAndFarms(Connection connection) throws SQLException {
        execute(connection, "insert into users (id, name, email, password, cpf) values "
                + "(1, 'W5 User', 'w5@example.com', 'password', '00000000001')");
        execute(connection, "insert into capril (id, name, user_id, tod) values "
                + "(101, 'W5 Historical Farm', 1, '11111'), (202, 'W5 Current Farm', 1, '22222')");
    }

    private void insertGoat(Connection connection, String registration, String status, String exitType,
                            String exitDate, Long farmId, String birthDate) throws SQLException {
        String exitTypeSql = exitType == null ? "null" : "'" + exitType + "'";
        String exitDateSql = exitDate == null ? "null" : "date '" + exitDate + "'";
        String farmSql = farmId == null ? "null" : farmId.toString();
        execute(connection, "insert into cabras (num_registro, nome, sexo, data_nascimento, status, usuario_id, capril_id, exit_type, exit_date) "
                + "values ('" + registration + "', 'W5 Goat', 'FEMEA', date '" + birthDate + "', '" + status
                + "', 1, " + farmSql + ", " + exitTypeSql + ", " + exitDateSql + ")");
    }

    private long goatId(Connection connection, String registration) throws SQLException {
        return queryLong(connection, "select id from cabras where num_registro = '" + registration + "'");
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
        return DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
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
}
