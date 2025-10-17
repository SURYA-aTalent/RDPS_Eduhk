package eduhk.fhr.web.model;

public class AsyncJobStatus {
	private int jobId;
	private String jobType;
	private int jobIdentifier;
	private String message;
	private String status;

	private String userstamp;

	public String getUserstamp() {
		return userstamp;
	}

	public void setUserstamp(String userstamp) {
		this.userstamp = userstamp;
	}

	public int getJobId() {
		return jobId;
	}

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

	public String getJobType() {
		return jobType;
	}

	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

	public int getJobIdentifier() {
		return jobIdentifier;
	}

	public void setJobIdentifier(int jobIdentifier) {
		this.jobIdentifier = jobIdentifier;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public AsyncJobStatus(String jobType, int jobIdentifier, String message, String status) {
		super();
		this.jobType = jobType;
		this.jobIdentifier = jobIdentifier;
		this.message = message;
		this.status = status;
	}

	public AsyncJobStatus() {
		super();
	}

	@Override
	public String toString() {
		return "AsyncJobStatus [jobId=" + jobId + ", jobType=" + jobType + ", jobIdentifier=" + jobIdentifier + ", message=" + message + ", status=" + status + ", userstamp=" + userstamp + "]";
	}

}
