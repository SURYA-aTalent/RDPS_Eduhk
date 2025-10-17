package eduhk.fhr.web.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import eduhk.fhr.web.model.AjaxResponse;
import eduhk.fhr.web.model.SampleBooking;
import eduhk.fhr.web.model.Email;
import eduhk.fhr.web.model.EmailTemplate;
import eduhk.fhr.web.model.UserProfile;
import eduhk.fhr.web.service.EmailService;
import eduhk.fhr.web.service.LogService;
import eduhk.fhr.web.service.SampleService;
import eduhk.fhr.web.service.UserProfileService;
import eduhk.fhr.web.service.ValidationService;
import eduhk.fhr.web.util.Common;

@Controller
@RequestMapping(value = "/main/ajax")
public class AjaxGuestController extends BaseController {
	
	@Autowired
	private SampleService sampleService;
	@Autowired
	private ValidationService validationService;
	@Autowired
	private EmailService emailService;
	@Autowired
	private UserProfileService UserProfileService;
	@Autowired
	private LogService logService;
		
	@RequestMapping(value = "/makeReservation", method = RequestMethod.POST)	
	@ResponseBody
	public AjaxResponse saveBookingList(@RequestParam String jsonInput, Locale locale, HttpServletRequest request) {
	    AjaxResponse res = new AjaxResponse();
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			SampleBooking booking = objectMapper.readValue(jsonInput, SampleBooking.class);
			booking.setClientIp(Common.null2Empty(request.getRemoteAddr()));
			booking.setClientInfo(Common.null2Empty(request.getHeader("User-Agent")));
			booking.setUserstamp(getCurrentUser().getUsername());
			String validationResult = validationService.validateSampleReservationRequest(booking);
			String saveResult = "Error during save";
			if (validationResult.equalsIgnoreCase("OK")) {
				//Validation passed, save the booking
				booking = sampleService.saveSampleReservation(booking);
				if (booking.getBookingId() > 0) {
					//Booking saved, send email
					EmailTemplate template = emailService.getEmailTemplate("BOOKING_ACK");
					Email email = emailService.replaceContentForBookingAck(template, booking);
					String emailResult = emailService.sendEmail(email);					
					if (emailResult.equalsIgnoreCase("OK")) {
					    saveResult = sampleService.updateSampleReservationBookingAckDate(booking);						
					}
					if (saveResult.equalsIgnoreCase("OK")) {
						res.success("Submission successfully");
					}
				} else {
					res.fail(saveResult);
				}
			} else {
				res.fail(validationResult);
			}
		} catch (Exception e) {
			res.fail(messageSource.getMessage("global.general.error", null, locale));
			logger.error(e.getMessage());
		}
		return res;
	}
	
	@RequestMapping(value = "/getBookingList", method = RequestMethod.POST)
	@ResponseBody
	public AjaxResponse getBookingList(Locale locale) {
		AjaxResponse res = new AjaxResponse();
		try {
			res.setResult(sampleService.getBookingList());
			res.success();
		} catch (Exception e) {
			res.fail(messageSource.getMessage("global.general.error", null, locale));
			logger.error(e.getMessage());
		}
		return res;
	}
	
	@RequestMapping(value = "/getUserProfile", method = RequestMethod.POST)
	@ResponseBody
	public AjaxResponse ajaxGetUserProfile(Locale locale) {
		AjaxResponse res = new AjaxResponse();
		try {
			res.setResult(UserProfileService.getAllUserProfile());
			res.success();
		} catch (Exception e) {
			res.fail(messageSource.getMessage("global.general.error", null, locale));
			logger.error(e.getMessage());
		}
		return res;
	}
	
	@RequestMapping(value = "/saveUserProfile", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResponse ajaxSaveUserProfile(@RequestParam String app, Locale locale, String userstamp) {
        AjaxResponse res = new AjaxResponse();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            UserProfile userProfile = objectMapper.readValue(app, UserProfile.class);
            
            userstamp = getCurrentUser().getUsername();
            String saveResult = UserProfileService.saveUserProfile(userProfile, userstamp);

            if(saveResult == "added") {
            	res.success("A new userprofile added");
            } else if(saveResult == "existed") {
            	res.warn("Fail: record already existed");
            } else if (saveResult == "updated") {
            	res.success("User Profile updated");
            } else {
            	res.fail(saveResult);
            }
                        
        } catch (Exception e) {
            res.fail(messageSource.getMessage("global.general.error", null, locale));
            logger.error(e.getMessage());
        }
        return res;
    }
	
	@RequestMapping(value = "/getThisUserAllProfile", method = RequestMethod.POST)
	@ResponseBody
	public AjaxResponse ajaxGetThisUserAllProfile(@RequestParam String cn, @RequestParam String role, Locale locale) {
		AjaxResponse res = new AjaxResponse();
		try {
			UserProfile app = new UserProfile();
	
			if(role.isEmpty()) {
				//app = UserProfileService.getThisUserProfile(cn);
				app.setStaffName(UserProfileService.getUserNameByCN(cn));
			} else {
				app = UserProfileService.getThisUserAllProfile(cn, role);
			}
	
			res.setResult(app);
	
			if (app.getStaffName() == null) {
				res.warn("User Profile not found");
			} else {
				res.success("User Profile loaded");
			}
		} catch (Exception e) {
			res.fail(messageSource.getMessage("global.general.error", null, locale));
			logger.error(e.getMessage());
		}
		return res;
	}
	
	@RequestMapping(value = "/getStaffNameByCN", method = RequestMethod.POST)
	@ResponseBody
	public AjaxResponse ajaxGetStaffNameByCN(@RequestParam String cn, Locale locale) {
		AjaxResponse res = new AjaxResponse();
		try {
			res.setResult(UserProfileService.getUserNameByCN(cn));
			res.success();
			if(res.getResult() == "") {
				res.warn("User Profile not found");
			} else {
				res.success("User Profile loaded");
			}
		} catch (Exception e) {
			res.fail(messageSource.getMessage("global.general.error", null, locale));
			logger.error(e.getMessage());
		}
		return res;
	}
	
	@RequestMapping(value = "/deleteUserProfile", method = RequestMethod.POST)
    @ResponseBody
    public AjaxResponse ajaxDeleteUserProfile(@RequestParam String app, Locale locale, String userstamp) {
        AjaxResponse res = new AjaxResponse();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            UserProfile userProfile = objectMapper.readValue(app, UserProfile.class);
            
            userstamp = getCurrentUser().getUsername();
            String deleteResult = UserProfileService.deleteUserProfile(userProfile, userstamp);
            if(deleteResult == "deleted") {
            	res.success("User Profile deleted");
            }
            
        } catch (Exception e) {
            res.fail(messageSource.getMessage("global.general.error", null, locale));
            logger.error(e.getMessage());
        }
        return res;
    }
	
	@RequestMapping(value = "/getLogs", method = RequestMethod.POST)
	@ResponseBody
	public AjaxResponse getLogs(Locale locale) {
		AjaxResponse res = new AjaxResponse();
		try {
			res.setResult(logService.getLogs());
			res.success();
		} catch (Exception e) {
			res.fail(messageSource.getMessage("global.general.error", null, locale));
			logger.error(e.getMessage());
		}
		return res;
	}

}