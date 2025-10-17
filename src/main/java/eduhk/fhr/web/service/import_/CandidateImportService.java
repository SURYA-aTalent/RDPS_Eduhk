package eduhk.fhr.web.service.import_;

// File Path: src/main/java/eduhk/fhr/web/service/import_/CandidateImportService.java
// Purpose: Service for importing candidate data and orchestrating related entity imports

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eduhk.fhr.web.dto.import_.ImportErrorDetail;
import eduhk.fhr.web.dto.talentlink.TalentLinkCandidateDTO;
import eduhk.fhr.web.mapper.CandidateMapper;
import eduhk.fhr.web.model.Candidate;

/**
 * Candidate Import Service
 *
 * Handles importing individual candidate records:
 * - Map DTO to model
 * - Validate data
 * - Insert/update candidate in database
 * - Coordinate import of related entities (work exp, education, etc.)
 */
@Service
public class CandidateImportService {

    private static final Logger logger = LoggerFactory.getLogger(CandidateImportService.class);

    @Autowired
    private CandidateMapper candidateMapper;

    @Autowired
    private WorkExperienceImportService workExperienceImportService;

    @Autowired
    private EducationImportService educationImportService;

    @Autowired
    private RefereeImportService refereeImportService;

    @Autowired
    private OtherInfoImportService otherInfoImportService;

    @Autowired
    private eduhk.fhr.web.dao.CandidateDAO candidateDAO;

    @Autowired
    private CandidateValidationService candidateValidationService;

    /**
     * Import a single candidate and all related entities
     *
     * @param candidateDTO TalentLink candidate DTO
     * @param batchId Import batch ID for tracking validation errors
     * @return ImportErrorDetail if error occurs, null if successful
     */
    @Transactional
    public ImportErrorDetail importCandidate(TalentLinkCandidateDTO candidateDTO, String batchId) {
        try {
            logger.info("Importing candidate: {}", candidateDTO.getId());

            // Map DTO to model
            Candidate candidate = candidateMapper.mapToModel(candidateDTO);

            // Validate basic fields
            if (!candidateMapper.validate(candidate)) {
                return createError(candidateDTO.getId(), candidateDTO.getRequisitionNumber(),
                    "VALIDATION", "Candidate basic validation failed");
            }

            // Perform comprehensive validation and record errors
            java.util.List<eduhk.fhr.web.model.ImportValidationError> validationErrors =
                    candidateValidationService.validateCandidate(candidate, batchId);

            if (!validationErrors.isEmpty()) {
                // Validation failed - return first error
                eduhk.fhr.web.model.ImportValidationError firstError = validationErrors.get(0);
                String errorMsg = String.format("Validation failed: %d error(s). First error: %s - %s",
                        validationErrors.size(),
                        firstError.getFieldName() != null ? firstError.getFieldName() : "General",
                        firstError.getErrorMessage());

                logger.warn("Candidate {} failed validation: {}", candidateDTO.getId(), errorMsg);
                return createError(candidateDTO.getId(), candidateDTO.getRequisitionNumber(),
                        "VALIDATION_ERROR", errorMsg);
            }

            // Insert/update candidate in database
            int rows = candidateDAO.upsertCandidate(candidate);
            if (rows == 0) {
                logger.warn("No rows affected for candidate: {}", candidateDTO.getId());
            }

            // Import related entities
            String candidateId = candidateDTO.getId();
            String reqNumber = candidateDTO.getRequisitionNumber();

            // Import work experience
            //if (candidateDTO.getWorkExperience() != null && !candidateDTO.getWorkExperience().isEmpty()) {
            //    workExperienceImportService.importWorkExperience(candidateDTO, candidateId, reqNumber);
            //}

            // Import education
            //if (candidateDTO.getEducation() != null && !candidateDTO.getEducation().isEmpty()) {
            //    educationImportService.importEducation(candidateDTO, candidateId, reqNumber);
            //}

            // Import referees
            //if (candidateDTO.getReferees() != null && !candidateDTO.getReferees().isEmpty()) {
            //    refereeImportService.importReferees(candidateDTO, candidateId, reqNumber);
            //}

            // Import other info
            //if (candidateDTO.getOtherInfo() != null) {
            //    otherInfoImportService.importOtherInfo(candidateDTO, candidateId, reqNumber);
            //}

            logger.info("Successfully imported candidate: {}", candidateDTO.getId());
            return null; // Success

        } catch (Exception e) {
            logger.error("Error importing candidate: {}", candidateDTO.getId(), e);
            return createError(candidateDTO.getId(),
                candidateDTO.getRequisitionNumber(), "EXCEPTION", e.getMessage());
        }
    }

    /**
     * Create an error detail object
     *
     * @param candidateId Candidate ID
     * @param reqNumber Requisition number
     * @param errorType Error type
     * @param errorMessage Error message
     * @return ImportErrorDetail
     */
    private ImportErrorDetail createError(String candidateId, String reqNumber,
            String errorType, String errorMessage) {
        ImportErrorDetail error = new ImportErrorDetail();
        error.setCandidateId(candidateId);
        error.setRequisitionNumber(reqNumber);
        error.setErrorType(errorType);
        error.setErrorMessage(errorMessage);
        error.setErrorTimestamp(new java.util.Date());
        return error;
    }
}
