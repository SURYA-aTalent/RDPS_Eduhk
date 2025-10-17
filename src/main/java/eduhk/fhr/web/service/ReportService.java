package eduhk.fhr.web.service;

import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eduhk.fhr.web.dao.ReportDao;
@Service
@Transactional(rollbackFor = Exception.class)
public class ReportService extends BaseService {
	
	@Autowired
	private ReportDao reportDao;
	
	public ArrayList<HashMap<String, String>> getSampleReservationData(HashMap<String, String> param) {
		ArrayList<HashMap<String, String>> rptData = reportDao.getSampleReservationData(param);
		return rptData;
	}
	
	
}
