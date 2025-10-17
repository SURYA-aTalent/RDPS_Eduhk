package eduhk.fhr.web.model;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;

public class User {
	private String username;
	private String name;
	private String department;
	private String instituteId;

	private List<GrantedAuthority> authorities;

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public List<GrantedAuthority> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(List<GrantedAuthority> authorities) {
		this.authorities = authorities;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String userName) {
		this.username = userName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getInstituteId() {
		return instituteId;
	}

	public void setInstituteId(String instituteId) {
		this.instituteId = instituteId;
	}

	@Override
	public String toString() {
		return "User [username=" + username + ", name=" + name + ", department=" + department + ", instituteId=" + instituteId + ", authorities=" + authorities + "]";
	}

}
