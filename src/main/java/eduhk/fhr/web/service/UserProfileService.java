package eduhk.fhr.web.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eduhk.fhr.web.dao.UserProfileDao;
import eduhk.fhr.web.model.UserProfile;

@Service
@Transactional(rollbackFor = Exception.class)
public class UserProfileService {

	@Autowired
	private UserProfileDao userProfileDao;
	@Autowired
	private ValidationService validationService;

	public ArrayList<UserProfile> getAllUserProfile() {
		return userProfileDao.getAllUserProfile();
	}
	
	public String getUserNameByCN(String service_networkID) {
		return userProfileDao.getUserNameByCN(service_networkID);
	}
	
	
	public UserProfile getThisUserAllProfile(String service_networkID, String service_staffRole) {
		UserProfile userProfile = userProfileDao.getThisUserAllProfile(service_networkID, service_staffRole);
		return userProfile;
	}
	
	public ArrayList<String> getRoleList() {
		return userProfileDao.getRoleList();
	}
	
	public String saveUserProfile(UserProfile userProfile, String userstamp) {
		String saveResult = validationService.validateUserProfile(userProfile);
		if (saveResult.equalsIgnoreCase("OK")) {
			saveResult = userProfileDao.saveUserProfile(userProfile, userstamp);
		}
		
		return saveResult;
	}
	
	public String deleteUserProfile(UserProfile userProfile, String userstamp) {
		return userProfileDao.deleteUserProfile(userProfile, userstamp);
	}
		
}
