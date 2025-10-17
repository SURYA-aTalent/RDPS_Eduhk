package eduhk.fhr.web.dto;

import java.util.Date;

public class CkFile {
	private String fileName;
	private Date uploadedDate;
	private String url;
	private String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Date getUploadedDate() {
		return uploadedDate;
	}

	public void setUploadedDate(Date uploadedDate) {
		this.uploadedDate = uploadedDate;
	}

	@Override
	public String toString() {
		return "CkFile [fileName=" + fileName + ", uploadedDate=" + uploadedDate + ", url=" + url + ", message=" + message + "]";
	}

}
