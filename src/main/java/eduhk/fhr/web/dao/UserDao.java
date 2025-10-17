package eduhk.fhr.web.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import eduhk.fhr.web.config.Parameters;
import eduhk.fhr.web.model.User;
import eduhk.fhr.web.util.Common;

@Repository
public class UserDao {
	
	@Autowired
	@Qualifier("ssoJdbcTemplate")
	private JdbcTemplate ssoJdbcTemplate;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private Parameters parameters;
	
	public void setSsoJdbcTemplate(JdbcTemplate ssoJdbcTemplate) {
		this.ssoJdbcTemplate = ssoJdbcTemplate;
	}
	
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public User findByUsername(String username) {
		User user = null;
		List<GrantedAuthority> authorities = new ArrayList<>();
		if (username.equals(parameters.getExternalGuestUserName())) {
			user = new User();
			user.setUsername(username);
			user.setName(parameters.getExternalGuestName());
			authorities.add(new SimpleGrantedAuthority("ROLE_GUEST"));
		} else {
			String sql =
			"SELECT * FROM ( " +
				"SELECT nds.cn, nds.institute_id, UPPER(p.surname) || ' ' || INITCAP(p.othername) AS name, ae.department_code AS department FROM nds_user_info nds " +
				"LEFT OUTER JOIN staff sf ON sf.staff_number = nds.institute_id  " +
				"LEFT OUTER JOIN person p ON p.pid = sf.pid " +
				"LEFT OUTER JOIN active_employment ae ON ae.staff_number = nds.institute_id " +
				"WHERE UPPER(nds.cn) = UPPER(?) AND nds.institute_id LIKE '8%' " +
				"ORDER BY ae.department_code NULLS LAST, nds.institute_id " +
			") " +
			"WHERE ROWNUM = 1 "
			;
			try {
				user = ssoJdbcTemplate.queryForObject(sql, new UserRowMapper(), username);
				authorities.add(new SimpleGrantedAuthority("ROLE_STAFF"));
            // this application is staff application, therefore, student role is not needed to check.
            /*
			} catch (EmptyResultDataAccessException e) {
				sql = 	"SELECT nds.cn, nds.institute_id, UPPER(sp.spriden_last_name) || ' ' || INITCAP(sp.spriden_first_name) || ' ' || INITCAP(sp.spriden_mi) AS name, '' AS department, '' AS rank_type  " +
						"FROM nds_user_info nds  " +
						"LEFT OUTER JOIN spriden sp ON sp.spriden_id = nds.institute_id AND sp.spriden_change_ind IS NULL  " +
						"WHERE UPPER(nds.cn) = UPPER(?) AND nds.account_status = 'ANS' AND nds.institute_id NOT LIKE '8%' ";
				user = ssoJdbcTemplate.queryForObject(sql, new UserRowMapper(), username);
				authorities.add(new SimpleGrantedAuthority("ROLE_STUDENT"));
            */
			} catch (Exception e) {
				// Fallback for local development when SSO tables are not available
				// Suppressing error log to reduce noise in development
				// Common.printException(e, null, false);
				user = new User();
				user.setUsername(username);
				user.setName("Local Development User");
				user.setInstituteId("DEV001");
				user.setDepartment("Local Development");
				authorities.add(new SimpleGrantedAuthority("ROLE_STAFF"));
			}

			sql = 	"SELECT up.role, ur.fcn " +
					"FROM "+parameters.getTablePrefix()+"user_profile up "+
					"LEFT OUTER JOIN "+parameters.getTablePrefix()+"user_role ur ON up.role = ur.role AND ur.active = 'Y' "+
					"WHERE UPPER(up.cn) = UPPER(?) "+
					"AND up.active = 'Y' "+
					"AND COALESCE(up.effective_date_from, CURRENT_DATE) <= CURRENT_DATE "+
					"AND COALESCE(up.effective_date_to, CURRENT_DATE) >= CURRENT_DATE";
			authorities.addAll(jdbcTemplate.query(sql, (rs, index) -> {
	//			String role = rs.getString("role");
				String fcn = rs.getString("fcn");
				if (fcn != null) {
					SimpleGrantedAuthority sga = new SimpleGrantedAuthority(rs.getString("fcn"));
					return sga;				
				}
				return new SimpleGrantedAuthority("ROLE_USER");
			},username));
		}
		user.setAuthorities(authorities);
		return user;
	}

}

@Component
class UserRowMapper implements RowMapper<User> {

	@Override
	public User mapRow(ResultSet rs, int index) throws SQLException {
		User u = new User();
		u.setUsername(rs.getString("cn").toUpperCase());
		u.setInstituteId(rs.getString("institute_id"));
		u.setName(rs.getString("name"));
		u.setDepartment(rs.getString("department"));
		return u;
	}
	
}
