package eduhk.fhr.web.dao;

// File Path: src/main/java/eduhk/fhr/web/dao/RefereeDAO.java
// Purpose: Data Access Object for RDPS_REFEREE table

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Repository;

import eduhk.fhr.web.model.Referee;

/**
 * Referee DAO
 *
 * Handles database operations for RDPS_REFEREE table.
 */
@Repository
public class RefereeDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Insert a referee record
     *
     * @param referee Referee model to insert
     * @return Number of rows affected
     */
    public int insertReferee(Referee referee) {
        String sql = "INSERT INTO RDPS.RDPS_REFEREE (" +
                "CANDIDATE_ID, REQ_NUMBER, SEQ, TITLE, FIRST_NAME, LAST_NAME, " +
                "POSITION_TITLE, PHONE_NUMBER, EMAIL, RELATIONSHIP, " +
                "CREATED_BY, CREATION_DATE, USERSTAMP, TIMESTAMP" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, ?, SYSDATE)";

        return jdbcTemplate.update(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                ps.setString(1, referee.getCandidateId());
                ps.setString(2, referee.getReqNumber());
                ps.setInt(3, referee.getSeq());
                ps.setString(4, referee.getTitle());
                ps.setString(5, referee.getFirstName());
                ps.setString(6, referee.getLastName());
                ps.setString(7, referee.getPositionTitle());
                ps.setString(8, referee.getPhoneNumber());
                ps.setString(9, referee.getEmail());
                ps.setString(10, referee.getRelationship());
                ps.setString(11, referee.getCreatedBy() != null ? referee.getCreatedBy() : "SYSTEM");
                ps.setString(12, referee.getUserstamp() != null ? referee.getUserstamp() : "SYSTEM");
            }
        });
    }

    /**
     * Delete all referee records for a candidate
     *
     * @param candidateId Candidate ID
     * @param reqNumber Requisition number
     * @return Number of rows deleted
     */
    public int deleteRefereesByCandidateId(String candidateId, String reqNumber) {
        String sql = "DELETE FROM RDPS.RDPS_REFEREE " +
                "WHERE CANDIDATE_ID = ? AND REQ_NUMBER = ?";

        return jdbcTemplate.update(sql, candidateId, reqNumber);
    }
}
