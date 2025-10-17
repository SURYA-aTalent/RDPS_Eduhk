package eduhk.fhr.web.dao;

// File Path: src/main/java/eduhk/fhr/web/dao/WorkExperienceDAO.java
// Purpose: Data Access Object for RDPS_WORK_EXPERIENCE table

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Repository;

import eduhk.fhr.web.model.WorkExperience;

/**
 * Work Experience DAO
 *
 * Handles database operations for RDPS_WORK_EXPERIENCE table.
 */
@Repository
public class WorkExperienceDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Insert a work experience record
     *
     * @param workExp Work experience model to insert
     * @return Number of rows affected
     */
    public int insertWorkExperience(WorkExperience workExp) {
        String sql = "INSERT INTO RDPS.RDPS_WORK_EXPERIENCE (" +
                "CANDIDATE_ID, REQ_NUMBER, SEQ, EMPLOYER_NAME, NATURE_OF_BUSINESS, " +
                "POSITION_TITLE, CURRENT_JOB, MODE_OF_EMPLOYMENT, HOURS_PER_WEEK, " +
                "START_DATE, END_DATE, NATURE_OF_DUTIES, " +
                "CREATED_BY, CREATION_DATE, USERSTAMP, TIMESTAMP" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, ?, SYSDATE)";

        return jdbcTemplate.update(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                ps.setString(1, workExp.getCandidateId());
                ps.setString(2, workExp.getReqNumber());
                ps.setInt(3, workExp.getSeq());
                ps.setString(4, workExp.getEmployerName());
                ps.setString(5, workExp.getNatureOfBusiness());
                ps.setString(6, workExp.getPositionTitle());
                ps.setString(7, workExp.getCurrentJob());
                ps.setString(8, workExp.getModeOfEmployment());
                ps.setString(9, workExp.getHoursPerWeek());

                if (workExp.getStartDate() != null) {
                    ps.setDate(10, new java.sql.Date(workExp.getStartDate().getTime()));
                } else {
                    ps.setNull(10, java.sql.Types.DATE);
                }

                if (workExp.getEndDate() != null) {
                    ps.setDate(11, new java.sql.Date(workExp.getEndDate().getTime()));
                } else {
                    ps.setNull(11, java.sql.Types.DATE);
                }

                ps.setString(12, workExp.getNatureOfDuties());
                ps.setString(13, workExp.getCreatedBy() != null ? workExp.getCreatedBy() : "SYSTEM");
                ps.setString(14, workExp.getUserstamp() != null ? workExp.getUserstamp() : "SYSTEM");
            }
        });
    }

    /**
     * Delete all work experience records for a candidate
     *
     * @param candidateId Candidate ID
     * @param reqNumber Requisition number
     * @return Number of rows deleted
     */
    public int deleteWorkExperienceByCandidateId(String candidateId, String reqNumber) {
        String sql = "DELETE FROM RDPS.RDPS_WORK_EXPERIENCE " +
                "WHERE CANDIDATE_ID = ? AND REQ_NUMBER = ?";

        return jdbcTemplate.update(sql, candidateId, reqNumber);
    }
}
