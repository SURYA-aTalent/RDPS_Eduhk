package eduhk.fhr.web.model;

public class SchedulerModel extends BaseModel {
	
	private int scheduleId;
	private String description;
	
	public int getScheduleId() {
		return scheduleId;
	}
	public void setScheduleId(int scheduleId) {
		this.scheduleId = scheduleId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}

