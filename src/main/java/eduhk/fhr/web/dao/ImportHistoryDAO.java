package eduhk.fhr.web.dao;

// File Path: src/main/java/eduhk/fhr/web/dao/ImportHistoryDAO.java
// Purpose: Data access object for RDPS_IMPORT_HISTORY table

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import eduhk.fhr.web.model.ImportHistory;

/**
 * Import History DAO
 *
 * Handles database operations for RDPS_IMPORT_HISTORY table.
 */
@Repository
public class ImportHistoryDAO {

    private static final Logger logger = LoggerFactory.getLogger(ImportHistoryDAO.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Insert a new import history record
     *
     * @param history Import history record
     * @return Number of rows inserted
     */
    public int insertImportHistory(ImportHistory history) {
        String sql = "INSERT INTO RDPS_IMPORT_HISTORY (" +
            "IMPORT_ID, IMPORT_DATE, TRIGGERED_BY, TOTAL_CANDIDATES, " +
            "SUCCESSFUL_IMPORTS, FAILED_IMPORTS, DURATION_MS, " +
            "LAST_CANDIDATE_ID, STATUS, ERROR_MESSAGE, " +
            "CREATED_BY, CREATION_DATE, USERSTAMP, DATESTAMP" +
            ") VALUES (" +
            "RDPS_IMPORT_HISTORY_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, ?, SYSDATE" +
            ")";

        try {
            return jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql);
                int idx = 1;
                ps.setTimestamp(idx++, new java.sql.Timestamp(history.getImportDate().getTime()));
                ps.setString(idx++, history.getTriggeredBy());
                ps.setInt(idx++, history.getTotalCandidates());
                ps.setInt(idx++, history.getSuccessfulImports());
                ps.setInt(idx++, history.getFailedImports());
                ps.setLong(idx++, history.getDurationMs());
                ps.setString(idx++, history.getLastCandidateId());
                ps.setString(idx++, history.getStatus());
                ps.setString(idx++, history.getErrorMessage());
                ps.setString(idx++, history.getCreatedBy());
                ps.setString(idx++, history.getUserstamp());
                return ps;
            });
        } catch (Exception e) {
            logger.error("Error inserting import history record", e);
            throw new RuntimeException("Failed to insert import history", e);
        }
    }

    /**
     * Get recent import history records
     *
     * @param limit Maximum number of records to return
     * @return List of import history records
     */
    public List<ImportHistory> getRecentImports(int limit) {
        String sql = "SELECT " +
            "IMPORT_ID, IMPORT_DATE, TRIGGERED_BY, TOTAL_CANDIDATES, " +
            "SUCCESSFUL_IMPORTS, FAILED_IMPORTS, DURATION_MS, " +
            "LAST_CANDIDATE_ID, STATUS, ERROR_MESSAGE " +
            "FROM RDPS_IMPORT_HISTORY " +
            "ORDER BY IMPORT_DATE DESC " +
            "FETCH FIRST ? ROWS ONLY";

        try {
            return jdbcTemplate.query(sql, new Object[]{limit}, (rs, rowNum) -> {
                ImportHistory history = new ImportHistory();
                history.setImportId(rs.getInt("IMPORT_ID"));
                history.setImportDate(rs.getTimestamp("IMPORT_DATE"));
                history.setTriggeredBy(rs.getString("TRIGGERED_BY"));
                history.setTotalCandidates(rs.getInt("TOTAL_CANDIDATES"));
                history.setSuccessfulImports(rs.getInt("SUCCESSFUL_IMPORTS"));
                history.setFailedImports(rs.getInt("FAILED_IMPORTS"));
                history.setDurationMs(rs.getLong("DURATION_MS"));
                history.setLastCandidateId(rs.getString("LAST_CANDIDATE_ID"));
                history.setStatus(rs.getString("STATUS"));
                history.setErrorMessage(rs.getString("ERROR_MESSAGE"));
                return history;
            });
        } catch (Exception e) {
            logger.error("Error retrieving recent imports", e);
            return new java.util.ArrayList<>();
        }
    }

    /**
     * Get import history by ID
     *
     * @param importId Import ID
     * @return Import history record or null if not found
     */
    public ImportHistory getImportById(int importId) {
        String sql = "SELECT " +
            "IMPORT_ID, IMPORT_DATE, TRIGGERED_BY, TOTAL_CANDIDATES, " +
            "SUCCESSFUL_IMPORTS, FAILED_IMPORTS, DURATION_MS, " +
            "LAST_CANDIDATE_ID, STATUS, ERROR_MESSAGE " +
            "FROM RDPS_IMPORT_HISTORY " +
            "WHERE IMPORT_ID = ?";

        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{importId}, (rs, rowNum) -> {
                ImportHistory history = new ImportHistory();
                history.setImportId(rs.getInt("IMPORT_ID"));
                history.setImportDate(rs.getTimestamp("IMPORT_DATE"));
                history.setTriggeredBy(rs.getString("TRIGGERED_BY"));
                history.setTotalCandidates(rs.getInt("TOTAL_CANDIDATES"));
                history.setSuccessfulImports(rs.getInt("SUCCESSFUL_IMPORTS"));
                history.setFailedImports(rs.getInt("FAILED_IMPORTS"));
                history.setDurationMs(rs.getLong("DURATION_MS"));
                history.setLastCandidateId(rs.getString("LAST_CANDIDATE_ID"));
                history.setStatus(rs.getString("STATUS"));
                history.setErrorMessage(rs.getString("ERROR_MESSAGE"));
                return history;
            });
        } catch (Exception e) {
            logger.error("Error retrieving import by ID: {}", importId, e);
            return null;
        }
    }

    /**
     * Get imports with failures
     *
     * @param limit Maximum number of records to return
     * @return List of import history records with failures
     */
    public List<ImportHistory> getImportsWithFailures(int limit) {
        String sql = "SELECT " +
            "IMPORT_ID, IMPORT_DATE, TRIGGERED_BY, TOTAL_CANDIDATES, " +
            "SUCCESSFUL_IMPORTS, FAILED_IMPORTS, DURATION_MS, " +
            "LAST_CANDIDATE_ID, STATUS, ERROR_MESSAGE " +
            "FROM RDPS_IMPORT_HISTORY " +
            "WHERE FAILED_IMPORTS > 0 " +
            "ORDER BY IMPORT_DATE DESC " +
            "FETCH FIRST ? ROWS ONLY";

        try {
            return jdbcTemplate.query(sql, new Object[]{limit}, (rs, rowNum) -> {
                ImportHistory history = new ImportHistory();
                history.setImportId(rs.getInt("IMPORT_ID"));
                history.setImportDate(rs.getTimestamp("IMPORT_DATE"));
                history.setTriggeredBy(rs.getString("TRIGGERED_BY"));
                history.setTotalCandidates(rs.getInt("TOTAL_CANDIDATES"));
                history.setSuccessfulImports(rs.getInt("SUCCESSFUL_IMPORTS"));
                history.setFailedImports(rs.getInt("FAILED_IMPORTS"));
                history.setDurationMs(rs.getLong("DURATION_MS"));
                history.setLastCandidateId(rs.getString("LAST_CANDIDATE_ID"));
                history.setStatus(rs.getString("STATUS"));
                history.setErrorMessage(rs.getString("ERROR_MESSAGE"));
                return history;
            });
        } catch (Exception e) {
            logger.error("Error retrieving imports with failures", e);
            return new java.util.ArrayList<>();
        }
    }
}
