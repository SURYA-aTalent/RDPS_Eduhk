package eduhk.fhr.web.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ErrorController {

	@RequestMapping(value = "/accessDenied", method = RequestMethod.GET)
	public ModelAndView accessDeined(Principal principal) {
		ModelAndView model = new ModelAndView("accessDenied");
		return model;
	}
	
}
