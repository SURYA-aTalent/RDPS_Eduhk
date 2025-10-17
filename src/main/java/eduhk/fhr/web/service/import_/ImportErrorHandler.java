package eduhk.fhr.web.service.import_;

// File Path: src/main/java/eduhk/fhr/web/service/import_/ImportErrorHandler.java
// Purpose: Centralized error handling and logging for import operations

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eduhk.fhr.web.dto.import_.ImportErrorDetail;
import eduhk.fhr.web.service.LogService;

/**
 * Import Error Handler
 *
 * Provides centralized error handling for import operations:
 * - Log errors to database (RDPS_SYSTEM_LOG)
 * - Format error messages
 * - Track error statistics
 */
@Component
public class ImportErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(ImportErrorHandler.class);

    @Autowired
    private LogService logService;

    /**
     * Handle and log an import error
     *
     * @param error Import error detail
     */
    public void handleError(ImportErrorDetail error) {
        // Format error message with all details
        String errorMessage = String.format(
            "[IMPORT_ERROR] Candidate: %s | Requisition: %s | Type: %s | Message: %s | Timestamp: %s",
            error.getCandidateId(),
            error.getRequisitionNumber() != null ? error.getRequisitionNumber() : "N/A",
            error.getErrorType(),
            error.getErrorMessage(),
            error.getErrorTimestamp()
        );

        // Log to console
        logger.error(errorMessage);

        // Persist to log file via LogService
        try {
            logService.writeErrorLog(errorMessage);
        } catch (Exception e) {
            logger.error("Failed to write error to log service", e);
        }
    }

    /**
     * Log an informational message about import progress
     *
     * @param message Info message
     */
    public void logInfo(String message) {
        String formattedMessage = "[IMPORT_INFO] " + message;

        // Log to console
        logger.info(formattedMessage);

        // Persist to log file via LogService
        try {
            logService.writeLog(formattedMessage);
        } catch (Exception e) {
            logger.error("Failed to write info to log service", e);
        }
    }

    /**
     * Log a warning message
     *
     * @param message Warning message
     */
    public void logWarning(String message) {
        String formattedMessage = "[IMPORT_WARNING] " + message;

        // Log to console
        logger.warn(formattedMessage);

        // Persist to log file via LogService
        try {
            logService.writeLog(formattedMessage);
        } catch (Exception e) {
            logger.error("Failed to write warning to log service", e);
        }
    }
}
