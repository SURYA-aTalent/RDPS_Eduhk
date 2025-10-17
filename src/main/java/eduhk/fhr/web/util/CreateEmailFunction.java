package eduhk.fhr.web.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Utility class to create the EXECUTE_EMAIL_JOB function
 * This is a one-time setup utility
 */
public class CreateEmailFunction {

    public static void main(String[] args) {
        String url = "jdbc:oracle:thin:@//localhost:1521/FREEPDB1";
        String username = "SYSTEM";
        String password = "password123";

        String functionSQL =
            "CREATE OR REPLACE FUNCTION RDPS.EXECUTE_EMAIL_JOB ( " +
            "    V_EMAIL_ID_IN IN VARCHAR2, " +
            "    V_SEND_DATE_IN IN DATE, " +
            "    V_USERSTAMP_IN IN VARCHAR2 " +
            ") RETURN VARCHAR2 " +
            "IS " +
            "    v_result VARCHAR2(100); " +
            "    v_count NUMBER; " +
            "BEGIN " +
            "    SELECT COUNT(*) INTO v_count " +
            "    FROM RDPS.RDPS_EMAIL_JOB " +
            "    WHERE EMAIL_ID = V_EMAIL_ID_IN; " +
            " " +
            "    IF v_count = 0 THEN " +
            "        RETURN 'ERROR: Email job not found'; " +
            "    END IF; " +
            " " +
            "    UPDATE RDPS.RDPS_EMAIL_JOB " +
            "    SET SEND_STATUS = 'SENT', " +
            "        SEND_DATE = NVL(V_SEND_DATE_IN, SYSDATE), " +
            "        USERSTAMP = V_USERSTAMP_IN, " +
            "        TIMESTAMP = SYSDATE " +
            "    WHERE EMAIL_ID = V_EMAIL_ID_IN; " +
            " " +
            "    IF SQL%ROWCOUNT > 0 THEN " +
            "        v_result := 'SUCCESS'; " +
            "    ELSE " +
            "        v_result := 'ERROR: Update failed'; " +
            "    END IF; " +
            " " +
            "    COMMIT; " +
            "    RETURN v_result; " +
            " " +
            "EXCEPTION " +
            "    WHEN OTHERS THEN " +
            "        ROLLBACK; " +
            "        RETURN 'ERROR: ' || SQLERRM; " +
            "END";

        Connection conn = null;
        Statement stmt = null;

        try {
            System.out.println("========================================");
            System.out.println("Creating RDPS.EXECUTE_EMAIL_JOB function");
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

            System.out.println("Creating function RDPS.EXECUTE_EMAIL_JOB...");
            stmt.execute(functionSQL);
            System.out.println("✓ Function created successfully");

            System.out.println();
            System.out.println("========================================");
            System.out.println("✓ FUNCTION CREATED SUCCESSFULLY!");
            System.out.println("========================================");
            System.out.println();
            System.out.println("Function: RDPS.EXECUTE_EMAIL_JOB");
            System.out.println("Parameters:");
            System.out.println("  - V_EMAIL_ID_IN (VARCHAR2)");
            System.out.println("  - V_SEND_DATE_IN (DATE)");
            System.out.println("  - V_USERSTAMP_IN (VARCHAR2)");
            System.out.println("Returns: VARCHAR2");

        } catch (Exception e) {
            System.err.println();
            System.err.println("========================================");
            System.err.println("✗ ERROR CREATING FUNCTION");
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
