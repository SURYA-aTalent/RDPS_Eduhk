package eduhk.fhr.web.dto.talentlink;

/**
 * TalentLink Emergency Contact Person DTO
 *
 * Represents emergency contact information data extracted from
 * "EdUHK Onboarding Data Submission Questionnaire (Candidate)" - Emergency Contact section
 *
 * Maps to: RDPS_PERSON_ECP table
 */
public class TalentLinkEmergencyContactDTO {

    // Contact person details
    private String fullName;
    private String phoneNo;
    private String relationship;

    // Getters and Setters

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    @Override
    public String toString() {
        return "TalentLinkEmergencyContactDTO{" +
                "fullName='" + fullName + '\'' +
                ", phoneNo='" + phoneNo + '\'' +
                ", relationship='" + relationship + '\'' +
                '}';
    }
}
