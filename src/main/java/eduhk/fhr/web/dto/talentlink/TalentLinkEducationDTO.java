package eduhk.fhr.web.dto.talentlink;

// File Path: src/main/java/eduhk/fhr/web/dto/talentlink/TalentLinkEducationDTO.java
// Purpose: DTO representing education/qualification data from TalentLink API response

import java.util.Date;

/**
 * TalentLink Education DTO
 *
 * Data Transfer Object for education and professional qualification information
 * received from TalentLink API.
 */
public class TalentLinkEducationDTO {

    private String institution;
    private String country;
    private String educationLevel;
    private String studyMode;
    private String qualificationAwardDesc;
    private String qualificationAwardClass;
    private String others;
    private String majorStudyArea;
    private Date startDate;
    private Date dateOfAward;

    // Getters and Setters
    // TODO: Add getters and setters for all fields

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getEducationLevel() {
        return educationLevel;
    }

    public void setEducationLevel(String educationLevel) {
        this.educationLevel = educationLevel;
    }

    public Date getDateOfAward() {
        return dateOfAward;
    }

    public void setDateOfAward(Date dateOfAward) {
        this.dateOfAward = dateOfAward;
    }

    public String getStudyMode() {
        return studyMode;
    }

    public void setStudyMode(String studyMode) {
        this.studyMode = studyMode;
    }

    public String getQualificationAwardDesc() {
        return qualificationAwardDesc;
    }

    public void setQualificationAwardDesc(String qualificationAwardDesc) {
        this.qualificationAwardDesc = qualificationAwardDesc;
    }

    public String getQualificationAwardClass() {
        return qualificationAwardClass;
    }

    public void setQualificationAwardClass(String qualificationAwardClass) {
        this.qualificationAwardClass = qualificationAwardClass;
    }

    public String getOthers() {
        return others;
    }

    public void setOthers(String others) {
        this.others = others;
    }

    public String getMajorStudyArea() {
        return majorStudyArea;
    }

    public void setMajorStudyArea(String majorStudyArea) {
        this.majorStudyArea = majorStudyArea;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
}
