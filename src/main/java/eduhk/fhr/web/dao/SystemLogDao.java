package eduhk.fhr.web.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import eduhk.fhr.web.model.SystemLog;

/**
 * System Log DAO
 *
 * Handles database operations for RDPS_SYSTEM_LOG table.
 */
@Repository
public class SystemLogDao extends BaseDao {

    /**
     * Get import-related error logs from the last N days
     *
     * @param days Number of days to look back
     * @return List of system logs related to import errors
     */
    public List<SystemLog> getImportErrors(int days) {
        String sql = "SELECT ROW_NO, LOG_LEVEL, LOG_MESSAGE, TIMESTAMP, USERSTAMP " +
                     "FROM RDPS_SYSTEM_LOG " +
                     "WHERE LOG_LEVEL IN ('ERROR', 'WARN') " +
                     "  AND LOG_MESSAGE LIKE '%IMPORT%' " +
                     "  AND TIMESTAMP >= SYSDATE - :days " +
                     "ORDER BY TIMESTAMP DESC";

        return jdbcTemplate.query(sql, new Object[]{days}, new SystemLogRowMapper());
    }

    /**
     * Get all error logs from the last N days
     *
     * @param days Number of days to look back
     * @return List of system logs
     */
    public List<SystemLog> getErrorLogs(int days) {
        String sql = "SELECT ROW_NO, LOG_LEVEL, LOG_MESSAGE, TIMESTAMP, USERSTAMP " +
                     "FROM RDPS_SYSTEM_LOG " +
                     "WHERE LOG_LEVEL = 'ERROR' " +
                     "  AND TIMESTAMP >= SYSDATE - :days " +
                     "ORDER BY TIMESTAMP DESC";

        return jdbcTemplate.query(sql, new Object[]{days}, new SystemLogRowMapper());
    }

    /**
     * Row mapper for SystemLog
     */
    private static class SystemLogRowMapper implements RowMapper<SystemLog> {
        @Override
        public SystemLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            SystemLog log = new SystemLog();
            log.setRowNo(rs.getInt("ROW_NO"));
            log.setLogLevel(rs.getString("LOG_LEVEL"));
            log.setLogMessage(rs.getString("LOG_MESSAGE"));
            log.setTimestamp(rs.getTimestamp("TIMESTAMP"));
            log.setUserstamp(rs.getString("USERSTAMP"));
            return log;
        }
    }
}
