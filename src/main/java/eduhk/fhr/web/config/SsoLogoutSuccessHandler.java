package eduhk.fhr.web.config;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import eduhk.fhr.web.service.ParameterService;

@Component
public class SsoLogoutSuccessHandler implements LogoutSuccessHandler {

	@Autowired
	private ParameterService parameterService;
	
	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
		response.sendRedirect(parameterService.getSsoLogoutUrl());
	}

}
