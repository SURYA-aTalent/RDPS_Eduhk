package eduhk.fhr.web.service.import_;

// File Path: src/main/java/eduhk/fhr/web/service/import_/ImportOrchestrationService.java
// Purpose: Main orchestrator for the import process from TalentLink to RDPS

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eduhk.fhr.web.dao.ImportHistoryDAO;
import eduhk.fhr.web.dto.import_.ImportErrorDetail;
import eduhk.fhr.web.dto.import_.ImportResultSummary;
import eduhk.fhr.web.dto.talentlink.TalentLinkCandidateDTO;
import eduhk.fhr.web.model.ImportHistory;
import eduhk.fhr.web.service.ParameterService;
import eduhk.fhr.web.service.TalentLinkSOAPCandidateService;
import eduhk.fhr.web.soap.candidate.Profile;

/**
 * Import Orchestration Service
 *
 * Coordinates the entire import process:
 * 1. Retrieve last imported candidate ID
 * 2. Fetch new candidates from TalentLink API
 * 3. Import candidates and related entities
 * 4. Handle errors and logging
 * 5. Send notifications
 * 6. Update last import timestamp
 */
@Service
public class ImportOrchestrationService {

    private static final Logger logger = LoggerFactory.getLogger(ImportOrchestrationService.class);

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private CandidateImportService candidateImportService;

    @Autowired
    private ImportNotificationService importNotificationService;

    @Autowired
    private ImportErrorHandler importErrorHandler;

    @Autowired
    private TalentLinkSOAPCandidateService soapCandidateService;

    @Autowired
    private ImportHistoryDAO importHistoryDAO;

    /**
     * Execute the complete import workflow
     *
     * @return Import result summary
     */
    public ImportResultSummary importNewCandidates() {
        logger.info("[importNewCandidates] Starting import process");

        ImportResultSummary summary = new ImportResultSummary();
        summary.setImportStartTime(new Date());

        // Generate batch ID for tracking validation errors
        String batchId = "BATCH_" + new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        try {
            // Step 1: Get last imported candidate ID
            String lastImportedId = getLastImportedCandidateId();
            logger.info("Last imported candidate ID: {}", lastImportedId);

            // Step 2: Get batch size from configuration
            int batchSize = getBatchSize();
            logger.info("Batch size: {}", batchSize);

            // Step 3: Fetch candidates from TalentLink SOAP API
            // Using page 0 for now - can be enhanced to support pagination
            java.util.List<Profile> profiles = soapCandidateService.getCandidates(0, batchSize);
            logger.info("Received {} candidate profiles from SOAP API", profiles.size());

            if (profiles == null || profiles.isEmpty()) {
                logger.warn("No candidates found in SOAP response");
                summary.setTotalCandidatesProcessed(0);
                summary.setSuccessfulImports(0);
                summary.setFailedImports(0);
                summary.setImportEndTime(new Date());
                return summary;
            }

            // Step 4: Process each candidate
            int totalCount = 0;
            int successCount = 0;
            int errorCount = 0;
            String lastProcessedId = lastImportedId;

            for (Profile profile : profiles) {
                totalCount++;

                try {
                    // Convert SOAP Profile to DTO
                    TalentLinkCandidateDTO candidateDTO = soapCandidateService.convertProfileToDTO(profile);

                    if (candidateDTO == null || candidateDTO.getId() == null) {
                        logger.warn("Skipping candidate with null profile or ID");
                        continue;
                    }

                    logger.info("Processing candidate {}/{}: ID={}",
                        totalCount, profiles.size(), candidateDTO.getId());

                    // Import candidate with batch ID for validation tracking
                    ImportErrorDetail error = candidateImportService.importCandidate(candidateDTO, batchId);

                    if (error == null) {
                        // Success
                        successCount++;
                        lastProcessedId = candidateDTO.getId();
                        logger.info("Successfully imported candidate: {}", candidateDTO.getId());
                    } else {
                        // Error occurred but continuing
                        errorCount++;
                        summary.addError(error);
                        importErrorHandler.handleError(error);
                        logger.warn("Failed to import candidate {}: {}",
                            candidateDTO.getId(), error.getErrorMessage());
                    }

                } catch (Exception e) {
                    // Unexpected error - continue with next candidate
                    errorCount++;
                    logger.error("Unexpected error processing candidate at index {}: {}",
                        totalCount, e.getMessage(), e);

                    ImportErrorDetail error = new ImportErrorDetail();
                    error.setCandidateId(profile.getId() != null ? String.valueOf(profile.getId()) : "Unknown");
                    error.setErrorType("PROCESSING_ERROR");
                    error.setErrorMessage(e.getMessage());
                    error.setErrorTimestamp(new Date());
                    summary.addError(error);
                    importErrorHandler.handleError(error);
                }
            }

            // Step 5: Update last imported candidate ID
            if (successCount > 0) {
                updateLastImportedCandidateId(lastProcessedId);
                logger.info("Updated last imported candidate ID to: {}", lastProcessedId);
            }

            // Step 6: Set summary statistics
            summary.setTotalCandidatesProcessed(totalCount);
            summary.setSuccessfulImports(successCount);
            summary.setFailedImports(errorCount);

            logger.info("Import summary: Total={}, Success={}, Failed={}",
                totalCount, successCount, errorCount);

        } catch (Exception e) {
            logger.error("[importNewCandidates] Unexpected error during import: {}", e.getMessage(), e);
            importErrorHandler.logInfo("Import failed due to unexpected error: " + e.getMessage());
        } finally {
            // Always set end time
            summary.setImportEndTime(new Date());

            // Calculate duration
            long duration = summary.getImportEndTime().getTime() -
                summary.getImportStartTime().getTime();
            // summary.setDurationMillis(duration);

            // Step 9: Send email notification
            try {
                importNotificationService.sendImportNotification(summary);
            } catch (Exception e) {
                logger.error("Error sending import notification: {}", e.getMessage(), e);
            }

            // Step 10: Save import history
            try {
                saveImportHistory(summary);
            } catch (Exception e) {
                logger.error("Error saving import history: {}", e.getMessage(), e);
            }
        }

        logger.info("[importNewCandidates] Import process completed");
        return summary;
    }

    /**
     * Save import history to database
     *
     * @param summary Import result summary
     */
    private void saveImportHistory(ImportResultSummary summary) {
        ImportHistory history = new ImportHistory();
        history.setImportDate(summary.getImportStartTime());
        history.setTriggeredBy("SYSTEM");  // TODO: Pass actual username from controller
        history.setTotalCandidates(summary.getTotalCandidatesProcessed());
        history.setSuccessfulImports(summary.getSuccessfulImports());
        history.setFailedImports(summary.getFailedImports());
        // history.setDurationMs(summary.getDurationMillis());
        // history.setLastCandidateId(summary.getLastImportedCandidateId());

        // Determine status
        String status;
        if (summary.getFailedImports() == 0) {
            status = "COMPLETED";
        } else if (summary.getSuccessfulImports() == 0) {
            status = "FAILED";
        } else {
            status = "PARTIAL";
        }
        history.setStatus(status);

        // Set error message if complete failure
        if (summary.getSuccessfulImports() == 0 && summary.getErrors() != null && !summary.getErrors().isEmpty()) {
            history.setErrorMessage(summary.getErrors().get(0).getErrorMessage());
        }

        history.setCreatedBy("SYSTEM");
        history.setUserstamp("SYSTEM");

        importHistoryDAO.insertImportHistory(history);
        logger.info("Import history saved: Status={}, Total={}, Success={}, Failed={}",
            status, summary.getTotalCandidatesProcessed(),
            summary.getSuccessfulImports(), summary.getFailedImports());
    }

    /**
     * Get the last successfully imported candidate ID
     *
     * @return Candidate ID string, or "0" if first import
     */
    private String getLastImportedCandidateId() {
        String lastId = parameterService.getParameterValue("LAST_IMPORTED_CANDIDATE_ID");
        if (lastId == null || lastId.isEmpty()) {
            return "0";
        }
        return lastId;
    }

    /**
     * Update the last imported candidate ID after successful import
     *
     * @param candidateId The last successfully imported candidate ID
     */
    private void updateLastImportedCandidateId(String candidateId) {
        // Update last imported candidate ID
        parameterService.updateParameter("LAST_IMPORTED_CANDIDATE_ID", candidateId);

        // Update last import timestamp
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        parameterService.updateParameter("LAST_IMPORT_TIMESTAMP", timestamp);

        logger.info("Updated last imported candidate ID to: {}", candidateId);
    }

    /**
     * Get batch size from configuration
     *
     * @return Batch size
     */
    private int getBatchSize() {
        String value = parameterService.getParameterValue("IMPORT_BATCH_SIZE");
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid IMPORT_BATCH_SIZE value: {}", value);
            }
        }
        return 100; // Default
    }
}
