package eduhk.fhr.web.dto.talentlink;

import java.util.Date;

/**
 * TalentLink Person Info DTO
 *
 * Represents personal information data extracted from
 * "EdUHK Onboarding Data Submission Questionnaire (Candidate)" - Personal Info section
 *
 * Maps to: RDPS_PERSON_INFO table
 */
public class TalentLinkPersonInfoDTO {

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

    // Getters and Setters

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

    @Override
    public String toString() {
        return "TalentLinkPersonInfoDTO{" +
                "personNumber='" + personNumber + '\'' +
                ", nationality='" + nationality + '\'' +
                ", placeOfOrigin='" + placeOfOrigin + '\'' +
                ", highestEducation='" + highestEducation + '\'' +
                ", maritalStatus='" + maritalStatus + '\'' +
                ", statusDate=" + statusDate +
                ", immigrationStatus='" + immigrationStatus + '\'' +
                ", hkEntryDate=" + hkEntryDate +
                '}';
    }
}
