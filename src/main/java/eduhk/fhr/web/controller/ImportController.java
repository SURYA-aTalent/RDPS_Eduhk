package eduhk.fhr.web.controller;

// File Path: src/main/java/eduhk/fhr/web/controller/ImportController.java
// Purpose: Controller for TalentLink data import UI and operations

import java.security.Principal;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import eduhk.fhr.web.dao.ImportHistoryDAO;
import eduhk.fhr.web.dto.import_.ImportResultSummary;
import eduhk.fhr.web.model.ImportHistory;
import eduhk.fhr.web.service.ParameterService;
import eduhk.fhr.web.service.import_.ImportOrchestrationService;

/**
 * Import Controller
 *
 * Handles web UI for TalentLink data import:
 * - Display import dashboard with last import info
 * - Trigger manual import
 * - Display import results
 * - View import history
 */
@Controller
@RequestMapping(value = "/import")
public class ImportController extends BaseController {

    @Autowired
    private ImportOrchestrationService importOrchestrationService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private ImportHistoryDAO importHistoryDAO;

    @Autowired
    private eduhk.fhr.web.dao.ImportValidationErrorDAO validationErrorDAO;

    /**
     * Display import dashboard page
     *
     * GET /import/data
     */
    @RequestMapping(value = "/data", method = RequestMethod.GET)
    public ModelAndView importData(Principal principal, HttpServletRequest request) {
        logger.info("User {} accessing import dashboard", principal.getName());

        ModelAndView mv = new ModelAndView("importData");

        try {
            // Retrieve last import information from parameters
            String lastImportDate = parameterService.getParameterValue("LAST_IMPORT_TIMESTAMP");
            String lastCandidateId = parameterService.getParameterValue("LAST_IMPORTED_CANDIDATE_ID");
            String batchSize = parameterService.getParameterValue("IMPORT_BATCH_SIZE");

            // Add to model for display
            mv.addObject("lastImportDate", lastImportDate != null ? lastImportDate : "Never");
            mv.addObject("lastCandidateId", lastCandidateId != null ? lastCandidateId : "0");
            mv.addObject("batchSize", batchSize != null ? batchSize : "100");

            // Add TalentLink API configuration status
            String apiUrl = parameterService.getParameterValue("TALENTLINK_API_URL");
            boolean apiConfigured = apiUrl != null && !apiUrl.isEmpty();
            mv.addObject("apiConfigured", apiConfigured);

        } catch (Exception e) {
            logger.error("Error retrieving import dashboard data", e);
            mv.addObject("errorMessage", "Error loading import information: " + e.getMessage());
        }

        return mv;
    }

    /**
     * Trigger data import from TalentLink
     *
     * POST /import/trigger
     */
    @RequestMapping(value = "/trigger", method = RequestMethod.POST)
    @ResponseBody
    public ImportResultSummary triggerImport(Principal principal, HttpServletRequest request) {
        logger.info("Import triggered by user: {}", principal.getName());

        ImportResultSummary result = new ImportResultSummary();

        try {
            // TODO: Fix this authorization check
            //if (!hasRole(principal, "ROLE_ADMIN")) {
            //    logger.warn("User {} attempted to trigger import without admin privileges", principal.getName());
            //    result.setTotalCandidatesProcessed(0);
            //    result.setSuccessfulImports(0);
            //    result.setFailedImports(1);
            //
            //    eduhk.fhr.web.dto.import_.ImportErrorDetail error = new eduhk.fhr.web.dto.import_.ImportErrorDetail();
            //    error.setErrorType("PERMISSION_DENIED");
            //    error.setErrorMessage("Only administrators can trigger imports");
            //    error.setErrorTimestamp(new java.util.Date());
            //    result.addError(error);
            //
            //    return result;
            //}

            // Verify API is configured
            String apiUrl = parameterService.getParameterValue("TALENTLINK_API_URL");
            if (apiUrl == null || apiUrl.isEmpty()) {
                logger.error("TalentLink API URL not configured");
                result.setTotalCandidatesProcessed(0);
                result.setSuccessfulImports(0);
                result.setFailedImports(1);

                eduhk.fhr.web.dto.import_.ImportErrorDetail error = new eduhk.fhr.web.dto.import_.ImportErrorDetail();
                error.setErrorType("CONFIGURATION_ERROR");
                error.setErrorMessage("TalentLink API URL not configured in system parameters");
                error.setErrorTimestamp(new java.util.Date());
                result.addError(error);

                return result;
            }

            // Execute import
            logger.info("Starting import process for user: {}", principal.getName());
            result = importOrchestrationService.importNewCandidates();
            logger.info("Import completed. Total: {}, Success: {}, Failed: {}",
                result.getTotalCandidatesProcessed(),
                result.getSuccessfulImports(),
                result.getFailedImports());

            return result;

        } catch (Exception e) {
            logger.error("Unexpected error during import trigger", e);

            result.setTotalCandidatesProcessed(0);
            result.setSuccessfulImports(0);
            result.setFailedImports(1);
            result.setImportEndTime(new java.util.Date());

            eduhk.fhr.web.dto.import_.ImportErrorDetail error = new eduhk.fhr.web.dto.import_.ImportErrorDetail();
            error.setErrorType("SYSTEM_ERROR");
            error.setErrorMessage("Unexpected error: " + e.getMessage());
            error.setErrorTimestamp(new java.util.Date());
            result.addError(error);

            return result;
        }
    }

    /**
     * Display import history page
     *
     * GET /import/history
     */
    @RequestMapping(value = "/history", method = RequestMethod.GET)
    public ModelAndView importHistory(Principal principal) {
        logger.info("User {} accessing import history", principal.getName());

        ModelAndView mv = new ModelAndView("importHistory");

        try {
            // Retrieve recent import history from database
            List<ImportHistory> importHistory = importHistoryDAO.getRecentImports(50);
            mv.addObject("importHistory", importHistory);

            // Also add last import info from parameters for compatibility
            String lastImportDate = parameterService.getParameterValue("LAST_IMPORT_TIMESTAMP");
            String lastCandidateId = parameterService.getParameterValue("LAST_IMPORTED_CANDIDATE_ID");

            mv.addObject("lastImportDate", lastImportDate);
            mv.addObject("lastCandidateId", lastCandidateId);

        } catch (Exception e) {
            logger.error("Error retrieving import history", e);
            mv.addObject("errorMessage", "Error loading import history: " + e.getMessage());
            mv.addObject("importHistory", new java.util.ArrayList<>());
        }

        return mv;
    }

    /**
     * View validation errors for import
     *
     * GET /import/validation-errors
     *
     * Displays candidates that failed pre-import validation
     */
    @RequestMapping(value = "/validation-errors", method = RequestMethod.GET)
    public ModelAndView viewValidationErrors(Principal principal,
                                              @org.springframework.web.bind.annotation.RequestParam(value = "days", defaultValue = "7") int days,
                                              @org.springframework.web.bind.annotation.RequestParam(value = "limit", defaultValue = "100") int limit) {
        logger.info("User {} accessing validation errors (last {} days)", principal.getName(), days);

        ModelAndView mv = new ModelAndView("importValidationErrors");

        try {
            // Get recent validation errors
            List<eduhk.fhr.web.model.ImportValidationError> validationErrors =
                    validationErrorDAO.getRecentErrors(days, limit);

            // Get count of unresolved errors
            int unresolvedCount = validationErrorDAO.getUnresolvedErrorCount();

            mv.addObject("validationErrors", validationErrors);
            mv.addObject("unresolvedCount", unresolvedCount);
            mv.addObject("days", days);
            mv.addObject("limit", limit);

            logger.info("Found {} validation errors in last {} days", validationErrors.size(), days);

        } catch (Exception e) {
            logger.error("Error retrieving validation errors", e);
            mv.addObject("errorMessage", "Error loading validation errors: " + e.getMessage());
            mv.addObject("validationErrors", new java.util.ArrayList<>());
            mv.addObject("unresolvedCount", 0);
        }

        return mv;
    }

    /**
     * Get validation errors as JSON (for AJAX calls)
     *
     * GET /import/validation-errors/json
     */
    @RequestMapping(value = "/validation-errors/json", method = RequestMethod.GET)
    @ResponseBody
    public List<eduhk.fhr.web.model.ImportValidationError> getValidationErrorsJson(
            @org.springframework.web.bind.annotation.RequestParam(value = "days", defaultValue = "7") int days,
            @org.springframework.web.bind.annotation.RequestParam(value = "limit", defaultValue = "100") int limit) {
        try {
            return validationErrorDAO.getRecentErrors(days, limit);
        } catch (Exception e) {
            logger.error("Error retrieving validation errors JSON", e);
            return new java.util.ArrayList<>();
        }
    }

    /**
     * View detailed error log for import errors
     *
     * GET /import/errors
     *
     * NOTE: This queries recent import-related errors from RDPS_SYSTEM_LOG table
     * Filters by log_type = 'IMPORT_ERROR' or similar
     */
    @RequestMapping(value = "/errors", method = RequestMethod.GET)
    public ModelAndView viewImportErrors(Principal principal,
                                         @org.springframework.web.bind.annotation.RequestParam(value = "days", defaultValue = "7") int days) {
        logger.info("User {} accessing import errors (last {} days)", principal.getName(), days);

        ModelAndView mv = new ModelAndView("importErrors");

        try {
            // TODO: Query error logs from RDPS_SYSTEM_LOG table
            // Example query:
            // List<SystemLog> errors = logService.getImportErrors(days);
            // mv.addObject("importErrors", errors);

            mv.addObject("days", days);
            mv.addObject("message", "Error log query not yet implemented. " +
                "Requires LogService integration to query RDPS_SYSTEM_LOG table with log_type='IMPORT_ERROR'");

            // For now, show placeholder
            mv.addObject("importErrors", new java.util.ArrayList<>());

        } catch (Exception e) {
            logger.error("Error retrieving import error logs", e);
            mv.addObject("errorMessage", "Error loading import errors: " + e.getMessage());
        }

        return mv;
    }
}
