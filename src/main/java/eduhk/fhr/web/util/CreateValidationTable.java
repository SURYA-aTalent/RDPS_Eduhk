package eduhk.fhr.web.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Utility class to create the RDPS_IMPORT_VALIDATION_ERROR table
 * This is a one-time setup utility
 */
public class CreateValidationTable {

    public static void main(String[] args) {
        String url = "jdbc:oracle:thin:@//localhost:1521/FREEPDB1";
        String username = "SYSTEM";
        String password = "password123";

        String[] sqlStatements = {
            // Drop existing table if it exists
            "DROP TABLE RDPS.RDPS_IMPORT_VALIDATION_ERROR CASCADE CONSTRAINTS",

            // Create table (using default tablespace)
            "CREATE TABLE RDPS.RDPS_IMPORT_VALIDATION_ERROR " +
            "(" +
            "  ERROR_ID          NUMBER                      NOT NULL, " +
            "  CANDIDATE_ID      VARCHAR2(20 BYTE)           NOT NULL, " +
            "  REQ_NUMBER        VARCHAR2(20 BYTE)           NOT NULL, " +
            "  ERROR_TYPE        VARCHAR2(50 BYTE)           NOT NULL, " +
            "  FIELD_NAME        VARCHAR2(50 BYTE), " +
            "  ERROR_MESSAGE     VARCHAR2(500 BYTE)          NOT NULL, " +
            "  ERROR_DETAIL      CLOB, " +
            "  IMPORT_BATCH_ID   VARCHAR2(50 BYTE), " +
            "  CREATED_BY        VARCHAR2(32 BYTE)           DEFAULT USER                  NOT NULL, " +
            "  CREATION_DATE     DATE                        DEFAULT SYSDATE               NOT NULL, " +
            "  RESOLVED          CHAR(1 BYTE)                DEFAULT 'N'                   NOT NULL, " +
            "  RESOLVED_BY       VARCHAR2(32 BYTE), " +
            "  RESOLVED_DATE     DATE " +
            ")",

            // Create sequence
            "CREATE SEQUENCE RDPS.RDPS_VALIDATION_ERROR_SEQ " +
            "  START WITH 1 " +
            "  INCREMENT BY 1 " +
            "  NOCACHE " +
            "  NOCYCLE",

            // Create index on candidate_id and req_number
            "CREATE INDEX RDPS.IDX_VALIDATION_ERROR_CANDIDATE ON RDPS.RDPS_IMPORT_VALIDATION_ERROR " +
            "(CANDIDATE_ID, REQ_NUMBER)",

            // Create index on creation_date
            "CREATE INDEX RDPS.IDX_VALIDATION_ERROR_DATE ON RDPS.RDPS_IMPORT_VALIDATION_ERROR " +
            "(CREATION_DATE DESC)",

            // Create index on resolved status
            "CREATE INDEX RDPS.IDX_VALIDATION_ERROR_RESOLVED ON RDPS.RDPS_IMPORT_VALIDATION_ERROR " +
            "(RESOLVED)",

            // Add primary key constraint
            "ALTER TABLE RDPS.RDPS_IMPORT_VALIDATION_ERROR ADD ( " +
            "  CONSTRAINT PK_VALIDATION_ERROR " +
            "  PRIMARY KEY (ERROR_ID) " +
            "  ENABLE VALIDATE)",

            // Add check constraint for resolved flag
            "ALTER TABLE RDPS.RDPS_IMPORT_VALIDATION_ERROR ADD ( " +
            "  CONSTRAINT CHK_VALIDATION_ERROR_RESOLVED " +
            "  CHECK (RESOLVED IN ('Y', 'N')) " +
            "  ENABLE VALIDATE)"
        };

        Connection conn = null;
        Statement stmt = null;

        try {
            System.out.println("========================================");
            System.out.println("Creating RDPS_IMPORT_VALIDATION_ERROR table");
            System.out.println("========================================");
            System.out.println();

            // Load Oracle JDBC driver
            Class.forName("oracle.jdbc.driver.OracleDriver");
            System.out.println("✓ Oracle JDBC driver loaded");

            // Connect to database
            System.out.println("Connecting to database: " + url);
            conn = DriverManager.getConnection(url, username, password);
            System.out.println("✓ Connected to database");
            System.out.println();

            stmt = conn.createStatement();

            // Execute each SQL statement
            for (int i = 0; i < sqlStatements.length; i++) {
                String sql = sqlStatements[i];

                try {
                    if (i == 0) {
                        System.out.println("Dropping existing table (if exists)...");
                    } else if (sql.startsWith("CREATE TABLE")) {
                        System.out.println("Creating table...");
                    } else if (sql.startsWith("CREATE SEQUENCE")) {
                        System.out.println("Creating sequence...");
                    } else if (sql.startsWith("CREATE INDEX")) {
                        System.out.println("Creating index...");
                    } else if (sql.startsWith("ALTER TABLE")) {
                        System.out.println("Adding constraint...");
                    }

                    stmt.execute(sql);
                    System.out.println("  ✓ Success");

                } catch (Exception e) {
                    // Ignore error for DROP TABLE if table doesn't exist
                    if (i == 0 && e.getMessage().contains("ORA-00942")) {
                        System.out.println("  (Table doesn't exist - skipping drop)");
                    } else {
                        System.out.println("  ✗ Error: " + e.getMessage());
                        if (i > 0) { // Don't fail on drop table error
                            throw e;
                        }
                    }
                }
            }

            System.out.println();
            System.out.println("========================================");
            System.out.println("✓ TABLE CREATED SUCCESSFULLY!");
            System.out.println("========================================");
            System.out.println();
            System.out.println("Table: RDPS.RDPS_IMPORT_VALIDATION_ERROR");
            System.out.println("Sequence: RDPS.RDPS_VALIDATION_ERROR_SEQ");
            System.out.println("Indexes: 3 indexes created");
            System.out.println("Constraints: Primary key and check constraint added");

        } catch (Exception e) {
            System.err.println();
            System.err.println("========================================");
            System.err.println("✗ ERROR CREATING TABLE");
            System.err.println("========================================");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);

        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) {
                    conn.close();
                    System.out.println();
                    System.out.println("Database connection closed.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
