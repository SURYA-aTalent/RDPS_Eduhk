package eduhk.fhr.web.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import eduhk.fhr.web.model.Offer;

/**
 * OfferDAO
 *
 * Data Access Object for RDPS_OFFER table operations.
 * Handles insert, update, and upsert operations for job offer records.
 */
@Repository
public class OfferDAO extends BaseDao {

    /**
     * Insert a new offer record
     *
     * @param offer Offer model to insert
     */
    public void insertOffer(Offer offer) {
        String sql = "INSERT INTO RDPS_OFFER (" +
                "OFFER_ID, CANDIDATE_ID, REQ_NUMBER, OFFER_STATUS, " +
                "PAY_BASIS, EMP_DEP, TOA, FUNC_TITLE, BAND, GRADE, POST, " +
                "CONTRACT_START_DATE, CONTRACT_END_DATE, " +
                "PROBATION_LENGTH, PROBATION_UNITS, NOTICE_LENGTH, NOTICE_UNITS, " +
                "SALARY_AMOUNT, GRATUITY_PCT, EMP_MODE, PLAN_HOURS, " +
                "MPF, SUPERANNUATION, PENSION, PROJECT_TITLE, OFFER_REMARKS, " +
                "CREATED_BY, CREATION_DATE, USERSTAMP, TIMESTAMP" +
                ") VALUES (" +
                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, ?, SYSDATE" +
                ")";

        jdbcTemplate.update(sql,
            offer.getOfferId(),
            offer.getCandidateId(),
            offer.getReqNumber(),
            offer.getOfferStatus(),
            offer.getPayBasis(),
            offer.getEmpDep(),
            offer.getToa(),
            offer.getFuncTitle(),
            offer.getBand(),
            offer.getGrade(),
            offer.getPost(),
            offer.getContractStartDate(),
            offer.getContractEndDate(),
            offer.getProbationLength(),
            offer.getProbationUnits(),
            offer.getNoticeLength(),
            offer.getNoticeUnits(),
            offer.getSalaryAmount(),
            offer.getGratuityPct(),
            offer.getEmpMode(),
            offer.getPlanHours(),
            offer.getMpf(),
            offer.getSuperannuation(),
            offer.getPension(),
            offer.getProjectTitle(),
            offer.getOfferRemarks(),
            offer.getCreatedBy(),
            offer.getUserstamp()
        );

        logger.info("Inserted offer: {}", offer.getOfferId());
    }

    /**
     * Check if an offer exists by ID
     *
     * @param offerId Offer ID to check
     * @return true if offer exists, false otherwise
     */
    public boolean offerExists(String offerId) {
        String sql = "SELECT COUNT(*) FROM RDPS_OFFER WHERE OFFER_ID = ?";
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, offerId);
            return count != null && count > 0;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    /**
     * Update an existing offer record
     *
     * @param offer Offer model to update
     */
    public void updateOffer(Offer offer) {
        String sql = "UPDATE RDPS_OFFER SET " +
                "OFFER_STATUS = ?, " +
                "PAY_BASIS = ?, " +
                "EMP_DEP = ?, " +
                "TOA = ?, " +
                "FUNC_TITLE = ?, " +
                "BAND = ?, " +
                "GRADE = ?, " +
                "POST = ?, " +
                "CONTRACT_START_DATE = ?, " +
                "CONTRACT_END_DATE = ?, " +
                "PROBATION_LENGTH = ?, " +
                "PROBATION_UNITS = ?, " +
                "NOTICE_LENGTH = ?, " +
                "NOTICE_UNITS = ?, " +
                "SALARY_AMOUNT = ?, " +
                "GRATUITY_PCT = ?, " +
                "EMP_MODE = ?, " +
                "PLAN_HOURS = ?, " +
                "MPF = ?, " +
                "SUPERANNUATION = ?, " +
                "PENSION = ?, " +
                "PROJECT_TITLE = ?, " +
                "OFFER_REMARKS = ?, " +
                "USERSTAMP = ?, " +
                "TIMESTAMP = SYSDATE " +
                "WHERE OFFER_ID = ?";

        jdbcTemplate.update(sql,
            offer.getOfferStatus(),
            offer.getPayBasis(),
            offer.getEmpDep(),
            offer.getToa(),
            offer.getFuncTitle(),
            offer.getBand(),
            offer.getGrade(),
            offer.getPost(),
            offer.getContractStartDate(),
            offer.getContractEndDate(),
            offer.getProbationLength(),
            offer.getProbationUnits(),
            offer.getNoticeLength(),
            offer.getNoticeUnits(),
            offer.getSalaryAmount(),
            offer.getGratuityPct(),
            offer.getEmpMode(),
            offer.getPlanHours(),
            offer.getMpf(),
            offer.getSuperannuation(),
            offer.getPension(),
            offer.getProjectTitle(),
            offer.getOfferRemarks(),
            offer.getUserstamp(),
            offer.getOfferId()
        );

        logger.info("Updated offer: {}", offer.getOfferId());
    }

    /**
     * Insert or update offer (upsert operation)
     * If offer exists, updates it; otherwise inserts new record
     *
     * @param offer Offer model to upsert
     */
    public void upsertOffer(Offer offer) {
        if (offerExists(offer.getOfferId())) {
            updateOffer(offer);
        } else {
            insertOffer(offer);
        }
    }

    /**
     * Find offer by ID
     *
     * @param offerId Offer ID to find
     * @return Offer model or null if not found
     */
    public Offer findById(String offerId) {
        String sql = "SELECT * FROM RDPS_OFFER WHERE OFFER_ID = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Offer offer = new Offer();
                offer.setOfferId(rs.getString("OFFER_ID"));
                offer.setCandidateId(rs.getString("CANDIDATE_ID"));
                offer.setReqNumber(rs.getString("REQ_NUMBER"));
                offer.setOfferStatus(rs.getString("OFFER_STATUS"));
                offer.setPayBasis(rs.getString("PAY_BASIS"));
                offer.setEmpDep(rs.getString("EMP_DEP"));
                offer.setToa(rs.getString("TOA"));
                offer.setFuncTitle(rs.getString("FUNC_TITLE"));
                offer.setBand(rs.getString("BAND"));
                offer.setGrade(rs.getString("GRADE"));
                offer.setPost(rs.getString("POST"));
                offer.setContractStartDate(rs.getDate("CONTRACT_START_DATE"));
                offer.setContractEndDate(rs.getDate("CONTRACT_END_DATE"));
                offer.setProbationLength(rs.getInt("PROBATION_LENGTH"));
                offer.setProbationUnits(rs.getString("PROBATION_UNITS"));
                offer.setNoticeLength(rs.getInt("NOTICE_LENGTH"));
                offer.setNoticeUnits(rs.getString("NOTICE_UNITS"));
                offer.setSalaryAmount(rs.getBigDecimal("SALARY_AMOUNT"));
                offer.setGratuityPct(rs.getBigDecimal("GRATUITY_PCT"));
                offer.setEmpMode(rs.getString("EMP_MODE"));
                offer.setPlanHours(rs.getInt("PLAN_HOURS"));
                offer.setMpf(rs.getString("MPF"));
                offer.setSuperannuation(rs.getString("SUPERANNUATION"));
                offer.setPension(rs.getString("PENSION"));
                offer.setProjectTitle(rs.getString("PROJECT_TITLE"));
                offer.setOfferRemarks(rs.getString("OFFER_REMARKS"));
                offer.setCreatedBy(rs.getString("CREATED_BY"));
                offer.setCreationDate(rs.getDate("CREATION_DATE"));
                offer.setUserstamp(rs.getString("USERSTAMP"));
                offer.setTimestamp(rs.getDate("TIMESTAMP"));
                return offer;
            }, offerId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
