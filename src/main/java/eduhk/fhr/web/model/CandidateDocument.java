package eduhk.fhr.web.model;

// File Path: src/main/java/eduhk/fhr/web/model/CandidateDocument.java
// Purpose: Model representing RDPS_CANDIDATE_DOCUMENT table

import java.util.Date;

/**
 * CandidateDocument Model
 *
 * Represents a candidate document record in the RDPS_CANDIDATE_DOCUMENT table.
 * Tracks documents synced from TalentLink to SharePoint.
 */
public class CandidateDocument {

    private Long docId;
    private String candidateId;  // VARCHAR2(20) in database
    private Long talentLinkAppId;
    private Long talentLinkDocId;
    private String fileName;
    private Long fileSize;
    private String docType;
    private String sharePointFileId;
    private String sharePointWebUrl;
    private String sharePointFolderPath;
    private String syncStatus;
    private Date syncDate;
    private String errorMessage;
    private String createdBy;
    private Date createdDate;

    // Constructors

    public CandidateDocument() {
    }

    // Getters and Setters

    public Long getDocId() {
        return docId;
    }

    public void setDocId(Long docId) {
        this.docId = docId;
    }

    public String getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(String candidateId) {
        this.candidateId = candidateId;
    }

    public Long getTalentLinkAppId() {
        return talentLinkAppId;
    }

    public void setTalentLinkAppId(Long talentLinkAppId) {
        this.talentLinkAppId = talentLinkAppId;
    }

    public Long getTalentLinkDocId() {
        return talentLinkDocId;
    }

    public void setTalentLinkDocId(Long talentLinkDocId) {
        this.talentLinkDocId = talentLinkDocId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getSharePointFileId() {
        return sharePointFileId;
    }

    public void setSharePointFileId(String sharePointFileId) {
        this.sharePointFileId = sharePointFileId;
    }

    public String getSharePointWebUrl() {
        return sharePointWebUrl;
    }

    public void setSharePointWebUrl(String sharePointWebUrl) {
        this.sharePointWebUrl = sharePointWebUrl;
    }

    public String getSharePointFolderPath() {
        return sharePointFolderPath;
    }

    public void setSharePointFolderPath(String sharePointFolderPath) {
        this.sharePointFolderPath = sharePointFolderPath;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }

    public Date getSyncDate() {
        return syncDate;
    }

    public void setSyncDate(Date syncDate) {
        this.syncDate = syncDate;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public String toString() {
        return "CandidateDocument{" +
                "docId=" + docId +
                ", candidateId=" + candidateId +
                ", talentLinkDocId=" + talentLinkDocId +
                ", fileName='" + fileName + '\'' +
                ", syncStatus='" + syncStatus + '\'' +
                ", syncDate=" + syncDate +
                '}';
    }
}
