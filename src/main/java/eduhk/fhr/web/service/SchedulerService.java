package eduhk.fhr.web.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eduhk.fhr.web.dao.SchedulerDao;
import eduhk.fhr.web.model.SchedulerModel;

@Service
@Transactional(rollbackFor = Exception.class)
public class SchedulerService extends BaseService {
	
	@Autowired
	private SchedulerDao scheduleDao;
		
	public String insertIntoTable(SchedulerModel schedulerModel) {
		return scheduleDao.insertIntoTable(schedulerModel);
	}
	
}
