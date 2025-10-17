package eduhk.fhr.web.service;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eduhk.fhr.web.model.SampleBooking;
import eduhk.fhr.web.model.UserProfile;
import eduhk.fhr.web.util.Common;

@Service
@Transactional(rollbackFor = Exception.class)
public class ValidationService extends BaseService {

	@Autowired
	private SampleService sampleService;
	
	private Pattern numericPattern = Pattern.compile("-?\\d+(\\.\\d+)?");
	
	public boolean isNumeric(String strNum) {
	    if (strNum == null) {
	        return false; 
	    }
	    return numericPattern.matcher(strNum).matches();
	}
	
	public String validateSampleReservationRequest(SampleBooking booking) {
		/*if (sampleService.getAlreadyApplied(booking) > 0) {
			return "You already have a booking";
		} else*/ if (Common.null2Empty(booking.getStaffNumber()).length() == 0) {
			return "Please provide a valid staff ID";
		} else if (Common.null2Empty(booking.getStaffNumber()).length() != 8) {
			return "Student ID should be 8 digits";
		} else if (!isNumeric(Common.null2Empty(booking.getStaffNumber()))) {
			return "Student ID should consist of numbers only";
		} else if (Common.null2Empty(booking.getStaffName()).length() == 0) {
			return "Please provide your name";
		} else if ((Common.null2Empty(booking.getEmailAddress()).length() == 0) || (!Common.null2Empty(booking.getEmailAddress()).contains("@"))) {
			return "Please provide a valid email address";
		} else {
			return "OK";
		}
	}
	
	public String validateSampleReservationSearch(SampleBooking booking) {
		if (Common.null2Empty(booking.getStaffNumber()).length() == 0) {
			return "Please provide a student ID";
		} else if (Common.null2Empty(booking.getStaffNumber()).length() != 8) {
			return "Student ID should be 8 digits";
		} else if (!isNumeric(Common.null2Empty(booking.getStaffNumber()))) {
			return "Student ID should consist of numbers only";
		} else if ((Common.null2Empty(booking.getEmailAddress()).length() == 0) || (!Common.null2Empty(booking.getEmailAddress()).contains("@"))) {
			return "Please provide a valid email address";
		} else {
			return "OK";
		}
	}
	
	public String validateUserProfile(UserProfile userProfile) {
		if(Common.null2Empty(userProfile.getCn()).equals("")) {
			return "Invalid NetworkID";
		}else if(Common.null2Empty(userProfile.getStaffName()).equals("")) {
			return "Invalid Staff Name";
		}else if(Common.null2Empty(userProfile.getRole()).equals("")) {
			return "Invalid Staff Role";
		}else if(Common.null2Empty(userProfile.getEffectiveDateFrom()).equals("")) {
			return "Invalid Effective Date From";
		}else {
			return "OK";
		}
	}
	
}
