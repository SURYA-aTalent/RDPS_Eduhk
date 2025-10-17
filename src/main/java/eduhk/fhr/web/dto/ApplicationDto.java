package eduhk.fhr.web.dto;

import java.util.Date;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank; 

public class ApplicationDto extends BaseDto {
	
	private String applicationNo;
	@NotBlank (message = "Scheme Code cannot be blank.")
	private String schemeCode;
	@NotBlank (message = "Academic Year cannot be blank.")
	private String academicYear;
	@NotBlank (message = "Student ID cannot be blank.")
	private String studentId;
	@NotBlank (message = "Student grant-loan scheme cannot be blank")
	private String grantLoanLevel;
	private String activityOrganizer;
	private String activityName;
	@NotBlank (message = "Exchange Country cannot be blank.")
	private String destination;
	@NotBlank (message = "Destination cannot be blank.")
	private String destinationCategory;
	@NotNull (message = "Duration cannot be blank")
	private Date durationFrom;
	@NotNull (message = "Duration cannot be blank")
	private Date durationTo;
	@NotNull (message = "Received Date cannot be blank")
	private Date receivedDate;
	private String remarks;
	@NotNull (message = "Assistance Level cannot be blank.")
	private Double assistanceLevel;
	@NotNull (message = "Subsidy Amount cannot be blank.")
	private Double subsidyAmount;
	private double actualExpenses;
	@NotNull (message = "First Installment cannot be blank.")
	private double firstInstallment;
	@NotNull (message = "Second Installment cannot be blank.")
	private double secondInstallment;
	private Date firstInstallDate;
	private Date secondInstallDate;

	public String getApplicationNo() {
		return applicationNo;
	}

	public void setApplicationNo(String applicationNo) {
		this.applicationNo = applicationNo;
	}

	public String getSchemeCode() {
		return schemeCode;
	}

	public void setSchemeCode(String schemeCode) {
		this.schemeCode = schemeCode;
	}

	public String getAcademicYear() {
		return academicYear;
	}

	public void setAcademicYear(String academicYear) {
		this.academicYear = academicYear;
	}

	public String getStudentId() {
		return studentId;
	}

	public void setStudentId(String studentId) {
		this.studentId = studentId;
	}

	public String getGrantLoanLevel() {
		return grantLoanLevel;
	}

	public void setGrantLoanLevel(String grantLoanLevel) {
		this.grantLoanLevel = grantLoanLevel;
	}

	public String getActivityOrganizer() {
		return activityOrganizer;
	}

	public void setActivityOrganizer(String activityOrganizer) {
		this.activityOrganizer = activityOrganizer;
	}

	public String getActivityName() {
		return activityName;
	}

	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getDestinationCategory() {
		return destinationCategory;
	}

	public void setDestinationCategory(String destinationCategory) {
		this.destinationCategory = destinationCategory;
	}

	public Date getDurationFrom() {
		return durationFrom;
	}

	public void setDurationFrom(Date durationFrom) {
		this.durationFrom = durationFrom;
	}

	public Date getDurationTo() {
		return durationTo;
	}

	public void setDurationTo(Date durationTo) {
		this.durationTo = durationTo;
	}

	public Date getReceivedDate() {
		return receivedDate;
	}

	public void setReceivedDate(Date receivedDate) {
		this.receivedDate = receivedDate;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public Double getAssistanceLevel() {
		return assistanceLevel;
	}

	public void setAssistanceLevel(Double assistanceLevel) {
		this.assistanceLevel = assistanceLevel;
	}

	public Double getSubsidyAmount() {
		return subsidyAmount;
	}

	public void setSubsidyAmount(Double subsidyAmount) {
		this.subsidyAmount = subsidyAmount;
	}

	public double getActualExpenses() {
		return actualExpenses;
	}

	public void setActualExpenses(double actualExpenses) {
		this.actualExpenses = actualExpenses;
	}

	public double getFirstInstallment() {
		return firstInstallment;
	}

	public void setFirstInstallment(double firstInstallment) {
		this.firstInstallment = firstInstallment;
	}

	public double getSecondInstallment() {
		return secondInstallment;
	}

	public void setSecondInstallment(double secondInstallment) {
		this.secondInstallment = secondInstallment;
	}

	public Date getFirstInstallDate() {
		return firstInstallDate;
	}

	public void setFirstInstallDate(Date firstInstallDate) {
		this.firstInstallDate = firstInstallDate;
	}

	public Date getSecondInstallDate() {
		return secondInstallDate;
	}

	public void setSecondInstallDate(Date sInstallDate) {
		this.secondInstallDate = sInstallDate;
	}

	@Override
	public String toString() {
		return "ApplicationDto [applicationNo=" + applicationNo + ", schemeCode=" + schemeCode + ", academicYear="
				+ academicYear + ", studentId=" + studentId + ", grantLoanLevel=" + grantLoanLevel
				+ ", activityOrganizer=" + activityOrganizer + ", activityName=" + activityName + ", destination="
				+ destination + ", destinationCategory=" + destinationCategory + ", durationFrom=" + durationFrom
				+ ", durationTo=" + durationTo + ", receivedDate=" + receivedDate + ", remarks=" + remarks
				+ ", assistanceLevel=" + assistanceLevel + ", subsidyAmount=" + subsidyAmount + ", actualExpenses="
				+ actualExpenses + ", firstInstallment=" + firstInstallment + ", secondInstallment=" + secondInstallment
				+ ", firstInstallDate=" + firstInstallDate + ", sInstallDate=" + secondInstallDate + "]";
	}

}
