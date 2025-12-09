package eduhk.fhr.web.dao;

import org.springframework.stereotype.Repository;

import eduhk.fhr.web.model.PersonECP;

/**
 * PersonECPDAO
 *
 * Data Access Object for RDPS_PERSON_ECP table operations.
 */
@Repository
public class PersonECPDAO extends BaseDao {

    /**
     * Insert a new emergency contact person record
     *
     * @param ecp PersonECP model to insert
     */
    public void insertEmergencyContact(PersonECP ecp) {
        String sql = "INSERT INTO RDPS_PERSON_ECP (" +
                "OFFER_ID, REQ_NUMBER, FULL_NAME, PHONE_NO, RELATIONSHIP, " +
                "CREATED_BY, CREATION_DATE, USERSTAMP, TIMESTAMP" +
                ") VALUES (" +
                "?, ?, ?, ?, ?, ?, SYSDATE, ?, SYSDATE" +
                ")";

        jdbcTemplate.update(sql,
            ecp.getOfferId(),
            ecp.getReqNumber(),
            ecp.getFullName(),
            ecp.getPhoneNo(),
            ecp.getRelationship(),
            ecp.getCreatedBy(),
            ecp.getUserstamp()
        );

        logger.info("Inserted PersonECP for offer: {}", ecp.getOfferId());
    }

    /**
     * Delete emergency contact by offer ID
     *
     * @param offerId Offer ID
     * @param reqNumber Requisition number
     */
    public void deleteEmergencyContact(String offerId, String reqNumber) {
        String sql = "DELETE FROM RDPS_PERSON_ECP WHERE OFFER_ID = ? AND REQ_NUMBER = ?";
        jdbcTemplate.update(sql, offerId, reqNumber);
        logger.debug("Deleted PersonECP for offer: {}", offerId);
    }
}
