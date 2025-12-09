package eduhk.fhr.web.model;

import java.util.Date;

/**
 * Person Emergency Contact Person Model
 *
 * Represents emergency contact information record in the RDPS_PERSON_ECP table.
 */
public class PersonECP {

    // Primary keys
    private String offerId;
    private String reqNumber;

    // Contact person details
    private String fullName;
    private String phoneNo;
    private String relationship;

    // Audit fields
    private String createdBy;
    private Date creationDate;
    private String userstamp;
    private Date timestamp;

    // Getters and Setters

    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public String getReqNumber() {
        return reqNumber;
    }

    public void setReqNumber(String reqNumber) {
        this.reqNumber = reqNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getUserstamp() {
        return userstamp;
    }

    public void setUserstamp(String userstamp) {
        this.userstamp = userstamp;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "PersonECP{" +
                "offerId='" + offerId + '\'' +
                ", reqNumber='" + reqNumber + '\'' +
                ", fullName='" + fullName + '\'' +
                ", relationship='" + relationship + '\'' +
                '}';
    }
}
