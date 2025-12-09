package eduhk.fhr.web.dao;

import org.springframework.stereotype.Repository;

import eduhk.fhr.web.model.PersonInfo;

/**
 * PersonInfoDAO
 *
 * Data Access Object for RDPS_PERSON_INFO table operations.
 */
@Repository
public class PersonInfoDAO extends BaseDao {

    /**
     * Insert a new person info record
     *
     * @param personInfo PersonInfo model to insert
     */
    public void insertPersonInfo(PersonInfo personInfo) {
        String sql = "INSERT INTO RDPS_PERSON_INFO (" +
                "OFFER_ID, REQ_NUMBER, PERSON_NUMBER, NATIONALITY, PLACE_OF_ORIGIN, " +
                "HIGHEST_EDUCATION, MARITAL_STATUS, STATUS_DATE, " +
                "VISA_ISSUE_DATE, VISA_EXPIRY_DATE, IMMIGRATION_STATUS, HK_ENTRY_DATE, " +
                "CREATED_BY, CREATION_DATE, USERSTAMP, TIMESTAMP" +
                ") VALUES (" +
                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, ?, SYSDATE" +
                ")";

        jdbcTemplate.update(sql,
            personInfo.getOfferId(),
            personInfo.getReqNumber(),
            personInfo.getPersonNumber(),
            personInfo.getNationality(),
            personInfo.getPlaceOfOrigin(),
            personInfo.getHighestEducation(),
            personInfo.getMaritalStatus(),
            personInfo.getStatusDate(),
            personInfo.getVisaIssueDate(),
            personInfo.getVisaExpiryDate(),
            personInfo.getImmigrationStatus(),
            personInfo.getHkEntryDate(),
            personInfo.getCreatedBy(),
            personInfo.getUserstamp()
        );

        logger.info("Inserted PersonInfo for offer: {}", personInfo.getOfferId());
    }

    /**
     * Delete person info by offer ID
     *
     * @param offerId Offer ID
     * @param reqNumber Requisition number
     */
    public void deletePersonInfo(String offerId, String reqNumber) {
        String sql = "DELETE FROM RDPS_PERSON_INFO WHERE OFFER_ID = ? AND REQ_NUMBER = ?";
        jdbcTemplate.update(sql, offerId, reqNumber);
        logger.debug("Deleted PersonInfo for offer: {}", offerId);
    }
}
