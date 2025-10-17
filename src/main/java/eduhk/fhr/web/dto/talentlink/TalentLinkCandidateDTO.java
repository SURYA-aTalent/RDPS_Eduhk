package eduhk.fhr.web.dto.talentlink;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TalentLinkCandidateDTO {

    private String id;
    private String firstname;
    private String lastname;
    private String email;

    @JsonProperty("position")
    private TalentLinkPositionDTO position;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public TalentLinkPositionDTO getPosition() {
        return position;
    }

    public void setPosition(TalentLinkPositionDTO position) {
        this.position = position;
    }

    public String getRequisitionNumber() {
        if (position != null) {
            return position.getPosition();
        }
        return null;
    }
}
