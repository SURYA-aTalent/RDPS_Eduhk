package eduhk.fhr.web.model;

public class SampleBooking extends BaseModel {
	
	private int bookingId;
	private String staffNumber;
	private String staffName;
	private String emailAddress;
	private String ackEmailSentDate;
	private String cancelEmailSentDate;
	private String clientIp;
	private String clientInfo;
	
	public int getBookingId() {
		return bookingId;
	}
	public void setBookingId(int bookingId) {
		this.bookingId = bookingId;
	}
	public String getStaffNumber() {
		return staffNumber;
	}
	public void setStaffNumber(String staffNumber) {
		this.staffNumber = staffNumber;
	}
	public String getStaffName() {
		return staffName;
	}
	public void setStaffName(String staffName) {
		this.staffName = staffName;
	}
	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}	
	public String getAckEmailSentDate() {
		return ackEmailSentDate;
	}
	public void setAckEmailSentDate(String ackEmailSentDate) {
		this.ackEmailSentDate = ackEmailSentDate;
	}
	public String getCancelEmailSentDate() {
		return cancelEmailSentDate;
	}
	public void setCancelEmailSentDate(String cancelEmailSentDate) {
		this.cancelEmailSentDate = cancelEmailSentDate;
	}
	public String getClientIp() {
		return clientIp;
	}
	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}
	public String getClientInfo() {
		return clientInfo;
	}
	public void setClientInfo(String clientInfo) {
		this.clientInfo = clientInfo;
	}
	
}

