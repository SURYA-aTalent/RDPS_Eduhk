package eduhk.fhr.web.dto.talentlink;

// File Path: src/main/java/eduhk/fhr/web/dto/talentlink/TalentLinkOtherInfoDTO.java
// Purpose: DTO representing other information (salary, skills, etc.) from TalentLink API response

import java.math.BigDecimal;
import java.util.Date;

/**
 * TalentLink Other Info DTO
 *
 * Data Transfer Object for additional candidate information received from TalentLink API.
 * Includes salary, skills, availability, etc.
 */
public class TalentLinkOtherInfoDTO {

    private BigDecimal salary;
    private Integer numberOfMonths;
    private BigDecimal allowanceAmount;
    private BigDecimal allowancePercentage;
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

    // Getters and Setters
    // TODO: Add getters and setters for all fields

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public BigDecimal getExpectedSalary() {
        return expectedSalary;
    }

    public void setExpectedSalary(BigDecimal expectedSalary) {
        this.expectedSalary = expectedSalary;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getNoticePeriod() {
        return noticePeriod;
    }

    public void setNoticePeriod(String noticePeriod) {
        this.noticePeriod = noticePeriod;
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

    public BigDecimal getAllowancePercentage() {
        return allowancePercentage;
    }

    public void setAllowancePercentage(BigDecimal allowancePercentage) {
        this.allowancePercentage = allowancePercentage;
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

    public Date getNextSalaryReviewDate() {
        return nextSalaryReviewDate;
    }

    public void setNextSalaryReviewDate(Date nextSalaryReviewDate) {
        this.nextSalaryReviewDate = nextSalaryReviewDate;
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
}
