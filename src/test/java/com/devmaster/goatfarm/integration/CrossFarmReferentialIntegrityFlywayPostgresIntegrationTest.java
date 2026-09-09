package com.devmaster.goatfarm.integration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers(disabledWithoutDocker = true)
class CrossFarmReferentialIntegrityFlywayPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void databaseRejectsCrossFarmReferencesWhileAcceptingSameFarmReferences() throws SQLException {
        Flyway version37 = Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .target("37")
                .load();

        version37.migrate();

        try (Connection connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())) {
            seed(connection);
        }

        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load()
                .migrate();

        try (Connection connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())) {

            assertThatCode(() -> execute(connection, """
                    insert into animal_sale (farm_id, customer_id, goat_registration_number, goat_name, sale_date, amount, due_date, payment_status)
                    values (101, 201, 'G-101', 'Cabra 101', date '2026-01-01', 10, date '2026-01-01', 'PENDING')
                    """)).doesNotThrowAnyException();

            assertRejected(connection, """
                    insert into pregnancy (farm_id, goat_id, status, created_at, updated_at)
                    values (101, 'G-102', 'ACTIVE', now(), now())
                    """);
            assertRejected(connection, """
                    insert into reproductive_event (farm_id, goat_id, event_type, event_date, created_at, updated_at)
                    values (101, 'G-102', 'COVERAGE', date '2026-01-01', now(), now())
                    """);
            assertRejected(connection, """
                    insert into health_events (farm_id, goat_id, type, status, title, scheduled_date)
                    values (101, 'G-102', 'VACCINE', 'SCHEDULED', 'Cross farm', date '2026-01-01')
                    """);
            assertRejected(connection, """
                    insert into animal_sale (farm_id, customer_id, goat_registration_number, goat_name, sale_date, amount, due_date, payment_status)
                    values (101, 202, 'G-101', 'Cabra 101', date '2026-01-02', 10, date '2026-01-02', 'PENDING')
                    """);
            assertRejected(connection, """
                    insert into animal_sale (farm_id, customer_id, goat_registration_number, goat_name, sale_date, amount, due_date, payment_status)
                    values (101, 201, 'G-102', 'Cabra 102', date '2026-01-02', 10, date '2026-01-02', 'PENDING')
                    """);
            assertRejected(connection, """
                    insert into milk_sale (farm_id, customer_id, sale_date, quantity_liters, unit_price, total_amount, due_date, payment_status)
                    values (101, 202, date '2026-01-01', 1, 1, 1, date '2026-01-01', 'PENDING')
                    """);
            assertRejected(connection, """
                    insert into inventory_lot (farm_id, item_id, code, code_normalized, active)
                    values (101, 602, 'INVALID', 'invalid', true)
                    """);
            assertRejected(connection, """
                    insert into inventory_balance (farm_id, item_id, lot_id, quantity)
                    values (101, 602, null, 1)
                    """);
            assertRejected(connection, """
                    insert into inventory_balance (farm_id, item_id, lot_id, quantity)
                    values (101, 601, 702, 1)
                    """);
            assertRejected(connection, """
                    insert into inventory_movement (farm_id, type, quantity, item_id, lot_id, movement_date, resulting_balance, created_at)
                    values (101, 'ENTRY', 1, 601, 702, date '2026-01-01', 1, now())
                    """);
            assertRejected(connection, """
                    insert into lactation (farm_id, goat_id, status, start_date)
                    values (101, 'G-102', 'ACTIVE', date '2026-01-01')
                    """);
            assertRejected(connection, """
                    insert into milk_production (farm_id, goat_id, lactation_id, date, shift, volume_liters)
                    values (101, 'G-101', 802, date '2026-01-01', 'MORNING', 1)
                    """);
            assertRejected(connection, """
                    insert into milk_production (farm_id, goat_id, lactation_id, date, shift, volume_liters)
                    values (101, 'G-102', 801, date '2026-01-01', 'AFTERNOON', 1)
                    """);
            assertRejected(connection, """
                    insert into milk_production (farm_id, goat_id, lactation_id, date, shift, volume_liters, milk_withdrawal_event_id)
                    values (101, 'G-101', 801, date '2026-01-02', 'MORNING', 1, 502)
                    """);
            assertRejected(connection, """
                    insert into reproductive_event (farm_id, goat_id, event_type, event_date, pregnancy_id, created_at, updated_at)
                    values (101, 'G-101', 'PREGNANCY_CHECK', date '2026-01-01', 302, now(), now())
                    """);
            assertRejected(connection, """
                    insert into pregnancy (farm_id, goat_id, status, coverage_event_id, created_at, updated_at)
                    values (101, 'G-101', 'ACTIVE', 402, now(), now())
                    """);
            assertRejected(connection, """
                    insert into reproductive_event (farm_id, goat_id, event_type, event_date, related_event_id, created_at, updated_at)
                    values (101, 'G-101', 'COVERAGE_CORRECTION', date '2026-01-01', 402, now(), now())
                    """);
            assertRejected(connection, """
                    insert into operational_audit_entry (farm_id, goat_registration_number, action_type, actor_user_id, actor_name, actor_email, description)
                    values (101, 'G-102', 'TEST', 1, 'Test User', 'test@example.com', 'Cross farm')
                    """);
        }
    }

    private void seed(Connection connection) throws SQLException {
        execute(connection, """
                insert into users (id, name, email, password, cpf) values
                (1, 'Test User', 'test@example.com', 'password', '00000000000')
                """);
        execute(connection, """
                insert into capril (id, name, user_id) values
                (101, 'Farm 101', 1),
                (102, 'Farm 102', 1)
                """);
        execute(connection, """
                insert into cabras (num_registro, nome, sexo, data_nascimento, status, usuario_id, capril_id) values
                ('G-101', 'Cabra 101', 'FEMEA', date '2020-01-01', 'ATIVO', 1, 101),
                ('G-102', 'Cabra 102', 'FEMEA', date '2020-01-01', 'ATIVO', 1, 102)
                """);
        execute(connection, """
                insert into commercial_customer (id, farm_id, name) values
                (201, 101, 'Customer 101'),
                (202, 102, 'Customer 102')
                """);
        execute(connection, """
                insert into pregnancy (id, farm_id, goat_id, status, created_at, updated_at) values
                (301, 101, 'G-101', 'ACTIVE', now(), now()),
                (302, 102, 'G-102', 'ACTIVE', now(), now())
                """);
        execute(connection, """
                insert into reproductive_event (id, farm_id, goat_id, event_type, event_date, created_at, updated_at) values
                (401, 101, 'G-101', 'COVERAGE', date '2026-01-01', now(), now()),
                (402, 102, 'G-102', 'COVERAGE', date '2026-01-01', now(), now())
                """);
        execute(connection, """
                insert into health_events (id, farm_id, goat_id, type, status, title, scheduled_date) values
                (501, 101, 'G-101', 'VACCINE', 'SCHEDULED', 'Health 101', date '2026-01-01'),
                (502, 102, 'G-102', 'VACCINE', 'SCHEDULED', 'Health 102', date '2026-01-01')
                """);
        execute(connection, """
                insert into inventory_item (id, farm_id, name, name_normalized) values
                (601, 101, 'Item 101', 'item 101'),
                (602, 102, 'Item 102', 'item 102')
                """);
        execute(connection, """
                insert into inventory_lot (id, farm_id, item_id, code, code_normalized, active) values
                (701, 101, 601, 'LOT-101', 'lot-101', true),
                (702, 102, 602, 'LOT-102', 'lot-102', true)
                """);
        execute(connection, """
                insert into lactation (id, farm_id, goat_id, status, start_date) values
                (801, 101, 'G-101', 'ACTIVE', date '2026-01-01'),
                (802, 102, 'G-102', 'ACTIVE', date '2026-01-01')
                """);
    }

    private void assertRejected(Connection connection, String sql) {
        assertThatThrownBy(() -> execute(connection, sql)).isInstanceOf(SQLException.class);
    }

    private void execute(Connection connection, String sql) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
}
