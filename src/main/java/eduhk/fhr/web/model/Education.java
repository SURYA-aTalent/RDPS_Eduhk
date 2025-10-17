package eduhk.fhr.web.model;

// File Path: src/main/java/eduhk/fhr/web/model/Education.java
// Purpose: Model representing RDPS_EDU_PROF_QUAL table

import java.util.Date;

/**
 * Education Model
 *
 * Represents an education/qualification record in the RDPS_EDU_PROF_QUAL table.
 */
public class Education {

    private String candidateId;
    private String reqNumber;
    private Integer seq;
    private String institution;
    private String country;
    private Integer eduLevel;
    private Integer studyMode;
    private Integer qualAwardDesc;
    private Integer qualAwardClass;
    private String others;
    private String majorStudyArea;
    private Date startDate;
    private Date dateOfAward;
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

    public Integer getEduLevel() {
        return eduLevel;
    }

    public void setEduLevel(Integer eduLevel) {
        this.eduLevel = eduLevel;
    }

    public Integer getStudyMode() {
        return studyMode;
    }

    public void setStudyMode(Integer studyMode) {
        this.studyMode = studyMode;
    }

    public Integer getQualAwardDesc() {
        return qualAwardDesc;
    }

    public void setQualAwardDesc(Integer qualAwardDesc) {
        this.qualAwardDesc = qualAwardDesc;
    }

    public Integer getQualAwardClass() {
        return qualAwardClass;
    }

    public void setQualAwardClass(Integer qualAwardClass) {
        this.qualAwardClass = qualAwardClass;
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

    public Date getDateOfAward() {
        return dateOfAward;
    }

    public void setDateOfAward(Date dateOfAward) {
        this.dateOfAward = dateOfAward;
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
