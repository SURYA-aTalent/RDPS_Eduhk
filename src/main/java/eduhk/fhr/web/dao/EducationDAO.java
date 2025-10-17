package eduhk.fhr.web.dao;

// File Path: src/main/java/eduhk/fhr/web/dao/EducationDAO.java
// Purpose: Data Access Object for RDPS_EDU_PROF_QUAL table

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Repository;

import eduhk.fhr.web.model.Education;

/**
 * Education DAO
 *
 * Handles database operations for RDPS_EDU_PROF_QUAL table.
 */
@Repository
public class EducationDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Insert an education record
     *
     * @param education Education model to insert
     * @return Number of rows affected
     */
    public int insertEducation(Education education) {
        String sql = "INSERT INTO RDPS.RDPS_EDU_PROF_QUAL (" +
                "CANDIDATE_ID, REQ_NUMBER, SEQ, INSTITUTION, COUNTRY, " +
                "EDU_LEVEL, STUDY_MODE, QUAL_AWARD_DESC, QUAL_AWARD_CLASS, " +
                "OTHERS, MAJOR_STUDY_AREA, START_DATE, DATE_OF_AWARD, " +
                "CREATED_BY, CREATION_DATE, USERSTAMP, TIMESTAMP" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, ?, SYSDATE)";

        return jdbcTemplate.update(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                ps.setString(1, education.getCandidateId());
                ps.setString(2, education.getReqNumber());
                ps.setInt(3, education.getSeq());
                ps.setString(4, education.getInstitution());
                ps.setString(5, education.getCountry());

                if (education.getEduLevel() != null) {
                    ps.setInt(6, education.getEduLevel());
                } else {
                    ps.setNull(6, java.sql.Types.INTEGER);
                }

                if (education.getStudyMode() != null) {
                    ps.setInt(7, education.getStudyMode());
                } else {
                    ps.setNull(7, java.sql.Types.INTEGER);
                }

                if (education.getQualAwardDesc() != null) {
                    ps.setInt(8, education.getQualAwardDesc());
                } else {
                    ps.setNull(8, java.sql.Types.INTEGER);
                }

                if (education.getQualAwardClass() != null) {
                    ps.setInt(9, education.getQualAwardClass());
                } else {
                    ps.setNull(9, java.sql.Types.INTEGER);
                }

                ps.setString(10, education.getOthers());
                ps.setString(11, education.getMajorStudyArea());

                if (education.getStartDate() != null) {
                    ps.setDate(12, new java.sql.Date(education.getStartDate().getTime()));
                } else {
                    ps.setNull(12, java.sql.Types.DATE);
                }

                if (education.getDateOfAward() != null) {
                    ps.setDate(13, new java.sql.Date(education.getDateOfAward().getTime()));
                } else {
                    ps.setNull(13, java.sql.Types.DATE);
                }

                ps.setString(14, education.getCreatedBy() != null ? education.getCreatedBy() : "SYSTEM");
                ps.setString(15, education.getUserstamp() != null ? education.getUserstamp() : "SYSTEM");
            }
        });
    }

    /**
     * Delete all education records for a candidate
     *
     * @param candidateId Candidate ID
     * @param reqNumber Requisition number
     * @return Number of rows deleted
     */
    public int deleteEducationByCandidateId(String candidateId, String reqNumber) {
        String sql = "DELETE FROM RDPS.RDPS_EDU_PROF_QUAL " +
                "WHERE CANDIDATE_ID = ? AND REQ_NUMBER = ?";

        return jdbcTemplate.update(sql, candidateId, reqNumber);
    }
}
