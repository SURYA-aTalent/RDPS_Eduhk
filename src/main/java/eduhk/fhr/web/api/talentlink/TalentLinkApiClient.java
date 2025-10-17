package eduhk.fhr.web.api.talentlink;

// File Path: src/main/java/eduhk/fhr/web/api/talentlink/TalentLinkApiClient.java
// Purpose: HTTP client wrapper for TalentLink REST API interactions with authentication

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import eduhk.fhr.web.service.ParameterService;

/**
 * TalentLink API Client
 *
 * Handles all HTTP interactions with the TalentLink REST API.
 * Responsibilities:
 * - Build authenticated requests with proper headers
 * - Execute HTTP calls to TalentLink endpoints
 * - Handle API responses and errors
 * - Implement retry logic for transient failures
 */
@Component
public class TalentLinkApiClient {

    private static final Logger logger = LoggerFactory.getLogger(TalentLinkApiClient.class);

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Execute a GraphQL query against the TalentLink candidate endpoint
     *
     * @param graphQLQuery The GraphQL query string
     * @return JSON response as String
     * @throws TalentLinkApiException if API call fails
     */
    public String executeGraphQLQuery(String graphQLQuery) throws TalentLinkApiException {
        logger.info("Executing GraphQL query: {}", graphQLQuery);

        // Get the full API URL (already includes /candidate endpoint)
        String endpoint = getApiBaseUrl();

        // Get API key and pool ID from parameter service
        String apiKey = parameterService.getParameterValue("TALENTLINK_API_KEY");
        String poolId = parameterService.getParameterValue("TALENTLINK_POOL_ID");

        // Build URL with api_key and poolId parameters
        // Note: TalentLink API requires either poolId or openingId as URL parameter
        String fullUrl = endpoint + "?api_key=" + apiKey;

        // Add poolId if configured
        if (poolId != null && !poolId.isEmpty() && !poolId.equalsIgnoreCase("NONE")) {
            fullUrl += "&poolId=" + poolId;
            logger.info("Using poolId: {}", poolId);
        } else {
            logger.warn("No poolId configured - API may reject request");
        }

        logger.debug("Full URL: {}", fullUrl.replaceAll("api_key=[^&]+", "api_key=***"));

        // Build HTTP headers with authentication and GraphQL content type
        HttpHeaders headers = buildAuthHeaders();

        // Build GraphQL request body as JSON
        String requestBody = "{\"query\": \"" + graphQLQuery.replace("\"", "\\\"") + "\"}";
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        // Execute HTTP POST request with retry logic
        int maxAttempts = getMaxRetryAttempts();
        int attempt = 1;

        while (attempt <= maxAttempts) {
            try {
                logger.debug("API call attempt {}/{}", attempt, maxAttempts);

                ResponseEntity<String> response = restTemplate.exchange(
                        fullUrl,
                        HttpMethod.POST,
                        entity,
                        String.class
                );

                if (response.getStatusCode().is2xxSuccessful()) {
                    logger.info("API call successful");
                    return response.getBody();
                } else {
                    throw new TalentLinkApiException(
                            "API returned non-success status: " + response.getStatusCode(),
                            response.getStatusCode().value(),
                            response.getBody()
                    );
                }

            } catch (org.springframework.web.client.HttpClientErrorException e) {
                // 4xx errors - don't retry
                logger.error("HTTP client error: {}", e.getMessage());
                throw new TalentLinkApiException(
                        "HTTP client error: " + e.getMessage(),
                        e.getStatusCode().value(),
                        e.getResponseBodyAsString()
                );

            } catch (org.springframework.web.client.HttpServerErrorException e) {
                // 5xx errors - retry
                logger.warn("HTTP server error (attempt {}/{}): {}", attempt, maxAttempts, e.getMessage());

                if (attempt >= maxAttempts) {
                    throw new TalentLinkApiException(
                            "HTTP server error after " + maxAttempts + " attempts: " + e.getMessage(),
                            e.getStatusCode().value(),
                            e.getResponseBodyAsString()
                    );
                }

                // Wait before retry
                try {
                    long delayMs = getRetryDelayMs();
                    logger.debug("Waiting {}ms before retry", delayMs);
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new TalentLinkApiException("Interrupted during retry delay", ie);
                }

                attempt++;

            } catch (Exception e) {
                logger.error("Unexpected error during API call: {}", e.getMessage(), e);
                throw new TalentLinkApiException("Unexpected error: " + e.getMessage(), e);
            }
        }

        throw new TalentLinkApiException("Failed to execute query after " + maxAttempts + " attempts");
    }

    /**
     * Get candidate details by candidate ID
     *
     * @param candidateId The candidate ID
     * @return JSON response as String
     * @throws TalentLinkApiException if API call fails
     */
    public String getCandidateById(String candidateId) throws TalentLinkApiException {
        logger.info("Fetching candidate by ID: {}", candidateId);

        String baseUrl = getApiBaseUrl();
        String url = baseUrl + "candidate/" + candidateId + "/summary";

        HttpHeaders headers = buildAuthHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                throw new TalentLinkApiException(
                        "Failed to fetch candidate: " + response.getStatusCode(),
                        response.getStatusCode().value(),
                        response.getBody()
                );
            }

        } catch (Exception e) {
            logger.error("Error fetching candidate {}: {}", candidateId, e.getMessage());
            throw new TalentLinkApiException("Error fetching candidate: " + e.getMessage(), e);
        }
    }

    /**
     * Build HTTP headers with TalentLink authentication
     *
     * @return HttpHeaders with authentication
     */
    private HttpHeaders buildAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();

        // Get credentials from parameter service
        String username = parameterService.getParameterValue("TALENTLINK_USERNAME");
        String password = parameterService.getParameterValue("TALENTLINK_PASSWORD");

        // Set headers as per TalentLink API requirements
        // Note: api_key is passed as URL parameter, not header
        headers.set("content-type", "application/json");
        headers.set("username", username != null ? username : "");
        headers.set("password", password != null ? password : "");

        return headers;
    }

    /**
     * Get TalentLink API base URL from configuration
     *
     * @return Base URL string
     */
    private String getApiBaseUrl() {
        String url = parameterService.getParameterValue("TALENTLINK_API_URL");
        if (url == null || url.isEmpty()) {
            url = "https://apiproxy.shared.lumessetalentlink.com/tlk/rest/";
        }
        // Ensure URL ends with /
        if (!url.endsWith("/")) {
            url += "/";
        }
        return url;
    }

    /**
     * Get maximum retry attempts from configuration
     *
     * @return Max retry attempts
     */
    private int getMaxRetryAttempts() {
        String value = parameterService.getParameterValue("API_RETRY_MAX_ATTEMPTS");
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid API_RETRY_MAX_ATTEMPTS value: {}", value);
            }
        }
        return 3; // Default
    }

    /**
     * Get retry delay in milliseconds from configuration
     *
     * @return Retry delay in ms
     */
    private long getRetryDelayMs() {
        String value = parameterService.getParameterValue("API_RETRY_DELAY_MS");
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid API_RETRY_DELAY_MS value: {}", value);
            }
        }
        return 5000; // Default 5 seconds
    }
}
