package eduhk.fhr.web.service;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import eduhk.fhr.web.dao.ParameterDao;
import eduhk.fhr.web.util.Common;

@Service
public class ParameterService {
	@Autowired
	private ParameterDao parameterDao;	
	
	public String getSsoLogoutUrl() {
		String result = "";
		try {
			result = parameterDao.getParameter("SSO_LOGOUT_URL");
		} catch (Exception e) {
			Common.printException(e, null, false);
		}
		return result;
	}
	
	@Cacheable(cacheNames = "systemEnv")
	public String getSystemEnv() {
		String result = "";
		try {
			result = parameterDao.getParameter("ENVIRONMENT");
			if (result.equalsIgnoreCase("PRODUCTION")) {
				result = "";
			}
		} catch (Exception e) {
			result = "";
		}
		return result;
	}
	
	public String getParameter(String paramCode) {
		String result = "";
		try {
			result = parameterDao.getParameter(paramCode);
		} catch (Exception e) {
			Common.printException(e, null, false);
		}
		return result;
	}

	/**
	 * Alias for getParameter() - for consistency with import service usage
	 */
	public String getParameterValue(String paramCode) {
		return getParameter(paramCode);
	}

	public void updateParameter(String paramCode, String newValue) {
		parameterDao.updateParameter(paramCode, newValue);
	}
	
	public HashMap<String, String> getConsentForm() {
		HashMap<String, String> result = new HashMap<String, String>();
		try {
			result = parameterDao.getConsentForm();
		} catch (Exception e) {
			Common.printException(e, null, false);
		}	
		return result;
	}
	
}
