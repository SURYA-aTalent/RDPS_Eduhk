package eduhk.fhr.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@Configuration
@PropertySource("classpath:parameters.properties")
//@ConfigurationProperties()
public class Parameters {
	@Value("${system.version}")
	private String version;
	@Value("${system.name}")
	private String systemName;
	@Value("${system.logDir}")
	private String logDir;
	@Value("${system.database.datetimeFormat}")
	private String datetimeFormat;
	@Value("${system.database.dateFormat}")
	private String dateFormat;
	@Value("${database.schema.prefix}")
	private String tablePrefix;
	@Value("${system.report.currencyFormat}")
	private String reportCurrencyFormat;
	@Value("${system.java.dateFormat}")
	private String javaDateFormat;
	@Value("${system.java.datetimeFormat}")
	private String javaDatetimeFormat;
	@Value("${system.js.dateFormat}")
	private String jsDateFormat;
	@Value("${system.js.datetimeFormat}")
	private String jsDatetimeFormat;
	@Value("${system.js.currencyFormat}")
	private String jsCurrencyFormat;

	@Value("${local.username}")
	private String localUserName;
	@Value("${local.ip}")
	private String localIp;
	
	@Value("${external.guestUserName}")
	private String externalGuestUserName;
	@Value("${external.guestName}")
	private String externalGuestName;

	public String getJsCurrencyFormat() {
		return jsCurrencyFormat;
	}

	public void setJsCurrencyFormat(String jsCurrencyFormat) {
		this.jsCurrencyFormat = jsCurrencyFormat;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getSystemName() {
		return systemName;
	}

	public void setSystemName(String systemName) {
		this.systemName = systemName;
	}
	
	public String getLogDir() {
		return logDir;
	}

	public void setLogDir(String logDir) {
		this.logDir = logDir;
	}

	public String getDatetimeFormat() {
		return datetimeFormat;
	}

	public void setDatetimeFormat(String datetimeFormat) {
		this.datetimeFormat = datetimeFormat;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public String getTablePrefix() {
		return tablePrefix;
	}

	public void setTablePrefix(String tablePrefix) {
		this.tablePrefix = tablePrefix;
	}

	public String getReportCurrencyFormat() {
		return reportCurrencyFormat;
	}

	public void setReportCurrencyFormat(String reportCurrencyFormat) {
		this.reportCurrencyFormat = reportCurrencyFormat;
	}

	public String getJavaDateFormat() {
		return javaDateFormat;
	}

	public void setJavaDateFormat(String javaDateFormat) {
		this.javaDateFormat = javaDateFormat;
	}

	public String getJavaDatetimeFormat() {
		return javaDatetimeFormat;
	}

	public void setJavaDatetimeFormat(String javaDatetimeFormat) {
		this.javaDatetimeFormat = javaDatetimeFormat;
	}

	public String getJsDateFormat() {
		return jsDateFormat;
	}

	public void setJsDateFormat(String jsDateFormat) {
		this.jsDateFormat = jsDateFormat;
	}

	public String getJsDatetimeFormat() {
		return jsDatetimeFormat;
	}

	public void setJsDatetimeFormat(String jsDatetimeFormat) {
		this.jsDatetimeFormat = jsDatetimeFormat;
	}

	public String getLocalUserName() {
		return localUserName;
	}

	public void setLocalUserName(String localUserName) {
		this.localUserName = localUserName;
	}

	public String getExternalGuestUserName() {
		return externalGuestUserName;
	}

	public void setExternalGuestUserName(String externalGuestUserName) {
		this.externalGuestUserName = externalGuestUserName;
	}

	public String getExternalGuestName() {
		return externalGuestName;
	}

	public void setExternalGuestName(String externalGuestName) {
		this.externalGuestName = externalGuestName;
	}

	public String getLocalIp() {
		return localIp;
	}

	public void setLocalIp(String localIp) {
		this.localIp = localIp;
	}

}
