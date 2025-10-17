package eduhk.fhr.web.dto;

import java.util.Date;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank; 

public class OcoolPrepareDto extends BaseDto {
	
	@NotNull (message = "Round Number cannot be blank.")
	private int roundNo;	
	private String questionnaireNo;
	@NotBlank (message = "Term cannot be blank.")
	private String term;
	@NotBlank (message = "CRN cannot be blank.")
	private String crn;
	@NotBlank (message = "Course Code cannot be blank.")
	private String courseCode;
	@NotBlank (message = "Section cannot be blank.")
	private String section;
//	@NotBlank (message = "Cross List Code cannot be blank.")
	private String crossListCode;
	@NotBlank (message = "Hosting cannot be blank.")
	private String hosting;
	@NotBlank (message = "Credit Point cannot be blank.")
	private String creditPoint;
	@NotBlank (message = "Short Course Title cannot be blank.")
	private String shortCourseTitle;
	@NotBlank (message = "Programme Code cannot be blank.")
	private String progCode;
	private Date lastSubmissionDate;

	private String instructorId;

	public int getRoundNo() {
		return roundNo;
	}

	public void setRoundNo(int roundNo) {
		this.roundNo = roundNo;
	}

	public String getQuestionnaireNo() {
		return questionnaireNo;
	}

	public void setQuestionnaireNo(String questionnaireNo) {
		this.questionnaireNo = questionnaireNo;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public String getCrn() {
		return crn;
	}

	public void setCrn(String crn) {
		this.crn = crn;
	}

	public String getCourseCode() {
		return courseCode;
	}

	public void setCourseCode(String courseCode) {
		this.courseCode = courseCode;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public String getCrossListCode() {
		return crossListCode;
	}

	public void setCrossListCode(String crossListCode) {
		this.crossListCode = crossListCode;
	}

	public String getHosting() {
		return hosting;
	}

	public void setHosting(String hosting) {
		this.hosting = hosting;
	}

	public String getCreditPoint() {
		return creditPoint;
	}

	public void setCreditPoint(String creditPoint) {
		this.creditPoint = creditPoint;
	}

	public String getShortCourseTitle() {
		return shortCourseTitle;
	}

	public void setShortCourseTitle(String shortCourseTitle) {
		this.shortCourseTitle = shortCourseTitle;
	}

	public String getProgCode() {
		return progCode;
	}

	public void setProgCode(String progCode) {
		this.progCode = progCode;
	}

	public Date getLastSubmissionDate() {
		return lastSubmissionDate;
	}

	public void setLastSubmissionDate(Date lastSubmissionDate) {
		this.lastSubmissionDate = lastSubmissionDate;
	}

	public String getInstructorId() {
		return instructorId;
	}

	public void setInstructorId(String instructorId) {
		this.instructorId = instructorId;
	}

}
