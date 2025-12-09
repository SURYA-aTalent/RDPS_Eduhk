package eduhk.fhr.web.model;

import java.util.Date;

/**
 * Person Child Model
 *
 * Represents child information record in the RDPS_PERSON_CHILD table.
 * Supports multiple children per offer (repeating section).
 */
public class PersonChild {

    // Primary keys
    private String offerId;
    private String reqNumber;
    private Integer childSeq;

    // Name fields
    private String fullName;
    private String firstName;
    private String lastName;
    private String chineseName;

    // Personal details
    private Date dateOfBirth;
    private String gender;

    // Identity documents
    private String hkid;
    private String passport;
    private String nationality;

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

    public Integer getChildSeq() {
        return childSeq;
    }

    public void setChildSeq(Integer childSeq) {
        this.childSeq = childSeq;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getChineseName() {
        return chineseName;
    }

    public void setChineseName(String chineseName) {
        this.chineseName = chineseName;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getHkid() {
        return hkid;
    }

    public void setHkid(String hkid) {
        this.hkid = hkid;
    }

    public String getPassport() {
        return passport;
    }

    public void setPassport(String passport) {
        this.passport = passport;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
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
        return "PersonChild{" +
                "offerId='" + offerId + '\'' +
                ", reqNumber='" + reqNumber + '\'' +
                ", childSeq=" + childSeq +
                ", fullName='" + fullName + '\'' +
                '}';
    }
}
