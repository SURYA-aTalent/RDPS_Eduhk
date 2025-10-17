package eduhk.fhr.web.dto.talentlink;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TalentLinkPositionDTO {

    @JsonProperty("position")
    private String position;

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
