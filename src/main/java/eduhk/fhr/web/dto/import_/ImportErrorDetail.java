package eduhk.fhr.web.dto.import_;

// File Path: src/main/java/eduhk/fhr/web/dto/import_/ImportErrorDetail.java
// Purpose: DTO for detailed error information during import

import java.util.Date;

/**
 * Import Error Detail
 *
 * Contains detailed information about an error that occurred during import.
 * Used for troubleshooting and error reporting.
 */
public class ImportErrorDetail {

    private String candidateId;
    private String requisitionNumber;
    private String errorType;
    private String errorMessage;
    private String fieldName;
    private String stackTrace;
    private Date errorTimestamp;

    // Getters and Setters
    // TODO: Add getters and setters for all fields

    public String getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(String candidateId) {
        this.candidateId = candidateId;
    }

    public String getRequisitionNumber() {
        return requisitionNumber;
    }

    public void setRequisitionNumber(String requisitionNumber) {
        this.requisitionNumber = requisitionNumber;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Date getErrorTimestamp() {
        return errorTimestamp;
    }

    public void setErrorTimestamp(Date errorTimestamp) {
        this.errorTimestamp = errorTimestamp;
    }

    // TODO: Add remaining getters and setters
}
