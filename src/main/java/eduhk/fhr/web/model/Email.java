package eduhk.fhr.web.model;

public class Email extends BaseModel {
	
	private int emailId;
	private String recipient;
	private String cc;
	private String bcc;
	private String emailSubject;
	private String emailBody;
	private String fhrJobId;
	
	public int getEmailId() {
		return emailId;
	}
	public void setEmailId(int emailId) {
		this.emailId = emailId;
	}
	public String getRecipient() {
		return recipient;
	}
	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}
	public String getCc() {
		return cc;
	}
	public void setCc(String cc) {
		this.cc = cc;
	}
	public String getBcc() {
		return bcc;
	}
	public void setBcc(String bcc) {
		this.bcc = bcc;
	}
	public String getEmailSubject() {
		return emailSubject;
	}
	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}
	public String getEmailBody() {
		return emailBody;
	}
	public void setEmailBody(String emailBody) {
		this.emailBody = emailBody;
	}
	public String getFhrJobId() {
		return fhrJobId;
	}
	public void setFhrJobId(String fhrJobId) {
		this.fhrJobId = fhrJobId;
	}
	
}
