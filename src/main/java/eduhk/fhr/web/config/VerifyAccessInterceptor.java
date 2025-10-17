package eduhk.fhr.web.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class VerifyAccessInterceptor implements HandlerInterceptor {

	@Autowired
	@Lazy
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private Parameters parameters;

	@Override
	public void afterCompletion(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, Exception arg3) throws Exception {

	}

	@Override
	public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, ModelAndView arg3) throws Exception {

	}

	@Override
	public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
		String principal = "";
		//Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
		if (req.getRemoteAddr().indexOf(parameters.getLocalIp()) == 0) {
			principal = parameters.getLocalUserName();
		} else {
			principal = req.getHeader(HeaderAuthenticationFilter.PRINCIPAL_REQUEST_HEADER);
			if (StringUtils.isEmpty(principal)) {
				if (!StringUtils.isEmpty(parameters.getExternalGuestUserName())) {
					principal = parameters.getExternalGuestUserName();
				}
			}
		}
		PreAuthenticatedAuthenticationToken authReq = new PreAuthenticatedAuthenticationToken(principal, "");
		try {
			Authentication auth = authenticationManager.authenticate(authReq);
			SecurityContextHolder.getContext().setAuthentication(auth);
		} catch (Exception e) {
			System.out.println(e);
		}
		return true;
	}

}
