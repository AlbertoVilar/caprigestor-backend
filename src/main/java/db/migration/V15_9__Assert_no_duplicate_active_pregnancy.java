package db.migration;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Guardrail migration to ensure no duplicate ACTIVE pregnancies exist before applying V16 unique index.
 * This migration runs before V16 and fails if duplicates are found, pointing the developer to the manual fix.
 */
public class V15_9__Assert_no_duplicate_active_pregnancy extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        try (Statement statement = context.getConnection().createStatement()) {
            String sql =
                "SELECT farm_id, goat_id, COUNT(*) AS active_count " +
                "FROM pregnancy " +
                "WHERE status = 'ACTIVE' " +
                "GROUP BY farm_id, goat_id " +
                "HAVING COUNT(*) > 1";

            try (ResultSet resultSet = statement.executeQuery(sql)) {
                StringBuilder duplicatesSummary = new StringBuilder();
                int totalDuplicates = 0;
                boolean hasMoreThanLimit = false;

                while (resultSet.next()) {
                    long farmId = resultSet.getLong("farm_id");
                    String goatId = resultSet.getString("goat_id");
                    long activeCount = resultSet.getLong("active_count");

                    if (totalDuplicates < 10) {
                        if (totalDuplicates > 0) {
                            duplicatesSummary.append("; ");
                        }
                        duplicatesSummary
                            .append("farm_id=")
                            .append(farmId)
                            .append(", goat_id=")
                            .append(goatId)
                            .append(", active_count=")
                            .append(activeCount);
                    } else {
                        hasMoreThanLimit = true;
                    }

                    totalDuplicates++;
                }

                if (totalDuplicates > 0) {
                    if (hasMoreThanLimit) {
                        duplicatesSummary.append("; and more...");
                    }

                    String message =
                        "Found duplicate ACTIVE pregnancies for "
                            + totalDuplicates
                            + " goat(s). "
                            + "Migration V16 (unique index) will fail while these duplicates exist. "
                            + "Please run the manual data-fix script at "
                            + "'src/main/resources/db/manual/datafix_duplicate_active_pregnancy.sql' "
                            + "to close the older ACTIVE pregnancies, then rerun the migration. "
                            + "Duplicated pairs: "
                            + duplicatesSummary;

                    throw new FlywayException(message);
                }
            }
        }
    }
}
