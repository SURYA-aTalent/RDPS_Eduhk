package eduhk.fhr.web.dao;

import org.springframework.stereotype.Repository;

import eduhk.fhr.web.model.PersonChild;

/**
 * PersonChildDAO
 *
 * Data Access Object for RDPS_PERSON_CHILD table operations.
 * Handles multiple children per offer (repeating section).
 */
@Repository
public class PersonChildDAO extends BaseDao {

    /**
     * Insert a new child record
     *
     * @param child PersonChild model to insert
     */
    public void insertChild(PersonChild child) {
        String sql = "INSERT INTO RDPS_PERSON_CHILD (" +
                "OFFER_ID, REQ_NUMBER, CHILD_SEQ, FULL_NAME, FIRST_NAME, LAST_NAME, CHINESE_NAME, " +
                "DATE_OF_BIRTH, GENDER, HKID, PASSPORT, NATIONALITY, " +
                "CREATED_BY, CREATION_DATE, USERSTAMP, TIMESTAMP" +
                ") VALUES (" +
                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, ?, SYSDATE" +
                ")";

        jdbcTemplate.update(sql,
            child.getOfferId(),
            child.getReqNumber(),
            child.getChildSeq(),
            child.getFullName(),
            child.getFirstName(),
            child.getLastName(),
            child.getChineseName(),
            child.getDateOfBirth(),
            child.getGender(),
            child.getHkid(),
            child.getPassport(),
            child.getNationality(),
            child.getCreatedBy(),
            child.getUserstamp()
        );

        logger.info("Inserted PersonChild {} for offer: {}", child.getChildSeq(), child.getOfferId());
    }

    /**
     * Delete all children by offer ID
     *
     * @param offerId Offer ID
     * @param reqNumber Requisition number
     */
    public void deleteChildrenByOfferId(String offerId, String reqNumber) {
        String sql = "DELETE FROM RDPS_PERSON_CHILD WHERE OFFER_ID = ? AND REQ_NUMBER = ?";
        int rows = jdbcTemplate.update(sql, offerId, reqNumber);
        logger.debug("Deleted {} children for offer: {}", rows, offerId);
    }
}
