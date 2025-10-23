package eduhk.fhr.web.service;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eduhk.fhr.web.config.Parameters;
import eduhk.fhr.web.dao.SystemLogDao;
import eduhk.fhr.web.model.LogInfo;
import eduhk.fhr.web.model.SystemLog;


@Service
@Transactional(rollbackFor = Exception.class)
public class LogService extends BaseService {

	@Autowired
	private Parameters parameters;

	@Autowired
	private SystemLogDao systemLogDao;

	private static final Logger logger = LoggerFactory.getLogger(LogService.class);
	
    public List<LogInfo> getLogs() {
        List<LogInfo> logs = new ArrayList<>();
        File dir = new File(parameters.getLogDir());
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((file) -> file.isFile() && (file.getName().endsWith(".log") || file.getName().endsWith(".txt")));
            if (files != null) {
                for (File file : files) {
                    String name = file.getName();
                    long size = file.length();
                    String lastModified = Instant.ofEpochMilli(file.lastModified())
                        .atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                    logs.add(new LogInfo(name, size, lastModified));
                }
            }
        }
        return logs;
    }
    
    public void writeLog(String message) {
        logger.info(message);
    }

    public void writeErrorLog(String message) {
        logger.error(message);
    }

    /**
     * Get import-related error logs from the last N days
     *
     * @param days Number of days to look back
     * @return List of system logs related to import errors
     */
    public List<SystemLog> getImportErrors(int days) {
        try {
            return systemLogDao.getImportErrors(days);
        } catch (Exception e) {
            logger.error("Error fetching import errors from system log", e);
            return new ArrayList<>();
        }
    }
}
