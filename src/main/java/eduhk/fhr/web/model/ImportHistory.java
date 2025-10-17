package eduhk.fhr.web.model;

// File Path: src/main/java/eduhk/fhr/web/model/ImportHistory.java
// Purpose: Model class representing RDPS_IMPORT_HISTORY table

import java.util.Date;

/**
 * Import History Model
 *
 * Represents a record in the RDPS_IMPORT_HISTORY table.
 * Tracks each import operation with statistics and results.
 */
public class ImportHistory extends BaseModel {

    private int importId;
    private Date importDate;
    private String triggeredBy;
    private int totalCandidates;
    private int successfulImports;
    private int failedImports;
    private long durationMs;
    private String lastCandidateId;
    private String status;  // 'COMPLETED', 'FAILED', 'PARTIAL'
    private String errorMessage;

    // Getters and Setters

    public int getImportId() {
        return importId;
    }

    public void setImportId(int importId) {
        this.importId = importId;
    }

    public Date getImportDate() {
        return importDate;
    }

    public void setImportDate(Date importDate) {
        this.importDate = importDate;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public int getTotalCandidates() {
        return totalCandidates;
    }

    public void setTotalCandidates(int totalCandidates) {
        this.totalCandidates = totalCandidates;
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

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public String getLastCandidateId() {
        return lastCandidateId;
    }

    public void setLastCandidateId(String lastCandidateId) {
        this.lastCandidateId = lastCandidateId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
