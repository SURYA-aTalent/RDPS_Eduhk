package eduhk.fhr.web.dao;

// File Path: src/main/java/eduhk/fhr/web/dao/ImportValidationErrorDAO.java
// Purpose: Data Access Object for RDPS_IMPORT_VALIDATION_ERROR table

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import eduhk.fhr.web.model.ImportValidationError;

/**
 * Import Validation Error DAO
 *
 * Handles database operations for RDPS_IMPORT_VALIDATION_ERROR table.
 */
@Repository
public class ImportValidationErrorDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Insert a new validation error record
     *
     * @param error ImportValidationError model to insert
     * @return Number of rows affected
     */
    public int insertValidationError(ImportValidationError error) {
        String sql = "INSERT INTO RDPS.RDPS_IMPORT_VALIDATION_ERROR (" +
                "ERROR_ID, CANDIDATE_ID, REQ_NUMBER, ERROR_TYPE, FIELD_NAME, " +
                "ERROR_MESSAGE, ERROR_DETAIL, IMPORT_BATCH_ID, CREATED_BY, CREATION_DATE, RESOLVED" +
                ") VALUES (RDPS.RDPS_VALIDATION_ERROR_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, 'N')";

        return jdbcTemplate.update(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                ps.setString(1, error.getCandidateId());
                ps.setString(2, error.getReqNumber());
                ps.setString(3, error.getErrorType());
                ps.setString(4, error.getFieldName());
                ps.setString(5, error.getErrorMessage());

                if (error.getErrorDetail() != null) {
                    ps.setString(6, error.getErrorDetail());
                } else {
                    ps.setNull(6, java.sql.Types.CLOB);
                }

                ps.setString(7, error.getImportBatchId());
                ps.setString(8, error.getCreatedBy() != null ? error.getCreatedBy() : "SYSTEM");
            }
        });
    }

    /**
     * Get unresolved validation errors
     *
     * @param limit Maximum number of records to return
     * @return List of unresolved validation errors
     */
    public List<ImportValidationError> getUnresolvedErrors(int limit) {
        String sql = "SELECT * FROM RDPS.RDPS_IMPORT_VALIDATION_ERROR " +
                "WHERE RESOLVED = 'N' " +
                "ORDER BY CREATION_DATE DESC " +
                "FETCH FIRST ? ROWS ONLY";

        return jdbcTemplate.query(sql, new Object[]{limit}, new ValidationErrorRowMapper());
    }

    /**
     * Get validation errors by import batch ID
     *
     * @param batchId Import batch ID
     * @return List of validation errors for the batch
     */
    public List<ImportValidationError> getErrorsByBatchId(String batchId) {
        String sql = "SELECT * FROM RDPS.RDPS_IMPORT_VALIDATION_ERROR " +
                "WHERE IMPORT_BATCH_ID = ? " +
                "ORDER BY CREATION_DATE DESC";

        return jdbcTemplate.query(sql, new Object[]{batchId}, new ValidationErrorRowMapper());
    }

    /**
     * Get validation errors by candidate ID
     *
     * @param candidateId Candidate ID
     * @param reqNumber Requisition number
     * @return List of validation errors for the candidate
     */
    public List<ImportValidationError> getErrorsByCandidateId(String candidateId, String reqNumber) {
        String sql = "SELECT * FROM RDPS.RDPS_IMPORT_VALIDATION_ERROR " +
                "WHERE CANDIDATE_ID = ? AND REQ_NUMBER = ? " +
                "ORDER BY CREATION_DATE DESC";

        return jdbcTemplate.query(sql, new Object[]{candidateId, reqNumber}, new ValidationErrorRowMapper());
    }

    /**
     * Get recent validation errors
     *
     * @param days Number of days to look back
     * @param limit Maximum number of records to return
     * @return List of recent validation errors
     */
    public List<ImportValidationError> getRecentErrors(int days, int limit) {
        String sql = "SELECT * FROM RDPS.RDPS_IMPORT_VALIDATION_ERROR " +
                "WHERE CREATION_DATE >= SYSDATE - ? " +
                "ORDER BY CREATION_DATE DESC " +
                "FETCH FIRST ? ROWS ONLY";

        return jdbcTemplate.query(sql, new Object[]{days, limit}, new ValidationErrorRowMapper());
    }

    /**
     * Mark validation error as resolved
     *
     * @param errorId Error ID
     * @param resolvedBy User who resolved the error
     * @return Number of rows affected
     */
    public int markAsResolved(Long errorId, String resolvedBy) {
        String sql = "UPDATE RDPS.RDPS_IMPORT_VALIDATION_ERROR " +
                "SET RESOLVED = 'Y', RESOLVED_BY = ?, RESOLVED_DATE = SYSDATE " +
                "WHERE ERROR_ID = ?";

        return jdbcTemplate.update(sql, resolvedBy, errorId);
    }

    /**
     * Delete old resolved validation errors
     *
     * @param daysToKeep Number of days to keep resolved errors
     * @return Number of rows deleted
     */
    public int deleteOldResolvedErrors(int daysToKeep) {
        String sql = "DELETE FROM RDPS.RDPS_IMPORT_VALIDATION_ERROR " +
                "WHERE RESOLVED = 'Y' AND RESOLVED_DATE < SYSDATE - ?";

        return jdbcTemplate.update(sql, daysToKeep);
    }

    /**
     * Get count of unresolved errors
     *
     * @return Count of unresolved errors
     */
    public int getUnresolvedErrorCount() {
        String sql = "SELECT COUNT(*) FROM RDPS.RDPS_IMPORT_VALIDATION_ERROR WHERE RESOLVED = 'N'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    /**
     * Row mapper for ImportValidationError
     */
    private static class ValidationErrorRowMapper implements RowMapper<ImportValidationError> {
        @Override
        public ImportValidationError mapRow(ResultSet rs, int rowNum) throws SQLException {
            ImportValidationError error = new ImportValidationError();
            error.setErrorId(rs.getLong("ERROR_ID"));
            error.setCandidateId(rs.getString("CANDIDATE_ID"));
            error.setReqNumber(rs.getString("REQ_NUMBER"));
            error.setErrorType(rs.getString("ERROR_TYPE"));
            error.setFieldName(rs.getString("FIELD_NAME"));
            error.setErrorMessage(rs.getString("ERROR_MESSAGE"));
            error.setErrorDetail(rs.getString("ERROR_DETAIL"));
            error.setImportBatchId(rs.getString("IMPORT_BATCH_ID"));
            error.setCreatedBy(rs.getString("CREATED_BY"));
            error.setCreationDate(rs.getDate("CREATION_DATE"));
            error.setResolved(rs.getString("RESOLVED"));
            error.setResolvedBy(rs.getString("RESOLVED_BY"));
            error.setResolvedDate(rs.getDate("RESOLVED_DATE"));
            return error;
        }
    }
}
