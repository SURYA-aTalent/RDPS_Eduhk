package eduhk.fhr.web.dto.import_;

// File Path: src/main/java/eduhk/fhr/web/dto/import_/ImportResultSummary.java
// Purpose: DTO for import operation results summary

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Import Result Summary
 *
 * Contains summary information about a completed import operation.
 * Used to display results to users and send email notifications.
 */
public class ImportResultSummary {

    private Date importStartTime;
    private Date importEndTime;
    private long durationMillis;
    private int totalCandidatesProcessed;
    private int successfulImports;
    private int failedImports;
    private String lastImportedCandidateId;
    private List<ImportErrorDetail> errors;

    public ImportResultSummary() {
        this.errors = new ArrayList<>();
    }

    // Getters and Setters
    // TODO: Add getters and setters for all fields

    public Date getImportStartTime() {
        return importStartTime;
    }

    public void setImportStartTime(Date importStartTime) {
        this.importStartTime = importStartTime;
    }

    public Date getImportEndTime() {
        return importEndTime;
    }

    public void setImportEndTime(Date importEndTime) {
        this.importEndTime = importEndTime;
    }

    public int getTotalCandidatesProcessed() {
        return totalCandidatesProcessed;
    }

    public void setTotalCandidatesProcessed(int totalCandidatesProcessed) {
        this.totalCandidatesProcessed = totalCandidatesProcessed;
    }

    public int getSuccessfulImports() {
        return successfulImports;
    }

    public void setSuccessfulImports(int successfulImports) {
        this.successfulImports = successfulImports;
    }

    public int getFailedImports() {
        return failedImports;
    }

    public void setFailedImports(int failedImports) {
        this.failedImports = failedImports;
    }

    public List<ImportErrorDetail> getErrors() {
        return errors;
    }

    public void setErrors(List<ImportErrorDetail> errors) {
        this.errors = errors;
    }

    public void addError(ImportErrorDetail error) {
        this.errors.add(error);
    }

    // TODO: Add helper methods (getDurationFormatted, getSuccessRate, etc.)
}
