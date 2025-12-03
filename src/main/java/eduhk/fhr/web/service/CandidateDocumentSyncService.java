package eduhk.fhr.web.service;

// File Path: src/main/java/eduhk/fhr/web/service/CandidateDocumentSyncService.java
// Purpose: Service to sync candidate documents from TalentLink to SharePoint

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.microsoft.graph.models.DriveItem;

import eduhk.fhr.web.dao.CandidateDAO;
import eduhk.fhr.web.dao.CandidateDocumentDAO;
import eduhk.fhr.web.model.CandidateDocument;
import eduhk.fhr.web.soap.candidate.AttachedFileDto;
import eduhk.fhr.web.soap.candidate.DocumentBaseDto;

/**
 * CandidateDocumentSyncService
 *
 * Orchestrates the sync of candidate documents from TalentLink to SharePoint.
 * - Fetches documents from TalentLink SOAP API
 * - Uploads to SharePoint with folder organization
 * - Tracks sync status in database
 */
@Service
public class CandidateDocumentSyncService extends BaseService {

    @Autowired
    private TalentLinkSOAPCandidateService talentLinkService;

    @Autowired
    private SharePointUploadService sharePointService;

    @Autowired
    private CandidateDocumentDAO documentDAO;

    @Autowired
    private CandidateDAO candidateDAO;

    /**
     * Sync documents for a single candidate
     *
     * @param candidateId Candidate ID (String type, matches database VARCHAR2)
     * @return Number of documents synced
     */
    public int syncCandidateDocuments(String candidateId) {
        int syncedCount = 0;

        try {
            logger.info("Starting document sync for candidate {}", candidateId);

            // Convert candidateId to Long for TalentLink API
            Long tlCandidateId = Long.parseLong(candidateId);

            // 1. Get all documents for candidate from TalentLink
            List<DocumentBaseDto> documents = talentLinkService.getCandidateDocuments(tlCandidateId);

            if (documents.isEmpty()) {
                logger.info("No documents found for candidate {}", candidateId);
                return 0;
            }

            logger.info("Found {} documents for candidate {}", documents.size(), candidateId);

            // 2. Process each document
            for (DocumentBaseDto docMeta : documents) {
                try {
                    // Log document metadata for debugging
                    logger.info("Document {}: name='{}', type='{}', objectType='{}'",
                        docMeta.getId(), docMeta.getName(), docMeta.getDocumentType(), docMeta.getObjectType());

                    // 2a. Skip non-attachment documents (emails, notifications, forms, etc.)
                    // Only process ATTACHEDFILE type (actual uploaded files)
                    if (docMeta.getObjectType() == null ||
                        !docMeta.getObjectType().equalsIgnoreCase("ATTACHEDFILE")) {
                        logger.info("Document {} is not a file attachment (type: {}), skipping",
                            docMeta.getId(), docMeta.getObjectType());
                        continue;
                    }

                    // 2b. Check if already synced (duplicate detection)
                    if (documentDAO.isDocumentSynced(docMeta.getId())) {
                        logger.info("Document {} already synced, skipping", docMeta.getId());
                        continue;
                    }

                    // 2b. Download file from TalentLink
                    logger.info("Downloading document {} for candidate {}", docMeta.getId(), candidateId);
                    AttachedFileDto file;
                    try {
                        file = talentLinkService.downloadAttachedFile(docMeta.getId());
                    } catch (Exception downloadEx) {
                        // Document doesn't exist or is not a downloadable file (e.g., email template)
                        logger.warn("Document {} is not downloadable (likely email template/notification): {}",
                            docMeta.getId(), downloadEx.getMessage());
                        continue;
                    }

                    if (file == null || file.getBinaryData() == null) {
                        logger.warn("Document {} has no binary data, skipping", docMeta.getId());
                        continue;
                    }

                    // 2c. Convert DataHandler to byte array
                    byte[] fileBytes;
                    try {
                        fileBytes = file.getBinaryData().getInputStream().readAllBytes();
                    } catch (Exception readEx) {
                        // Error reading binary data from DataHandler
                        logger.warn("Document {} has invalid binary data: {}", docMeta.getId(), readEx.getMessage());
                        continue;
                    }

                    if (fileBytes.length == 0) {
                        logger.warn("Document {} is empty, skipping", docMeta.getId());
                        continue;
                    }

                    // 2d. Upload to SharePoint in candidate folder
                    String folderPath = "Candidate_" + candidateId;
                    logger.info("Uploading document {} to SharePoint folder {}", file.getFileName(), folderPath);

                    DriveItem driveItem = sharePointService.uploadFileToFolder(
                        fileBytes,
                        file.getFileName(),
                        folderPath
                    );

                    // 2e. Record successful upload
                    CandidateDocument doc = new CandidateDocument();
                    doc.setCandidateId(candidateId);
                    doc.setTalentLinkDocId(docMeta.getId());
                    doc.setFileName(file.getFileName());
                    doc.setFileSize(file.getSize());
                    doc.setDocType(docMeta.getDocumentType());
                    doc.setSharePointFileId(driveItem.id);
                    doc.setSharePointWebUrl(driveItem.webUrl);
                    doc.setSharePointFolderPath(folderPath);
                    doc.setSyncStatus("SYNCED");
                    doc.setCreatedBy("SYSTEM");

                    documentDAO.insert(doc);
                    syncedCount++;

                    logger.info("Successfully synced document {} for candidate {} ({})",
                        docMeta.getId(), candidateId, file.getFileName());

                } catch (Exception e) {
                    // Record failed upload
                    logger.error("Failed to sync document {} for candidate {}: {}",
                        docMeta.getId(), candidateId, e.getMessage(), e);

                    try {
                        CandidateDocument doc = new CandidateDocument();
                        doc.setCandidateId(candidateId);
                        doc.setTalentLinkDocId(docMeta.getId());
                        doc.setFileName(docMeta.getName());
                        doc.setDocType(docMeta.getDocumentType());
                        doc.setSyncStatus("FAILED");
                        doc.setErrorMessage(e.getMessage());
                        doc.setCreatedBy("SYSTEM");

                        documentDAO.insert(doc);
                    } catch (Exception dbEx) {
                        logger.error("Failed to record error in database: {}", dbEx.getMessage(), dbEx);
                    }
                }
            }

            logger.info("Document sync completed for candidate {}. Synced: {}/{}",
                candidateId, syncedCount, documents.size());

        } catch (Exception e) {
            logger.error("Error syncing documents for candidate {}: {}",
                candidateId, e.getMessage(), e);
        }

        return syncedCount;
    }

    /**
     * Sync documents for all candidates in database
     *
     * @return Statistics map with counts
     */
    public Map<String, Integer> syncAllCandidateDocuments() {
        Map<String, Integer> stats = new HashMap<>();
        int totalCandidates = 0;
        int totalSynced = 0;
        int totalFailed = 0;
        int candidatesProcessed = 0;

        try {
            logger.info("=== Starting bulk document sync ===");

            // Get all candidates from database
            List<String> candidateIds = candidateDAO.getAllCandidateIds();
            totalCandidates = candidateIds.size();

            logger.info("Found {} candidates to process", totalCandidates);

            for (String candidateId : candidateIds) {
                try {
                    int synced = syncCandidateDocuments(candidateId);
                    totalSynced += synced;
                    candidatesProcessed++;

                    // Log progress every 10 candidates
                    if (candidatesProcessed % 10 == 0) {
                        logger.info("Progress: {}/{} candidates processed, {} documents synced",
                            candidatesProcessed, totalCandidates, totalSynced);
                    }

                } catch (Exception e) {
                    totalFailed++;
                    logger.error("Failed to process candidate {}: {}",
                        candidateId, e.getMessage(), e);
                }
            }

            logger.info("=== Bulk document sync completed ===");
            logger.info("Total candidates: {}, Candidates processed: {}, Documents synced: {}, Failed candidates: {}",
                totalCandidates, candidatesProcessed, totalSynced, totalFailed);

        } catch (Exception e) {
            logger.error("Error in bulk document sync: {}", e.getMessage(), e);
        }

        stats.put("totalCandidates", totalCandidates);
        stats.put("candidatesProcessed", candidatesProcessed);
        stats.put("totalSynced", totalSynced);
        stats.put("totalFailed", totalFailed);
        return stats;
    }

    /**
     * Sync documents for specific list of candidates
     *
     * @param candidateIds List of candidate IDs (String type)
     * @return Statistics map with counts
     */
    public Map<String, Integer> syncCandidateDocuments(List<String> candidateIds) {
        Map<String, Integer> stats = new HashMap<>();
        int totalSynced = 0;
        int candidatesProcessed = 0;
        int totalFailed = 0;

        logger.info("Starting document sync for {} candidates", candidateIds.size());

        for (String candidateId : candidateIds) {
            try {
                int synced = syncCandidateDocuments(candidateId);
                totalSynced += synced;
                candidatesProcessed++;
            } catch (Exception e) {
                totalFailed++;
                logger.error("Failed to process candidate {}: {}", candidateId, e.getMessage(), e);
            }
        }

        stats.put("candidatesProcessed", candidatesProcessed);
        stats.put("totalSynced", totalSynced);
        stats.put("totalFailed", totalFailed);
        return stats;
    }
}
