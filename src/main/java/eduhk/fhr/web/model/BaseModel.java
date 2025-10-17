package eduhk.fhr.web.model;

import java.sql.ResultSet;
import java.util.Date;

public class BaseModel {

	private Date creationDate;
	private String createdBy;
	private Date timestamp;
	private String userstamp;

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		if (this.creationDate == null) {
			this.creationDate = timestamp;
		}
		this.timestamp = timestamp;
	}

	public String getUserstamp() {
		return userstamp;
	}

	public void mapResultSet(ResultSet rs) {
		try {
			this.setCreatedBy(rs.getString("created_by"));
			this.setCreationDate(rs.getTimestamp("creation_date"));
			this.setUserstamp(rs.getString("userstamp"));
			this.setTimestamp(rs.getTimestamp("timestamp"));
		} catch (Exception e) {

		}
	}

	public void setUserstamp(String userstamp) {
		if (this.createdBy == null) {
			this.createdBy = userstamp;
		}
		this.userstamp = userstamp;
	}

}
