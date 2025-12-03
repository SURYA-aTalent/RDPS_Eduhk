package eduhk.fhr.web.dao;

// File Path: src/main/java/eduhk/fhr/web/dao/CandidateDocumentDAO.java
// Purpose: Data Access Object for RDPS_CANDIDATE_DOCUMENT table

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import eduhk.fhr.web.model.CandidateDocument;

/**
 * CandidateDocument DAO
 *
 * Handles database operations for RDPS_CANDIDATE_DOCUMENT table.
 * Tracks documents synced from TalentLink to SharePoint.
 */
@Repository
public class CandidateDocumentDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Check if a document has already been synced
     *
     * @param talentLinkDocId TalentLink document ID
     * @return true if document exists in database
     */
    public boolean isDocumentSynced(Long talentLinkDocId) {
        String sql = "SELECT COUNT(*) FROM RDPS_CANDIDATE_DOCUMENT WHERE TALENTLINK_DOC_ID = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, talentLinkDocId);
        return count != null && count > 0;
    }

    /**
     * Insert a new candidate document record
     *
     * @param doc CandidateDocument model to insert
     * @return Number of rows affected
     */
    public int insert(CandidateDocument doc) {
        String sql = "INSERT INTO RDPS_CANDIDATE_DOCUMENT (" +
                "DOC_ID, CANDIDATE_ID, TALENTLINK_APP_ID, TALENTLINK_DOC_ID, " +
                "FILE_NAME, FILE_SIZE, DOC_TYPE, SHAREPOINT_FILE_ID, " +
                "SHAREPOINT_WEB_URL, SHAREPOINT_FOLDER_PATH, SYNC_STATUS, " +
                "SYNC_DATE, ERROR_MESSAGE, CREATED_BY, CREATED_DATE" +
                ") VALUES (RDPS_CANDIDATE_DOCUMENT_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?, CURRENT_TIMESTAMP)";

        return jdbcTemplate.update(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                ps.setString(1, doc.getCandidateId());

                if (doc.getTalentLinkAppId() != null) {
                    ps.setLong(2, doc.getTalentLinkAppId());
                } else {
                    ps.setNull(2, java.sql.Types.NUMERIC);
                }

                ps.setLong(3, doc.getTalentLinkDocId());
                ps.setString(4, doc.getFileName());

                if (doc.getFileSize() != null) {
                    ps.setLong(5, doc.getFileSize());
                } else {
                    ps.setNull(5, java.sql.Types.NUMERIC);
                }

                ps.setString(6, doc.getDocType());
                ps.setString(7, doc.getSharePointFileId());
                ps.setString(8, doc.getSharePointWebUrl());
                ps.setString(9, doc.getSharePointFolderPath());
                ps.setString(10, doc.getSyncStatus());

                if (doc.getErrorMessage() != null && doc.getErrorMessage().length() > 4000) {
                    ps.setString(11, doc.getErrorMessage().substring(0, 4000));
                } else {
                    ps.setString(11, doc.getErrorMessage());
                }

                ps.setString(12, doc.getCreatedBy() != null ? doc.getCreatedBy() : "SYSTEM");
            }
        });
    }

    /**
     * Update sync status for a document
     *
     * @param docId Document ID
     * @param status New status (SYNCED, FAILED, SKIPPED)
     * @param errorMsg Error message (can be null)
     * @return Number of rows affected
     */
    public int updateSyncStatus(Long docId, String status, String errorMsg) {
        String sql = "UPDATE RDPS_CANDIDATE_DOCUMENT SET " +
                "SYNC_STATUS = ?, SYNC_DATE = CURRENT_TIMESTAMP, ERROR_MESSAGE = ? " +
                "WHERE DOC_ID = ?";

        return jdbcTemplate.update(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                ps.setString(1, status);

                if (errorMsg != null && errorMsg.length() > 4000) {
                    ps.setString(2, errorMsg.substring(0, 4000));
                } else {
                    ps.setString(2, errorMsg);
                }

                ps.setLong(3, docId);
            }
        });
    }

    /**
     * Get all documents for a specific candidate
     *
     * @param candidateId Candidate ID
     * @return List of CandidateDocument objects
     */
    public List<CandidateDocument> findByCandidateId(String candidateId) {
        String sql = "SELECT * FROM RDPS_CANDIDATE_DOCUMENT WHERE CANDIDATE_ID = ? ORDER BY SYNC_DATE DESC";
        return jdbcTemplate.query(sql, new CandidateDocumentRowMapper(), candidateId);
    }

    /**
     * Get all failed documents for retry
     *
     * @return List of CandidateDocument objects with FAILED status
     */
    public List<CandidateDocument> findFailedDocuments() {
        String sql = "SELECT * FROM RDPS_CANDIDATE_DOCUMENT WHERE SYNC_STATUS = 'FAILED' ORDER BY SYNC_DATE DESC";
        return jdbcTemplate.query(sql, new CandidateDocumentRowMapper());
    }

    /**
     * Get all synced documents
     *
     * @return List of CandidateDocument objects with SYNCED status
     */
    public List<CandidateDocument> findSyncedDocuments() {
        String sql = "SELECT * FROM RDPS_CANDIDATE_DOCUMENT WHERE SYNC_STATUS = 'SYNCED' ORDER BY SYNC_DATE DESC";
        return jdbcTemplate.query(sql, new CandidateDocumentRowMapper());
    }

    /**
     * Get count of documents by status
     *
     * @param status Sync status (SYNCED, FAILED, SKIPPED)
     * @return Count of documents
     */
    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM RDPS_CANDIDATE_DOCUMENT WHERE SYNC_STATUS = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, status);
        return count != null ? count : 0;
    }

    /**
     * RowMapper for CandidateDocument
     */
    private static class CandidateDocumentRowMapper implements RowMapper<CandidateDocument> {
        @Override
        public CandidateDocument mapRow(ResultSet rs, int rowNum) throws SQLException {
            CandidateDocument doc = new CandidateDocument();
            doc.setDocId(rs.getLong("DOC_ID"));
            doc.setCandidateId(rs.getString("CANDIDATE_ID"));

            long appId = rs.getLong("TALENTLINK_APP_ID");
            if (!rs.wasNull()) {
                doc.setTalentLinkAppId(appId);
            }

            doc.setTalentLinkDocId(rs.getLong("TALENTLINK_DOC_ID"));
            doc.setFileName(rs.getString("FILE_NAME"));

            long fileSize = rs.getLong("FILE_SIZE");
            if (!rs.wasNull()) {
                doc.setFileSize(fileSize);
            }

            doc.setDocType(rs.getString("DOC_TYPE"));
            doc.setSharePointFileId(rs.getString("SHAREPOINT_FILE_ID"));
            doc.setSharePointWebUrl(rs.getString("SHAREPOINT_WEB_URL"));
            doc.setSharePointFolderPath(rs.getString("SHAREPOINT_FOLDER_PATH"));
            doc.setSyncStatus(rs.getString("SYNC_STATUS"));
            doc.setSyncDate(rs.getTimestamp("SYNC_DATE"));
            doc.setErrorMessage(rs.getString("ERROR_MESSAGE"));
            doc.setCreatedBy(rs.getString("CREATED_BY"));
            doc.setCreatedDate(rs.getTimestamp("CREATED_DATE"));

            return doc;
        }
    }
}
