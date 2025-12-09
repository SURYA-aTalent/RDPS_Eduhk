package eduhk.fhr.web.service;

import java.util.List;

import eduhk.fhr.web.dto.talentlink.TalentLinkChildDTO;
import eduhk.fhr.web.dto.talentlink.TalentLinkEmergencyContactDTO;
import eduhk.fhr.web.dto.talentlink.TalentLinkPersonInfoDTO;
import eduhk.fhr.web.dto.talentlink.TalentLinkSpouseDTO;

/**
 * Parsed Onboarding Data Container
 *
 * Contains all data extracted from "EdUHK Onboarding Data Submission Questionnaire (Candidate)"
 * structured document.
 *
 * Sections:
 * 1. Personal Info → PersonInfo table
 * 2. Spouse Info → PersonSpouse table
 * 3. Child Info (repeating) → PersonChild table
 * 4. Emergency Contact → PersonECP table
 * 5. Photo → PhotoUpload table
 */
public class ParsedOnboardingData {

    private boolean documentFound;

    private TalentLinkPersonInfoDTO personInfo;
    private TalentLinkSpouseDTO spouse;
    private List<TalentLinkChildDTO> children;
    private TalentLinkEmergencyContactDTO emergencyContact;

    // TODO: Add photo DTO when photo upload feature is implemented
    // private TalentLinkPhotoDTO photo;

    /**
     * Check if the onboarding document was found
     *
     * @return true if document was located, false otherwise
     */
    public boolean isDocumentFound() {
        return documentFound;
    }

    public void setDocumentFound(boolean documentFound) {
        this.documentFound = documentFound;
    }

    /**
     * Check if meaningful onboarding data exists
     *
     * @return true if at least one section has data
     */
    public boolean hasData() {
        return personInfo != null ||
               spouse != null ||
               (children != null && !children.isEmpty()) ||
               emergencyContact != null;
    }

    public TalentLinkPersonInfoDTO getPersonInfo() {
        return personInfo;
    }

    public void setPersonInfo(TalentLinkPersonInfoDTO personInfo) {
        this.personInfo = personInfo;
    }

    public TalentLinkSpouseDTO getSpouse() {
        return spouse;
    }

    public void setSpouse(TalentLinkSpouseDTO spouse) {
        this.spouse = spouse;
    }

    public List<TalentLinkChildDTO> getChildren() {
        return children;
    }

    public void setChildren(List<TalentLinkChildDTO> children) {
        this.children = children;
    }

    public TalentLinkEmergencyContactDTO getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(TalentLinkEmergencyContactDTO emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    @Override
    public String toString() {
        return "ParsedOnboardingData{" +
                "documentFound=" + documentFound +
                ", hasPersonInfo=" + (personInfo != null) +
                ", hasSpouse=" + (spouse != null) +
                ", childrenCount=" + (children != null ? children.size() : 0) +
                ", hasEmergencyContact=" + (emergencyContact != null) +
                '}';
    }
}
