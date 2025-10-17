package eduhk.fhr.web.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import eduhk.fhr.web.model.SampleBooking;

@Repository
public class SampleDao extends BaseDao {
	
	public SampleBooking saveSampleReservation(SampleBooking booking) {		
		String sql = "SELECT COALESCE(MAX(booking_id), 0)+1 FROM sample_booking";
		int bookingId = this.namedParameterJdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), int.class);
		booking.setBookingId(bookingId);
		sql = "INSERT INTO sample_booking (booking_id, staff_id, staff_name, email_address, client_ip, client_info, created_by, userstamp) "
			+ "    VALUES (:bookingId, :staffNumber, :staffName, TRIM(LOWER(:emailAddress)), :clientIp, :clientInfo, :userstamp, :userstamp)";
		this.namedParameterJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(booking));
		return booking;
	}
	
	public ArrayList<SampleBooking> getBookingList() {
		String sql = "SELECT booking_id, staff_id, staff_name, email_address "
					+ "FROM rdps.sample_booking "
					+ "WHERE active = 1 "
					+ "ORDER BY booking_id";
		MapSqlParameterSource params = new MapSqlParameterSource();
		return (ArrayList<SampleBooking>)this.namedParameterJdbcTemplate.query(sql, params, new ResultSetExtractor<ArrayList<SampleBooking>>() {
			public ArrayList<SampleBooking> extractData(ResultSet rs) throws SQLException, DataAccessException {
				ArrayList<SampleBooking> resultList = new ArrayList<SampleBooking>();					
				while (rs.next()) {
				    SampleBooking resultBooking = new SampleBooking();
					resultBooking.setBookingId(rs.getInt("booking_id"));
					resultBooking.setStaffNumber(rs.getString("staff_id"));
					resultBooking.setStaffName(rs.getString("staff_name"));
					resultBooking.setEmailAddress(rs.getString("email_address"));
					resultList.add(resultBooking);
				}
				return resultList;
			}
		});
	}
	
	public String updateSampleReservationBookingAckDate(SampleBooking booking) {
		String sql = "UPDATE rdps.sample_booking SET ack_email_sent_date = CURRENT_DATE WHERE booking_id = :bookingId";
		this.namedParameterJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(booking));
		return "OK";
	}
	
	public int getAlreadyApplied(SampleBooking booking) {
		String sql = "SELECT COUNT(*) "
					+ "FROM rdps.sample_booking "
					+ "WHERE (staff_id = :staffNumber OR UPPER(email_address) = TRIM(UPPER(:emailAddress))) "
					+ "AND active = 1 ";
		return this.namedParameterJdbcTemplate.queryForObject(sql, new BeanPropertySqlParameterSource(booking), int.class);
	}
	
	public SampleBooking searchSampleBooking(SampleBooking booking) {
		String sql = "SELECT booking_id, staff_id, staff_name, email_address "
					+ "FROM rdps.sample_booking "
					+ "WHERE staff_id = :staffNumber "
					+ "AND active = 1 ";
		return (SampleBooking)this.namedParameterJdbcTemplate.query(sql, new BeanPropertySqlParameterSource(booking), new ResultSetExtractor<SampleBooking>() {
			public SampleBooking extractData(ResultSet rs) throws SQLException, DataAccessException {
			    SampleBooking resultBooking = new SampleBooking();
				if (rs.next()) { 
					resultBooking.setBookingId(rs.getInt("booking_id"));
					resultBooking.setStaffNumber(rs.getString("staff_id"));
					resultBooking.setStaffName(rs.getString("staff_name"));
					resultBooking.setEmailAddress(rs.getString("email_address"));
				}
				return resultBooking;
			}
		});
	}
	
	public String updateSampleReservationCancelAckDate(SampleBooking booking) {
		String sql = "UPDATE rdps.sample_booking SET cancel_email_sent_date = CURRENT_DATE WHERE booking_id = :bookingId";
		this.namedParameterJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(booking));
		return "OK";
	}
	
}