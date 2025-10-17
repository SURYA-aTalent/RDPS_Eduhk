package eduhk.fhr.web.dao;

import java.util.ArrayList;

import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import eduhk.fhr.web.model.SchedulerModel;

@Repository
public class SchedulerDao extends BaseDao {
	
	@Transactional
	public String insertIntoTable(SchedulerModel schedulerModel) {
		String sql = "SELECT COALESCE(MAX(id), 0)+1 FROM test_schedule_table";
		int scheduleId = this.namedParameterJdbcTemplate.queryForObject(sql, new MapSqlParameterSource(), int.class);
		schedulerModel.setScheduleId(scheduleId);
		schedulerModel.setDescription("testing");
		sql = "INSERT INTO rdps.test_schedule_table (id, description) "
			+ "    VALUES (:scheduleId, :description)";
		this.namedParameterJdbcTemplate.update(sql, new BeanPropertySqlParameterSource(schedulerModel));
		return "Success";
	}
	
}