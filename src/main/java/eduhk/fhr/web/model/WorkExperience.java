package eduhk.fhr.web.model;

// File Path: src/main/java/eduhk/fhr/web/model/WorkExperience.java
// Purpose: Model representing RDPS_WORK_EXPERIENCE table

import java.util.Date;

/**
 * Work Experience Model
 *
 * Represents a work experience record in the RDPS_WORK_EXPERIENCE table.
 */
public class WorkExperience {

    private String candidateId;
    private String reqNumber;
    private Integer seq;
    private String employerName;
    private String natureOfBusiness;
    private String positionTitle;
    private String currentJob;
    private String modeOfEmployment;
    private String hoursPerWeek;
    private Date startDate;
    private Date endDate;
    private String natureOfDuties;
    private String createdBy;
    private Date creationDate;
    private String userstamp;
    private Date timestamp;

    // Getters and Setters

    public String getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(String candidateId) {
        this.candidateId = candidateId;
    }

    public String getReqNumber() {
        return reqNumber;
    }

    public void setReqNumber(String reqNumber) {
        this.reqNumber = reqNumber;
    }

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public String getEmployerName() {
        return employerName;
    }

    public void setEmployerName(String employerName) {
        this.employerName = employerName;
    }

    public String getNatureOfBusiness() {
        return natureOfBusiness;
    }

    public void setNatureOfBusiness(String natureOfBusiness) {
        this.natureOfBusiness = natureOfBusiness;
    }

    public String getPositionTitle() {
        return positionTitle;
    }

    public void setPositionTitle(String positionTitle) {
        this.positionTitle = positionTitle;
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

    public String getNatureOfDuties() {
        return natureOfDuties;
    }

    public void setNatureOfDuties(String natureOfDuties) {
        this.natureOfDuties = natureOfDuties;
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
}
