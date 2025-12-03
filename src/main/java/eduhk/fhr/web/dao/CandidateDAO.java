package eduhk.fhr.web.dao;

// File Path: src/main/java/eduhk/fhr/web/dao/CandidateDAO.java
// Purpose: Data Access Object for RDPS_CANDIDATE table

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Repository;

import eduhk.fhr.web.model.Candidate;

/**
 * Candidate DAO
 *
 * Handles database operations for RDPS_CANDIDATE table.
 */
@Repository
public class CandidateDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Insert a new candidate record
     *
     * @param candidate Candidate model to insert
     * @return Number of rows affected
     */
    public int insertCandidate(Candidate candidate) {
        String sql = "INSERT INTO RDPS.RDPS_CANDIDATE (" +
                "CANDIDATE_ID, REQ_NUMBER, POST_APPLIED_FOR, TITLE, " +
                "LAST_NAME, FIRST_NAME, CHINESE_NAME, DATE_OF_BIRTH, " +
                "PERM_HK, GENDER, HKID, PASSPORT_NO, VISA_REQUIRED, " +
                "ADDRESS_LINE1, ADDRESS_LINE2, DISTRICT, PHONE_NUMBER, EMAIL, " +
                "CREATED_BY, CREATION_DATE, USERSTAMP, TIMESTAMP" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, ?, SYSDATE)";

        return jdbcTemplate.update(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                ps.setString(1, candidate.getCandidateId());
                ps.setString(2, candidate.getReqNumber());
                ps.setString(3, candidate.getPostAppliedFor());
                ps.setString(4, candidate.getTitle());
                ps.setString(5, candidate.getLastName());
                ps.setString(6, candidate.getFirstName());
                ps.setString(7, candidate.getChineseName());

                if (candidate.getDateOfBirth() != null) {
                    ps.setDate(8, new java.sql.Date(candidate.getDateOfBirth().getTime()));
                } else {
                    ps.setNull(8, java.sql.Types.DATE);
                }

                ps.setString(9, candidate.getPermHK());
                ps.setString(10, candidate.getGender());
                ps.setString(11, candidate.getHkid());
                ps.setString(12, candidate.getPassportNo());
                ps.setString(13, candidate.getVisaRequired());
                ps.setString(14, candidate.getAddressLine1());
                ps.setString(15, candidate.getAddressLine2());

                if (candidate.getDistrict() != null) {
                    ps.setInt(16, candidate.getDistrict());
                } else {
                    ps.setNull(16, java.sql.Types.INTEGER);
                }

                ps.setString(17, candidate.getPhoneNumber());
                ps.setString(18, candidate.getEmail());
                ps.setString(19, candidate.getCreatedBy() != null ? candidate.getCreatedBy() : "SYSTEM");
                ps.setString(20, candidate.getUserstamp() != null ? candidate.getUserstamp() : "SYSTEM");
            }
        });
    }

    /**
     * Update an existing candidate record
     *
     * @param candidate Candidate model to update
     * @return Number of rows affected
     */
    public int updateCandidate(Candidate candidate) {
        String sql = "UPDATE RDPS.RDPS_CANDIDATE SET " +
                "POST_APPLIED_FOR = ?, TITLE = ?, LAST_NAME = ?, FIRST_NAME = ?, " +
                "CHINESE_NAME = ?, DATE_OF_BIRTH = ?, PERM_HK = ?, GENDER = ?, " +
                "HKID = ?, PASSPORT_NO = ?, VISA_REQUIRED = ?, ADDRESS_LINE1 = ?, " +
                "ADDRESS_LINE2 = ?, DISTRICT = ?, PHONE_NUMBER = ?, EMAIL = ?, " +
                "USERSTAMP = ?, TIMESTAMP = SYSDATE " +
                "WHERE CANDIDATE_ID = ? AND REQ_NUMBER = ?";

        return jdbcTemplate.update(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                ps.setString(1, candidate.getPostAppliedFor());
                ps.setString(2, candidate.getTitle());
                ps.setString(3, candidate.getLastName());
                ps.setString(4, candidate.getFirstName());
                ps.setString(5, candidate.getChineseName());

                if (candidate.getDateOfBirth() != null) {
                    ps.setDate(6, new java.sql.Date(candidate.getDateOfBirth().getTime()));
                } else {
                    ps.setNull(6, java.sql.Types.DATE);
                }

                ps.setString(7, candidate.getPermHK());
                ps.setString(8, candidate.getGender());
                ps.setString(9, candidate.getHkid());
                ps.setString(10, candidate.getPassportNo());
                ps.setString(11, candidate.getVisaRequired());
                ps.setString(12, candidate.getAddressLine1());
                ps.setString(13, candidate.getAddressLine2());

                if (candidate.getDistrict() != null) {
                    ps.setInt(14, candidate.getDistrict());
                } else {
                    ps.setNull(14, java.sql.Types.INTEGER);
                }

                ps.setString(15, candidate.getPhoneNumber());
                ps.setString(16, candidate.getEmail());
                ps.setString(17, candidate.getUserstamp() != null ? candidate.getUserstamp() : "SYSTEM");
                ps.setString(18, candidate.getCandidateId());
                ps.setString(19, candidate.getReqNumber());
            }
        });
    }

    /**
     * Check if candidate exists
     *
     * @param candidateId Candidate ID
     * @param reqNumber Requisition number
     * @return true if exists, false otherwise
     */
    public boolean candidateExists(String candidateId, String reqNumber) {
        String sql = "SELECT COUNT(*) FROM RDPS.RDPS_CANDIDATE " +
                "WHERE CANDIDATE_ID = ? AND REQ_NUMBER = ?";

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, candidateId, reqNumber);
        return count != null && count > 0;
    }

    /**
     * Insert or update candidate (upsert operation)
     *
     * @param candidate Candidate model
     * @return Number of rows affected
     */
    public int upsertCandidate(Candidate candidate) {
        if (candidateExists(candidate.getCandidateId(), candidate.getReqNumber())) {
            return updateCandidate(candidate);
        } else {
            return insertCandidate(candidate);
        }
    }

    /**
     * Get all candidate IDs from the database
     *
     * @return List of candidate IDs
     */
    public java.util.List<String> getAllCandidateIds() {
        String sql = "SELECT CANDIDATE_ID FROM RDPS.RDPS_CANDIDATE ORDER BY CANDIDATE_ID";
        return jdbcTemplate.queryForList(sql, String.class);
    }
}
