package eduhk.fhr.web.dto.talentlink;

// File Path: src/main/java/eduhk/fhr/web/dto/talentlink/TalentLinkRefereeDTO.java
// Purpose: DTO representing referee data from TalentLink API response

/**
 * TalentLink Referee DTO
 *
 * Data Transfer Object for referee information received from TalentLink API.
 */
public class TalentLinkRefereeDTO {

    private String title;
    private String firstname;
    private String lastname;
    private String positionTitle;
    private String phoneNumber;
    private String email;
    private String relationship;

    // Getters and Setters
    // TODO: Add getters and setters for all fields

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPositionTitle() {
        return positionTitle;
    }

    public void setPositionTitle(String positionTitle) {
        this.positionTitle = positionTitle;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }
}
