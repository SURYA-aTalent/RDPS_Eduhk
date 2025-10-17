package eduhk.fhr.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import eduhk.fhr.web.config.Parameters;
import eduhk.fhr.web.service.ParameterService;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalControllerAdvice {
	@Autowired
	private Parameters parameters;
	
	@Autowired
	private ParameterService parameterService;		
	
	@Value("${vuejs.path}")
	private String vueJsPath;
	
	@ModelAttribute("vuejsPath")
	public String getVueJsPath() {
		return this.vueJsPath;
	}
	
	@ModelAttribute("systemName")
	public String systemName() {
		return parameters.getSystemName();
	}

	@ModelAttribute("version")
	public String getVersion() {
		return parameters.getVersion();
	}
	
	@ModelAttribute("ssoLogoutUrl")	
	public String getSsoLogoutUrl() {
		return parameterService.getSsoLogoutUrl();
	}
	
	@ModelAttribute("systemEnv")
	public String getSystemEnv() {
		return parameterService.getSystemEnv();
	}
	
	@ModelAttribute("currentUri")
	public String currentUri(HttpServletRequest req) {
	    return req.getRequestURI();
	}
	
	@ModelAttribute("contextPath")
	public String contextPath(HttpServletRequest req) {
	    return req.getContextPath();
	}
}
