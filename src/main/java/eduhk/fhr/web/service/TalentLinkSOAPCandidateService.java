package eduhk.fhr.web.service;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Holder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eduhk.fhr.web.dto.talentlink.TalentLinkCandidateDTO;
import eduhk.fhr.web.dto.talentlink.TalentLinkPositionDTO;
import eduhk.fhr.web.soap.candidate.*;
import eduhk.fhr.web.soap.handler.TalentLinkSOAPHandler;

/**
 * SOAP-based service for TalentLink Candidate operations
 * Uses JAX-WS generated stubs with WS-Security authentication
 */
@Service
public class TalentLinkSOAPCandidateService {

    private static final Logger logger = LoggerFactory.getLogger(TalentLinkSOAPCandidateService.class);

    @Autowired
    private ParameterService parameterService;

    private CandidateWebService candidateService;
    private boolean initialized = false;

    /**
     * Initialize SOAP service with credentials and endpoint
     */
    @PostConstruct
    public void init() {
        try {
            logger.info("Initializing TalentLink SOAP Candidate Service");

            // Disable automatic GZIP decompression at JVM level
            // This prevents "Not in GZIP format" errors when server sends uncompressed data
            System.setProperty("jdk.httpclient.allowRestrictedHeaders", "content-length,connection,host");
            System.setProperty("http.agent", "JAX-WS/Jakarta (no-gzip)");

            // Get credentials from parameter service
            String username = parameterService.getParameterValue("TALENTLINK_USERNAME");
            String password = parameterService.getParameterValue("TALENTLINK_PASSWORD");
            String apiKey = parameterService.getParameterValue("TALENTLINK_API_KEY");

            // Set credentials in SOAP handler
            TalentLinkSOAPHandler.setCredentials(username, password);

            // Create SOAP service
            CandidateWebService_Service service = new CandidateWebService_Service();
            candidateService = service.getCandidateWebServicePort();

            // Add SOAP handlers programmatically
            BindingProvider bindingProvider = (BindingProvider) candidateService;
            java.util.List<jakarta.xml.ws.handler.Handler> handlerChain = new java.util.ArrayList<>();

            // Add GZIP fix handler FIRST to process responses before authentication handler
            handlerChain.add(new eduhk.fhr.web.soap.handler.GzipFixHandler());

            // Add authentication handler
            handlerChain.add(new TalentLinkSOAPHandler());

            bindingProvider.getBinding().setHandlerChain(handlerChain);

            // Set endpoint with API key
            String endpoint = getCandidateSoapUrl() + "?api_key=" + apiKey;
            bindingProvider.getRequestContext().put(
                BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                endpoint
            );

            // Disable GZIP decompression to avoid "Not in GZIP format" errors
            // TalentLink sends uncompressed data but JAX-WS tries to decompress it
            // Try both property names for different JAX-WS implementations
            bindingProvider.getRequestContext().put(
                "com.sun.xml.ws.transport.http.client.HttpTransportPipe.noGzip",
                Boolean.TRUE
            );
            bindingProvider.getRequestContext().put(
                "com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.noGzip",
                Boolean.TRUE
            );
            bindingProvider.getRequestContext().put(
                "com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump",
                Boolean.FALSE
            );

            // Also try setting HTTP headers to disable gzip encoding
            java.util.Map<String, java.util.List<String>> httpHeaders = new java.util.HashMap<>();
            httpHeaders.put("Accept-Encoding", java.util.Collections.singletonList("identity"));
            bindingProvider.getRequestContext().put(
                jakarta.xml.ws.BindingProvider.SESSION_MAINTAIN_PROPERTY,
                Boolean.TRUE
            );
            bindingProvider.getRequestContext().put(
                "com.sun.xml.ws.transport.http.client.HttpClientTransport.HTTP_REQUEST_HEADERS",
                httpHeaders
            );
            // Set timeouts for document downloads (can be large files)
            bindingProvider.getRequestContext().put(
                "com.sun.xml.internal.ws.developer.JAXWSProperties.CONNECT_TIMEOUT",
                30000 // 30 seconds
            );
            bindingProvider.getRequestContext().put(
                "com.sun.xml.internal.ws.developer.JAXWSProperties.REQUEST_TIMEOUT",
                60000 // 60 seconds for large file downloads
            );

            initialized = true;
            logger.info("TalentLink SOAP Candidate Service initialized successfully");

        } catch (Exception e) {
            logger.error("Failed to initialize TalentLink SOAP Candidate Service: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize SOAP candidate service", e);
        }
    }

    /**
     * Get candidates by search criteria
     *
     * @param page Page number (0-based)
     * @param limit Maximum number of candidates to return (not used by actual API)
     * @return List of candidate profiles
     * @throws Exception if SOAP call fails
     */
    public List<Profile> getCandidates(Integer page, int limit) throws Exception {
        if (!initialized) {
            throw new IllegalStateException("SOAP Candidate service not initialized");
        }

        try {
            logger.info("Fetching candidates via SOAP, page: {}", page);

            // Create candidate search criteria
            CandidateSearchCriteriaDTO searchDto = new CandidateSearchCriteriaDTO();
            // The API accepts search criteria but we'll fetch all for now

            // Get candidate IDs (page-based)
            CandidateIdsDTO candidateIds = candidateService.getCandidates(page, searchDto);

            if (candidateIds == null || candidateIds.getResult() == null ||
                candidateIds.getResult().getCandidateId() == null) {
                logger.warn("No candidates found");
                return new ArrayList<>();
            }

            List<Long> ids = candidateIds.getResult().getCandidateId();
            logger.info("Found {} candidate IDs on page {}", ids.size(), page);

            // Fetch full profiles for each candidate (up to limit)
            List<Profile> profiles = new ArrayList<>();
            int count = 0;
            for (Long candidateId : ids) {
                if (limit > 0 && count >= limit) {
                    break;
                }
                try {
                    // getCandidateById requires displayConsents and displayBoTags flags
                    Profile profile = candidateService.getCandidateById(candidateId, false, false);
                    if (profile != null) {
                        profiles.add(profile);
                        count++;
                    }
                } catch (Exception e) {
                    logger.error("Error fetching candidate {}: {}", candidateId, e.getMessage());
                }
            }

            logger.info("Successfully retrieved {} candidate profiles via SOAP", profiles.size());
            return profiles;

        } catch (Exception e) {
            logger.error("Error getting candidates via SOAP: {}", e.getMessage(), e);
            throw new Exception("Failed to get candidates: " + e.getMessage(), e);
        }
    }

    /**
     * Get candidate by ID
     *
     * @param candidateId Candidate ID
     * @return Candidate profile
     * @throws Exception if SOAP call fails
     */
    public Profile getCandidateById(Long candidateId) throws Exception {
        if (!initialized) {
            throw new IllegalStateException("SOAP Candidate service not initialized");
        }

        try {
            logger.info("Fetching candidate {} via SOAP", candidateId);
            Profile profile = candidateService.getCandidateById(candidateId, false, false);
            logger.info("Successfully retrieved candidate {} via SOAP", candidateId);
            return profile;
        } catch (Exception e) {
            logger.error("Error getting candidate {}: {}", candidateId, e.getMessage(), e);
            throw new Exception("Failed to get candidate: " + e.getMessage(), e);
        }
    }

    /**
     * Create candidate via folder/pool
     *
     * @param candidate Candidate holder
     * @param folderId Folder/Pool ID
     * @throws Exception if SOAP call fails
     */
    public void createCandidateViaFolder(Holder<Profile> candidate, Long folderId) throws Exception {
        if (!initialized) {
            throw new IllegalStateException("SOAP Candidate service not initialized");
        }

        try {
            logger.info("Creating candidate via SOAP with folderId: {}", folderId);
            candidateService.createCandidateViaFolder(candidate, folderId);
            logger.info("Successfully created candidate via SOAP, ID: {}", candidate.value.getId());
        } catch (Exception e) {
            logger.error("Error creating candidate via SOAP: {}", e.getMessage(), e);
            throw new Exception("Failed to create candidate: " + e.getMessage(), e);
        }
    }

    /**
     * Convert SOAP Profile to DTO
     *
     * @param profile SOAP Profile
     * @return TalentLinkCandidateDTO
     */
    public TalentLinkCandidateDTO convertProfileToDTO(Profile profile) {
        if (profile == null) {
            return null;
        }

        TalentLinkCandidateDTO dto = new TalentLinkCandidateDTO();

        // Map basic fields
        if (profile.getId() != null) {
            dto.setId(String.valueOf(profile.getId()));
        }
        dto.setFirstname(profile.getFirstname());
        dto.setLastname(profile.getLastname());
        dto.setEmail(profile.getEmail());

        // Set default position for candidates without application
        // SOAP Profile doesn't contain opening/requisition info directly
        // This would require fetching application data separately
        TalentLinkPositionDTO position = new TalentLinkPositionDTO();
        position.setPosition("POOL"); // Default requisition for pool candidates
        dto.setPosition(position);

        // Note: Additional date fields from profile.getCreation() not mapped
        // as TalentLinkCandidateDTO doesn't have a creation date field

        return dto;
    }

    /**
     * Get candidate SOAP URL from parameters
     *
     * @return SOAP URL
     */
    private String getCandidateSoapUrl() {
        String url = parameterService.getParameterValue("TALENTLINK_CANDIDATE_SOAP_URL");
        if (url == null || url.trim().isEmpty()) {
            // Default URL if parameter not configured
            url = "https://api3.lumesse-talenthub.com/HRIS/SOAP/Candidate";
            logger.warn("TALENTLINK_CANDIDATE_SOAP_URL not configured, using default: {}", url);
        }
        return url;
    }

    /**
     * Get all applications for a candidate (contains document lists)
     *
     * @param candidateId Candidate ID
     * @return List of applications
     * @throws Exception if SOAP call fails
     */
    public List<ApplicationDto> getApplicationsByCandidateId(Long candidateId) throws Exception {
        if (!initialized) {
            throw new IllegalStateException("SOAP Candidate service not initialized");
        }

        try {
            logger.info("Fetching applications for candidate {}", candidateId);
            List<ApplicationDto> apps = candidateService.getApplicationsByCandidateId(candidateId, true);
            logger.info("Found {} applications for candidate {}", apps != null ? apps.size() : 0, candidateId);
            return apps != null ? apps : new ArrayList<>();
        } catch (Exception e) {
            logger.error("Error fetching applications for candidate {}: {}", candidateId, e.getMessage(), e);
            throw new Exception("Failed to get applications: " + e.getMessage(), e);
        }
    }

    /**
     * Download document binary data from TalentLink
     * Uses REST API to avoid SOAP GZIP decompression issues
     *
     * @param documentId Document ID
     * @return AttachedFileDto with binary data
     * @throws Exception if download fails
     */
    public AttachedFileDto downloadAttachedFile(Long documentId) throws Exception {
        if (!initialized) {
            throw new IllegalStateException("SOAP Candidate service not initialized");
        }

        try {
            logger.info("Downloading document {} via REST API", documentId);
            return downloadAttachedFileViaRest(documentId);
        } catch (Exception e) {
            logger.error("Error downloading document {}: {}", documentId, e.getMessage(), e);
            throw new Exception("Failed to download document: " + e.getMessage(), e);
        }
    }

    /**
     * Download document using TalentLink REST API
     * REST API URL: https://apiproxy.shared.lumessetalentlink.com/tlk/rest/candidate/attachment/{documentId}
     *
     * @param documentId Document ID
     * @return AttachedFileDto with binary data
     * @throws Exception if REST call fails
     */
    private AttachedFileDto downloadAttachedFileViaRest(Long documentId) throws Exception {
        String apiKey = parameterService.getParameterValue("TALENTLINK_API_KEY");
        String username = parameterService.getParameterValue("TALENTLINK_USERNAME");
        String password = parameterService.getParameterValue("TALENTLINK_PASSWORD");

        // REST API endpoint
        String restUrl = "https://apiproxy.shared.lumessetalentlink.com/tlk/rest/candidate/attachment/" + documentId;

        java.net.URL url = new java.net.URL(restUrl + "?api_key=" + apiKey);
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();

        try {
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Authorization", createBasicAuthHeader(username, password));
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000);

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new Exception("HTTP error: " + responseCode);
            }

            // Read JSON response
            String jsonResponse;
            try (java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                jsonResponse = sb.toString();
            }

            // Parse JSON response (simple parsing without JSON library)
            String fileName = extractJsonValue(jsonResponse, "fileName");
            String base64Content = extractJsonValue(jsonResponse, "base64EncodedContent");

            if (fileName == null || base64Content == null) {
                throw new Exception("Invalid REST response - missing fileName or base64EncodedContent");
            }

            // Decode base64 content
            byte[] fileBytes = java.util.Base64.getDecoder().decode(base64Content);

            // Create AttachedFileDto
            AttachedFileDto fileDto = new AttachedFileDto();
            fileDto.setFileName(fileName);
            fileDto.setSize((long) fileBytes.length);

            // Create DataSource
            javax.activation.DataSource dataSource = new javax.activation.DataSource() {
                @Override
                public java.io.InputStream getInputStream() throws java.io.IOException {
                    return new java.io.ByteArrayInputStream(fileBytes);
                }

                @Override
                public java.io.OutputStream getOutputStream() throws java.io.IOException {
                    throw new java.io.IOException("Read-only data source");
                }

                @Override
                public String getContentType() {
                    return "application/octet-stream";
                }

                @Override
                public String getName() {
                    return fileName;
                }
            };

            fileDto.setBinaryData(new javax.activation.DataHandler(dataSource));

            logger.info("Downloaded document via REST: {} (size: {} bytes)", fileName, fileBytes.length);
            return fileDto;

        } finally {
            conn.disconnect();
        }
    }

    /**
     * Create Basic Authentication header
     *
     * @param username Username
     * @param password Password
     * @return Basic auth header value
     */
    private String createBasicAuthHeader(String username, String password) {
        String credentials = username + ":" + password;
        String encoded = java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
        return "Basic " + encoded;
    }

    /**
     * Extract value from JSON string (simple parser)
     *
     * @param json JSON string
     * @param key JSON key
     * @return Value or null
     */
    private String extractJsonValue(String json, String key) {
        // Simple JSON parsing - looks for "key":"value" or "key":"value with spaces"
        String searchPattern = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchPattern);
        if (keyIndex == -1) {
            return null;
        }

        int colonIndex = json.indexOf(":", keyIndex + searchPattern.length());
        if (colonIndex == -1) {
            return null;
        }

        int valueStartIndex = json.indexOf("\"", colonIndex);
        if (valueStartIndex == -1) {
            return null;
        }
        valueStartIndex++; // Skip opening quote

        int valueEndIndex = json.indexOf("\"", valueStartIndex);
        if (valueEndIndex == -1) {
            return null;
        }

        return json.substring(valueStartIndex, valueEndIndex);
    }

    /**
     * Download document using direct HTTP connection (bypasses SOAP GZIP issue)
     *
     * @param documentId Document ID
     * @return AttachedFileDto with binary data
     * @throws Exception if HTTP call fails
     */
    private AttachedFileDto downloadAttachedFileViaHttp(Long documentId) throws Exception {
        String baseUrl = getCandidateSoapUrl();
        String apiKey = parameterService.getParameterValue("TALENTLINK_API_KEY");
        String username = parameterService.getParameterValue("TALENTLINK_USERNAME");
        String password = parameterService.getParameterValue("TALENTLINK_PASSWORD");

        // Build SOAP envelope for downloadAttachedFile
        String soapEnvelope = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
            "xmlns:can=\"http://candidate.soap.hris.lumesse.com/\">" +
            "<soap:Header>" +
            "<wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" " +
            "soap:mustUnderstand=\"1\">" +
            "<wsse:UsernameToken>" +
            "<wsse:Username>" + username + "</wsse:Username>" +
            "<wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">" +
            password + "</wsse:Password>" +
            "</wsse:UsernameToken>" +
            "</wsse:Security>" +
            "</soap:Header>" +
            "<soap:Body>" +
            "<can:downloadAttachedFile>" +
            "<documentId>" + documentId + "</documentId>" +
            "</can:downloadAttachedFile>" +
            "</soap:Body>" +
            "</soap:Envelope>";

        java.net.URL url = new java.net.URL(baseUrl + "?api_key=" + apiKey);
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();

        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            conn.setRequestProperty("Accept-Encoding", "identity"); // Disable GZIP
            conn.setRequestProperty("SOAPAction", "");
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000);
            conn.setDoOutput(true);

            // Send SOAP request
            try (java.io.OutputStream os = conn.getOutputStream()) {
                os.write(soapEnvelope.getBytes("UTF-8"));
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                // Read error response
                String errorResponse = "";
                try (java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getErrorStream(), "UTF-8"))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }
                    errorResponse = sb.toString();
                } catch (Exception e) {
                    // Ignore error reading error stream
                }
                logger.error("HTTP download failed for document {}, response code: {}, error: {}",
                    documentId, responseCode, errorResponse);
                throw new Exception("HTTP error: " + responseCode + " - " + errorResponse);
            }

            // Read response - NO GZIP decompression applied
            String response;
            try (java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                response = sb.toString();
            }

            // Parse SOAP response to extract file data
            AttachedFileDto fileDto = parseSoapResponse(response, documentId);
            logger.info("Downloaded document via HTTP: {} (size: {} bytes)",
                fileDto.getFileName(), fileDto.getSize());

            return fileDto;

        } finally {
            conn.disconnect();
        }
    }

    /**
     * Parse SOAP response to extract AttachedFileDto
     *
     * @param soapResponse SOAP XML response
     * @param documentId Document ID (for logging)
     * @return AttachedFileDto
     * @throws Exception if parsing fails
     */
    private AttachedFileDto parseSoapResponse(String soapResponse, Long documentId) throws Exception {
        // Extract values from SOAP XML
        String fileName = extractXmlValue(soapResponse, "fileName");
        String sizeStr = extractXmlValue(soapResponse, "size");
        String base64Data = extractXmlValue(soapResponse, "binaryData");

        if (fileName == null || base64Data == null) {
            throw new Exception("Invalid SOAP response - missing fileName or binaryData");
        }

        AttachedFileDto fileDto = new AttachedFileDto();
        fileDto.setFileName(fileName);
        if (sizeStr != null && !sizeStr.isEmpty()) {
            fileDto.setSize(Long.parseLong(sizeStr));
        }

        // Decode base64 binary data and create DataHandler
        byte[] fileBytes = java.util.Base64.getDecoder().decode(base64Data);

        // Create simple DataSource implementation
        javax.activation.DataSource dataSource = new javax.activation.DataSource() {
            @Override
            public java.io.InputStream getInputStream() throws java.io.IOException {
                return new java.io.ByteArrayInputStream(fileBytes);
            }

            @Override
            public java.io.OutputStream getOutputStream() throws java.io.IOException {
                throw new java.io.IOException("Read-only data source");
            }

            @Override
            public String getContentType() {
                return "application/octet-stream";
            }

            @Override
            public String getName() {
                return fileName;
            }
        };

        fileDto.setBinaryData(new javax.activation.DataHandler(dataSource));

        return fileDto;
    }

    /**
     * Extract value from XML tag
     *
     * @param xml XML string
     * @param tagName Tag name
     * @return Tag value or null
     */
    private String extractXmlValue(String xml, String tagName) {
        // Simple XML parsing - looks for <tagName>value</tagName>
        String startTag = "<" + tagName + ">";
        String endTag = "</" + tagName + ">";

        int startIndex = xml.indexOf(startTag);
        if (startIndex == -1) {
            // Try with namespace prefix
            startTag = ":" + tagName + ">";
            startIndex = xml.indexOf(startTag);
            if (startIndex == -1) {
                return null;
            }
            startIndex += startTag.length();
        } else {
            startIndex += startTag.length();
        }

        int endIndex = xml.indexOf(endTag, startIndex);
        if (endIndex == -1) {
            return null;
        }

        return xml.substring(startIndex, endIndex).trim();
    }

    /**
     * Extract all document metadata from candidate's applications
     *
     * @param candidateId Candidate ID
     * @return List of DocumentBaseDto objects
     * @throws Exception if SOAP call fails
     */
    public List<DocumentBaseDto> getCandidateDocuments(Long candidateId) throws Exception {
        List<DocumentBaseDto> allDocs = new ArrayList<>();

        try {
            List<ApplicationDto> applications = getApplicationsByCandidateId(candidateId);

            for (ApplicationDto app : applications) {
                ApplicationDto.Documents docs = app.getDocuments();
                if (docs != null && docs.getDocument() != null) {
                    List<DocumentBaseDto> docList = docs.getDocument();
                    if (docList != null && !docList.isEmpty()) {
                        allDocs.addAll(docList);
                        logger.debug("Application {} has {} documents", app.getId(), docList.size());
                    }
                }
            }

            logger.info("Found {} total documents for candidate {}", allDocs.size(), candidateId);
        } catch (Exception e) {
            logger.error("Error getting documents for candidate {}: {}", candidateId, e.getMessage(), e);
            throw new Exception("Failed to get candidate documents: " + e.getMessage(), e);
        }

        return allDocs;
    }

    /**
     * Check if service is initialized
     *
     * @return true if initialized
     */
    public boolean isInitialized() {
        return initialized;
    }
}
