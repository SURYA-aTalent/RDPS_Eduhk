package eduhk.fhr.web.dto.talentlink;

import java.util.Date;

/**
 * TalentLink Child DTO
 *
 * Represents child information data extracted from
 * "EdUHK Onboarding Data Submission Questionnaire (Candidate)" - Child Info section
 *
 * This is a repeating section - multiple children can be listed.
 *
 * Maps to: RDPS_PERSON_CHILD table
 */
public class TalentLinkChildDTO {

    // Sequence number (for multiple children)
    private Integer childSeq;

    // Name fields
    private String fullName;
    private String firstName;
    private String lastName;
    private String chineseName;

    // Personal details
    private Date dateOfBirth;
    private String gender;  // M/F

    // Identity documents
    private String hkid;
    private String passport;
    private String nationality;

    // Getters and Setters

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

    @Override
    public String toString() {
        return "TalentLinkChildDTO{" +
                "childSeq=" + childSeq +
                ", fullName='" + fullName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", chineseName='" + chineseName + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", gender='" + gender + '\'' +
                ", nationality='" + nationality + '\'' +
                '}';
    }
}
