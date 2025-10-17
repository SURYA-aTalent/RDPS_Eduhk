package eduhk.fhr.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import eduhk.fhr.web.model.User;
import eduhk.fhr.web.model.UserDetailsImp;

public class BaseController {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	protected MessageSource messageSource;
	
	protected Authentication getAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}
	
	protected User getCurrentUser() {
		return ((UserDetailsImp) getAuthentication().getPrincipal()).getUser();
	}	
	
}
