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

            // Get credentials from parameter service
            String username = parameterService.getParameterValue("TALENTLINK_USERNAME");
            String password = parameterService.getParameterValue("TALENTLINK_PASSWORD");
            String apiKey = parameterService.getParameterValue("TALENTLINK_API_KEY");

            // Set credentials in SOAP handler
            TalentLinkSOAPHandler.setCredentials(username, password);

            // Create SOAP service
            CandidateWebService_Service service = new CandidateWebService_Service();
            candidateService = service.getCandidateWebServicePort();

            // Add SOAP handler programmatically
            BindingProvider bindingProvider = (BindingProvider) candidateService;
            java.util.List<jakarta.xml.ws.handler.Handler> handlerChain = new java.util.ArrayList<>();
            handlerChain.add(new TalentLinkSOAPHandler());
            bindingProvider.getBinding().setHandlerChain(handlerChain);

            // Set endpoint with API key
            String endpoint = getCandidateSoapUrl() + "?api_key=" + apiKey;
            bindingProvider.getRequestContext().put(
                BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                endpoint
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
     * Check if service is initialized
     *
     * @return true if initialized
     */
    public boolean isInitialized() {
        return initialized;
    }
}
