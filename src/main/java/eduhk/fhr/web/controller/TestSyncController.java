package eduhk.fhr.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import eduhk.fhr.web.dto.import_.ImportErrorDetail;
import eduhk.fhr.web.dto.talentlink.TalentLinkCandidateDTO;
import eduhk.fhr.web.service.TalentLinkSOAPCandidateService;
import eduhk.fhr.web.service.import_.CandidateImportService;
import eduhk.fhr.web.soap.candidate.Profile;
import eduhk.fhr.web.soap.candidate.StructuredDocument;
import eduhk.fhr.web.soap.candidate.GfDocument;
import eduhk.fhr.web.soap.candidate.Answer;
import eduhk.fhr.web.soap.candidate.AssignedQuestion;
import eduhk.fhr.web.soap.candidate.DocumentBaseDto;
import eduhk.fhr.web.soap.candidate.AttachedFileDto;
import eduhk.fhr.web.soap.candidate.ApplicationDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test Sync Controller
 *
 * Manual trigger endpoints for testing data sync from TalentLink to RDPS.
 * This controller is used for testing and debugging before enabling scheduled automation.
 *
 * Endpoints:
 * - GET /api/test-sync/trigger - Batch sync with optional parameters
 * - GET /api/test-sync/candidate/{candidateId} - Sync specific candidate
 * - GET /api/test-sync/stats - Get sync statistics
 */
@RestController
@RequestMapping("/api/test-sync")
public class TestSyncController {

    private static final Logger logger = LoggerFactory.getLogger(TestSyncController.class);

    @Autowired
    private TalentLinkSOAPCandidateService soapCandidateService;

    @Autowired
    private CandidateImportService candidateImportService;

    @Autowired
    private eduhk.fhr.web.service.TalentLinkSOAPDocumentService documentService;

    @Autowired
    private eduhk.fhr.web.service.EducationWorkDocumentParser documentParser;

    /**
     * Manual trigger endpoint for testing data sync
     *
     * GET /api/test-sync/trigger
     * Optional params:
     *   - batchSize: Number of candidates to sync (default: 10)
     *   - page: Page number (default: 0)
     *
     * Example: http://localhost:8080/api/test-sync/trigger?batchSize=5
     */
    @GetMapping("/trigger")
    public ResponseEntity<Map<String, Object>> triggerSync(
            @RequestParam(defaultValue = "10") int batchSize,
            @RequestParam(defaultValue = "0") int page) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Generate batch ID for tracking
            String batchId = "TEST_BATCH_" + new java.text.SimpleDateFormat("yyyyMMdd_HHmmss")
                    .format(new java.util.Date());

            // Fetch candidates from TalentLink
            List<Profile> profiles = soapCandidateService.getCandidates(page, batchSize);

            response.put("candidatesFetched", profiles != null ? profiles.size() : 0);
            response.put("page", page);
            response.put("batchSize", batchSize);
            response.put("batchId", batchId);

            if (profiles == null || profiles.isEmpty()) {
                response.put("status", "completed");
                response.put("message", "No candidates found");
                response.put("successCount", 0);
                response.put("failedCount", 0);
                return ResponseEntity.ok(response);
            }

            // Import each candidate
            int successCount = 0;
            int failedCount = 0;

            for (Profile profile : profiles) {
                try {
                    // Convert to DTO
                    TalentLinkCandidateDTO dto = soapCandidateService.convertProfileToDTO(profile);

                    if (dto == null || dto.getId() == null) {
                        failedCount++;
                        response.put("error_profile_" + (profile.getId() != null ? profile.getId() : "unknown"),
                                "Invalid profile or missing ID");
                        continue;
                    }

                    // Import candidate
                    ImportErrorDetail error = candidateImportService.importCandidate(dto, batchId);

                    if (error == null) {
                        successCount++;
                    } else {
                        failedCount++;
                        response.put("error_" + dto.getId(), error.getErrorMessage());
                    }

                } catch (Exception e) {
                    failedCount++;
                    String candidateId = profile.getId() != null ? String.valueOf(profile.getId()) : "unknown";
                    response.put("error_" + candidateId, e.getMessage());
                }
            }

            response.put("successCount", successCount);
            response.put("failedCount", failedCount);
            response.put("status", "completed");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "failed");
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Sync specific candidate by ID
     *
     * GET /api/test-sync/candidate/{candidateId}
     *
     * Example: http://localhost:8080/api/test-sync/candidate/12345
     */
    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<Map<String, Object>> syncCandidateById(@PathVariable String candidateId) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Generate batch ID for tracking
            String batchId = "TEST_SINGLE_" + new java.text.SimpleDateFormat("yyyyMMdd_HHmmss")
                    .format(new java.util.Date());

            // Fetch specific candidate
            Profile profile = soapCandidateService.getCandidateById(Long.parseLong(candidateId));

            if (profile == null) {
                response.put("candidateId", candidateId);
                response.put("status", "failed");
                response.put("error", "Candidate not found in TalentLink");
                return ResponseEntity.status(404).body(response);
            }

            // Convert to DTO
            TalentLinkCandidateDTO dto = soapCandidateService.convertProfileToDTO(profile);

            if (dto == null || dto.getId() == null) {
                response.put("candidateId", candidateId);
                response.put("status", "failed");
                response.put("error", "Invalid profile or missing ID");
                return ResponseEntity.status(400).body(response);
            }

            // Import candidate
            ImportErrorDetail error = candidateImportService.importCandidate(dto, batchId);

            if (error == null) {
                response.put("candidateId", candidateId);
                response.put("status", "success");
                response.put("message", "Candidate synced successfully");
                response.put("batchId", batchId);
                return ResponseEntity.ok(response);
            } else {
                response.put("candidateId", candidateId);
                response.put("status", "failed");
                response.put("error", error.getErrorMessage());
                response.put("errorType", error.getErrorType());
                return ResponseEntity.status(400).body(response);
            }

        } catch (NumberFormatException e) {
            response.put("candidateId", candidateId);
            response.put("status", "failed");
            response.put("error", "Invalid candidate ID format");
            return ResponseEntity.status(400).body(response);

        } catch (Exception e) {
            response.put("candidateId", candidateId);
            response.put("status", "failed");
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get sync statistics
     *
     * GET /api/test-sync/stats
     *
     * Returns count of records in each table
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSyncStats() {

        Map<String, Object> stats = new HashMap<>();

        try {
            // Note: This is a placeholder
            // TODO: Add DAO methods to count records in each table
            // For now, returning static message

            stats.put("message", "Statistics endpoint - To be implemented");
            stats.put("note", "Add DAO methods to count records from:");
            stats.put("tables", new String[]{
                    "RDPS_CANDIDATE",
                    "RDPS_EDU_PROF_QUAL",
                    "RDPS_WORK_EXPERIENCE",
                    "RDPS_REFEREE",
                    "RDPS_OTHER_INFO",
                    "RDPS_OFFER"
            });

            // TODO: Implement actual counts
            // stats.put("totalCandidates", candidateDAO.count());
            // stats.put("totalEducations", educationDAO.count());
            // etc.

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            stats.put("status", "failed");
            stats.put("error", e.getMessage());
            return ResponseEntity.status(500).body(stats);
        }
    }

    /**
     * DEBUG: Inspect Profile structure from TalentLink
     *
     * GET /api/test-sync/debug/profile/{candidateId}
     *
     * Returns complete Profile object structure to understand where education/work data is
     * Example: http://localhost:8080/api/test-sync/debug/profile/116
     */
    @GetMapping("/debug/profile/{candidateId}")
    public ResponseEntity<Map<String, Object>> debugProfile(@PathVariable String candidateId) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Fetch specific candidate
            Profile profile = soapCandidateService.getCandidateById(Long.parseLong(candidateId));

            if (profile == null) {
                response.put("candidateId", candidateId);
                response.put("error", "Candidate not found in TalentLink");
                return ResponseEntity.status(404).body(response);
            }

            response.put("candidateId", candidateId);
            response.put("profileFound", true);

            // Basic info
            Map<String, Object> basicInfo = new HashMap<>();
            basicInfo.put("id", profile.getId());
            basicInfo.put("firstname", profile.getFirstname());
            basicInfo.put("lastname", profile.getLastname());
            basicInfo.put("email", profile.getEmail());
            response.put("basicInfo", basicInfo);

            // Personal data
            if (profile.getPersonalData() != null) {
                Map<String, Object> personalData = new HashMap<>();
                personalData.put("dateOfBirth", profile.getPersonalData().getDateOfBirth());
                personalData.put("sex", profile.getPersonalData().getSex());
                response.put("personalData", personalData);
            }

            // Address
            if (profile.getAddress() != null) {
                Map<String, Object> address = new HashMap<>();
                address.put("address1", profile.getAddress().getAddress1());
                address.put("address2", profile.getAddress().getAddress2());
                address.put("city", profile.getAddress().getCity());
                address.put("mobilePhone", profile.getAddress().getMobilePhone());
                response.put("address", address);
            }

            // Position
            if (profile.getPosition() != null) {
                Map<String, Object> position = new HashMap<>();
                position.put("position", profile.getPosition().getPosition());
                response.put("position", position);
            }

            // Reference (referral info, NOT education/work)
            if (profile.getReference() != null) {
                Map<String, Object> reference = new HashMap<>();
                reference.put("company", profile.getReference().getCompany());
                reference.put("email", profile.getReference().getEmail());
                reference.put("employeeCode", profile.getReference().getEmployeeCode());
                reference.put("referredFrom", profile.getReference().getReferredFrom());
                response.put("reference", reference);
            }

            // Check other Profile fields that might contain education/work data
            response.put("hasAcademicTitle", profile.getAcademicTitle() != null);
            response.put("hasFormOfAddress", profile.getFormOfAddress() != null);
            response.put("hasMemo", profile.getMemo() != null);
            response.put("hasTags", profile.getTags() != null);

            // Important note
            response.put("IMPORTANT_NOTE",
                    "Profile does NOT contain education/work experience collections. " +
                    "These might be in StructuredDocument or require separate API calls.");

            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            response.put("candidateId", candidateId);
            response.put("error", "Invalid candidate ID format");
            return ResponseEntity.status(400).body(response);

        } catch (Exception e) {
            response.put("candidateId", candidateId);
            response.put("error", e.getMessage());
            response.put("stackTrace", e.getStackTrace()[0].toString());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * DEBUG: Inspect StructuredDocument from TalentLink
     *
     * GET /api/test-sync/debug/structured-doc/{candidateId}
     *
     * Returns StructuredDocument which might contain education/work as form data
     * Example: http://localhost:8080/api/test-sync/debug/structured-doc/116
     */
    @GetMapping("/debug/structured-doc/{candidateId}")
    public ResponseEntity<Map<String, Object>> debugStructuredDocument(@PathVariable String candidateId) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Call getStructuredDocumentById
            StructuredDocument doc = soapCandidateService.getStructuredDocumentById(Long.parseLong(candidateId));

            if (doc == null) {
                response.put("candidateId", candidateId);
                response.put("structuredDocumentFound", false);
                response.put("note", "StructuredDocument is null - might not be available for this candidate");
                return ResponseEntity.ok(response);
            }

            response.put("candidateId", candidateId);
            response.put("structuredDocumentFound", true);

            // Document details
            response.put("documentId", doc.getId());
            response.put("readOnly", doc.isReadOnly());
            response.put("formCreationDate", doc.getFormCreationDate());
            response.put("lastModified", doc.getLastModified());

            // Check if it has answers (GfDocument)
            if (doc.getAnswers() != null) {
                GfDocument answers = doc.getAnswers();
                response.put("hasAnswers", true);
                response.put("answersNote", "GfDocument contains form answers - inspect to see if education/work is here");

                // Try to get some info from GfDocument
                // (GfDocument structure would need to be explored)
                Map<String, Object> answersInfo = new HashMap<>();
                // Note: You'll need to explore GfDocument class to see what methods are available
                answersInfo.put("class", answers.getClass().getName());
                response.put("answersInfo", answersInfo);
            } else {
                response.put("hasAnswers", false);
            }

            response.put("FINDING",
                    "StructuredDocument exists but structure needs exploration. " +
                    "Education/work data might be stored as form questions/answers in GfDocument.");

            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            response.put("candidateId", candidateId);
            response.put("error", "Invalid candidate ID format");
            return ResponseEntity.status(400).body(response);

        } catch (Exception e) {
            response.put("candidateId", candidateId);
            response.put("error", e.getMessage());
            response.put("stackTrace", e.getStackTrace()[0].toString());
            response.put("note", "API method might not be implemented in TalentLinkSOAPCandidateService");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * DEBUG: Dump all form answers from StructuredDocument
     *
     * GET /api/test-sync/debug/answers/{candidateId}
     *
     * Shows all questions and answers - this reveals where education/work data is
     * Example: http://localhost:8080/api/test-sync/debug/answers/116
     */
    @GetMapping("/debug/answers/{candidateId}")
    public ResponseEntity<Map<String, Object>> debugAnswers(@PathVariable String candidateId) {

        Map<String, Object> response = new HashMap<>();

        try {
            StructuredDocument doc = soapCandidateService.getStructuredDocumentById(Long.parseLong(candidateId));

            if (doc == null || doc.getAnswers() == null) {
                response.put("candidateId", candidateId);
                response.put("error", "No StructuredDocument or answers found");
                return ResponseEntity.status(404).body(response);
            }

            response.put("candidateId", candidateId);
            response.put("documentId", doc.getId());

            GfDocument gfDoc = doc.getAnswers();
            List<Answer> answers = gfDoc.getAnswer();

            response.put("totalAnswers", answers.size());

            // Parse each answer
            List<Map<String, Object>> answerList = new ArrayList<>();
            for (Answer answer : answers) {
                Map<String, Object> answerInfo = new HashMap<>();

                answerInfo.put("answerId", answer.getId());
                answerInfo.put("order", answer.getOrder());
                answerInfo.put("isCandidateAnswer", answer.isIsCandidateAnswer());

                // Get the question
                if (answer.getAssignedQuestion() != null) {
                    AssignedQuestion question = answer.getAssignedQuestion();
                    Map<String, Object> questionInfo = new HashMap<>();
                    questionInfo.put("questionId", question.getId());
                    questionInfo.put("questionIdRef", question.getQuestionId());
                    questionInfo.put("text", question.getUnlocalValue());
                    questionInfo.put("type", question.getType() != null ? question.getType().toString() : null);
                    answerInfo.put("question", questionInfo);
                }

                // Get the answer value
                if (answer.getFreeText() != null) {
                    answerInfo.put("answerType", "freeText");
                    answerInfo.put("value", answer.getFreeText());
                }
                if (answer.getDateAnswer() != null) {
                    answerInfo.put("answerType", "date");
                    answerInfo.put("value", answer.getDateAnswer().toString());
                }
                if (answer.getSelectedOptions() != null && !answer.getSelectedOptions().isEmpty()) {
                    answerInfo.put("answerType", "selectedOptions");
                    List<String> options = new ArrayList<>();
                    for (var option : answer.getSelectedOptions()) {
                        if (option.getAssignedOption() != null) {
                            options.add(option.getAssignedOption().getUnlocalValue());
                        }
                    }
                    answerInfo.put("value", options);
                }

                // Check for children (nested/repeating answers)
                if (answer.getChildren() != null && !answer.getChildren().isEmpty()) {
                    answerInfo.put("hasChildren", true);
                    answerInfo.put("childrenCount", answer.getChildren().size());

                    List<Map<String, Object>> childrenInfo = new ArrayList<>();
                    for (Answer child : answer.getChildren()) {
                        Map<String, Object> childInfo = new HashMap<>();
                        childInfo.put("answerId", child.getId());

                        if (child.getAssignedQuestion() != null) {
                            childInfo.put("questionId", child.getAssignedQuestion().getId());
                            childInfo.put("questionText", child.getAssignedQuestion().getUnlocalValue());
                        }

                        if (child.getFreeText() != null) {
                            childInfo.put("value", child.getFreeText());
                        } else if (child.getDateAnswer() != null) {
                            childInfo.put("value", child.getDateAnswer().toString());
                        }

                        childrenInfo.add(childInfo);
                    }
                    answerInfo.put("children", childrenInfo);
                }

                answerList.add(answerInfo);
            }

            response.put("answers", answerList);

            response.put("FINDING",
                    "Look for questions with codes/labels like 'Education', 'WorkExperience', 'Referee'. " +
                    "These might have children arrays containing the actual records.");

            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            response.put("candidateId", candidateId);
            response.put("error", "Invalid candidate ID format");
            return ResponseEntity.status(400).body(response);

        } catch (Exception e) {
            response.put("candidateId", candidateId);
            response.put("error", e.getMessage());
            response.put("stackTrace", e.getStackTrace()[0].toString());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * DEBUG: List all documents for a candidate
     *
     * GET /api/test-sync/debug/documents/{candidateId}
     *
     * Shows all documents attached to candidate - find "EdUHK Education/Work Experience"
     * Example: http://localhost:8080/api/test-sync/debug/documents/116
     */
    @GetMapping("/debug/documents/{candidateId}")
    public ResponseEntity<Map<String, Object>> debugDocuments(@PathVariable String candidateId) {

        Map<String, Object> response = new HashMap<>();

        try {
            List<DocumentBaseDto> documents = soapCandidateService.getCandidateDocuments(Long.parseLong(candidateId));

            response.put("candidateId", candidateId);
            response.put("totalDocuments", documents != null ? documents.size() : 0);

            if (documents == null || documents.isEmpty()) {
                response.put("message", "No documents found for this candidate");
                return ResponseEntity.ok(response);
            }

            List<Map<String, Object>> docList = new ArrayList<>();
            for (DocumentBaseDto doc : documents) {
                Map<String, Object> docInfo = new HashMap<>();
                docInfo.put("documentId", doc.getId());
                docInfo.put("documentName", doc.getName());
                docInfo.put("documentType", doc.getDocumentType());
                docInfo.put("objectType", doc.getObjectType());
                docInfo.put("description", doc.getDescription());
                docInfo.put("author", doc.getAuthor());
                docInfo.put("creationDate", doc.getCreationDate());
                docInfo.put("lastUpdate", doc.getLastUpdateDate());

                // Check if this is the Education/Work Experience document
                if (doc.getName() != null &&
                    doc.getName().contains("Education") &&
                    doc.getName().contains("Work")) {
                    docInfo.put("IS_EDUCATION_WORK_DOC", true);
                }

                docList.add(docInfo);
            }

            response.put("documents", docList);
            response.put("NEXT_STEP",
                    "Find document with name containing 'Education/Work Experience' and use its ID to get StructuredDocument");

            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            response.put("candidateId", candidateId);
            response.put("error", "Invalid candidate ID format");
            return ResponseEntity.status(400).body(response);

        } catch (Exception e) {
            response.put("candidateId", candidateId);
            response.put("error", e.getMessage());
            response.put("stackTrace", e.getStackTrace()[0].toString());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * DEBUG: Try to get Education/Work document (ID 445) answers
     *
     * GET /api/test-sync/debug/edu-work-answers/{candidateId}
     *
     * Attempts to retrieve the Education/Work Experience document
     * Example: http://localhost:8080/api/test-sync/debug/edu-work-answers/116
     */
    @GetMapping("/debug/edu-work-answers/{candidateId}")
    public ResponseEntity<Map<String, Object>> debugEduWorkAnswers(@PathVariable String candidateId) {

        Map<String, Object> response = new HashMap<>();

        try {
            // First, find the Education/Work document ID
            List<DocumentBaseDto> documents = soapCandidateService.getCandidateDocuments(Long.parseLong(candidateId));

            Long eduWorkDocId = null;
            for (DocumentBaseDto doc : documents) {
                if (doc.getName() != null &&
                    doc.getName().contains("Education") &&
                    doc.getName().contains("Work")) {
                    eduWorkDocId = doc.getId();
                    break;
                }
            }

            if (eduWorkDocId == null) {
                response.put("error", "Education/Work Experience document not found");
                return ResponseEntity.status(404).body(response);
            }

            response.put("foundDocumentId", eduWorkDocId);

            // Try downloading it as an attached file
            try {
                AttachedFileDto file = soapCandidateService.downloadAttachedFile(eduWorkDocId);
                response.put("downloadMethod", "AttachedFile");
                response.put("fileInfo", Map.of(
                    "fileName", file.getFileName() != null ? file.getFileName() : "null"
                ));

                // If it's a structured document, this won't work well
                // But let's see what we get
                response.put("note", "This document might not be downloadable as a file - it's a STRUCTURED form");

            } catch (Exception e) {
                response.put("downloadMethod", "AttachedFile FAILED");
                response.put("downloadError", e.getMessage());
                response.put("note", "As expected - STRUCTURED documents can't be downloaded as files");
            }

            // The real solution: We need to find a way to get this specific structured document
            // Maybe there's an application-specific method or we need to iterate through forms
            response.put("SOLUTION_NEEDED",
                    "TalentLink API doesn't have a direct method to get StructuredDocument by documentId. " +
                    "We may need to: 1) Check if data is tied to an application, 2) Use a different API call, " +
                    "or 3) Parse all documents for the candidate");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("stackTrace", e.getStackTrace()[0].toString());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * DEBUG: Get applications and their documents
     *
     * GET /api/test-sync/debug/applications/{candidateId}
     *
     * Gets applications for candidate - Education/Work doc might be tied to application!
     * Example: http://localhost:8080/api/test-sync/debug/applications/116
     */
    @GetMapping("/debug/applications/{candidateId}")
    public ResponseEntity<Map<String, Object>> debugApplications(@PathVariable String candidateId) {

        Map<String, Object> response = new HashMap<>();

        try {
            List<ApplicationDto> applications = soapCandidateService.getApplicationsByCandidateId(Long.parseLong(candidateId));

            response.put("candidateId", candidateId);
            response.put("totalApplications", applications != null ? applications.size() : 0);

            if (applications == null || applications.isEmpty()) {
                response.put("message", "No applications found for this candidate");
                return ResponseEntity.ok(response);
            }

            List<Map<String, Object>> appList = new ArrayList<>();
            for (ApplicationDto app : applications) {
                Map<String, Object> appInfo = new HashMap<>();
                appInfo.put("applicationId", app.getId());
                appInfo.put("positionId", app.getPositionId());
                appInfo.put("status", app.getStatus());
                appInfo.put("applicationDate", app.getApplicationDate());

                // Check if application has documents
                if (app.getDocuments() != null && app.getDocuments().getDocument() != null) {
                    List<Map<String, Object>> docList = new ArrayList<>();
                    for (var doc : app.getDocuments().getDocument()) {
                        Map<String, Object> docInfo = new HashMap<>();
                        docInfo.put("id", doc.getId());
                        docInfo.put("name", doc.getName());
                        docInfo.put("type", doc.getDocumentType());
                        docInfo.put("objectType", doc.getObjectType());

                        // Check if this is the Education/Work document
                        if (doc.getName() != null &&
                            doc.getName().contains("Education") &&
                            doc.getName().contains("Work")) {
                            docInfo.put("IS_EDUCATION_WORK_DOC", true);
                        }

                        docList.add(docInfo);
                    }
                    appInfo.put("documents", docList);
                    appInfo.put("documentCount", docList.size());
                }

                appList.add(appInfo);
            }

            response.put("applications", appList);
            response.put("FINDING",
                    "If Education/Work doc is here, we can access it through the application! " +
                    "This is the breakthrough we needed.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("stackTrace", e.getStackTrace()[0].toString());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * DEBUG: Test Document Service - Get structured document by ID and parse it
     *
     * GET /api/test-sync/debug/document/{documentId}
     *
     * Tests the new DocumentService to retrieve and parse structured documents
     * Example: http://localhost:8080/api/test-sync/debug/document/445
     */
    @GetMapping("/debug/document/{documentId}")
    public ResponseEntity<Map<String, Object>> debugDocumentById(@PathVariable Long documentId) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Retrieve the structured document
            eduhk.fhr.web.soap.document.StructuredDocument doc =
                documentService.getStructuredDocumentByDocumentId(documentId);

            if (doc == null) {
                response.put("documentId", documentId);
                response.put("error", "Document not found or not accessible");
                return ResponseEntity.status(404).body(response);
            }

            response.put("documentId", doc.getId());
            response.put("name", doc.getName());
            response.put("candidateId", doc.getCandidateId());
            response.put("formCreationDate", doc.getFormCreationDate());
            response.put("lastModified", doc.getLastModified());

            // Parse the document to extract education/work/referee data
            try {
                eduhk.fhr.web.service.ParsedCandidateData parsedData =
                    documentParser.parseEducationWorkDocument(doc);

                response.put("parsingSuccess", true);

                // Education
                if (parsedData.getEducation() != null && !parsedData.getEducation().isEmpty()) {
                    List<Map<String, Object>> educationList = new ArrayList<>();
                    for (var edu : parsedData.getEducation()) {
                        Map<String, Object> eduInfo = new HashMap<>();
                        eduInfo.put("institution", edu.getInstitution());
                        eduInfo.put("country", edu.getCountry());
                        eduInfo.put("educationLevel", edu.getEducationLevel());
                        eduInfo.put("qualification", edu.getQualificationAwardDesc());
                        eduInfo.put("major", edu.getMajorStudyArea());
                        eduInfo.put("dateOfAward", edu.getDateOfAward());
                        educationList.add(eduInfo);
                    }
                    response.put("education", educationList);
                    response.put("educationCount", educationList.size());
                }

                // Work Experience
                if (parsedData.getWorkExperience() != null && !parsedData.getWorkExperience().isEmpty()) {
                    List<Map<String, Object>> workList = new ArrayList<>();
                    for (var work : parsedData.getWorkExperience()) {
                        Map<String, Object> workInfo = new HashMap<>();
                        workInfo.put("employer", work.getEmployerName());
                        workInfo.put("position", work.getPositionTitle());
                        workInfo.put("startDate", work.getStartDate());
                        workInfo.put("endDate", work.getEndDate());
                        workInfo.put("natureOfBusiness", work.getNatureOfBusiness());
                        workList.add(workInfo);
                    }
                    response.put("workExperience", workList);
                    response.put("workExperienceCount", workList.size());
                }

                // Referees
                if (parsedData.getReferees() != null && !parsedData.getReferees().isEmpty()) {
                    List<Map<String, Object>> refereeList = new ArrayList<>();
                    for (var referee : parsedData.getReferees()) {
                        Map<String, Object> refInfo = new HashMap<>();
                        refInfo.put("name", referee.getFirstname() + " " + referee.getLastname());
                        refInfo.put("email", referee.getEmail());
                        refInfo.put("phone", referee.getPhoneNumber());
                        refInfo.put("position", referee.getPositionTitle());
                        refereeList.add(refInfo);
                    }
                    response.put("referees", refereeList);
                    response.put("refereesCount", refereeList.size());
                }

                // Other Info
                if (parsedData.getOtherInfo() != null) {
                    Map<String, Object> otherInfo = new HashMap<>();
                    otherInfo.put("expectedSalary", parsedData.getOtherInfo().getExpectedSalary());
                    otherInfo.put("salary", parsedData.getOtherInfo().getSalary());
                    otherInfo.put("skills", parsedData.getOtherInfo().getSkills());
                    otherInfo.put("noticePeriod", parsedData.getOtherInfo().getNoticePeriod());
                    response.put("otherInfo", otherInfo);
                }

                response.put("SUCCESS",
                    "Document retrieved and parsed successfully! Education/work/referee data extracted.");

            } catch (Exception e) {
                response.put("parsingSuccess", false);
                response.put("parsingError", e.getMessage());
            }

            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            response.put("documentId", documentId);
            response.put("error", "Invalid document ID format");
            return ResponseEntity.status(400).body(response);

        } catch (Exception e) {
            response.put("documentId", documentId);
            response.put("error", e.getMessage());
            response.put("stackTrace", e.getStackTrace().length > 0 ? e.getStackTrace()[0].toString() : "No stack trace");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * DEBUG: Get parsed education/work data for a candidate
     *
     * GET /api/test-sync/debug/parsed-data/{candidateId}
     *
     * Shows exactly what data is parsed from the Education/Work document
     * Example: http://localhost:8080/api/test-sync/debug/parsed-data/116
     */
    @GetMapping("/debug/parsed-data/{candidateId}")
    public ResponseEntity<Map<String, Object>> getParseData(@PathVariable String candidateId) {
        Map<String, Object> response = new HashMap<>();

        try {
            response.put("candidateId", candidateId);

            // Step 1: Get applications for candidate
            List<ApplicationDto> applications = soapCandidateService.getApplicationsByCandidateId(Long.parseLong(candidateId));

            if (applications == null || applications.isEmpty()) {
                response.put("error", "No applications found for candidate");
                return ResponseEntity.status(404).body(response);
            }

            response.put("applicationsFound", applications.size());

            // Step 2: Get documents for each application
            Long eduWorkDocId = null;
            Long applicationId = null;

            for (ApplicationDto app : applications) {
                List<Object> documents = documentService.getDocumentsByApplicationId(app.getId());

                if (documents == null || documents.isEmpty()) {
                    continue;
                }

                response.put("documentsFound", documents.size());

                // Step 3: Find Education/Work document
                for (Object docObj : documents) {
                    if (docObj instanceof org.w3c.dom.Element) {
                        org.w3c.dom.Element element = (org.w3c.dom.Element) docObj;
                        org.w3c.dom.NodeList children = element.getChildNodes();

                        String objectType = null;
                        String documentId = null;
                        String documentName = null;

                        for (int i = 0; i < children.getLength(); i++) {
                            org.w3c.dom.Node node = children.item(i);
                            if ("objectType".equals(node.getNodeName())) {
                                objectType = node.getTextContent();
                            } else if ("id".equals(node.getNodeName())) {
                                documentId = node.getTextContent();
                            } else if ("name".equals(node.getNodeName())) {
                                documentName = node.getTextContent();
                            }
                        }

                        if ("STRUCTURED".equals(objectType)
                            && documentName != null
                            && (documentName.contains("Education") || documentName.contains("Work Experience"))) {
                            eduWorkDocId = Long.parseLong(documentId);
                            applicationId = app.getId();
                            response.put("eduWorkDocumentId", eduWorkDocId);
                            response.put("eduWorkDocumentName", documentName);
                            break;
                        }
                    }
                }

                if (eduWorkDocId != null) {
                    break;
                }
            }

            if (eduWorkDocId == null) {
                response.put("error", "Education/Work document not found");
                return ResponseEntity.status(404).body(response);
            }

            // Step 4: Retrieve structured document
            eduhk.fhr.web.soap.document.StructuredDocument eduWorkDoc =
                documentService.getStructuredDocumentByDocumentId(eduWorkDocId);

            if (eduWorkDoc == null) {
                response.put("error", "Failed to retrieve structured document");
                return ResponseEntity.status(500).body(response);
            }

            response.put("documentRetrieved", true);

            // Step 5: Parse the document
            eduhk.fhr.web.service.ParsedCandidateData parsedData =
                documentParser.parseEducationWorkDocument(eduWorkDoc);

            response.put("parsingSuccess", true);

            // Education details
            if (parsedData.getEducation() != null && !parsedData.getEducation().isEmpty()) {
                List<Map<String, Object>> educationList = new ArrayList<>();
                for (var edu : parsedData.getEducation()) {
                    Map<String, Object> eduInfo = new HashMap<>();
                    eduInfo.put("institution", edu.getInstitution());
                    eduInfo.put("country", edu.getCountry());
                    eduInfo.put("educationLevel", edu.getEducationLevel());
                    eduInfo.put("studyMode", edu.getStudyMode());
                    eduInfo.put("qualificationAwardDesc", edu.getQualificationAwardDesc());
                    eduInfo.put("qualificationAwardClass", edu.getQualificationAwardClass());
                    eduInfo.put("others", edu.getOthers());
                    eduInfo.put("majorStudyArea", edu.getMajorStudyArea());
                    eduInfo.put("startDate", edu.getStartDate());
                    eduInfo.put("dateOfAward", edu.getDateOfAward());
                    educationList.add(eduInfo);
                }
                response.put("education", educationList);
                response.put("educationCount", educationList.size());
            } else {
                response.put("education", new ArrayList<>());
                response.put("educationCount", 0);
            }

            // Work Experience details
            if (parsedData.getWorkExperience() != null && !parsedData.getWorkExperience().isEmpty()) {
                List<Map<String, Object>> workList = new ArrayList<>();
                for (var work : parsedData.getWorkExperience()) {
                    Map<String, Object> workInfo = new HashMap<>();
                    workInfo.put("employerName", work.getEmployerName());
                    workInfo.put("positionTitle", work.getPositionTitle());
                    workInfo.put("natureOfBusiness", work.getNatureOfBusiness());
                    workInfo.put("startDate", work.getStartDate());
                    workInfo.put("endDate", work.getEndDate());
                    workList.add(workInfo);
                }
                response.put("workExperience", workList);
                response.put("workExperienceCount", workList.size());
            } else {
                response.put("workExperience", new ArrayList<>());
                response.put("workExperienceCount", 0);
            }

            // Referee details
            if (parsedData.getReferees() != null && !parsedData.getReferees().isEmpty()) {
                List<Map<String, Object>> refereeList = new ArrayList<>();
                for (var referee : parsedData.getReferees()) {
                    Map<String, Object> refInfo = new HashMap<>();
                    refInfo.put("firstname", referee.getFirstname());
                    refInfo.put("lastname", referee.getLastname());
                    refInfo.put("email", referee.getEmail());
                    refInfo.put("phoneNumber", referee.getPhoneNumber());
                    refInfo.put("positionTitle", referee.getPositionTitle());
                    refInfo.put("relationship", referee.getRelationship());
                    refereeList.add(refInfo);
                }
                response.put("referees", refereeList);
                response.put("refereesCount", refereeList.size());
            } else {
                response.put("referees", new ArrayList<>());
                response.put("refereesCount", 0);
            }

            // Other Info details
            if (parsedData.getOtherInfo() != null) {
                Map<String, Object> otherInfo = new HashMap<>();
                otherInfo.put("expectedSalary", parsedData.getOtherInfo().getExpectedSalary());
                otherInfo.put("salary", parsedData.getOtherInfo().getSalary());
                otherInfo.put("skills", parsedData.getOtherInfo().getSkills());
                otherInfo.put("noticePeriod", parsedData.getOtherInfo().getNoticePeriod());
                response.put("otherInfo", otherInfo);
            } else {
                response.put("otherInfo", null);
            }

            response.put("STATUS", "SUCCESS - This shows exactly what fields are extracted from the document");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("candidateId", candidateId);
            response.put("error", e.getMessage());
            response.put("stackTrace", e.getStackTrace().length > 0 ? e.getStackTrace()[0].toString() : "No stack trace");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Debug endpoint to view raw document structure
     * Shows all questions and answers from the structured document BEFORE parsing
     *
     * URL: GET /api/test-sync/debug/raw-document/{documentId}
     */
    @GetMapping("/debug/raw-document/{documentId}")
    public ResponseEntity<Map<String, Object>> getRawDocument(@PathVariable Long documentId) {
        Map<String, Object> response = new HashMap<>();

        try {
            logger.info("=== DEBUG: Fetching raw document structure for document ID: {} ===", documentId);

            // Step 1: Retrieve the full structured document with answers
            eduhk.fhr.web.soap.document.StructuredDocument eduWorkDoc =
                documentService.getStructuredDocumentByDocumentId(documentId);

            if (eduWorkDoc == null) {
                response.put("error", "Document not found");
                return ResponseEntity.status(404).body(response);
            }

            response.put("documentId", eduWorkDoc.getId());
            response.put("documentName", eduWorkDoc.getName());

            // Step 2: Get the GfDocument with all answers
            eduhk.fhr.web.soap.document.GfDocument gfDoc = eduWorkDoc.getAnswers();

            if (gfDoc == null || gfDoc.getAnswer() == null) {
                response.put("error", "Document has no answers");
                return ResponseEntity.ok(response);
            }

            // Step 3: Process all top-level answers
            List<Map<String, Object>> topLevelAnswers = new ArrayList<>();
            java.util.List<eduhk.fhr.web.soap.document.Answer> answers = gfDoc.getAnswer();

            logger.info("Processing {} top-level answers", answers.size());

            for (eduhk.fhr.web.soap.document.Answer answer : answers) {
                Map<String, Object> answerMap = processAnswer(answer, 0);
                topLevelAnswers.add(answerMap);
            }

            response.put("topLevelAnswerCount", topLevelAnswers.size());
            response.put("answers", topLevelAnswers);
            response.put("STATUS", "SUCCESS - This shows the raw document structure with all questions and answer values");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching raw document", e);
            response.put("documentId", documentId);
            response.put("error", e.getMessage());
            response.put("stackTrace", e.getStackTrace().length > 0 ? e.getStackTrace()[0].toString() : "No stack trace");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Recursively process an Answer object to extract all data
     */
    private Map<String, Object> processAnswer(eduhk.fhr.web.soap.document.Answer answer, int depth) {
        Map<String, Object> result = new HashMap<>();

        if (answer == null) {
            return result;
        }

        // Get question text
        String questionText = "";
        if (answer.getAssignedQuestion() != null) {
            questionText = answer.getAssignedQuestion().getUnlocalValue();
            if (questionText == null) {
                questionText = "";
            }
        }
        result.put("questionText", questionText);

        // Get answer value (free text)
        String freeText = answer.getFreeText();
        result.put("freeText", freeText != null ? freeText : "");

        // Get selected options
        if (answer.getSelectedOptions() != null && !answer.getSelectedOptions().isEmpty()) {
            List<String> selectedOptions = new ArrayList<>();
            for (eduhk.fhr.web.soap.document.SelectedOption option : answer.getSelectedOptions()) {
                if (option.getAssignedOption() != null) {
                    String optionValue = option.getAssignedOption().getUnlocalValue();
                    if (optionValue != null && !optionValue.trim().isEmpty()) {
                        selectedOptions.add(optionValue);
                    }
                }
            }
            result.put("selectedOptions", selectedOptions);
        } else {
            result.put("selectedOptions", new ArrayList<String>());
        }

        // Get date answer
        if (answer.getDateAnswer() != null) {
            result.put("dateAnswer", answer.getDateAnswer().toString());
        } else {
            result.put("dateAnswer", null);
        }

        // Process children recursively
        if (answer.getChildren() != null && !answer.getChildren().isEmpty()) {
            List<Map<String, Object>> children = new ArrayList<>();
            for (eduhk.fhr.web.soap.document.Answer child : answer.getChildren()) {
                children.add(processAnswer(child, depth + 1));
            }
            result.put("children", children);
            result.put("childrenCount", children.size());
        } else {
            result.put("children", new ArrayList<Map<String, Object>>());
            result.put("childrenCount", 0);
        }

        result.put("depth", depth);

        return result;
    }
}
