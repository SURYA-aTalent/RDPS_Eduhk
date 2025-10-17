package eduhk.fhr.web.model;

// File Path: src/main/java/eduhk/fhr/web/model/OtherInfo.java
// Purpose: Model representing RDPS_OTHER_INFO table

import java.math.BigDecimal;
import java.util.Date;

/**
 * Other Info Model
 *
 * Represents other candidate information in the RDPS_OTHER_INFO table.
 */
public class OtherInfo {

    private String candidateId;
    private String reqNumber;
    private BigDecimal salary;
    private Integer numberOfMonths;
    private BigDecimal allowanceAmount;
    private BigDecimal allowancePct;
    private String allowanceType;
    private String allowanceNature;
    private BigDecimal gratuityAmount;
    private BigDecimal gratuityBasic;
    private String otherBenefits;
    private String skills;
    private Date nextSalaryReviewDate;
    private BigDecimal expectedSalary;
    private String noticePeriod;
    private Date dateOfAvailability;
    private String eduhkEmployee;
    private String staffNumber;
    private String sourceType;
    private String sourceTypeOther;
    private String dccpr;
    private String dccprDetails;
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

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public Integer getNumberOfMonths() {
        return numberOfMonths;
    }

    public void setNumberOfMonths(Integer numberOfMonths) {
        this.numberOfMonths = numberOfMonths;
    }

    public BigDecimal getAllowanceAmount() {
        return allowanceAmount;
    }

    public void setAllowanceAmount(BigDecimal allowanceAmount) {
        this.allowanceAmount = allowanceAmount;
    }

    public BigDecimal getAllowancePct() {
        return allowancePct;
    }

    public void setAllowancePct(BigDecimal allowancePct) {
        this.allowancePct = allowancePct;
    }

    public String getAllowanceType() {
        return allowanceType;
    }

    public void setAllowanceType(String allowanceType) {
        this.allowanceType = allowanceType;
    }

    public String getAllowanceNature() {
        return allowanceNature;
    }

    public void setAllowanceNature(String allowanceNature) {
        this.allowanceNature = allowanceNature;
    }

    public BigDecimal getGratuityAmount() {
        return gratuityAmount;
    }

    public void setGratuityAmount(BigDecimal gratuityAmount) {
        this.gratuityAmount = gratuityAmount;
    }

    public BigDecimal getGratuityBasic() {
        return gratuityBasic;
    }

    public void setGratuityBasic(BigDecimal gratuityBasic) {
        this.gratuityBasic = gratuityBasic;
    }

    public String getOtherBenefits() {
        return otherBenefits;
    }

    public void setOtherBenefits(String otherBenefits) {
        this.otherBenefits = otherBenefits;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public Date getNextSalaryReviewDate() {
        return nextSalaryReviewDate;
    }

    public void setNextSalaryReviewDate(Date nextSalaryReviewDate) {
        this.nextSalaryReviewDate = nextSalaryReviewDate;
    }

    public BigDecimal getExpectedSalary() {
        return expectedSalary;
    }

    public void setExpectedSalary(BigDecimal expectedSalary) {
        this.expectedSalary = expectedSalary;
    }

    public String getNoticePeriod() {
        return noticePeriod;
    }

    public void setNoticePeriod(String noticePeriod) {
        this.noticePeriod = noticePeriod;
    }

    public Date getDateOfAvailability() {
        return dateOfAvailability;
    }

    public void setDateOfAvailability(Date dateOfAvailability) {
        this.dateOfAvailability = dateOfAvailability;
    }

    public String getEduhkEmployee() {
        return eduhkEmployee;
    }

    public void setEduhkEmployee(String eduhkEmployee) {
        this.eduhkEmployee = eduhkEmployee;
    }

    public String getStaffNumber() {
        return staffNumber;
    }

    public void setStaffNumber(String staffNumber) {
        this.staffNumber = staffNumber;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourceTypeOther() {
        return sourceTypeOther;
    }

    public void setSourceTypeOther(String sourceTypeOther) {
        this.sourceTypeOther = sourceTypeOther;
    }

    public String getDccpr() {
        return dccpr;
    }

    public void setDccpr(String dccpr) {
        this.dccpr = dccpr;
    }

    public String getDccprDetails() {
        return dccprDetails;
    }

    public void setDccprDetails(String dccprDetails) {
        this.dccprDetails = dccprDetails;
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
