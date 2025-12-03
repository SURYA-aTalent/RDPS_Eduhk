package eduhk.fhr.web.config;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;
import org.springframework.util.StringUtils;

public class HeaderAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {

	public static String PRINCIPAL_REQUEST_HEADER = "OAM_REMOTE_USER";
	private String localUserName;
	private String externalUserName;
	private String localIp;

	/**
	 * Configure whether a missing SSO header is an exception.
	 */
	private boolean exceptionIfHeaderMissing = false;

	/**
	 * Read and return header named by <tt>principalRequestHeader</tt> from
	 * Request
	 *
	 * @throws PreAuthenticatedCredentialsNotFoundException
	 *             if the header is missing and
	 *             <tt>exceptionIfHeaderMissing</tt> is set to <tt>true</tt>.
	 */
	protected String getPreAuthenticatedPrincipal(HttpServletRequest request) {
		String principal = request.getHeader(PRINCIPAL_REQUEST_HEADER);
		String localAddr = request.getLocalAddr();
		// Check for both IPv4 (127.0.0.1) and IPv6 (0:0:0:0:0:0:0:1) localhost
		boolean isLocalhost = localAddr.indexOf(localIp) == 0 || localAddr.startsWith("127.0.0.1");
		if (isLocalhost) {
			if (!StringUtils.isEmpty(localUserName)) {
				principal = localUserName;
//				System.out.println("!!! " + principal);
			}
		} else {
			if (principal == null) {
				if (exceptionIfHeaderMissing) {
					throw new PreAuthenticatedCredentialsNotFoundException(PRINCIPAL_REQUEST_HEADER + " header not found in request.");
				} else {
					if (!StringUtils.isEmpty(externalUserName)) {
						principal = externalUserName;
					}
				}
			}
		}
		return principal;
	}

	/**
	 * Credentials aren't applicable here for OAM WebGate SSO.
	 */
	protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
		return "password_not_applicable";
	}

	/**
	 * Exception if the principal header is missing. Default <tt>false</tt>
	 * 
	 * @param exceptionIfHeaderMissing
	 */
	public void setExceptionIfHeaderMissing(boolean exceptionIfHeaderMissing) {
		this.exceptionIfHeaderMissing = exceptionIfHeaderMissing;
	}

	//	public void setAuthenticationDetailsSource(AuthenticationDetailsSource source) {
	//		super.setAuthenticationDetailsSource(source);
	//	}
		
	public void setLocalIp(String localIp) {
		this.localIp = localIp;
	}
	
	public void setLocalUserName(String localUserName) {
		this.localUserName = localUserName;
	}
	
	public void setExternalUserName(String externalUserName) {
		this.externalUserName = externalUserName;
	}
	
}