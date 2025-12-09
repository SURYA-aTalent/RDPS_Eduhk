package eduhk.fhr.web.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.xml.ws.BindingProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eduhk.fhr.web.soap.document.*;
import eduhk.fhr.web.soap.handler.TalentLinkSOAPHandler;

/**
 * SOAP-based service for TalentLink Document operations
 * Uses JAX-WS generated stubs with WS-Security authentication
 *
 * This service provides access to application-specific documents that contain
 * education, work experience, and referee data not available in the main Profile object.
 */
@Service
public class TalentLinkSOAPDocumentService {

    private static final Logger logger = LoggerFactory.getLogger(TalentLinkSOAPDocumentService.class);

    @Autowired
    private ParameterService parameterService;

    private DocumentWebService documentService;
    private boolean initialized = false;

    /**
     * Initialize SOAP service with credentials and endpoint
     */
    @PostConstruct
    public void init() {
        try {
            logger.info("Initializing TalentLink SOAP Document Service");

            // Disable automatic GZIP decompression at JVM level
            System.setProperty("jdk.httpclient.allowRestrictedHeaders", "content-length,connection,host");
            System.setProperty("http.agent", "JAX-WS/Jakarta (no-gzip)");

            // Get credentials from parameter service
            String username = parameterService.getParameterValue("TALENTLINK_USERNAME");
            String password = parameterService.getParameterValue("TALENTLINK_PASSWORD");
            String apiKey = parameterService.getParameterValue("TALENTLINK_API_KEY");

            // Set credentials in SOAP handler
            TalentLinkSOAPHandler.setCredentials(username, password);

            // Create SOAP service
            DocumentWebService_Service service = new DocumentWebService_Service();
            documentService = service.getDocumentWebServicePort();

            // Add SOAP handlers programmatically
            BindingProvider bindingProvider = (BindingProvider) documentService;
            java.util.List<jakarta.xml.ws.handler.Handler> handlerChain = new java.util.ArrayList<>();

            // Add GZIP fix handler FIRST to process responses before authentication handler
            handlerChain.add(new eduhk.fhr.web.soap.handler.GzipFixHandler());

            // Add authentication handler
            handlerChain.add(new TalentLinkSOAPHandler());

            bindingProvider.getBinding().setHandlerChain(handlerChain);

            // Set endpoint with API key
            String endpoint = getDocumentSoapUrl() + "?api_key=" + apiKey;
            bindingProvider.getRequestContext().put(
                BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                endpoint
            );

            // Disable GZIP decompression to avoid "Not in GZIP format" errors
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

            // Set HTTP headers to disable gzip encoding
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
            logger.info("TalentLink SOAP Document Service initialized successfully");

        } catch (Exception e) {
            logger.error("Failed to initialize TalentLink SOAP Document Service: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize SOAP document service", e);
        }
    }

    /**
     * Get structured document by document ID
     *
     * @param documentId Document ID
     * @return StructuredDocument with form answers
     * @throws Exception if SOAP call fails
     */
    public StructuredDocument getStructuredDocumentByDocumentId(Long documentId) throws Exception {
        if (!initialized) {
            throw new IllegalStateException("SOAP Document service not initialized");
        }

        try {
            logger.debug("Retrieving structured document {}", documentId);
            StructuredDocument doc = documentService.getStructuredDocumentById(
                documentId,
                true,  // showLocalizedValues
                LangCode.UK  // English language code
            );

            if (doc != null) {
                logger.debug("Retrieved document: {} (ID: {})", doc.getName(), doc.getId());
            } else {
                logger.warn("Document {} not found or not accessible", documentId);
            }

            return doc;
        } catch (Exception e) {
            logger.error("Failed to get document {}: {}", documentId, e.getMessage(), e);
            throw new Exception("Failed to retrieve document " + documentId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get all documents for a candidate
     *
     * @param candidateId Candidate ID
     * @return List of document objects (XML elements)
     */
    public List<Object> getDocumentsByCandidateId(Long candidateId) {
        if (!initialized) {
            throw new IllegalStateException("SOAP Document service not initialized");
        }

        try {
            logger.debug("Fetching documents for candidate {}", candidateId);
            List<Object> documents = documentService.getDocumentsByCandidateId(candidateId);
            logger.debug("Found {} documents for candidate {}",
                documents != null ? documents.size() : 0, candidateId);
            return documents != null ? documents : Collections.emptyList();
        } catch (Exception e) {
            logger.error("Failed to get documents for candidate {}: {}", candidateId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Get all documents for an application
     * This is the key method to access application-specific documents like
     * "EdUHK Education/Work Experience"
     *
     * @param applicationId Application ID
     * @return List of document objects (XML elements)
     */
    public List<Object> getDocumentsByApplicationId(Long applicationId) {
        if (!initialized) {
            throw new IllegalStateException("SOAP Document service not initialized");
        }

        try {
            logger.debug("Fetching documents for application {}", applicationId);
            List<Object> documents = documentService.getDocumentsByApplicationId(applicationId);
            logger.debug("Found {} documents for application {}",
                documents != null ? documents.size() : 0, applicationId);
            return documents != null ? documents : Collections.emptyList();
        } catch (Exception e) {
            logger.error("Failed to get documents for application {}: {}", applicationId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Get Document SOAP URL from parameters or use default
     *
     * @return SOAP URL
     */
    private String getDocumentSoapUrl() {
        String url = parameterService.getParameterValue("TALENTLINK_DOCUMENT_SOAP_URL");
        if (url == null || url.trim().isEmpty()) {
            // Default URL if parameter not configured
            url = "https://api3.lumesse-talenthub.com/HRIS/SOAP/Document";
            logger.warn("TALENTLINK_DOCUMENT_SOAP_URL not configured, using default: {}", url);
        }
        return url;
    }
}
