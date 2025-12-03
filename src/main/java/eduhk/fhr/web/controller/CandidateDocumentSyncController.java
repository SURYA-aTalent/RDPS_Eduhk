package eduhk.fhr.web.controller;

import eduhk.fhr.web.model.AjaxResponse;
import eduhk.fhr.web.service.CandidateDocumentSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for Candidate Document Sync operations
 * Handles manual triggering of document sync from TalentLink to SharePoint
 */
@Controller
@RequestMapping("/document-sync")
@PreAuthorize("hasRole('USE_RDPS')")
public class CandidateDocumentSyncController extends BaseController {

    @Autowired
    private CandidateDocumentSyncService syncService;

    /**
     * Show document sync page
     *
     * @param model Model
     * @return Template name
     */
    @GetMapping("/")
    public String syncPage(Model model) {
        return "document-sync/index";
    }

    /**
     * Sync documents for a single candidate (AJAX endpoint)
     *
     * @param candidateId Candidate ID (String type to match database)
     * @return AjaxResponse with sync result
     */
    @PostMapping("/ajax/sync-candidate")
    @ResponseBody
    public AjaxResponse syncCandidate(@RequestParam("candidateId") String candidateId) {
        AjaxResponse response = new AjaxResponse();

        try {
            logger.info("Manually triggering document sync for candidate {}", candidateId);

            int syncedCount = syncService.syncCandidateDocuments(candidateId);

            response.success("Document sync completed for candidate " + candidateId);
            response.setResult(Map.of(
                "candidateId", candidateId,
                "documentsSynced", syncedCount
            ));

            logger.info("Document sync completed for candidate {}. Synced: {} documents",
                candidateId, syncedCount);

        } catch (Exception e) {
            logger.error("Error syncing documents for candidate {}: {}", candidateId, e.getMessage(), e);
            response.fail("Sync failed: " + e.getMessage());
        }

        return response;
    }

    /**
     * Sync documents for all candidates (AJAX endpoint)
     *
     * @return AjaxResponse with sync statistics
     */
    @PostMapping("/ajax/sync-all")
    @ResponseBody
    public AjaxResponse syncAll() {
        AjaxResponse response = new AjaxResponse();

        try {
            logger.info("Manually triggering document sync for ALL candidates");

            Map<String, Integer> stats = syncService.syncAllCandidateDocuments();

            response.success("Bulk document sync completed");
            response.setResult(stats);

            logger.info("Bulk document sync completed. Total: {}, Processed: {}, Synced: {}, Failed: {}",
                stats.get("totalCandidates"), stats.get("candidatesProcessed"),
                stats.get("totalSynced"), stats.get("totalFailed"));

        } catch (Exception e) {
            logger.error("Error in bulk document sync: {}", e.getMessage(), e);
            response.fail("Bulk sync failed: " + e.getMessage());
        }

        return response;
    }

    /**
     * Get sync statistics (AJAX endpoint)
     *
     * @return AjaxResponse with statistics
     */
    @GetMapping("/ajax/statistics")
    @ResponseBody
    public AjaxResponse getStatistics() {
        AjaxResponse response = new AjaxResponse();

        try {
            // You can add more detailed statistics here from the DAO
            response.success("Statistics retrieved successfully");
            response.setResult(Map.of(
                "message", "Statistics functionality can be implemented as needed"
            ));

        } catch (Exception e) {
            logger.error("Error retrieving statistics: {}", e.getMessage(), e);
            response.fail("Failed to retrieve statistics: " + e.getMessage());
        }

        return response;
    }
}
