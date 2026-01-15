package db.migration;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class V15_9__Assert_no_duplicate_active_pregnancy extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        String sql = "SELECT farm_id, goat_id, COUNT(*) AS active_count " +
                "FROM pregnancy " +
                "WHERE status = 'ACTIVE' " +
                "GROUP BY farm_id, goat_id " +
                "HAVING COUNT(*) > 1";
        try (PreparedStatement stmt = context.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                throw new FlywayException("Duplicate ACTIVE pregnancies found. Run db/manual/datafix_duplicate_active_pregnancy.sql before applying V16__enforce_single_active_pregnancy.");
            }
        } catch (SQLException e) {
            throw new FlywayException("Failed to verify duplicate ACTIVE pregnancies before V16.", e);
        }
    }
}

