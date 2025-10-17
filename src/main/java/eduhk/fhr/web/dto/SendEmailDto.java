package eduhk.fhr.web.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class SendEmailDto {

	private String emailForm;
	private int roundNo;
	private String emailType;
	private boolean executeNow;
	@JsonFormat(pattern = "dd/MM/yyyy")
	private Date scheduleDate;

	public String getEmailForm() {
		return emailForm;
	}

	public void setEmailForm(String emailForm) {
		this.emailForm = emailForm;
	}

	public int getRoundNo() {
		return roundNo;
	}

	public void setRoundNo(int roundNo) {
		this.roundNo = roundNo;
	}

	public String getEmailType() {
		return emailType;
	}

	public void setEmailType(String emailType) {
		this.emailType = emailType;
	}

	public boolean isExecuteNow() {
		return executeNow;
	}

	public void setExecuteNow(boolean executeNow) {
		this.executeNow = executeNow;
	}

	public Date getScheduleDate() {
		return scheduleDate;
	}

	public void setScheduleDate(Date scheduleDate) {
		this.scheduleDate = scheduleDate;
	}

	@Override
	public String toString() {
		return "SendEmailDto [emailForm=" + emailForm + ", roundNo=" + roundNo + ", emailType=" + emailType + ", executeNow=" + executeNow + ", scheduleDate=" + scheduleDate + "]";
	}

}
