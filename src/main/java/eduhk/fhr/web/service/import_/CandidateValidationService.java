package eduhk.fhr.web.service.import_;

// File Path: src/main/java/eduhk/fhr/web/service/import_/CandidateValidationService.java
// Purpose: Service for validating candidate data before import

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eduhk.fhr.web.dao.ImportValidationErrorDAO;
import eduhk.fhr.web.model.Candidate;
import eduhk.fhr.web.model.ImportValidationError;

/**
 * Candidate Validation Service
 *
 * Validates candidate data before database insertion.
 * Records validation errors in RDPS_IMPORT_VALIDATION_ERROR table.
 */
@Service
public class CandidateValidationService {

    private static final Logger logger = LoggerFactory.getLogger(CandidateValidationService.class);

    @Autowired
    private ImportValidationErrorDAO validationErrorDAO;

    /**
     * Validate candidate data and record errors
     *
     * @param candidate Candidate model to validate
     * @param batchId Import batch ID for tracking
     * @return List of validation errors (empty if valid)
     */
    public List<ImportValidationError> validateCandidate(Candidate candidate, String batchId) {
        List<ImportValidationError> errors = new ArrayList<>();

        if (candidate == null) {
            ImportValidationError error = createError(null, null, "VALIDATION",
                    null, "Candidate object is null", batchId);
            errors.add(error);
            return errors;
        }

        // Validate CANDIDATE_ID (required)
        if (candidate.getCandidateId() == null || candidate.getCandidateId().trim().isEmpty()) {
            errors.add(createError(candidate.getCandidateId(), candidate.getReqNumber(),
                    "REQUIRED_FIELD", "CANDIDATE_ID",
                    "Candidate ID is required and cannot be null or empty", batchId));
        }

        // Validate REQ_NUMBER (required)
        if (candidate.getReqNumber() == null || candidate.getReqNumber().trim().isEmpty()) {
            errors.add(createError(candidate.getCandidateId(), candidate.getReqNumber(),
                    "REQUIRED_FIELD", "REQ_NUMBER",
                    "Requisition Number is required and cannot be null or empty", batchId));
        }

        // Validate DISTRICT (required) - THIS IS THE KEY VALIDATION TO FIX THE ISSUE
        if (candidate.getDistrict() == null) {
            errors.add(createError(candidate.getCandidateId(), candidate.getReqNumber(),
                    "REQUIRED_FIELD", "DISTRICT",
                    "District is required and cannot be null. Please provide a valid district value.", batchId));
        }

        // Validate CREATED_BY (required - has default but should be checked)
        if (candidate.getCreatedBy() == null || candidate.getCreatedBy().trim().isEmpty()) {
            // Set default if missing
            candidate.setCreatedBy("SYSTEM");
        }

        // Validate USERSTAMP (required - has default but should be checked)
        if (candidate.getUserstamp() == null || candidate.getUserstamp().trim().isEmpty()) {
            // Set default if missing
            candidate.setUserstamp("SYSTEM");
        }

        // Optional: Validate field lengths
        if (candidate.getCandidateId() != null && candidate.getCandidateId().length() > 20) {
            errors.add(createError(candidate.getCandidateId(), candidate.getReqNumber(),
                    "FIELD_LENGTH", "CANDIDATE_ID",
                    "Candidate ID exceeds maximum length of 20 characters", batchId));
        }

        if (candidate.getReqNumber() != null && candidate.getReqNumber().length() > 20) {
            errors.add(createError(candidate.getCandidateId(), candidate.getReqNumber(),
                    "FIELD_LENGTH", "REQ_NUMBER",
                    "Requisition Number exceeds maximum length of 20 characters", batchId));
        }

        if (candidate.getEmail() != null && candidate.getEmail().length() > 50) {
            errors.add(createError(candidate.getCandidateId(), candidate.getReqNumber(),
                    "FIELD_LENGTH", "EMAIL",
                    "Email exceeds maximum length of 50 characters", batchId));
        }

        if (candidate.getFirstName() != null && candidate.getFirstName().length() > 32) {
            errors.add(createError(candidate.getCandidateId(), candidate.getReqNumber(),
                    "FIELD_LENGTH", "FIRST_NAME",
                    "First Name exceeds maximum length of 32 characters", batchId));
        }

        if (candidate.getLastName() != null && candidate.getLastName().length() > 32) {
            errors.add(createError(candidate.getCandidateId(), candidate.getReqNumber(),
                    "FIELD_LENGTH", "LAST_NAME",
                    "Last Name exceeds maximum length of 32 characters", batchId));
        }

        // Optional: Validate email format
        if (candidate.getEmail() != null && !candidate.getEmail().isEmpty()) {
            if (!isValidEmail(candidate.getEmail())) {
                errors.add(createError(candidate.getCandidateId(), candidate.getReqNumber(),
                        "INVALID_FORMAT", "EMAIL",
                        "Email format is invalid: " + candidate.getEmail(), batchId));
            }
        }

        // Log and persist errors
        if (!errors.isEmpty()) {
            logger.warn("Validation failed for candidate {}: {} error(s) found",
                    candidate.getCandidateId(), errors.size());

            // Persist errors to database
            for (ImportValidationError error : errors) {
                try {
                    validationErrorDAO.insertValidationError(error);
                } catch (Exception e) {
                    logger.error("Failed to persist validation error for candidate {}: {}",
                            candidate.getCandidateId(), e.getMessage());
                }
            }
        }

        return errors;
    }

    /**
     * Create a validation error object
     */
    private ImportValidationError createError(String candidateId, String reqNumber,
                                               String errorType, String fieldName,
                                               String errorMessage, String batchId) {
        ImportValidationError error = new ImportValidationError();
        error.setCandidateId(candidateId != null ? candidateId : "UNKNOWN");
        error.setReqNumber(reqNumber != null ? reqNumber : "UNKNOWN");
        error.setErrorType(errorType);
        error.setFieldName(fieldName);
        error.setErrorMessage(errorMessage);
        error.setImportBatchId(batchId);
        error.setCreatedBy("SYSTEM");
        return error;
    }

    /**
     * Simple email validation
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        // Simple email pattern validation
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    /**
     * Get validation errors summary for batch
     *
     * @param batchId Import batch ID
     * @return List of validation errors for the batch
     */
    public List<ImportValidationError> getValidationErrorsForBatch(String batchId) {
        return validationErrorDAO.getErrorsByBatchId(batchId);
    }

    /**
     * Get unresolved validation errors
     *
     * @param limit Maximum number of records to return
     * @return List of unresolved validation errors
     */
    public List<ImportValidationError> getUnresolvedErrors(int limit) {
        return validationErrorDAO.getUnresolvedErrors(limit);
    }
}
