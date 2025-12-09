package eduhk.fhr.web.service;

// File Path: src/main/java/eduhk/fhr/web/service/ParsedPIFData.java
// Purpose: Container for parsed Personal Information Form (PIF) data

import eduhk.fhr.web.dto.talentlink.TalentLinkPIFDTO;

/**
 * ParsedPIFData
 *
 * Container for data extracted from "EdUHK Standard PIF" structured document.
 * Used to transport parsed PIF data from document parser to candidate import service.
 */
public class ParsedPIFData {

    private TalentLinkPIFDTO pifData;
    private boolean documentFound;

    public ParsedPIFData() {
        this.pifData = new TalentLinkPIFDTO();
        this.documentFound = false;
    }

    public TalentLinkPIFDTO getPifData() {
        return pifData;
    }

    public void setPifData(TalentLinkPIFDTO pifData) {
        this.pifData = pifData;
    }

    public boolean isDocumentFound() {
        return documentFound;
    }

    public void setDocumentFound(boolean documentFound) {
        this.documentFound = documentFound;
    }

    /**
     * Check if PIF data has meaningful content
     */
    public boolean hasData() {
        return pifData != null &&
               (pifData.getFirstName() != null ||
                pifData.getLastName() != null ||
                pifData.getEmail() != null);
    }

    @Override
    public String toString() {
        return "ParsedPIFData{" +
                "documentFound=" + documentFound +
                ", pifData=" + pifData +
                '}';
    }
}
