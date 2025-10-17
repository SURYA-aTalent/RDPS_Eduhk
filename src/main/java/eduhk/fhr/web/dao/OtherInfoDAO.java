package eduhk.fhr.web.dao;

// File Path: src/main/java/eduhk/fhr/web/dao/OtherInfoDAO.java
// Purpose: Data Access Object for RDPS_OTHER_INFO table

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Repository;

import eduhk.fhr.web.model.OtherInfo;

/**
 * Other Info DAO
 *
 * Handles database operations for RDPS_OTHER_INFO table.
 */
@Repository
public class OtherInfoDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Insert other info record
     *
     * @param otherInfo Other info model to insert
     * @return Number of rows affected
     */
    public int insertOtherInfo(OtherInfo otherInfo) {
        String sql = "INSERT INTO RDPS.RDPS_OTHER_INFO (" +
                "CANDIDATE_ID, REQ_NUMBER, SALARY, NUMBER_OF_MONTHS, " +
                "ALLOWANCE_AMOUNT, ALLOWANCE_PCT, ALLOWANCE_TYPE, ALLOWANCE_NATURE, " +
                "GRATUITY_AMOUNT, GRATUITY_BASIC, OTHER_BENEFITS, SKILLS, " +
                "NEXT_SALARY_REVIEW_DATE, EXPECTED_SALARY, NOTICE_PERIOD, " +
                "DATE_OF_AVAILABILITY, EDUHK_EMPLOYEE, STAFF_NUMBER, " +
                "SOURCE_TYPE, SOURCE_TYPE_OTHER, DCCPR, DCCPR_DETAILS, " +
                "CREATED_BY, CREATION_DATE, USERSTAMP, TIMESTAMP" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, ?, SYSDATE)";

        return jdbcTemplate.update(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                ps.setString(1, otherInfo.getCandidateId());
                ps.setString(2, otherInfo.getReqNumber());

                if (otherInfo.getSalary() != null) {
                    ps.setBigDecimal(3, otherInfo.getSalary());
                } else {
                    ps.setNull(3, java.sql.Types.NUMERIC);
                }

                if (otherInfo.getNumberOfMonths() != null) {
                    ps.setInt(4, otherInfo.getNumberOfMonths());
                } else {
                    ps.setNull(4, java.sql.Types.INTEGER);
                }

                if (otherInfo.getAllowanceAmount() != null) {
                    ps.setBigDecimal(5, otherInfo.getAllowanceAmount());
                } else {
                    ps.setNull(5, java.sql.Types.NUMERIC);
                }

                if (otherInfo.getAllowancePct() != null) {
                    ps.setBigDecimal(6, otherInfo.getAllowancePct());
                } else {
                    ps.setNull(6, java.sql.Types.NUMERIC);
                }

                ps.setString(7, otherInfo.getAllowanceType());
                ps.setString(8, otherInfo.getAllowanceNature());

                if (otherInfo.getGratuityAmount() != null) {
                    ps.setBigDecimal(9, otherInfo.getGratuityAmount());
                } else {
                    ps.setNull(9, java.sql.Types.NUMERIC);
                }

                if (otherInfo.getGratuityBasic() != null) {
                    ps.setBigDecimal(10, otherInfo.getGratuityBasic());
                } else {
                    ps.setNull(10, java.sql.Types.NUMERIC);
                }

                ps.setString(11, otherInfo.getOtherBenefits());
                ps.setString(12, otherInfo.getSkills());

                if (otherInfo.getNextSalaryReviewDate() != null) {
                    ps.setDate(13, new java.sql.Date(otherInfo.getNextSalaryReviewDate().getTime()));
                } else {
                    ps.setNull(13, java.sql.Types.DATE);
                }

                if (otherInfo.getExpectedSalary() != null) {
                    ps.setBigDecimal(14, otherInfo.getExpectedSalary());
                } else {
                    ps.setNull(14, java.sql.Types.NUMERIC);
                }

                ps.setString(15, otherInfo.getNoticePeriod());

                if (otherInfo.getDateOfAvailability() != null) {
                    ps.setDate(16, new java.sql.Date(otherInfo.getDateOfAvailability().getTime()));
                } else {
                    ps.setNull(16, java.sql.Types.DATE);
                }

                ps.setString(17, otherInfo.getEduhkEmployee());
                ps.setString(18, otherInfo.getStaffNumber());
                ps.setString(19, otherInfo.getSourceType());
                ps.setString(20, otherInfo.getSourceTypeOther());
                ps.setString(21, otherInfo.getDccpr());
                ps.setString(22, otherInfo.getDccprDetails());
                ps.setString(23, otherInfo.getCreatedBy() != null ? otherInfo.getCreatedBy() : "SYSTEM");
                ps.setString(24, otherInfo.getUserstamp() != null ? otherInfo.getUserstamp() : "SYSTEM");
            }
        });
    }

    /**
     * Update other info record
     *
     * @param otherInfo Other info model to update
     * @return Number of rows affected
     */
    public int updateOtherInfo(OtherInfo otherInfo) {
        String sql = "UPDATE RDPS.RDPS_OTHER_INFO SET " +
                "SALARY = ?, NUMBER_OF_MONTHS = ?, ALLOWANCE_AMOUNT = ?, " +
                "ALLOWANCE_PCT = ?, ALLOWANCE_TYPE = ?, ALLOWANCE_NATURE = ?, " +
                "GRATUITY_AMOUNT = ?, GRATUITY_BASIC = ?, OTHER_BENEFITS = ?, " +
                "SKILLS = ?, NEXT_SALARY_REVIEW_DATE = ?, EXPECTED_SALARY = ?, " +
                "NOTICE_PERIOD = ?, DATE_OF_AVAILABILITY = ?, EDUHK_EMPLOYEE = ?, " +
                "STAFF_NUMBER = ?, SOURCE_TYPE = ?, SOURCE_TYPE_OTHER = ?, " +
                "DCCPR = ?, DCCPR_DETAILS = ?, USERSTAMP = ?, TIMESTAMP = SYSDATE " +
                "WHERE CANDIDATE_ID = ? AND REQ_NUMBER = ?";

        return jdbcTemplate.update(sql, new PreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps) throws SQLException {
                if (otherInfo.getSalary() != null) {
                    ps.setBigDecimal(1, otherInfo.getSalary());
                } else {
                    ps.setNull(1, java.sql.Types.NUMERIC);
                }

                if (otherInfo.getNumberOfMonths() != null) {
                    ps.setInt(2, otherInfo.getNumberOfMonths());
                } else {
                    ps.setNull(2, java.sql.Types.INTEGER);
                }

                if (otherInfo.getAllowanceAmount() != null) {
                    ps.setBigDecimal(3, otherInfo.getAllowanceAmount());
                } else {
                    ps.setNull(3, java.sql.Types.NUMERIC);
                }

                if (otherInfo.getAllowancePct() != null) {
                    ps.setBigDecimal(4, otherInfo.getAllowancePct());
                } else {
                    ps.setNull(4, java.sql.Types.NUMERIC);
                }

                ps.setString(5, otherInfo.getAllowanceType());
                ps.setString(6, otherInfo.getAllowanceNature());

                if (otherInfo.getGratuityAmount() != null) {
                    ps.setBigDecimal(7, otherInfo.getGratuityAmount());
                } else {
                    ps.setNull(7, java.sql.Types.NUMERIC);
                }

                if (otherInfo.getGratuityBasic() != null) {
                    ps.setBigDecimal(8, otherInfo.getGratuityBasic());
                } else {
                    ps.setNull(8, java.sql.Types.NUMERIC);
                }

                ps.setString(9, otherInfo.getOtherBenefits());
                ps.setString(10, otherInfo.getSkills());

                if (otherInfo.getNextSalaryReviewDate() != null) {
                    ps.setDate(11, new java.sql.Date(otherInfo.getNextSalaryReviewDate().getTime()));
                } else {
                    ps.setNull(11, java.sql.Types.DATE);
                }

                if (otherInfo.getExpectedSalary() != null) {
                    ps.setBigDecimal(12, otherInfo.getExpectedSalary());
                } else {
                    ps.setNull(12, java.sql.Types.NUMERIC);
                }

                ps.setString(13, otherInfo.getNoticePeriod());

                if (otherInfo.getDateOfAvailability() != null) {
                    ps.setDate(14, new java.sql.Date(otherInfo.getDateOfAvailability().getTime()));
                } else {
                    ps.setNull(14, java.sql.Types.DATE);
                }

                ps.setString(15, otherInfo.getEduhkEmployee());
                ps.setString(16, otherInfo.getStaffNumber());
                ps.setString(17, otherInfo.getSourceType());
                ps.setString(18, otherInfo.getSourceTypeOther());
                ps.setString(19, otherInfo.getDccpr());
                ps.setString(20, otherInfo.getDccprDetails());
                ps.setString(21, otherInfo.getUserstamp() != null ? otherInfo.getUserstamp() : "SYSTEM");
                ps.setString(22, otherInfo.getCandidateId());
                ps.setString(23, otherInfo.getReqNumber());
            }
        });
    }

    /**
     * Check if other info exists
     *
     * @param candidateId Candidate ID
     * @param reqNumber Requisition number
     * @return true if exists, false otherwise
     */
    public boolean otherInfoExists(String candidateId, String reqNumber) {
        String sql = "SELECT COUNT(*) FROM RDPS.RDPS_OTHER_INFO " +
                "WHERE CANDIDATE_ID = ? AND REQ_NUMBER = ?";

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, candidateId, reqNumber);
        return count != null && count > 0;
    }

    /**
     * Upsert other info
     *
     * @param otherInfo Other info model
     * @return Number of rows affected
     */
    public int upsertOtherInfo(OtherInfo otherInfo) {
        if (otherInfoExists(otherInfo.getCandidateId(), otherInfo.getReqNumber())) {
            return updateOtherInfo(otherInfo);
        } else {
            return insertOtherInfo(otherInfo);
        }
    }
}
