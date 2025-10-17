package eduhk.fhr.web.model;

public class AjaxResponse {
	private String bsAlertLevel;
	private String notyAlertLevel;
	private String message;
	private boolean success;
	private Object result;

	public String getNotyAlertLevel() {
		return notyAlertLevel;
	}

	public void setNotyAlertLevel(String notyAlertLevel) {
		this.notyAlertLevel = notyAlertLevel;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getBsAlertLevel() {
		return bsAlertLevel;
	}

	public void setBsAlertLevel(String bsAlertLevel) {
		this.bsAlertLevel = bsAlertLevel;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public AjaxResponse(String bsAlertLevel, String message) {
		super();
		this.bsAlertLevel = bsAlertLevel;
		this.message = message;
	}

	public AjaxResponse() {
		super();
	}

	public void success(String message) {
		this.bsAlertLevel = "success";
		this.notyAlertLevel = "success";
		this.message = message;
		this.success = true;
	}

	public void success() {
		this.bsAlertLevel = "success";
		this.notyAlertLevel = "success";
		this.message = "";
		this.success = true;
	}

	public void error(String message) {
		this.bsAlertLevel = "danger";
		this.notyAlertLevel = "error";
		this.message = message;
		this.success = true;
	}

	public void fail(String message) {
		this.bsAlertLevel = "danger";
		this.notyAlertLevel = "error";
		this.message = message;
		this.success = false;
	}

	public void warn(String message) {
		this.bsAlertLevel = "warning";
		this.notyAlertLevel = "warning";
		this.message = message;
		this.success = true;
	}

}
