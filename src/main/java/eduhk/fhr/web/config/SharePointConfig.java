package eduhk.fhr.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Configuration class for SharePoint integration via MS Graph API
 * Loads configuration from application-local.properties
 */
@Configuration
@PropertySource("classpath:application-local.properties")
public class SharePointConfig {

    @Value("${sharepoint.tenant.id}")
    private String tenantId;

    @Value("${sharepoint.client.id}")
    private String clientId;

    @Value("${sharepoint.client.secret}")
    private String clientSecret;

    @Value("${sharepoint.site.id}")
    private String siteId;

    @Value("${sharepoint.drive.id}")
    private String driveId;

    @Value("${sharepoint.max.file.size.mb:50}")
    private int maxFileSizeMb;

    @Value("${sharepoint.allowed.extensions}")
    private String allowedExtensions;

    // Getters

    public String getTenantId() {
        return tenantId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getSiteId() {
        return siteId;
    }

    public String getDriveId() {
        return driveId;
    }

    public int getMaxFileSizeMb() {
        return maxFileSizeMb;
    }

    public String getAllowedExtensions() {
        return allowedExtensions;
    }
}
