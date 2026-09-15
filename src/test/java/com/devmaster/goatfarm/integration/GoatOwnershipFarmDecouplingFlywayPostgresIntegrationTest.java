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
class GoatOwnershipFarmDecouplingFlywayPostgresIntegrationTest {

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
    void freshV1ToV47CreatesGlobalGoatIdentityAndValidatedConstraints() throws SQLException {
        flyway("47").migrate();

        try (Connection connection = openConnection()) {
            assertThat(queryString(connection,
                    "select version from flyway_schema_history order by installed_rank desc limit 1"))
                    .isEqualTo("47");
            assertConstraintState(connection);
            assertThat(indexDefinition(connection, "ux_pregnancy_single_active_per_goat_technical"))
                    .contains("(goat_technical_id)")
                    .doesNotContain("farm_id");
            assertThat(indexDefinition(connection, "ux_lactation_single_active_per_goat_technical"))
                    .contains("(goat_technical_id)")
                    .doesNotContain("farm_id");
            assertThat(indexExists(connection, "ux_pregnancy_single_active_per_goat")).isFalse();
        }
    }

    @Test
    void upgradeFromV46ToV47PreservesRowsAndDoesNotBackfillOwnership() throws SQLException {
        flyway("46").migrate();

        try (Connection connection = openConnection()) {
            seedUsersAndFarms(connection);
            insertGoat(connection, "W4-UPGRADE-GOAT", 1, 101);
            long goatId = goatId(connection, "W4-UPGRADE-GOAT");
            insertPregnancy(connection, 101, goatId, "W4-UPGRADE-GOAT", "CLOSED");
            assertThat(queryLong(connection, "select count(*) from cabras")).isEqualTo(1L);
            assertThat(queryLong(connection, "select count(*) from pregnancy")).isEqualTo(1L);
        }

        flyway("47").migrate();

        try (Connection connection = openConnection()) {
            assertThat(queryString(connection,
                    "select version from flyway_schema_history order by installed_rank desc limit 1"))
                    .isEqualTo("47");
            assertThat(queryLong(connection, "select count(*) from cabras")).isEqualTo(1L);
            assertThat(queryLong(connection, "select count(*) from pregnancy")).isEqualTo(1L);
            assertThat(queryLong(connection, "select count(*) from goat_creator_reference")).isZero();
            assertThat(queryLong(connection, "select count(*) from goat_ownership_period")).isZero();
            assertThat(queryLong(connection, "select count(*) from ownership_transfer")).isZero();
            assertConstraintState(connection);
        }
    }

    @Test
    void historicalRecordsRemainValidWhenGoatMovesAcrossFarms() throws SQLException {
        flyway("47").migrate();

        try (Connection connection = openConnection()) {
            seedUsersAndFarms(connection);
            insertGoat(connection, "W4-CROSS-FARM-GOAT", 1, 101);
            long goatId = goatId(connection, "W4-CROSS-FARM-GOAT");
            long pregnancyId = insertPregnancy(connection, 101, goatId, "W4-CROSS-FARM-GOAT", "CLOSED");
            long reproductiveEventA = insertReproductiveEvent(connection, 101, goatId,
                    "W4-CROSS-FARM-GOAT", pregnancyId, null);
            long healthEvent = insertHealthEvent(connection, 101, goatId, "W4-CROSS-FARM-GOAT");
            long lactationId = insertLactation(connection, 101, goatId, 12345, "DRY");
            insertMilkProduction(connection, 101, goatId, 12345, lactationId, healthEvent);
            long customerA = insertCustomer(connection, 101, "Farm A Customer");
            insertAnimalSale(connection, 101, goatId, "W4-CROSS-FARM-GOAT", customerA);
            insertAudit(connection, 101, goatId, "W4-CROSS-FARM-GOAT");

            execute(connection, "update cabras set capril_id = 102 where id = " + goatId);

            // Historical farm context remains Farm A while the Goat is now in Farm B.
            assertThat(queryLong(connection,
                    "select count(*) from pregnancy where farm_id = 101 and goat_technical_id = " + goatId))
                    .isEqualTo(1L);
            assertThat(queryLong(connection,
                    "select count(*) from reproductive_event where farm_id = 101 and goat_technical_id = " + goatId))
                    .isEqualTo(1L);
            assertThat(queryLong(connection,
                    "select count(*) from health_events where farm_id = 101 and goat_technical_id = " + goatId))
                    .isEqualTo(1L);
            assertThat(queryLong(connection,
                    "select count(*) from lactation where farm_id = 101 and goat_technical_id = " + goatId))
                    .isEqualTo(1L);
            assertThat(queryLong(connection,
                    "select count(*) from milk_production where farm_id = 101 and goat_technical_id = " + goatId))
                    .isEqualTo(1L);
            assertThat(queryLong(connection,
                    "select count(*) from animal_sale where farm_id = 101 and goat_technical_id = " + goatId))
                    .isEqualTo(1L);
            assertThat(queryLong(connection,
                    "select count(*) from operational_audit_entry where farm_id = 101 and goat_technical_id = " + goatId))
                    .isEqualTo(1L);
            assertThat(queryLong(connection, "select capril_id from cabras where id = " + goatId))
                    .isEqualTo(102L);
        }
    }

    @Test
    void biologicalProcessesCanContinueAcrossOwnershipBoundary() throws SQLException {
        flyway("47").migrate();

        try (Connection connection = openConnection()) {
            seedUsersAndFarms(connection);
            insertGoat(connection, "W4-CONTINUITY-GOAT", 1, 101);
            long goatId = goatId(connection, "W4-CONTINUITY-GOAT");
            long pregnancyId = insertPregnancy(connection, 101, goatId, "W4-CONTINUITY-GOAT", "CLOSED");
            long eventA = insertReproductiveEvent(connection, 101, goatId,
                    "W4-CONTINUITY-GOAT", pregnancyId, null);
            long eventB = insertReproductiveEvent(connection, 102, goatId,
                    "W4-CONTINUITY-GOAT", pregnancyId, eventA);
            execute(connection, "update pregnancy set coverage_event_id = " + eventB
                    + " where id = " + pregnancyId);
            assertThat(queryLong(connection,
                    "select count(*) from reproductive_event where id = " + eventB
                            + " and farm_id = 102 and pregnancy_id = " + pregnancyId
                            + " and related_event_id = " + eventA)).isEqualTo(1L);

            long healthEvent = insertHealthEvent(connection, 101, goatId, "W4-CONTINUITY-GOAT");
            long lactationId = insertLactation(connection, 101, goatId, 12345, "DRY");
            insertMilkProduction(connection, 102, goatId, 12345, lactationId, healthEvent);
            assertThat(queryLong(connection,
                    "select count(*) from milk_production where farm_id = 102 and goat_technical_id = "
                            + goatId + " and lactation_id = " + lactationId
                            + " and milk_withdrawal_event_id = " + healthEvent)).isEqualTo(1L);
        }
    }

    @Test
    void activePregnancyAndLactationAreGlobalPerGoat() throws SQLException {
        flyway("47").migrate();

        try (Connection connection = openConnection()) {
            seedUsersAndFarms(connection);
            insertGoat(connection, "W4-ACTIVE-GOAT", 1, 101);
            long goatId = goatId(connection, "W4-ACTIVE-GOAT");
            insertPregnancy(connection, 101, goatId, "W4-ACTIVE-GOAT", "ACTIVE");
            assertThatThrownBy(() -> insertPregnancy(connection, 102, goatId, "W4-ACTIVE-GOAT", "ACTIVE"))
                    .isInstanceOf(SQLException.class);
            long dryLactation = insertLactation(connection, 101, goatId, 12345, "DRY");
            insertLactation(connection, 101, goatId, 12345, "ACTIVE");
            assertThat(dryLactation).isPositive();
            assertThatThrownBy(() -> insertLactation(connection, 102, goatId, 12345, "ACTIVE"))
                    .isInstanceOf(SQLException.class);
        }
    }

    @Test
    void v49EnforcesGlobalActiveMilkIdentityAcrossFarmsAndAllowsCanceledReplacement() throws SQLException {
        flyway("49").migrate();

        try (Connection connection = openConnection()) {
            seedUsersAndFarms(connection);
            insertGoat(connection, "W12-GLOBAL-MILK-A", 1, 101);
            insertGoat(connection, "W12-GLOBAL-MILK-B", 2, 102);
            long goatA = goatId(connection, "W12-GLOBAL-MILK-A");
            long goatB = goatId(connection, "W12-GLOBAL-MILK-B");
            long lactationA = insertLactation(connection, 101, goatA, 12345, "ACTIVE");
            long lactationB = insertLactation(connection, 102, goatB, 67890, "ACTIVE");

            execute(connection, """
                    insert into milk_production
                        (farm_id, goat_id, goat_technical_id, lactation_id, date, shift,
                         volume_liters, status)
                    values (101, 'W12-GLOBAL-MILK-A', %d, %d, date '2026-01-10', 'MORNING', 2.50, 'ACTIVE')
                    """.formatted(goatA, lactationA));

            // The same biological GoatId/date/shift is rejected even when a
            // caller supplies another farm and a different RG snapshot.
            assertThatThrownBy(() -> execute(connection, """
                    insert into milk_production
                        (farm_id, goat_id, goat_technical_id, lactation_id, date, shift,
                         volume_liters, status)
                    values (102, 'W12-GLOBAL-MILK-B-RG-SNAPSHOT', %d, %d,
                            date '2026-01-10', 'MORNING', 3.00, 'ACTIVE')
                    """.formatted(goatA, lactationA)))
                    .isInstanceOf(SQLException.class)
                    .hasMessageContaining("ux_milk_production_active_goat_technical_daily_shift");

            // Canceled history does not occupy the active identity slot and
            // can be replaced by a new active row for the same key.
            execute(connection, """
                    insert into milk_production
                        (farm_id, goat_id, goat_technical_id, lactation_id, date, shift,
                         volume_liters, status)
                    values (101, 'W12-GLOBAL-MILK-A', %d, %d, date '2026-01-11', 'MORNING', 1.00, 'CANCELED')
                    """.formatted(goatA, lactationA));
            execute(connection, """
                    insert into milk_production
                        (farm_id, goat_id, goat_technical_id, lactation_id, date, shift,
                         volume_liters, status)
                    values (102, 'W12-GLOBAL-MILK-A-NEW-RG', %d, %d, date '2026-01-11', 'MORNING', 1.25, 'ACTIVE')
                    """.formatted(goatA, lactationA));

            // Different shift/date and a different GoatId remain valid.
            execute(connection, """
                    insert into milk_production
                        (farm_id, goat_id, goat_technical_id, lactation_id, date, shift,
                         volume_liters, status)
                    values (101, 'W12-GLOBAL-MILK-A', %d, %d, date '2026-01-10', 'AFTERNOON', 2.00, 'ACTIVE')
                    """.formatted(goatA, lactationA));
            execute(connection, """
                    insert into milk_production
                        (farm_id, goat_id, goat_technical_id, lactation_id, date, shift,
                         volume_liters, status)
                    values (102, 'W12-GLOBAL-MILK-B', %d, %d, date '2026-01-10', 'MORNING', 2.00, 'ACTIVE')
                    """.formatted(goatB, lactationB));

            String definition = indexDefinition(connection,
                    "ux_milk_production_active_goat_technical_daily_shift").toLowerCase();
            assertThat(definition).contains("goat_technical_id", "date", "shift", "status", "active");
            assertThat(indexExists(connection, "ux_milk_production_active_daily_shift")).isFalse();
        }
    }

    @Test
    void unrelatedFarmCustomerIntegrityRemainsEnforced() throws SQLException {
        flyway("47").migrate();

        try (Connection connection = openConnection()) {
            seedUsersAndFarms(connection);
            insertGoat(connection, "W4-UNRELATED-GOAT", 1, 101);
            long goatId = goatId(connection, "W4-UNRELATED-GOAT");
            long farmBCustomer = insertCustomer(connection, 102, "Farm B Customer");
            assertThatThrownBy(() -> insertAnimalSale(connection, 101, goatId,
                    "W4-UNRELATED-GOAT", farmBCustomer)).isInstanceOf(SQLException.class);
        }
    }

    @Test
    void directGoatAndProcessForeignKeysRejectUnknownIdentities() throws SQLException {
        flyway("47").migrate();

        try (Connection connection = openConnection()) {
            seedUsersAndFarms(connection);
            insertGoat(connection, "W4-FK-GOAT", 1, 101);
            long goatId = goatId(connection, "W4-FK-GOAT");
            assertThatThrownBy(() -> insertPregnancy(connection, 101, 999999L,
                    "W4-FK-GOAT", "CLOSED")).isInstanceOf(SQLException.class);
            assertThatThrownBy(() -> insertReproductiveEvent(connection, 101, goatId,
                    "W4-FK-GOAT", 0L, 999999L)).isInstanceOf(SQLException.class);
            assertThatThrownBy(() -> execute(connection, """
                    insert into pregnancy
                        (farm_id, goat_id, goat_technical_id, status, coverage_event_id,
                         created_at, updated_at)
                    values (101, 'W4-FK-GOAT', %d, 'CLOSED', 999999, current_timestamp, current_timestamp)
                    """.formatted(goatId))).isInstanceOf(SQLException.class);
            long lactationId = insertLactation(connection, 101, goatId, 12345, "DRY");
            assertThatThrownBy(() -> insertMilkProduction(connection, 101, goatId, 12345,
                    lactationId, 999999L)).isInstanceOf(SQLException.class);
        }
    }

    @Test
    void processForeignKeysRejectCrossGoatReferences() throws SQLException {
        flyway("47").migrate();

        try (Connection connection = openConnection()) {
            seedUsersAndFarms(connection);
            insertGoat(connection, "W4-PROCESS-GOAT-A", 1, 101);
            insertGoat(connection, "W4-PROCESS-GOAT-B", 2, 102);
            long goatA = goatId(connection, "W4-PROCESS-GOAT-A");
            long goatB = goatId(connection, "W4-PROCESS-GOAT-B");
            long pregnancyA = insertPregnancy(connection, 101, goatA,
                    "W4-PROCESS-GOAT-A", "CLOSED");
            long eventA = insertReproductiveEvent(connection, 101, goatA,
                    "W4-PROCESS-GOAT-A", pregnancyA, null);
            long healthA = insertHealthEvent(connection, 101, goatA,
                    "W4-PROCESS-GOAT-A");
            long lactationA = insertLactation(connection, 101, goatA, 12345,
                    "DRY");
            long lactationB = insertLactation(connection, 102, goatB, 12346,
                    "DRY");

            assertThatThrownBy(() -> execute(connection, """
                    insert into reproductive_event
                        (farm_id, goat_id, goat_technical_id, event_type, event_date,
                         pregnancy_id, created_at, updated_at)
                    values (102, 'W4-PROCESS-GOAT-B', %d, 'BIRTH', date '2026-02-01',
                            %d, current_timestamp, current_timestamp)
                    """.formatted(goatB, pregnancyA))).isInstanceOf(SQLException.class);
            assertThatThrownBy(() -> execute(connection, """
                    insert into reproductive_event
                        (farm_id, goat_id, goat_technical_id, event_type, event_date,
                         related_event_id, created_at, updated_at)
                    values (102, 'W4-PROCESS-GOAT-B', %d, 'COVERAGE_CORRECTION',
                            date '2026-02-01', %d, current_timestamp, current_timestamp)
                    """.formatted(goatB, eventA))).isInstanceOf(SQLException.class);
            assertThatThrownBy(() -> execute(connection, """
                    insert into pregnancy
                        (farm_id, goat_id, goat_technical_id, status, coverage_event_id,
                         created_at, updated_at)
                    values (102, 'W4-PROCESS-GOAT-B', %d, 'CLOSED', %d,
                            current_timestamp, current_timestamp)
                    """.formatted(goatB, eventA))).isInstanceOf(SQLException.class);
            assertThatThrownBy(() -> execute(connection, """
                    insert into milk_production
                        (farm_id, goat_id, goat_technical_id, lactation_id, date, shift,
                         volume_liters, status)
                    values (102, 'W4-PROCESS-GOAT-B', %d, %d, date '2026-02-01',
                            'MORNING', 2.50, 'ACTIVE')
                    """.formatted(goatB, lactationA))).isInstanceOf(SQLException.class);
            assertThatThrownBy(() -> execute(connection, """
                    insert into milk_production
                        (farm_id, goat_id, goat_technical_id, lactation_id, date, shift,
                         volume_liters, status, milk_withdrawal_event_id)
                    values (102, 'W4-PROCESS-GOAT-B', %d, %d, date '2026-02-02',
                            'MORNING', 2.50, 'ACTIVE', %d)
                    """.formatted(goatB, lactationB, healthA))).isInstanceOf(SQLException.class);
        }
    }

    private void assertConstraintState(Connection connection) throws SQLException {
        String[] removed = {
                "fk_pregnancy_farm_goat_technical",
                "fk_reproductive_event_farm_goat_technical",
                "fk_health_events_farm_goat_technical",
                "fk_lactation_farm_goat_technical",
                "fk_animal_sale_farm_goat_technical",
                "fk_operational_audit_entry_farm_goat_technical",
                "fk_milk_production_farm_goat_lactation",
                "fk_milk_production_farm_goat_technical_lactation",
                "fk_reproductive_event_farm_pregnancy",
                "fk_reproductive_event_farm_related_event",
                "fk_pregnancy_farm_coverage_event",
                "fk_milk_production_farm_withdrawal_event"
        };
        for (String constraint : removed) {
            assertThat(constraintExists(connection, constraint)).as(constraint).isFalse();
        }
        String[] direct = {
                "fk_pregnancy_goat_technical_direct",
                "fk_reproductive_event_goat_technical_direct",
                "fk_health_events_goat_technical_direct",
                "fk_lactation_goat_technical_direct",
                "fk_milk_production_goat_technical_direct",
                "fk_animal_sale_goat_technical_direct",
                "fk_operational_audit_entry_goat_technical_direct",
                "fk_reproductive_event_pregnancy",
                "fk_milk_production_lactation",
                "fk_reproductive_event_related_event_direct",
                "fk_pregnancy_coverage_event_direct",
                "fk_milk_production_withdrawal_event_direct"
        };
        for (String constraint : direct) {
            assertThat(constraintExists(connection, constraint)).as(constraint).isTrue();
            assertThat(constraintValidated(connection, constraint)).as(constraint + " validated").isTrue();
        }
        for (String candidateKey : new String[]{
                "uk_pregnancy_goat_technical_id",
                "uk_reproductive_event_goat_technical_id",
                "uk_health_events_goat_technical_id",
                "uk_lactation_goat_technical_id"}) {
            assertThat(constraintExists(connection, candidateKey)).as(candidateKey).isTrue();
        }
    }

    private long insertPregnancy(Connection connection, long farmId, long goatId,
                                 String registration, String status) throws SQLException {
        return queryLong(connection, """
                insert into pregnancy
                    (farm_id, goat_id, goat_technical_id, status, breeding_date,
                     created_at, updated_at)
                values (%d, '%s', %d, '%s', date '2026-01-01', current_timestamp, current_timestamp)
                returning id
                """.formatted(farmId, registration, goatId, status));
    }

    private long insertReproductiveEvent(Connection connection, long farmId, long goatId,
                                         String registration, long pregnancyId, Long relatedEventId)
            throws SQLException {
        String related = relatedEventId == null ? "null" : relatedEventId.toString();
        return queryLong(connection, """
                insert into reproductive_event
                    (farm_id, goat_id, goat_technical_id, event_type, event_date,
                     pregnancy_id, related_event_id, created_at, updated_at)
                values (%d, '%s', %d, 'BIRTH', date '2026-02-01', %d, %s,
                        current_timestamp, current_timestamp)
                returning id
                """.formatted(farmId, registration, goatId, pregnancyId, related));
    }

    private long insertHealthEvent(Connection connection, long farmId, long goatId,
                                   String registration) throws SQLException {
        return queryLong(connection, """
                insert into health_events
                    (farm_id, goat_id, goat_technical_id, type, status, title, scheduled_date)
                values (%d, '%s', %d, 'VACCINATION', 'COMPLETED', 'W4 health', date '2026-01-01')
                returning id
                """.formatted(farmId, registration, goatId));
    }

    private long insertLactation(Connection connection, long farmId, long goatId,
                                  long legacyGoatId, String status) throws SQLException {
        return queryLong(connection, """
                insert into lactation
                    (farm_id, goat_id, goat_technical_id, status, start_date, created_at, updated_at)
                values (%d, %d, %d, '%s', date '2026-01-01', current_timestamp, current_timestamp)
                returning id
                """.formatted(farmId, legacyGoatId, goatId, status));
    }

    private void insertMilkProduction(Connection connection, long farmId, long goatId,
                                      long legacyGoatId, long lactationId, long withdrawalEventId)
            throws SQLException {
        execute(connection, """
                insert into milk_production
                    (farm_id, goat_id, goat_technical_id, lactation_id, date, shift,
                     volume_liters, status, milk_withdrawal_event_id)
                values (%d, %d, %d, %d, date '2026-01-02', 'MORNING', 2.50, 'ACTIVE', %d)
                """.formatted(farmId, legacyGoatId, goatId, lactationId, withdrawalEventId));
    }

    private long insertCustomer(Connection connection, long farmId, String name) throws SQLException {
        return queryLong(connection, "insert into commercial_customer (farm_id, name) values ("
                + farmId + ", '" + name + "') returning id");
    }

    private void insertAnimalSale(Connection connection, long farmId, long goatId,
                                  String registration, long customerId) throws SQLException {
        execute(connection, """
                insert into animal_sale
                    (farm_id, customer_id, goat_registration_number, goat_technical_id,
                     goat_name, sale_date, amount, due_date, payment_status)
                values (%d, %d, '%s', %d, 'W4 goat', date '2026-02-01', 100.00,
                        date '2026-03-01', 'PENDING')
                """.formatted(farmId, customerId, registration, goatId));
    }

    private void insertAudit(Connection connection, long farmId, long goatId,
                              String registration) throws SQLException {
        execute(connection, """
                insert into operational_audit_entry
                    (farm_id, goat_registration_number, goat_technical_id, action_type,
                     target_id, actor_user_id, actor_name, actor_email, description)
                values (%d, '%s', %d, 'UPDATE', 'W4', 1, 'W4 User One',
                        'w4-one@example.com', 'W4 audit')
                """.formatted(farmId, registration, goatId));
    }

    private void seedUsersAndFarms(Connection connection) throws SQLException {
        execute(connection, """
                insert into users (id, name, email, password, cpf)
                values (1, 'W4 User One', 'w4-one@example.com', 'password', '00000000001'),
                       (2, 'W4 User Two', 'w4-two@example.com', 'password', '00000000002')
                """);
        execute(connection, """
                insert into capril (id, name, user_id, tod)
                values (101, 'W4 Farm A', 1, '11111'),
                       (102, 'W4 Farm B', 2, '22222')
                """);
    }

    private void insertGoat(Connection connection, String registration, long userId, long farmId)
            throws SQLException {
        execute(connection, """
                insert into cabras (num_registro, nome, sexo, data_nascimento, status, usuario_id, capril_id)
                values ('%s', 'W4 Goat', 'FEMEA', date '2020-01-01', 'ATIVO', %d, %d)
                """.formatted(registration, userId, farmId));
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

    private boolean constraintExists(Connection connection, String name) throws SQLException {
        try (var statement = connection.prepareStatement("""
                select 1 from information_schema.table_constraints
                where table_schema = 'public' and constraint_name = ?
                """)) {
            statement.setString(1, name);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean constraintValidated(Connection connection, String name) throws SQLException {
        try (var statement = connection.prepareStatement("""
                select convalidated from pg_constraint where conname = ?
                """)) {
            statement.setString(1, name);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        }
    }

    private boolean indexExists(Connection connection, String name) throws SQLException {
        return queryLong(connection, "select count(*) from pg_indexes where schemaname = 'public' and indexname = '"
                + name + "'") > 0;
    }

    private String indexDefinition(Connection connection, String name) throws SQLException {
        try (var statement = connection.prepareStatement("""
                select indexdef from pg_indexes where schemaname = 'public' and indexname = ?
                """)) {
            statement.setString(1, name);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getString(1) : "";
            }
        }
    }
}
