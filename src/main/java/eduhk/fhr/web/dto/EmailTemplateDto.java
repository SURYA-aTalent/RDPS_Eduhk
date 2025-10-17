package eduhk.fhr.web.dto;

import jakarta.validation.constraints.NotBlank; 

public class EmailTemplateDto {
	@NotBlank
	private String emailType;
	@NotBlank
	private String emailForm;
	@NotBlank
	private String emailSubject;
	@NotBlank
	private String emailBody;

	public String getEmailForm() {
		return emailForm;
	}

	public void setEmailForm(String emailForm) {
		this.emailForm = emailForm;
	}

	public String getEmailType() {
		return emailType;
	}

	public void setEmailType(String emailType) {
		this.emailType = emailType;
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

	@Override
	public String toString() {
		return "EmailTemplateDto [emailType=" + emailType + ", emailForm=" + emailForm + ", emailSubject=" + emailSubject + ", emailBody=" + emailBody + "]";
	}

}
