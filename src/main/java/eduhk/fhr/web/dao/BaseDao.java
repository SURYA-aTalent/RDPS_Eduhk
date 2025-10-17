package eduhk.fhr.web.dao;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import eduhk.fhr.web.config.Parameters;

@Component
public class BaseDao {
	
	@Autowired
	protected JdbcTemplate jdbcTemplate;
	@Autowired
	protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	@Autowired
	protected Parameters parameters;
	@Autowired
	protected DataSource datasource; 
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
}
