package eduhk.fhr.web.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.dao.DataAccessException;

import eduhk.fhr.web.util.Common;

@Repository
public class ReportDao extends BaseDao {
	
	public ArrayList<HashMap<String, String>> getSampleReservationData(HashMap<String, String> param) {
		String sql = "SELECT staff_id, staff_name, email_address, "
					+ "    TO_CHAR(ack_email_sent_date, 'DD/MM/YYYY HH24:MI:SS') AS ack_email_sent_date "
					+ "FROM rdps.sample_booking "
					+ "WHERE active = 1 "
					+ "ORDER BY booking_id";
		MapSqlParameterSource params = new MapSqlParameterSource();
		return this.namedParameterJdbcTemplate.query(sql, params, new ResultSetExtractor<ArrayList<HashMap<String, String>>>() {
			public ArrayList<HashMap<String, String>> extractData(ResultSet rs) throws SQLException, DataAccessException {
				ArrayList<HashMap<String, String>> rptDataList = new ArrayList<HashMap<String, String>>();				
				while (rs.next()) {
					HashMap<String, String> rptDataRow = new HashMap<String, String>();
					rptDataRow.put("staff_id", Common.null2Empty(rs.getString("staff_id")));
					rptDataRow.put("staff_name", Common.null2Empty(rs.getString("staff_name")));
					rptDataRow.put("email_address", Common.null2Empty(rs.getString("email_address")));
					rptDataRow.put("ack_email_sent_date", Common.null2Empty(rs.getString("ack_email_sent_date")));
					rptDataList.add(rptDataRow);
				}				
				return rptDataList;
			}
		});
	}
	
}