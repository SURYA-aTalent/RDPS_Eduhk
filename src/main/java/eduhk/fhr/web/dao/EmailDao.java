package eduhk.fhr.web.dao;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.stereotype.Repository;

import eduhk.fhr.web.model.Email;
import eduhk.fhr.web.model.EmailTemplate;

@Repository
public class EmailDao extends BaseDao {
	
	public EmailTemplate getEmailTemplate(String templateType) {
		String sql = 
			"SELECT t1.template_id, t1.email_subject, t1.email_body " +
			"FROM rdps_email_template t1 " +
			"WHERE t1.template_id = ("+
			"    SELECT MAX(t2.template_id) "+
			"    FROM rdps_email_template t2 "+
			"    WHERE t2.NOTIFICATION_STAGE = :templateType "+
			")";
		MapSqlParameterSource params = new MapSqlParameterSource().addValue("templateType", templateType);
		return (EmailTemplate)this.namedParameterJdbcTemplate.query(sql, params, new ResultSetExtractor<EmailTemplate>() {
			public EmailTemplate extractData(ResultSet rs) throws SQLException, DataAccessException {
				EmailTemplate emailTemplate = new EmailTemplate();					
				if (rs.next()) {
					emailTemplate.setTemplateId(rs.getInt("template_id"));
					emailTemplate.setEmailSubject(rs.getString("email_subject"));
					emailTemplate.setEmailBody(rs.getString("email_body"));
				}
				return emailTemplate;
			}
		});
	}

	public String sendEmail(Email email) {
	    String sql = 
				"SELECT COALESCE(MAX(email_id), 0)+1 " +
				"FROM rdps_email_job";
		int emailId = this.namedParameterJdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), int.class);
		sql = "INSERT INTO rdps_email_job (email_id, recipient, cc_to, bcc_to, email_subject, email_body, created_by, userstamp) "+
				"VALUES (:emailId, :recipient, :cc, :bcc, :emailSubject, :emailBody, :userstamp, :userstamp)";
		email.setEmailId(emailId);
		this.namedParameterJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(email));
		
		SimpleJdbcCall fc = new SimpleJdbcCall(this.datasource).withSchemaName("rdps").withFunctionName("execute_email_job")
                                                                            	        .withoutProcedureColumnMetaDataAccess()
                                                                                        .declareParameters(
                                                                                                new SqlOutParameter("RETURN_VALUE", Types.VARCHAR),
                                                                                                new SqlParameter("V_EMAIL_ID_IN", Types.VARCHAR),
                                                                                                new SqlParameter("V_SEND_DATE_IN", Types.DATE),
                                                                                                new SqlParameter("V_USERSTAMP_IN", Types.VARCHAR));
		MapSqlParameterSource params = new MapSqlParameterSource()
                                				.addValue("V_EMAIL_ID_IN", email.getEmailId())
                                				.addValue("V_SEND_DATE_IN", null)
                                				.addValue("V_USERSTAMP_IN", email.getUserstamp());
		return fc.executeFunction(String.class, params);

		/*
		String functionCall = "{? = call execute_email_job(?, ?, ?)}"; // ? for return value, ? for IN, ? for IN, ? for IN
        List<SqlParameter> params = List.of(
                new SqlOutParameter("result", Types.VARCHAR),       // Return value
                new SqlParameter("v_email_id_in", Types.INTEGER),   // IN parameter
                new SqlParameter("v_send_date_in", Types.DATE),     // IN parameter
                new SqlParameter("v_userstamp_in", Types.VARCHAR)   // IN parameter
        );

        Map<String, Object> out = this.jdbcTemplate.call(new CallableStatementCreator() {
            @Override
            public CallableStatement createCallableStatement(java.sql.Connection con) throws SQLException {
                CallableStatement cs = con.prepareCall(functionCall);
                cs.registerOutParameter(1, Types.VARCHAR);      // Register return value
                cs.setInt(2, email.getEmailId());               // Set IN parameter
                cs.setDate(3, null);                            // Set IN parameter
                cs.setString(4, email.getUserstamp());          // Set IN parameter
                return cs;
            }
        }, params);
		
        return (String) out.get("result"); // Retrieve the function's return value
        */
	}
}
