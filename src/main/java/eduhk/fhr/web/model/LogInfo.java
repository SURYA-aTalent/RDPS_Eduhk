package eduhk.fhr.web.model;

public class LogInfo extends BaseModel {
	
	private String name;
    private long size;
    private String lastModified;
    
    public LogInfo(String name, long size, String lastModified) {
        this.name = name;
        this.size = size;
        this.lastModified = lastModified;
    }
    
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getLastModified() {
		return lastModified;
	}
	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}
	
}

