package eduhk.fhr.web.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import eduhk.fhr.web.model.TalentLinkUserStaging;

@Repository
public class TalentLinkUserStagingDao extends BaseDao {

    /**
     * Get all users from staging table
     *
     * @return List of all users
     */
    public List<TalentLinkUserStaging> getAllUsers() {
        String sql =
            "SELECT user_id, last_name, first_name, email, user_type, login_username, " +
            "       password, role, language, week_start_on, branding, timezone, " +
            "       status, activation_type, synced_to_talentlink, talentlink_user_id, sync_date, sync_log, " +
            "       created_by, created_date, last_updated_by, last_updated_date " +
            "FROM rdps_talentlink_user_staging " +
            "ORDER BY user_id";

        return this.namedParameterJdbcTemplate.query(sql, new MapSqlParameterSource(), new TalentLinkUserStagingRowMapper());
    }

    /**
     * Get all unsynced users from staging table
     *
     * @return List of unsynced users
     */
    public List<TalentLinkUserStaging> getUnsyncedUsers() {
        String sql =
            "SELECT user_id, last_name, first_name, email, user_type, login_username, " +
            "       password, role, language, week_start_on, branding, timezone, " +
            "       status, activation_type, synced_to_talentlink, talentlink_user_id, sync_date, sync_log, " +
            "       created_by, created_date, last_updated_by, last_updated_date " +
            "FROM rdps_talentlink_user_staging " +
            "WHERE synced_to_talentlink = 'N' " +
            "ORDER BY user_id";

        return this.namedParameterJdbcTemplate.query(sql, new MapSqlParameterSource(), new TalentLinkUserStagingRowMapper());
    }

    /**
     * Mark user as synced
     *
     * @param userId User ID
     * @param syncLog Sync log message
     */
    public void markAsSynced(Integer userId, String syncLog) {
        String sql =
            "UPDATE rdps_talentlink_user_staging " +
            "SET synced_to_talentlink = 'Y', " +
            "    sync_date = SYSDATE, " +
            "    sync_log = :syncLog " +
            "WHERE user_id = :userId";

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("userId", userId)
            .addValue("syncLog", syncLog);

        this.namedParameterJdbcTemplate.update(sql, params);
    }

    /**
     * Mark user sync as failed
     *
     * @param userId User ID
     * @param errorLog Error log message
     */
    public void markAsFailed(Integer userId, String errorLog) {
        String sql =
            "UPDATE rdps_talentlink_user_staging " +
            "SET sync_log = :errorLog, " +
            "    last_updated_date = SYSDATE " +
            "WHERE user_id = :userId";

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("userId", userId)
            .addValue("errorLog", errorLog);

        this.namedParameterJdbcTemplate.update(sql, params);
    }

    /**
     * Row mapper for TalentLinkUserStaging
     */
    private static class TalentLinkUserStagingRowMapper implements RowMapper<TalentLinkUserStaging> {
        @Override
        public TalentLinkUserStaging mapRow(ResultSet rs, int rowNum) throws SQLException {
            TalentLinkUserStaging user = new TalentLinkUserStaging();
            user.setUserId(rs.getInt("user_id"));
            user.setLastName(rs.getString("last_name"));
            user.setFirstName(rs.getString("first_name"));
            user.setEmail(rs.getString("email"));
            user.setUserType(rs.getString("user_type"));
            user.setLoginUsername(rs.getString("login_username"));
            user.setPassword(rs.getString("password"));
            user.setRole(rs.getString("role"));
            user.setLanguage(rs.getString("language"));
            user.setWeekStartOn(rs.getString("week_start_on"));
            user.setBranding(rs.getString("branding"));
            user.setTimezone(rs.getString("timezone"));
            user.setStatus(rs.getString("status"));
            user.setActivationType(rs.getString("activation_type"));
            user.setSyncedToTalentlink(rs.getString("synced_to_talentlink"));
            user.setSyncDate(rs.getDate("sync_date"));
            user.setSyncLog(rs.getString("sync_log"));
            user.setCreatedBy(rs.getString("created_by"));
            user.setCreationDate(rs.getDate("created_date"));
            user.setUserstamp(rs.getString("last_updated_by"));
            user.setTimestamp(rs.getDate("last_updated_date"));
            return user;
        }
    }
}
