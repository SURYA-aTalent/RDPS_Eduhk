package eduhk.fhr.web.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Offer Model
 *
 * Represents a job offer record in the RDPS_OFFER table.
 * Contains employment details, contract information, compensation, and benefits eligibility.
 */
public class Offer {

    // Primary key and references
    private String offerId;
    private String candidateId;
    private String reqNumber;
    private String offerStatus;

    // Employment details
    private String payBasis;        // Y=Yearly/H=Hourly/M=Monthly
    private String empDep;           // Department
    private String toa;              // Terms of Appointment (IC/IS/TMP/RSS)
    private String funcTitle;        // Functional Title
    private String band;             // Band
    private String grade;            // Job Grade
    private String post;             // Post

    // Contract dates
    private Date contractStartDate;
    private Date contractEndDate;

    // Probation period
    private Integer probationLength;
    private String probationUnits;   // Y=Year/H=Hour/M=Month

    // Notice period
    private Integer noticeLength;
    private String noticeUnits;      // M=Months/D=Days

    // Compensation
    private BigDecimal salaryAmount;
    private BigDecimal gratuityPct;

    // Employment mode
    private String empMode;          // F=Full-time/H=Half-time/P=Part-time
    private Integer planHours;       // Planned hours/days

    // Benefits eligibility
    private String mpf;              // Y/N
    private String superannuation;   // Y/N
    private String pension;          // Y/N

    // Other
    private String projectTitle;
    private String offerRemarks;

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

    public String getOfferStatus() {
        return offerStatus;
    }

    public void setOfferStatus(String offerStatus) {
        this.offerStatus = offerStatus;
    }

    public String getPayBasis() {
        return payBasis;
    }

    public void setPayBasis(String payBasis) {
        this.payBasis = payBasis;
    }

    public String getEmpDep() {
        return empDep;
    }

    public void setEmpDep(String empDep) {
        this.empDep = empDep;
    }

    public String getToa() {
        return toa;
    }

    public void setToa(String toa) {
        this.toa = toa;
    }

    public String getFuncTitle() {
        return funcTitle;
    }

    public void setFuncTitle(String funcTitle) {
        this.funcTitle = funcTitle;
    }

    public String getBand() {
        return band;
    }

    public void setBand(String band) {
        this.band = band;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public Date getContractStartDate() {
        return contractStartDate;
    }

    public void setContractStartDate(Date contractStartDate) {
        this.contractStartDate = contractStartDate;
    }

    public Date getContractEndDate() {
        return contractEndDate;
    }

    public void setContractEndDate(Date contractEndDate) {
        this.contractEndDate = contractEndDate;
    }

    public Integer getProbationLength() {
        return probationLength;
    }

    public void setProbationLength(Integer probationLength) {
        this.probationLength = probationLength;
    }

    public String getProbationUnits() {
        return probationUnits;
    }

    public void setProbationUnits(String probationUnits) {
        this.probationUnits = probationUnits;
    }

    public Integer getNoticeLength() {
        return noticeLength;
    }

    public void setNoticeLength(Integer noticeLength) {
        this.noticeLength = noticeLength;
    }

    public String getNoticeUnits() {
        return noticeUnits;
    }

    public void setNoticeUnits(String noticeUnits) {
        this.noticeUnits = noticeUnits;
    }

    public BigDecimal getSalaryAmount() {
        return salaryAmount;
    }

    public void setSalaryAmount(BigDecimal salaryAmount) {
        this.salaryAmount = salaryAmount;
    }

    public BigDecimal getGratuityPct() {
        return gratuityPct;
    }

    public void setGratuityPct(BigDecimal gratuityPct) {
        this.gratuityPct = gratuityPct;
    }

    public String getEmpMode() {
        return empMode;
    }

    public void setEmpMode(String empMode) {
        this.empMode = empMode;
    }

    public Integer getPlanHours() {
        return planHours;
    }

    public void setPlanHours(Integer planHours) {
        this.planHours = planHours;
    }

    public String getMpf() {
        return mpf;
    }

    public void setMpf(String mpf) {
        this.mpf = mpf;
    }

    public String getSuperannuation() {
        return superannuation;
    }

    public void setSuperannuation(String superannuation) {
        this.superannuation = superannuation;
    }

    public String getPension() {
        return pension;
    }

    public void setPension(String pension) {
        this.pension = pension;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public String getOfferRemarks() {
        return offerRemarks;
    }

    public void setOfferRemarks(String offerRemarks) {
        this.offerRemarks = offerRemarks;
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
        return "Offer{" +
                "offerId='" + offerId + '\'' +
                ", candidateId='" + candidateId + '\'' +
                ", reqNumber='" + reqNumber + '\'' +
                ", offerStatus='" + offerStatus + '\'' +
                ", payBasis='" + payBasis + '\'' +
                ", salaryAmount=" + salaryAmount +
                ", contractStartDate=" + contractStartDate +
                '}';
    }
}
