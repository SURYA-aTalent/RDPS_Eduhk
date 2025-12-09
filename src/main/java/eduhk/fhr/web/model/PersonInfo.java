package eduhk.fhr.web.model;

import java.util.Date;

/**
 * Person Info Model
 *
 * Represents personal information record in the RDPS_PERSON_INFO table.
 * Contains nationality, education, immigration, and visa information.
 */
public class PersonInfo {

    // Primary keys
    private String offerId;
    private String reqNumber;

    // Personal identification
    private String personNumber;
    private String nationality;
    private String placeOfOrigin;

    // Education and status
    private String highestEducation;
    private String maritalStatus;
    private Date statusDate;

    // Immigration and visa
    private Date visaIssueDate;
    private Date visaExpiryDate;
    private String immigrationStatus;
    private Date hkEntryDate;

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

    public String getPersonNumber() {
        return personNumber;
    }

    public void setPersonNumber(String personNumber) {
        this.personNumber = personNumber;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getPlaceOfOrigin() {
        return placeOfOrigin;
    }

    public void setPlaceOfOrigin(String placeOfOrigin) {
        this.placeOfOrigin = placeOfOrigin;
    }

    public String getHighestEducation() {
        return highestEducation;
    }

    public void setHighestEducation(String highestEducation) {
        this.highestEducation = highestEducation;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public Date getStatusDate() {
        return statusDate;
    }

    public void setStatusDate(Date statusDate) {
        this.statusDate = statusDate;
    }

    public Date getVisaIssueDate() {
        return visaIssueDate;
    }

    public void setVisaIssueDate(Date visaIssueDate) {
        this.visaIssueDate = visaIssueDate;
    }

    public Date getVisaExpiryDate() {
        return visaExpiryDate;
    }

    public void setVisaExpiryDate(Date visaExpiryDate) {
        this.visaExpiryDate = visaExpiryDate;
    }

    public String getImmigrationStatus() {
        return immigrationStatus;
    }

    public void setImmigrationStatus(String immigrationStatus) {
        this.immigrationStatus = immigrationStatus;
    }

    public Date getHkEntryDate() {
        return hkEntryDate;
    }

    public void setHkEntryDate(Date hkEntryDate) {
        this.hkEntryDate = hkEntryDate;
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
        return "PersonInfo{" +
                "offerId='" + offerId + '\'' +
                ", reqNumber='" + reqNumber + '\'' +
                ", personNumber='" + personNumber + '\'' +
                ", nationality='" + nationality + '\'' +
                ", maritalStatus='" + maritalStatus + '\'' +
                '}';
    }
}
