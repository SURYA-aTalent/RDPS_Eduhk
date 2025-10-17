package eduhk.fhr.web.config;

// File Path: src/main/java/eduhk/fhr/web/config/RestTemplateConfig.java
// Purpose: Configuration for RestTemplate bean (used by TalentLink API client)

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate Configuration
 *
 * Configures RestTemplate bean for HTTP client operations.
 * Used by TalentLinkApiClient to make API calls.
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Create RestTemplate bean with timeout configuration
     *
     * @return Configured RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        // TODO: Configure connection and read timeouts
        // TODO: Add error handler if needed
        // TODO: Add interceptors for logging if needed

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // Set timeouts (30 seconds)
        factory.setConnectTimeout(30000);
        factory.setReadTimeout(30000);

        return new RestTemplate(factory);
    }
}
