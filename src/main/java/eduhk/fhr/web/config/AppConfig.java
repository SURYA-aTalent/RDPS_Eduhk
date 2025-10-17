package eduhk.fhr.web.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.naming.NamingException;
import jakarta.servlet.MultipartConfigElement;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.web.error.ErrorPage;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jndi.JndiTemplate;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import com.zaxxer.hikari.HikariDataSource;

@EnableWebMvc
//@EnableAsync
@EnableTransactionManagement
@Configuration
@ComponentScan(basePackages = { "eduhk.fhr.web.*" })
@EnableMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
@EnableCaching
public class AppConfig implements WebMvcConfigurer {
    
    @Value("${spring.datasource.url:}")
    private String jdbcUrl;
    
    @Value("${spring.datasource.username:}")
    private String username;
    
    @Value("${spring.datasource.password:}")
    private String password;
    
    @Value("${spring.datasource.driver-class-name:}")
    private String driverClassName;
    
    @Autowired
    private VerifyAccessInterceptor verifyAccessInterceptor;
    
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/resources/**").addResourceLocations("classpath:static/");
		registry.addResourceHandler("/js/**").addResourceLocations("classpath:static/js/");
		registry.addResourceHandler("/css/**").addResourceLocations("classpath:static/css/");
		registry.addResourceHandler("/images/**").addResourceLocations("classpath:static/images/");
		registry.addResourceHandler("/fonts/**").addResourceLocations("classpath:static/fonts/");
	}

	@Primary
	@Bean("dataSource")
	public DataSource dataSource() throws NamingException {
		try {
			// Try to use JNDI first (for production)
			return (DataSource) new JndiTemplate().lookup("java:comp/env/jdbc/datasources/rdpsDS");
		} catch (NamingException e) {
			// Try to use direct JDBC configuration (for production without JNDI)
			if (jdbcUrl != null && !jdbcUrl.isEmpty()) {
				System.out.println("Using Oracle JDBC configuration for production");
				return DataSourceBuilder.create()
					.type(HikariDataSource.class)
					.url(jdbcUrl)
					.username(username)
					.password(password)
					.driverClassName(driverClassName)
					.build();
			} else {
				// Fallback to H2 in-memory database for local development
				System.out.println("JNDI not available, using H2 in-memory database for local development");
				org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder builder = 
					new org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder();
				return builder
					.setType(org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.H2)
					.addScript("classpath:schema.sql")
					.addScript("classpath:data.sql")
					.build();
			}
		}
	}
	
	@Bean("ssoDataSource")
	public DataSource ssoDataSource() throws NamingException {
		try {
			// Try to use JNDI first (for production)
			return (DataSource) new JndiTemplate().lookup("java:comp/env/jdbc/datasources/rdpsDS");
		} catch (NamingException e) {
			// Fallback to H2 in-memory database for local development
			System.out.println("JNDI not available, using H2 in-memory database for local development");
			org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder builder = 
				new org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder();
			return builder
				.setType(org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.H2)
				.addScript("classpath:schema.sql")
				.addScript("classpath:data.sql")
				.build();
		}
	}

	@Primary
	@Bean("jdbcTemplate")
	public JdbcTemplate jdbcTemplate() throws NamingException {
		JdbcTemplate jdbcTemplate = new JdbcTemplate();
		jdbcTemplate.setDataSource(dataSource());
		return jdbcTemplate;
	}
	
	@Bean("namedParameterJdbcTemplate")
	public NamedParameterJdbcTemplate namedParameterJdbcTemplate() throws NamingException {
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource());
		return namedParameterJdbcTemplate;
	}
	
	@Bean("ssoJdbcTemplate")
	public JdbcTemplate ssoJdbcTemplate() throws NamingException {
		JdbcTemplate jdbcTemplate = new JdbcTemplate();
		jdbcTemplate.setDataSource(ssoDataSource());
		return jdbcTemplate;
	}
	

	@Bean
	public CacheManager cacheManager() {

		SimpleCacheManager cacheManager = new SimpleCacheManager();
		cacheManager.setCaches(Arrays.asList(
				new ConcurrentMapCache("systemEnv")
				));
		return cacheManager;

	}


	@Bean
	public DataSourceTransactionManager dataSourceTransactionManager() throws NamingException {
	    DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
	    dataSourceTransactionManager.setDataSource(dataSource());
	    return dataSourceTransactionManager;
	}

	@Bean
	public LocaleResolver localeResolver() {
		SessionLocaleResolver slr = new SessionLocaleResolver();
		slr.setDefaultLocale(Locale.forLanguageTag("en-US"));
		return slr;
	}

	@Bean
	public ReloadableResourceBundleMessageSource messageSource() {
		ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
		source.setBasename("classpath:messages");
		source.setCacheSeconds(5);
		source.setDefaultEncoding("UTF-8");
		return source;
	}

	@Bean
	@Override
	public Validator getValidator() {
	    final LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
	    localValidatorFactoryBean.setValidationMessageSource(messageSource());
	    return localValidatorFactoryBean;
	}	
	
	@Bean("executorService")
	public ExecutorService executorService() {
		ExecutorService executorService = Executors.newFixedThreadPool(20);
//		ExecutorService executorService = Executors.newSingleThreadExecutor();
		return executorService;
	}
	
	@Bean
	public ErrorPage errorPageFilter() {
	    return new ErrorPage("");
	}
	
	@Bean
	public LocalValidatorFactoryBean validator(MessageSource messageSource) {
	    LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
	    bean.setValidationMessageSource(messageSource);
	    return bean;
	}	
	
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        return new MultipartConfigElement("");
    }

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

//	@Bean
//	public FilterRegistrationBean disableSpringBootErrorFilter(ErrorPageFilter filter) {
//	    FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
//	    filterRegistrationBean.setFilter(filter);
//	    filterRegistrationBean.setEnabled(false);
//	    return filterRegistrationBean;
//	}	
	
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(verifyAccessInterceptor).addPathPatterns("/**").excludePathPatterns("/resources/**", "/public/**");
    }

}
