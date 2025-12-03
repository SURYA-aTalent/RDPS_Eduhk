package eduhk.fhr.web.service;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.models.DriveItem;
import eduhk.fhr.web.config.SharePointConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Service for uploading files to SharePoint via MS Graph API
 * Uses Client Credentials (app-only) authentication
 * Pattern follows TalentLinkSOAPCandidateService initialization approach
 */
@Service
public class SharePointUploadService extends BaseService {

    @Autowired
    private SharePointConfig config;

    private GraphServiceClient graphClient;
    private boolean initialized = false;

    /**
     * Initialize MS Graph client with credentials from configuration
     * Called after bean construction (like TalentLink services)
     */
    @PostConstruct
    public void init() {
        try {
            logger.info("Initializing SharePoint Upload Service");

            // Create credential using Client Credentials flow
            TokenCredential credential = new ClientSecretCredentialBuilder()
                .tenantId(config.getTenantId())
                .clientId(config.getClientId())
                .clientSecret(config.getClientSecret())
                .build();

            // Create scopes for authentication
            List<String> scopes = Collections.singletonList("https://graph.microsoft.com/.default");

            // Create authentication provider
            TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(scopes, credential);

            // Build Graph client with authentication provider
            graphClient = GraphServiceClient.builder()
                .authenticationProvider(authProvider)
                .buildClient();

            initialized = true;
            logger.info("SharePoint Upload Service initialized successfully");

        } catch (Exception e) {
            logger.error("Failed to initialize SharePoint Upload Service: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize SharePoint service", e);
        }
    }

    /**
     * Upload file to SharePoint
     *
     * @param file MultipartFile to upload
     * @return DriveItem representing uploaded file
     * @throws Exception if upload fails
     */
    public DriveItem uploadFile(MultipartFile file) throws Exception {
        if (!initialized) {
            throw new IllegalStateException("SharePoint service not initialized");
        }

        // Validate file
        validateFile(file);

        // Sanitize filename
        String sanitizedFilename = sanitizeFilename(file.getOriginalFilename());

        logger.info("Uploading file: {} (size: {} bytes)", sanitizedFilename, file.getSize());

        // Upload to SharePoint root
        byte[] fileBytes = file.getInputStream().readAllBytes();

        DriveItem uploadedItem = graphClient
            .sites(config.getSiteId())
            .drives(config.getDriveId())
            .root()
            .itemWithPath(sanitizedFilename)
            .content()
            .buildRequest()
            .put(fileBytes);

        logger.info("File uploaded successfully. SharePoint ID: {}", uploadedItem.id);
        logger.info("Web URL: {}", uploadedItem.webUrl);

        return uploadedItem;
    }

    /**
     * Validate file before upload
     *
     * @param file File to validate
     * @throws Exception if validation fails
     */
    private void validateFile(MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new Exception("File is empty");
        }

        // Check file size
        long maxSizeBytes = config.getMaxFileSizeMb() * 1024L * 1024L;
        if (file.getSize() > maxSizeBytes) {
            throw new Exception("File size exceeds maximum: " + config.getMaxFileSizeMb() + " MB");
        }

        // Check file extension
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.contains(".")) {
            throw new Exception("File must have an extension");
        }

        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        List<String> allowedList = Arrays.asList(config.getAllowedExtensions().split(","));

        if (!allowedList.contains(extension)) {
            throw new Exception("File type not allowed. Allowed: " + config.getAllowedExtensions());
        }
    }

    /**
     * Sanitize filename - remove unsafe characters
     * Removes: \ / : * ? " < > |
     *
     * @param filename Original filename
     * @return Sanitized filename
     */
    private String sanitizeFilename(String filename) {
        if (filename == null) {
            return "unnamed_file";
        }

        // Remove unsafe characters
        String sanitized = filename.replaceAll("[\\\\/:*?\"<>|]", "_");

        // Limit length to 200 characters
        if (sanitized.length() > 200) {
            String extension = "";
            int lastDot = sanitized.lastIndexOf('.');
            if (lastDot > 0) {
                extension = sanitized.substring(lastDot);
                sanitized = sanitized.substring(0, 200 - extension.length()) + extension;
            } else {
                sanitized = sanitized.substring(0, 200);
            }
        }

        return sanitized;
    }

    /**
     * Upload file to specific folder path in SharePoint
     *
     * @param fileBytes File content as byte array
     * @param fileName File name
     * @param folderPath Relative folder path (e.g., "Candidate_123")
     * @return DriveItem with SharePoint details
     * @throws Exception if upload fails
     */
    public DriveItem uploadFileToFolder(byte[] fileBytes, String fileName, String folderPath)
            throws Exception {
        if (!initialized) {
            throw new IllegalStateException("SharePoint service not initialized");
        }

        // Sanitize filename
        String sanitizedFilename = sanitizeFilename(fileName);

        // Validate file size
        long maxSizeBytes = config.getMaxFileSizeMb() * 1024L * 1024L;
        if (fileBytes.length > maxSizeBytes) {
            throw new Exception("File size exceeds maximum: " + config.getMaxFileSizeMb() + " MB");
        }

        logger.info("Uploading file: {} to folder: {} (size: {} bytes)",
            sanitizedFilename, folderPath, fileBytes.length);

        try {
            // Build path: folderPath/fileName
            String fullPath = folderPath + "/" + sanitizedFilename;

            // Upload to SharePoint (MS Graph API creates folders automatically)
            DriveItem uploadedItem = graphClient
                .sites(config.getSiteId())
                .drives(config.getDriveId())
                .root()
                .itemWithPath(fullPath)  // Includes folder path
                .content()
                .buildRequest()
                .put(fileBytes);

            logger.info("File uploaded successfully. SharePoint ID: {}", uploadedItem.id);
            logger.info("Web URL: {}", uploadedItem.webUrl);

            return uploadedItem;

        } catch (Exception e) {
            logger.error("Error uploading file {} to SharePoint folder {}: {}",
                sanitizedFilename, folderPath, e.getMessage(), e);
            throw new Exception("Failed to upload file to SharePoint: " + e.getMessage(), e);
        }
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
