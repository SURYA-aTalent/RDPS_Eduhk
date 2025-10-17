package eduhk.fhr.web.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eduhk.fhr.web.util.Common;

@Repository
public class ParameterDao extends BaseDao {
	
	public String getParameter(String paramCode) {
		String sql = "SELECT param_value FROM "+parameters.getTablePrefix()+"parameter WHERE param_code = :paramCode AND active = 'Y' ";
		MapSqlParameterSource params = new MapSqlParameterSource().addValue("paramCode", paramCode);
		return this.namedParameterJdbcTemplate.queryForObject(sql, params, String.class);
	}

	@Transactional
	public void updateParameter(String paramCode, String newValue) {
		String sql = "UPDATE "+parameters.getTablePrefix()+"parameter SET param_value = :newValue, timestamp = CURRENT_DATE WHERE param_code = :paramCode ";
		MapSqlParameterSource params = new MapSqlParameterSource().addValue("paramCode", paramCode).addValue("newValue", newValue);
		namedParameterJdbcTemplate.update(sql, params);
	}
	
	public HashMap<String, String> getConsentForm() {
		String sql = "SELECT form_title, form_body "
					+ "FROM consent_form "
					+ "WHERE version_id = ( "
					+ "    SELECT COALESCE(MAX(version_id), 1) "
					+ "    FROM consent_form "
					+ "    WHERE active = 'Y' "
					+ ") ";
		return (HashMap<String, String>)this.namedParameterJdbcTemplate.query(sql, new ResultSetExtractor<HashMap<String, String>>() {
			public HashMap<String, String> extractData(ResultSet rs) throws SQLException, DataAccessException {
				HashMap<String, String> consentForm = new HashMap<String, String>();
				if (rs.next()) {
					consentForm.put("CONSENT_FORM_TITLE", Common.null2Empty(rs.getString("form_title")));
					consentForm.put("CONSENT_FORM_BODY", Common.null2Empty(rs.getString("form_body")));
				}
				return consentForm;
			}
		});
	}
	
}
