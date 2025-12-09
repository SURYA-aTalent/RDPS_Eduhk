package eduhk.fhr.web.dao;

import org.springframework.stereotype.Repository;

import eduhk.fhr.web.model.PersonSpouse;

/**
 * PersonSpouseDAO
 *
 * Data Access Object for RDPS_PERSON_SPOUSE table operations.
 */
@Repository
public class PersonSpouseDAO extends BaseDao {

    /**
     * Insert a new spouse record
     *
     * @param spouse PersonSpouse model to insert
     */
    public void insertSpouse(PersonSpouse spouse) {
        String sql = "INSERT INTO RDPS_PERSON_SPOUSE (" +
                "OFFER_ID, REQ_NUMBER, FULL_NAME, FIRST_NAME, LAST_NAME, CHINESE_NAME, " +
                "DATE_OF_BIRTH, GENDER, HKID, PASSPORT, NATIONALITY, EMAIL, PHONE_NO, " +
                "CREATED_BY, CREATION_DATE, USERSTAMP, TIMESTAMP" +
                ") VALUES (" +
                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, ?, SYSDATE" +
                ")";

        jdbcTemplate.update(sql,
            spouse.getOfferId(),
            spouse.getReqNumber(),
            spouse.getFullName(),
            spouse.getFirstName(),
            spouse.getLastName(),
            spouse.getChineseName(),
            spouse.getDateOfBirth(),
            spouse.getGender(),
            spouse.getHkid(),
            spouse.getPassport(),
            spouse.getNationality(),
            spouse.getEmail(),
            spouse.getPhoneNo(),
            spouse.getCreatedBy(),
            spouse.getUserstamp()
        );

        logger.info("Inserted PersonSpouse for offer: {}", spouse.getOfferId());
    }

    /**
     * Delete spouse by offer ID
     *
     * @param offerId Offer ID
     * @param reqNumber Requisition number
     */
    public void deleteSpouse(String offerId, String reqNumber) {
        String sql = "DELETE FROM RDPS_PERSON_SPOUSE WHERE OFFER_ID = ? AND REQ_NUMBER = ?";
        jdbcTemplate.update(sql, offerId, reqNumber);
        logger.debug("Deleted PersonSpouse for offer: {}", offerId);
    }
}
