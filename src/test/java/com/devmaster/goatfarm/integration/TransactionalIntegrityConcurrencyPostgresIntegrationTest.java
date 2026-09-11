package com.devmaster.goatfarm.integration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
class TransactionalIntegrityConcurrencyPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            System.getProperty("caprigestor.test.postgres.image", "postgres:16-alpine"));

    @BeforeAll
    static void migrateSchema() {
        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load()
                .migrate();
    }

    @BeforeEach
    void resetData() throws SQLException {
        try (Connection connection = openConnection()) {
            execute(connection, "truncate table operational_audit_entry, goat_registration_history, "
                    + "reproductive_event, pregnancy, milk_production, lactation, cabras, capril, users "
                    + "restart identity cascade");
            execute(connection, "insert into users (id, name, email, password, cpf) "
                    + "values (1, 'Concurrency User', 'concurrency@example.com', 'password', '00000000001')");
            execute(connection, "insert into capril (id, name, user_id, version) "
                    + "values (1, 'Concurrency Farm', 1, 0)");
        }
    }

    @Test
    void concurrentGoatCreationWithSameRegistrationPersistsExactlyOneGoat() throws Exception {
        List<Outcome> outcomes = runConcurrently(
                connection -> insertGoat(connection, "1615300100", "First Goat"),
                connection -> insertGoat(connection, "1615300100", "Second Goat")
        );

        assertSingleWinnerAndUniqueViolation(outcomes, "uk_cabras_farm_registration");
        try (Connection connection = openConnection()) {
            assertThat(queryLong(connection,
                    "select count(*) from cabras where num_registro = '1615300100'"))
                    .isEqualTo(1L);
            assertThat(queryLong(connection, "select count(*) from cabras")).isEqualTo(1L);
        }
    }

    @Test
    void concurrentRegistrationRectificationRollsBackLosingHistoryAndAudit() throws Exception {
        long firstGoatId;
        long secondGoatId;
        try (Connection connection = openConnection()) {
            firstGoatId = insertGoat(connection, "1615300101", "First Identity");
            secondGoatId = insertGoat(connection, "1615300102", "Second Identity");
        }

        List<Outcome> outcomes = runConcurrently(
                connection -> rectifyRegistration(connection, firstGoatId, "1615300101", "1615300199"),
                connection -> rectifyRegistration(connection, secondGoatId, "1615300102", "1615300199")
        );

        assertSingleWinnerAndUniqueViolation(outcomes, "uk_cabras_farm_registration");
        try (Connection connection = openConnection()) {
            assertThat(queryLong(connection,
                    "select count(*) from cabras where num_registro = '1615300199'"))
                    .isEqualTo(1L);
            assertThat(queryLong(connection,
                    "select count(*) from cabras where num_registro in ('1615300101', '1615300102')"))
                    .isEqualTo(1L);
            assertThat(queryLong(connection,
                    "select count(*) from cabras where id in (%d, %d)".formatted(firstGoatId, secondGoatId)))
                    .isEqualTo(2L);
            assertThat(queryLong(connection,
                    "select count(*) from goat_registration_history where new_registration_number = '1615300199'"))
                    .isEqualTo(1L);
            assertThat(queryLong(connection,
                    "select count(*) from operational_audit_entry "
                            + "where action_type = 'GOAT_REGISTRATION_RECTIFIED' "
                            + "and goat_registration_number = '1615300199'"))
                    .isEqualTo(1L);
        }
    }

    @Test
    void concurrentPregnancyConfirmationKeepsOneActivePregnancyAndOneCheckEvent() throws Exception {
        long goatId;
        try (Connection connection = openConnection()) {
            goatId = insertGoat(connection, "1615300200", "Pregnancy Goat");
        }

        List<Outcome> outcomes = runConcurrently(
                connection -> confirmPregnancy(connection, goatId, "First check"),
                connection -> confirmPregnancy(connection, goatId, "Second check")
        );

        assertSingleWinnerAndUniqueViolation(outcomes, "ux_pregnancy_single_active_per_goat");
        try (Connection connection = openConnection()) {
            assertThat(queryLong(connection,
                    "select count(*) from pregnancy where farm_id = 1 and goat_technical_id = " + goatId
                            + " and status = 'ACTIVE'"))
                    .isEqualTo(1L);
            assertThat(queryLong(connection,
                    "select count(*) from reproductive_event where farm_id = 1 and goat_technical_id = "
                            + goatId + " and event_type = 'PREGNANCY_CHECK'"))
                    .isEqualTo(1L);
        }
    }

    @Test
    void concurrentLactationOpeningKeepsExactlyOneActiveLactation() throws Exception {
        long goatId;
        try (Connection connection = openConnection()) {
            goatId = insertGoat(connection, "1615300300", "Lactation Goat");
        }

        List<Outcome> outcomes = runConcurrently(
                connection -> openLactation(connection, goatId, "2026-01-01"),
                connection -> openLactation(connection, goatId, "2026-01-02")
        );

        assertSingleWinnerAndUniqueViolation(outcomes, "ux_lactation_single_active_per_goat_technical");
        try (Connection connection = openConnection()) {
            assertThat(queryLong(connection,
                    "select count(*) from lactation where farm_id = 1 and goat_technical_id = " + goatId
                            + " and status = 'ACTIVE'"))
                    .isEqualTo(1L);
        }
    }

    private long insertGoat(Connection connection, String registrationNumber, String name) throws SQLException {
        try (var statement = connection.prepareStatement(
                "insert into cabras (num_registro, nome, sexo, data_nascimento, status, tod, toe, usuario_id, capril_id) "
                        + "values (?, ?, 'FEMEA', date '2024-01-01', 'ATIVO', ?, ?, 1, 1) returning id")) {
            statement.setString(1, registrationNumber);
            statement.setString(2, name);
            statement.setString(3, registrationNumber.substring(0, 5));
            statement.setString(4, registrationNumber.substring(5));
            try (ResultSet resultSet = statement.executeQuery()) {
                assertThat(resultSet.next()).isTrue();
                return resultSet.getLong(1);
            }
        }
    }

    private void rectifyRegistration(Connection connection, long goatId, String oldRegistration,
                                     String newRegistration) throws SQLException {
        try (var update = connection.prepareStatement(
                "update cabras set num_registro = ?, tod = ?, toe = ? where id = ?")) {
            update.setString(1, newRegistration);
            update.setString(2, newRegistration.substring(0, 5));
            update.setString(3, newRegistration.substring(5));
            update.setLong(4, goatId);
            assertThat(update.executeUpdate()).isEqualTo(1);
        }
        try (var history = connection.prepareStatement(
                "insert into goat_registration_history "
                        + "(goat_id, farm_id, old_registration_number, old_tod, old_toe, "
                        + "new_registration_number, new_tod, new_toe, source, evidence_reference, reason, actor_user_id) "
                        + "values (?, 1, ?, ?, ?, ?, ?, ?, 'OFFICIAL_DOCUMENT', 'CONCURRENCY-TEST', "
                        + "'Concurrent rectification test', 1)")) {
            history.setLong(1, goatId);
            history.setString(2, oldRegistration);
            history.setString(3, oldRegistration.substring(0, 5));
            history.setString(4, oldRegistration.substring(5));
            history.setString(5, newRegistration);
            history.setString(6, newRegistration.substring(0, 5));
            history.setString(7, newRegistration.substring(5));
            history.executeUpdate();
        }
        try (var audit = connection.prepareStatement(
                "insert into operational_audit_entry "
                        + "(farm_id, goat_technical_id, goat_registration_number, action_type, target_id, "
                        + "actor_user_id, actor_name, actor_email, description) "
                        + "values (1, ?, ?, 'GOAT_REGISTRATION_RECTIFIED', ?, 1, "
                        + "'Concurrency User', 'concurrency@example.com', 'Concurrent rectification test')")) {
            audit.setLong(1, goatId);
            audit.setString(2, newRegistration);
            audit.setString(3, newRegistration);
            audit.executeUpdate();
        }
    }

    private void confirmPregnancy(Connection connection, long goatId, String notes) throws SQLException {
        long eventId;
        try (var event = connection.prepareStatement(
                "insert into reproductive_event "
                        + "(farm_id, goat_id, goat_technical_id, event_type, event_date, check_result, notes, created_at, updated_at) "
                        + "values (1, '1615300200', ?, 'PREGNANCY_CHECK', date '2026-01-01', 'POSITIVE', ?, now(), now()) "
                        + "returning id")) {
            event.setLong(1, goatId);
            event.setString(2, notes);
            try (ResultSet resultSet = event.executeQuery()) {
                assertThat(resultSet.next()).isTrue();
                eventId = resultSet.getLong(1);
            }
        }
        try (var pregnancy = connection.prepareStatement(
                "insert into pregnancy "
                        + "(farm_id, goat_id, goat_technical_id, status, breeding_date, confirm_date, "
                        + "expected_due_date, coverage_event_id, notes, created_at, updated_at) "
                        + "values (1, '1615300200', ?, 'ACTIVE', date '2025-11-01', date '2026-01-01', "
                        + "date '2026-04-01', null, ?, now(), now())")) {
            pregnancy.setLong(1, goatId);
            pregnancy.setString(2, notes + " event=" + eventId);
            pregnancy.executeUpdate();
        }
    }

    private void openLactation(Connection connection, long goatId, String startDate) throws SQLException {
        try (var statement = connection.prepareStatement(
                "insert into lactation (farm_id, goat_id, goat_technical_id, status, start_date) "
                        + "values (1, '1615300300', ?, 'ACTIVE', cast(? as date))")) {
            statement.setLong(1, goatId);
            statement.setString(2, startDate);
            statement.executeUpdate();
        }
    }

    private List<Outcome> runConcurrently(SqlWork first, SqlWork second) throws Exception {
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Outcome> firstFuture = executor.submit(transaction(ready, start, first));
            Future<Outcome> secondFuture = executor.submit(transaction(ready, start, second));
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            return List.of(firstFuture.get(30, TimeUnit.SECONDS), secondFuture.get(30, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }
    }

    private Callable<Outcome> transaction(CountDownLatch ready, CountDownLatch start, SqlWork work) {
        return () -> {
            try (Connection connection = openConnection()) {
                connection.setAutoCommit(false);
                ready.countDown();
                if (!start.await(10, TimeUnit.SECONDS)) {
                    connection.rollback();
                    return Outcome.failed(null, "Start barrier timed out");
                }
                try {
                    work.execute(connection);
                    connection.commit();
                    return Outcome.succeeded();
                } catch (SQLException exception) {
                    connection.rollback();
                    return Outcome.failed(exception.getSQLState(), exception.getMessage());
                }
            }
        };
    }

    private void assertSingleWinnerAndUniqueViolation(List<Outcome> outcomes, String constraintName) {
        assertThat(outcomes).filteredOn(Outcome::success).hasSize(1);
        assertThat(outcomes).filteredOn(outcome -> !outcome.success()).singleElement().satisfies(loser -> {
            assertThat(loser.sqlState()).isEqualTo("23505");
            assertThat(loser.message()).contains(constraintName);
        });
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
        try (var statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            assertThat(resultSet.next()).isTrue();
            return resultSet.getLong(1);
        }
    }

    @FunctionalInterface
    private interface SqlWork {
        void execute(Connection connection) throws SQLException;
    }

    private record Outcome(boolean success, String sqlState, String message) {
        static Outcome succeeded() {
            return new Outcome(true, null, null);
        }

        static Outcome failed(String sqlState, String message) {
            return new Outcome(false, sqlState, message);
        }
    }
}
