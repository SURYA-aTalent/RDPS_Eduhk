package eduhk.fhr.web.config;

import java.util.ArrayList;
import java.util.List;

import eduhk.fhr.web.config.VerifyAccessInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig{
	
	@Autowired
	private UserDetailsServiceImpl userDetailsService;
	@Autowired
	private Parameters parameters;
	@Autowired
	private SsoLogoutSuccessHandler ssoLogoutSuccessHandler;

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer()  {
		return web -> web.ignoring().requestMatchers("/public/**", "/js/**", "/css/**", "/images/**", "/fonts/**");
	}
	
	@Bean
	protected SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
	    http
	    	.addFilter(customRequestHeaderAutdhenticationFilter())
	        .authorizeHttpRequests(auth -> auth
        		.requestMatchers("/makeReservation").hasRole("USE_RDPS")
        		.requestMatchers("/viewReservation").hasRole("USE_RDPS")
        		.requestMatchers("/downloadLogs").hasRole("ACCESS_RIGHT_SETUP")
        		.requestMatchers("/setUserProfile").hasRole("ACCESS_RIGHT_SETUP")
        		.requestMatchers("/log/**").hasRole("USE_RDPS")
        		.requestMatchers("/main/**").hasRole("USE_RDPS")
        		.requestMatchers("/report/**").hasRole("USE_RDPS")
        		.requestMatchers("/").authenticated()
        		.requestMatchers("/about").authenticated()
        		.requestMatchers("/contact").authenticated()	
        		.requestMatchers("/clear").authenticated()
        		.requestMatchers("/resources/**").authenticated()
        		.requestMatchers("/accessDenied").permitAll()
        		.requestMatchers("/logout").permitAll()
        		.anyRequest().authenticated()
	        )
	        .logout(logout -> logout
	            .logoutUrl("/logout")
	            .deleteCookies("JSESSIONID")
	            .invalidateHttpSession(true)
	            .logoutSuccessHandler(ssoLogoutSuccessHandler)
	            .permitAll()
	        )
	        .exceptionHandling(exception -> exception
	            .authenticationEntryPoint(authenticationEntryPoint())
	            .accessDeniedPage("/accessDenied")
	        )
	        .headers(headers -> headers
	            .cacheControl(cache -> cache.disable())
	            .frameOptions(frame -> frame.sameOrigin())
	        );
		return http.build();
	}	
	
//	@Bean(name = "customRequestHeaderAuthenticationFilter")
	public HeaderAuthenticationFilter customRequestHeaderAutdhenticationFilter() throws Exception {
		HeaderAuthenticationFilter requestHeaderAuthenticationFilter = new HeaderAuthenticationFilter();
		requestHeaderAuthenticationFilter.setLocalIp(parameters.getLocalIp());
		requestHeaderAuthenticationFilter.setLocalUserName(parameters.getLocalUserName());
		requestHeaderAuthenticationFilter.setExternalUserName(parameters.getExternalGuestUserName());
		requestHeaderAuthenticationFilter.setAuthenticationManager(authenticationManager());		
		return requestHeaderAuthenticationFilter;
	}
	
	@Bean
	protected AuthenticationManager authenticationManager() throws Exception {
		final List<AuthenticationProvider> providers = new ArrayList<>(1);
		providers.add(preauthAuthProvider());
		return new ProviderManager(providers);
	}
	
	@Bean(name = "preauthProvider")
	PreAuthenticatedAuthenticationProvider preauthAuthProvider() throws Exception {
		PreAuthenticatedAuthenticationProvider provider = new PreAuthenticatedAuthenticationProvider();
		provider.setPreAuthenticatedUserDetailsService(userDetailsServiceWrapper());
		return provider;
	}
	
	@Bean
	UserDetailsByNameServiceWrapper<PreAuthenticatedAuthenticationToken> userDetailsServiceWrapper() throws Exception {
		UserDetailsByNameServiceWrapper<PreAuthenticatedAuthenticationToken> wrapper = new UserDetailsByNameServiceWrapper<>();
		wrapper.setUserDetailsService(userDetailsService);
		return wrapper;
	}
	
	@Bean
	Http403ForbiddenEntryPoint authenticationEntryPoint() {
		return new Http403ForbiddenEntryPoint();
	}

}
