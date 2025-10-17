package eduhk.fhr.web.dto.talentlink;

// File Path: src/main/java/eduhk/fhr/web/dto/talentlink/TalentLinkWorkExperienceDTO.java
// Purpose: DTO representing work experience data from TalentLink API response

import java.util.Date;

/**
 * TalentLink Work Experience DTO
 *
 * Data Transfer Object for work experience information received from TalentLink API.
 */
public class TalentLinkWorkExperienceDTO {

    private String employerName;
    private String natureOfBusiness;
    private String positionTitle;
    private String currentJob;
    private String modeOfEmployment;
    private String hoursPerWeek;
    private Date startDate;
    private Date endDate;
    private String natureOfDuties;

    // Getters and Setters
    // TODO: Add getters and setters for all fields

    public String getEmployerName() {
        return employerName;
    }

    public void setEmployerName(String employerName) {
        this.employerName = employerName;
    }

    public String getPositionTitle() {
        return positionTitle;
    }

    public void setPositionTitle(String positionTitle) {
        this.positionTitle = positionTitle;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getNatureOfBusiness() {
        return natureOfBusiness;
    }

    public void setNatureOfBusiness(String natureOfBusiness) {
        this.natureOfBusiness = natureOfBusiness;
    }

    public String getCurrentJob() {
        return currentJob;
    }

    public void setCurrentJob(String currentJob) {
        this.currentJob = currentJob;
    }

    public String getModeOfEmployment() {
        return modeOfEmployment;
    }

    public void setModeOfEmployment(String modeOfEmployment) {
        this.modeOfEmployment = modeOfEmployment;
    }

    public String getHoursPerWeek() {
        return hoursPerWeek;
    }

    public void setHoursPerWeek(String hoursPerWeek) {
        this.hoursPerWeek = hoursPerWeek;
    }

    public String getNatureOfDuties() {
        return natureOfDuties;
    }

    public void setNatureOfDuties(String natureOfDuties) {
        this.natureOfDuties = natureOfDuties;
    }
}
