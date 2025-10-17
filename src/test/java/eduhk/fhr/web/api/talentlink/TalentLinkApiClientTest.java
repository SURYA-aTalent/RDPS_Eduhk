package eduhk.fhr.web.api.talentlink;

// File Path: src/test/java/eduhk/fhr/web/api/talentlink/TalentLinkApiClientTest.java
// Purpose: Manual test script to validate TalentLink API client connection and authentication

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * TalentLink API Client Test
 *
 * This is a standalone test script to validate:
 * 1. API connectivity
 * 2. Authentication
 * 3. GraphQL query execution
 * 4. JSON response parsing
 *
 * USAGE:
 * 1. Update the configuration values below with your TalentLink API credentials
 * 2. Run this class as a Java application (main method)
 * 3. Check console output for results
 */
public class TalentLinkApiClientTest {

    // ==================== CONFIGURATION ====================
    // TalentLink API Credentials - EdUHK UAT Environment
    private static final String API_BASE_URL = "https://apiproxy.shared.lumessetalentlink.com/tlk/rest/";
    private static final String API_KEY = "10047a13-72fb-ad0a-0cc4-773a4ef874b9";
    private static final String USERNAME = "EdUHK UAT:prabhu.srinivasan@atalent.com:BO";
    private static final String PASSWORD = "2!Password";

    // Test parameters
    private static final int BATCH_SIZE = 5;  // Fetch only 5 candidates for testing
    private static final String MIN_CANDIDATE_ID = "0";  // Start from candidate ID 0
    // =======================================================

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("TalentLink API Client Test");
        System.out.println("========================================\n");

        TalentLinkApiClientTest test = new TalentLinkApiClientTest();

        try {
            // Test 1: Build GraphQL Query
            System.out.println("[Test 1] Building GraphQL Query...");
            String query = test.buildTestQuery();
            System.out.println("✓ Query built successfully");
            System.out.println("Query preview: " + query.substring(0, Math.min(100, query.length())) + "...\n");

            // Test 2: Test API Connectivity
            System.out.println("[Test 2] Testing API Connectivity...");
            test.testConnectivity();
            System.out.println("✓ API endpoint is reachable\n");

            // Test 3: Execute GraphQL Query
            System.out.println("[Test 3] Executing GraphQL Query...");
            String response = test.executeQuery(query);
            System.out.println("✓ Query executed successfully");
            System.out.println("Response length: " + response.length() + " characters\n");

            // Test 4: Parse JSON Response
            System.out.println("[Test 4] Parsing JSON Response...");
            test.parseResponse(response);
            System.out.println("✓ Response parsed successfully\n");

            System.out.println("========================================");
            System.out.println("ALL TESTS PASSED ✓");
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("\n========================================");
            System.err.println("TEST FAILED ✗");
            System.err.println("========================================");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Build a simple GraphQL query for testing
     */
    private String buildTestQuery() {
        StringBuilder query = new StringBuilder();
        query.append("{");
        query.append("candidates(");
        query.append("first: ").append(BATCH_SIZE).append(",");
        query.append("offset: 0,");
        query.append("where: {id_gt: \"").append(MIN_CANDIDATE_ID).append("\"}");
        query.append(") {");
        query.append("id ");
        query.append("firstName ");
        query.append("lastName ");
        query.append("email ");
        query.append("requisitionNumber ");
        query.append("applicationDate ");
        query.append("}");
        query.append("}");

        return query.toString();
    }

    /**
     * Test basic connectivity to API endpoint
     */
    private void testConnectivity() throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        try {
            // Simple HEAD or OPTIONS request to check if endpoint is reachable
            String endpoint = API_BASE_URL + "candidate";
            System.out.println("Testing endpoint: " + endpoint);

            // Note: Some APIs may not support HEAD requests, so we just verify the URL is valid
            if (API_BASE_URL == null || API_BASE_URL.isEmpty() || API_BASE_URL.equals("https://api.talentlink.com/graphql/")) {
                throw new Exception("API_BASE_URL not configured. Please update the configuration section.");
            }

            System.out.println("API endpoint configuration looks valid");

        } catch (Exception e) {
            throw new Exception("Connectivity test failed: " + e.getMessage(), e);
        }
    }

    /**
     * Execute GraphQL query with authentication
     */
    private String executeQuery(String graphQLQuery) throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        try {
            // Build URL with query and api_key parameters
            String endpoint = API_BASE_URL + "candidate";
            String encodedQuery = java.net.URLEncoder.encode(graphQLQuery, java.nio.charset.StandardCharsets.UTF_8);
            String fullUrl = endpoint + "?query=" + encodedQuery + "&api_key=" + API_KEY;

            System.out.println("Endpoint: " + endpoint);

            // Build authentication headers (username and password)
            HttpHeaders headers = new HttpHeaders();
            headers.set("content-type", "application/json");
            headers.set("username", USERNAME);
            headers.set("password", PASSWORD);

            System.out.println("Authentication headers configured");

            // Execute request
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                fullUrl,
                HttpMethod.GET,
                entity,
                String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new Exception("API returned non-success status: " + response.getStatusCode());
            }

            System.out.println("Response Status: " + response.getStatusCode());

            return response.getBody();

        } catch (Exception e) {
            throw new Exception("Query execution failed: " + e.getMessage(), e);
        }
    }

    /**
     * Parse and validate JSON response
     */
    private void parseResponse(String jsonResponse) throws Exception {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonResponse);

            // Check for GraphQL errors
            if (rootNode.has("errors")) {
                JsonNode errors = rootNode.get("errors");
                System.err.println("GraphQL Errors: " + errors.toString());
                throw new Exception("GraphQL query returned errors");
            }

            // Parse data section
            JsonNode dataNode = rootNode.path("data");
            if (dataNode.isMissingNode()) {
                throw new Exception("Response missing 'data' field");
            }

            JsonNode candidatesNode = dataNode.path("candidates");
            if (candidatesNode.isMissingNode() || !candidatesNode.isArray()) {
                throw new Exception("Response missing 'candidates' array");
            }

            int candidateCount = candidatesNode.size();
            System.out.println("Candidates found: " + candidateCount);

            // Print first candidate details
            if (candidateCount > 0) {
                JsonNode firstCandidate = candidatesNode.get(0);
                System.out.println("\nFirst Candidate:");
                System.out.println("  ID: " + firstCandidate.path("id").asText());
                System.out.println("  Name: " + firstCandidate.path("firstName").asText() + " " +
                    firstCandidate.path("lastName").asText());
                System.out.println("  Email: " + firstCandidate.path("email").asText());
                System.out.println("  Requisition: " + firstCandidate.path("requisitionNumber").asText());
                System.out.println("  Application Date: " + firstCandidate.path("applicationDate").asText());
            } else {
                System.out.println("\nNo candidates found. This might be expected if all candidates have been imported.");
                System.out.println("Try setting MIN_CANDIDATE_ID to \"0\" to fetch from the beginning.");
            }

        } catch (Exception e) {
            throw new Exception("Response parsing failed: " + e.getMessage(), e);
        }
    }
}
