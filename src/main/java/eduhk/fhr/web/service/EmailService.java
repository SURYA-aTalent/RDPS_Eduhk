package eduhk.fhr.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eduhk.fhr.web.dao.EmailDao;
import eduhk.fhr.web.model.SampleBooking;
import eduhk.fhr.web.model.Email;
import eduhk.fhr.web.model.EmailTemplate;
import eduhk.fhr.web.util.Common;

@Service
@Transactional(rollbackFor = Exception.class)
public class EmailService extends BaseService {
	
	@Autowired
	private SampleService sampleService;
	
	@Autowired
	private EmailDao emailDao;
	
	public EmailTemplate getEmailTemplate(String templateType) {
		return emailDao.getEmailTemplate(templateType);
	}
	
	@Transactional
	public String sendEmail(Email email) {
		if (Common.null2Empty(email.getRecipient()).equals("") || Common.null2Empty(email.getEmailSubject()).equals("") || Common.null2Empty(email.getEmailBody()).equals("")) {
			return "Either key is empty";
		} else {
			return emailDao.sendEmail(email);
		}
	}
	
	public Email replaceContentForBookingAck(EmailTemplate template, SampleBooking booking) {
		Email email = new Email();
		email.setRecipient(booking.getEmailAddress());
		email.setEmailSubject(template.getEmailSubject());
		String emailBody = template.getEmailBody();
		emailBody = emailBody.replaceAll("==student_name==", booking.getStaffName()).
				replaceAll("==student_number==", booking.getStaffNumber());
		email.setEmailBody(emailBody);
		email.setUserstamp(booking.getUserstamp());
		return email;
	}
	
	public Email replaceContentForCancelAck(EmailTemplate template, SampleBooking booking) {
		//Same as booking act
		return this.replaceContentForBookingAck(template, booking);
	}
	
}
