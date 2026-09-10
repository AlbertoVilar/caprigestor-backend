package com.devmaster.goatfarm.integration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers(disabledWithoutDocker = true)
class GoatTechnicalIdentityFlywayPostgresIntegrationTest {

    @Container
    final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void cleanInstallHandlesEmptyCabrasAndGeneratesIdentityFromOne() throws SQLException {
        flyway().migrate();

        try (Connection connection = openConnection()) {
            assertIdentityColumn(connection);
            assertPrimaryKeyRemainsRegistrationNumber(connection);
            assertReferencingForeignKeysRemainOnRegistrationNumber(connection);

            seedUsersAndFarm(connection);
            insertGoat(connection, "G-EMPTY", "Cabra vazia");

            assertThat(queryLong(connection, "select id from cabras where num_registro = 'G-EMPTY'"))
                    .isEqualTo(1L);
            assertThat(queryLong(connection, "select count(*) from cabras where id is null"))
                    .isZero();

            assertThatThrownBy(() -> execute(connection, """
                    insert into cabras (id, num_registro, nome, sexo, data_nascimento, status, usuario_id, capril_id)
                    values (99, 'G-EXPLICIT', 'Cabra explicita', 'FEMEA', date '2020-01-01', 'ATIVO', 1, 101)
                    """))
                    .isInstanceOf(SQLException.class);

            assertThatThrownBy(() -> execute(connection,
                    "update cabras set id = 99 where num_registro = 'G-EMPTY'"))
                    .isInstanceOf(SQLException.class);
        }
    }

    @Test
    void upgradeWithExistingGoatsBackfillsAndAlignsTheFinalSequence() throws SQLException {
        flyway("38").migrate();

        try (Connection connection = openConnection()) {
            seedRepresentativeData(connection);
            assertThat(queryLong(connection, "select count(*) from cabras")).isEqualTo(2L);
        }

        flyway().migrate();

        try (Connection connection = openConnection()) {
            assertIdentityColumn(connection);
            assertPrimaryKeyRemainsRegistrationNumber(connection);
            assertReferencingForeignKeysRemainOnRegistrationNumber(connection);

            assertThat(queryLong(connection, "select count(*) from cabras")).isEqualTo(2L);
            assertThat(queryLong(connection, "select count(distinct id) from cabras")).isEqualTo(2L);
            assertThat(queryLong(connection, "select count(*) from cabras where id is null")).isZero();
            assertThat(queryLong(connection, "select id from cabras where num_registro = 'G-ROOT'"))
                    .isEqualTo(1L);
            assertThat(queryLong(connection, "select id from cabras where num_registro = 'G-CHILD'"))
                    .isEqualTo(2L);
            assertThat(queryString(connection, "select pai_num_registro from cabras where num_registro = 'G-CHILD'"))
                    .isEqualTo("G-ROOT");
            assertThat(queryLong(connection, "select count(*) from eventos where goat_registration_number = 'G-ROOT'"))
                    .isEqualTo(1L);

            long rootId = queryLong(connection, "select id from cabras where num_registro = 'G-ROOT'");
            long childId = queryLong(connection, "select id from cabras where num_registro = 'G-CHILD'");
            execute(connection, "update cabras set nome = 'Raiz atualizada' where num_registro = 'G-ROOT'");
            assertThat(queryLong(connection, "select id from cabras where num_registro = 'G-ROOT'"))
                    .isEqualTo(rootId);

            execute(connection, """
                    insert into cabras (num_registro, nome, sexo, data_nascimento, status, usuario_id, capril_id)
                    values ('G-NEW', 'Cabra nova', 'FEMEA', date '2024-01-01', 'ATIVO', 1, 101)
                    """);
            assertThat(queryLong(connection, "select id from cabras where num_registro = 'G-NEW'"))
                    .isEqualTo(3L);

            execute(connection, """
                    insert into cabras (num_registro, nome, sexo, data_nascimento, status, usuario_id, capril_id)
                    values ('G-ROOT', 'Raiz via seed', 'FEMEA', date '2020-01-01', 'ATIVO', 1, 101)
                    on conflict (num_registro) do update set nome = excluded.nome
                    """);
            assertThat(queryLong(connection, "select id from cabras where num_registro = 'G-ROOT'"))
                    .isEqualTo(rootId);
            assertThat(queryLong(connection, "select id from cabras where num_registro = 'G-CHILD'"))
                    .isEqualTo(childId);
            assertThat(queryString(connection, "select nome from cabras where num_registro = 'G-ROOT'"))
                    .isEqualTo("Raiz via seed");

            assertThatThrownBy(() -> execute(connection, """
                    update cabras set id = 99 where num_registro = 'G-ROOT'
                    """))
                    .isInstanceOf(SQLException.class);
            assertThat(queryLong(connection, "select id from cabras where num_registro = 'G-ROOT'"))
                    .isEqualTo(rootId);
        }
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

    private void seedRepresentativeData(Connection connection) throws SQLException {
        seedUsersAndFarm(connection);
        insertGoat(connection, "G-ROOT", "Cabra raiz");
        execute(connection, """
                insert into cabras (num_registro, nome, sexo, data_nascimento, status, usuario_id, capril_id, pai_num_registro)
                values ('G-CHILD', 'Cabra filha', 'FEMEA', date '2022-01-01', 'ATIVO', 1, 101, 'G-ROOT')
                """);
        execute(connection, """
                insert into eventos (goat_registration_number, tipo_evento, data, descricao)
                values ('G-ROOT', 'PESAGEM', date '2024-01-01', 'Evento de compatibilidade')
                """);
    }

    private void seedUsersAndFarm(Connection connection) throws SQLException {
        execute(connection, """
                insert into users (id, name, email, password, cpf)
                values (1, 'Test User', 'test@example.com', 'password', '00000000000')
                """);
        execute(connection, """
                insert into capril (id, name, user_id)
                values (101, 'Farm 101', 1)
                """);
    }

    private void insertGoat(Connection connection, String registrationNumber, String name) throws SQLException {
        execute(connection, """
                insert into cabras (num_registro, nome, sexo, data_nascimento, status, usuario_id, capril_id)
                values ('%s', '%s', 'FEMEA', date '2020-01-01', 'ATIVO', 1, 101)
                """.formatted(registrationNumber, name));
    }

    private void assertIdentityColumn(Connection connection) throws SQLException {
        String sql = """
                select is_nullable, is_identity, identity_generation
                from information_schema.columns
                where table_schema = 'public' and table_name = 'cabras' and column_name = 'id'
                """;
        try (var statement = connection.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getString("is_nullable")).isEqualTo("NO");
            assertThat(rs.getString("is_identity")).isEqualTo("YES");
            assertThat(rs.getString("identity_generation")).isEqualTo("ALWAYS");
        }
        assertThat(hasConstraint(connection, "uk_cabras_id", "UNIQUE")).isTrue();
    }

    private void assertPrimaryKeyRemainsRegistrationNumber(Connection connection) throws SQLException {
        String sql = """
                select pg_get_constraintdef(oid)
                from pg_constraint
                where conrelid = 'public.cabras'::regclass and conname = 'cabras_pkey'
                """;
        assertThat(queryString(connection, sql)).contains("PRIMARY KEY (num_registro)");
    }

    private void assertReferencingForeignKeysRemainOnRegistrationNumber(Connection connection) throws SQLException {
        String sql = """
                select pg_get_constraintdef(oid)
                from pg_constraint
                where contype = 'f' and confrelid = 'public.cabras'::regclass
                order by conname
                """;
        List<String> definitions = new ArrayList<>();
        try (var statement = connection.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
            while (rs.next()) {
                definitions.add(rs.getString(1));
            }
        }
        assertThat(definitions).hasSize(11);
        assertThat(definitions).allMatch(definition -> definition.contains("num_registro"));
        assertThat(definitions).noneMatch(definition -> definition.contains("cabras (id"));
    }

    private boolean hasConstraint(Connection connection, String constraintName, String constraintType)
            throws SQLException {
        String sql = """
                select 1
                from information_schema.table_constraints
                where table_schema = 'public' and constraint_name = ? and constraint_type = ?
                """;
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, constraintName);
            statement.setString(2, constraintType);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    private long queryLong(Connection connection, String sql) throws SQLException {
        try (var statement = connection.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
            if (!rs.next()) {
                throw new SQLException("Consulta nao retornou linhas: " + sql);
            }
            return rs.getLong(1);
        }
    }

    private String queryString(Connection connection, String sql) throws SQLException {
        try (var statement = connection.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
            if (!rs.next()) {
                throw new SQLException("Consulta nao retornou linhas: " + sql);
            }
            return rs.getString(1);
        }
    }

    private void execute(Connection connection, String sql) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
}
