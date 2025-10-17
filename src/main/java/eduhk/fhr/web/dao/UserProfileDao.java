package eduhk.fhr.web.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import eduhk.fhr.web.model.UserProfile;

@Repository
public class UserProfileDao extends BaseDao{
	
	//get All available staff role
	public ArrayList<String> getRoleList() {
		String sql = "SELECT DISTINCT ur.role FROM rdps_user_role ur WHERE ur.active = 'Y'";
		MapSqlParameterSource params = new MapSqlParameterSource();
		return this.namedParameterJdbcTemplate.query(sql, params, new ResultSetExtractor<ArrayList<String>>() {
			public ArrayList<String> extractData(ResultSet rs) throws SQLException, DataAccessException {
				ArrayList<String> resultList = new ArrayList<String>();
				while (rs.next()) {
					resultList.add(rs.getString("role"));
				}
				return resultList;
			}
		});
	}
		
	public String deleteUserProfile(UserProfile userProfile, String userstamp) {
		String rtnMessage = "OK";
		try {
			String sqlForDeleteUser = "UPDATE RDPS_USER_PROFILE SET active = 'N', timestamp = CURRENT_DATE, userstamp = :userstamp " + 
								"WHERE cn = :networkID AND role = :role";
			MapSqlParameterSource paramsForDeleteUser = new MapSqlParameterSource();
			paramsForDeleteUser.addValue("networkID", userProfile.getCn());
			paramsForDeleteUser.addValue("role", userProfile.getRole());
			paramsForDeleteUser.addValue("userstamp", userstamp);
			
			namedParameterJdbcTemplate.update(sqlForDeleteUser, paramsForDeleteUser);
			rtnMessage = "deleted";
		} catch (Exception e) {
			rtnMessage = e.getMessage();
		}
		return rtnMessage;
	}
	
	public String saveUserProfile(UserProfile userProfile, String userstamp) {
		String rtnMessage = "OK";
		try {	
			String sql1 = "SELECT COUNT(*) FROM RDPS_USER_PROFILE WHERE cn = :networkID AND role = :role";
			String sql999 = "SELECT * FROM RDPS_USER_PROFILE WHERE cn = :networkID AND role = :role";
			MapSqlParameterSource params1 = new MapSqlParameterSource();
			params1.addValue("networkID", userProfile.getCn());
			params1.addValue("role", userProfile.getRole());
			
			int record = namedParameterJdbcTemplate.queryForObject(sql1, params1, int.class);	
			String active = "";
			String sqlForUpdateUser = "";
			List<Map<String, Object>> user = null;
						
			//If found this user in DB, then check he/she is active or not
			if(record > 0 ) {
				user = namedParameterJdbcTemplate.queryForList(sql999, params1);
				active = user.get(0).get("active").toString();
			}
			
			
			//If this user is already exist in DB & active = "Y", then send a warning message to user "User already exist"
			if(record > 0 && active.equals("Y")) {
				
				rtnMessage = "existed";	
				int updateRow = 0;		
									
				//Handling for update
				
				//Case 1: EffectiveDateTo is null in DB
				if(user.get(0).get("effective_date_to") == null) {	
					
					//update EffectiveDateFrom & EffectiveDateTo
					//effectiveDateFrom A -> effectiveDateFrom B
					//effectiveDateTo null -> effectiveDateTo A
					if(userProfile.getEffectiveDateTo() != "") {
						
						sqlForUpdateUser = "UPDATE RDPS_USER_PROFILE SET effective_date_from = PARSEDATETIME(:effectiveDateFrom, 'dd/MM/yyyy'), "+
										"effective_date_to = PARSEDATETIME(:effectiveDateTo, 'dd/MM/yyyy'), "+
										"timestamp = CURRENT_DATE, userstamp = :userstamp " + 
										"WHERE cn = :networkID AND role = :role ";
					} else {
						
						//update EffectiveDateFrom oldDate to newDate
						//effectiveDateFrom A -> effectiveDateFrom B
						sqlForUpdateUser = "UPDATE RDPS_USER_PROFILE SET effective_date_from = PARSEDATETIME(:effectiveDateFrom, 'dd/MM/yyyy'), " + 
										"timestamp = CURRENT_DATE, userstamp = :userstamp " + 
										"WHERE cn = :networkID AND role = :role "+ 
										"AND ( (effective_date_from != PARSEDATETIME(:effectiveDateFrom, 'dd/MM/yyyy')) )";
					}
					
					MapSqlParameterSource paramsForUpdateUser = new MapSqlParameterSource();
					paramsForUpdateUser.addValue("effectiveDateFrom", userProfile.getEffectiveDateFrom());
					paramsForUpdateUser.addValue("effectiveDateTo", userProfile.getEffectiveDateTo());
					paramsForUpdateUser.addValue("userstamp", userstamp);
					paramsForUpdateUser.addValue("networkID", userProfile.getCn());
					paramsForUpdateUser.addValue("role", userProfile.getRole());
					updateRow = namedParameterJdbcTemplate.update(sqlForUpdateUser, paramsForUpdateUser);
					
					//Case 2: EffectiveDateTo is NOT null in DB
				} else if(user.get(0).get("effective_date_to") != null){
					
					//update EffectiveDateFrom & EffectiveDateTo
					//effectiveDateFrom A -> effectiveDateFrom null
					if(userProfile.getEffectiveDateTo() == "") {
						
						sqlForUpdateUser = "UPDATE RDPS_USER_PROFILE SET effective_date_from = PARSEDATETIME(:effectiveDateFrom, 'dd/MM/yyyy'), "+
								"effective_date_to = PARSEDATETIME(:effectiveDateTo, 'dd/MM/yyyy'), timestamp = CURRENT_DATE, userstamp = :userstamp " + 
								"WHERE cn = :networkID AND role = :role ";
					} else {
						
					//update EffectiveDateFrom oldDate to newDate
					//effectiveDateFrom A -> effectiveDateFrom B
					//effectiveDateTo A -> effectiveDateTo B
						sqlForUpdateUser = "UPDATE RDPS_USER_PROFILE SET effective_date_from = PARSEDATETIME(:effectiveDateFrom, 'dd/MM/yyyy'), " + 
							"effective_date_to = PARSEDATETIME(:effectiveDateTo, 'dd/MM/yyyy'), timestamp = CURRENT_DATE, userstamp = :userstamp " + 
							"WHERE cn = :networkID AND role = :role "+ 
							"AND ( (effective_date_from != PARSEDATETIME(:effectiveDateFrom, 'dd/MM/yyyy')) " + 
							"OR (effective_date_to != PARSEDATETIME(:effectiveDateTo, 'dd/MM/yyyy')) )";
					}
					
					MapSqlParameterSource paramsForUpdateUser = new MapSqlParameterSource();
					paramsForUpdateUser.addValue("effectiveDateFrom", userProfile.getEffectiveDateFrom());
					paramsForUpdateUser.addValue("effectiveDateTo", userProfile.getEffectiveDateTo());
					paramsForUpdateUser.addValue("userstamp", userstamp);
					paramsForUpdateUser.addValue("networkID", userProfile.getCn());
					paramsForUpdateUser.addValue("role", userProfile.getRole());
					
					updateRow = namedParameterJdbcTemplate.update(sqlForUpdateUser, paramsForUpdateUser);
					
				}	
				
				//Show "update successful message when any row is updated in DB
				if(updateRow > 0) {
					rtnMessage = "updated";
				}				
				
			}else if(record > 0 && active.equals("N")) {
				//Handling for reuse deleted networkID
				//If this user is already exist in DB but NOT active, then set active = 'Y' and update effectiveDateFrom, effectiveDateTo
				
				String sqlForReuseCN = "UPDATE RDPS_USER_PROFILE SET effective_date_from = PARSEDATETIME(:effectiveDateFrom, 'dd/MM/yyyy'), " + 
						"effective_date_to = PARSEDATETIME(:effectiveDateTo, 'dd/MM/yyyy'), active = 'Y', timestamp = CURRENT_DATE, userstamp = :userstamp " + 
						"WHERE cn = :networkID AND role = :role ";
				
				MapSqlParameterSource paramsForReuseCN = new MapSqlParameterSource();
				paramsForReuseCN.addValue("effectiveDateFrom", userProfile.getEffectiveDateFrom());
				paramsForReuseCN.addValue("effectiveDateTo", userProfile.getEffectiveDateTo());
				paramsForReuseCN.addValue("userstamp", userstamp);
				paramsForReuseCN.addValue("networkID", userProfile.getCn());
				paramsForReuseCN.addValue("role", userProfile.getRole());
				
				namedParameterJdbcTemplate.update(sqlForReuseCN, paramsForReuseCN);
				rtnMessage = "added";
								
			}else if(record == 0){
				//Handling for if this user's networkID is not exist in DB, then insert a new record
								
				int rowsUpdated = 0;
				String sqlForAddNewUser = "INSERT INTO RDPS_USER_PROFILE (cn, role, effective_date_from, effective_date_to, active, timestamp, userstamp) VALUES " + 
						"(:networkID, :role, PARSEDATETIME(:effectiveDateFrom, 'dd/MM/yyyy'), PARSEDATETIME(:effectiveDateTo, 'dd/MM/yyyy'), 'Y', CURRENT_DATE, :userstamp) ";
				MapSqlParameterSource paramsForAddNewUser = new MapSqlParameterSource();
				paramsForAddNewUser.addValue("networkID", userProfile.getCn());
				paramsForAddNewUser.addValue("role", userProfile.getRole());
				paramsForAddNewUser.addValue("effectiveDateFrom", userProfile.getEffectiveDateFrom());
				paramsForAddNewUser.addValue("effectiveDateTo", userProfile.getEffectiveDateTo());	
				paramsForAddNewUser.addValue("userstamp", userstamp);
				rowsUpdated = namedParameterJdbcTemplate.update(sqlForAddNewUser, paramsForAddNewUser);
				
				if(rowsUpdated > 0) {
					rtnMessage = "added";
				}
				
			}
		} catch (Exception e) {
			rtnMessage = e.getMessage();
		}
		return rtnMessage;
	}
	
	//get This.UserProfile by cn & role, return -> cn, staff_name, role, effective_date_from, effective_date_to
	public UserProfile getThisUserAllProfile(String dao_networkID, String dao_role) {
		String sql = "SELECT up.cn, INITCAP(ps.title||' '||ps.surname||' '||ps.othername) " + 
				"AS staff_name,up.role, TO_CHAR(up.effective_date_from, 'DD/MM/YYYY') AS effective_date_from, "+
				"TO_CHAR(up.effective_date_to, 'DD/MM/YYYY') AS effective_date_to, "+
				"active "+
				"FROM RDPS_USER_PROFILE up "+
				"LEFT OUTER JOIN nds_user_info nds ON UPPER(up.cn) = UPPER(nds.cn) AND nds.account_status = 'ANS' AND nds.institute_id LIKE '8%' "+
				"LEFT OUTER JOIN staff sf ON sf.staff_number = nds.institute_id "+
				"LEFT OUTER JOIN person ps ON sf.pid = ps.pid "+
				"WHERE up.cn = :networkID AND up.role = :role "+
				"ORDER BY ps.surname, ps.othername, up.role";
		
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("networkID", dao_networkID);
		params.addValue("role", dao_role);
				
		return this.namedParameterJdbcTemplate.query(sql, params, new ResultSetExtractor<UserProfile>() {
			public UserProfile extractData(ResultSet rs) throws SQLException, DataAccessException {
				UserProfile userProfile = new UserProfile();
				String active;
				while(rs.next()) {
					active = rs.getString("active");
					userProfile.setCn(rs.getString("cn"));
					userProfile.setStaffName(rs.getString("staff_name"));
					if(active.equals("Y")) {
						userProfile.setRole(rs.getString("role"));
						userProfile.setEffectiveDateFrom(rs.getString("effective_date_from"));
						userProfile.setEffectiveDateTo(rs.getString("effective_date_to"));
					}					
				}			
				return userProfile;
			}
		});
	}
	
	//get STAFF_NAME by type cn, return -> staff name
	public String getUserNameByCN(String dao_networkID) {
		
		String sql = "SELECT INITCAP(TRIM(pr.surname||' '||pr.othername)) AS staff_name " +
				"FROM nds_user_info nds " + 
				"LEFT OUTER JOIN staff sf ON sf.staff_number = nds.institute_id " + 
				"LEFT OUTER JOIN person pr ON pr.pid = sf.pid " + 
				"WHERE nds.account_status = 'ANS' " + 
				"AND nds.institute_id LIKE '8%' " + 
				"AND UPPER(nds.cn) = UPPER(:usernameInput) " + 
				"AND ROWNUM = 1";
				
		MapSqlParameterSource params = new MapSqlParameterSource().addValue("usernameInput", dao_networkID);
		return this.namedParameterJdbcTemplate.query(sql, params, new ResultSetExtractor<String>() {
			public String extractData(ResultSet rs) throws SQLException, DataAccessException {
				//UserProfile userProfile = new UserProfile();
				String staffName = "";
				while(rs.next()) {
					//userProfile.setStaffName(rs.getString("staff_name"));
					staffName = rs.getString("staff_name");
				}
				//System.out.println("dao: staff_name: " + staffName);
				return staffName;
			}
		});
		
	}
	
	// get All UserProfile display in /aeoi/setUserProfile page
	public ArrayList<UserProfile> getAllUserProfile() {

		// Modified for local development - removed SSO table joins
		// Original query joined with pr.nds_user_info, hr.staff, pr.person
		String sql = "SELECT up.cn, up.cn AS staff_name, up.role, "
				+"TO_CHAR(up.effective_date_from, 'DD/MM/YYYY') AS effective_date_from, "
				+"TO_CHAR(up.effective_date_to, 'DD/MM/YYYY') AS effective_date_to "
				+"FROM RDPS_USER_PROFILE up "
				+"WHERE up.active = 'Y' "
				+"ORDER BY up.cn, up.role";
		MapSqlParameterSource params = new MapSqlParameterSource();
		
		return this.namedParameterJdbcTemplate.query(sql, params, new ResultSetExtractor<ArrayList<UserProfile>>() {
			public ArrayList<UserProfile> extractData(ResultSet rs) throws SQLException, DataAccessException {
				ArrayList<UserProfile> resultList = new ArrayList<UserProfile>();
				
				while(rs.next()) {
					UserProfile userProfile = new UserProfile();	
					userProfile.setCn(rs.getString("cn"));
					userProfile.setStaffName(rs.getString("staff_name"));
					userProfile.setRole(rs.getString("role"));
					userProfile.setEffectiveDateFrom(rs.getString("effective_date_from"));
					userProfile.setEffectiveDateTo(rs.getString("effective_date_to"));
					resultList.add(userProfile);
				}
				return resultList;
			}
		});
	}
	
}
