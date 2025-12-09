package eduhk.fhr.web.mapper;

import org.springframework.stereotype.Component;

import eduhk.fhr.web.dto.talentlink.TalentLinkChildDTO;
import eduhk.fhr.web.model.PersonChild;

/**
 * Person Child Mapper
 *
 * Transforms TalentLink Child DTOs to RDPS database models.
 */
@Component
public class PersonChildMapper {

    /**
     * Map TalentLink Child DTO to RDPS PersonChild model
     *
     * @param dto TalentLink child DTO
     * @param offerId Offer ID
     * @param reqNumber Requisition number
     * @return RDPS PersonChild model
     */
    public PersonChild mapToModel(TalentLinkChildDTO dto, String offerId, String reqNumber) {
        PersonChild child = new PersonChild();

        if (dto == null) {
            return child;
        }

        // Primary keys
        child.setOfferId(offerId);
        child.setReqNumber(reqNumber);
        child.setChildSeq(dto.getChildSeq());

        // Name fields
        child.setFullName(dto.getFullName());
        child.setFirstName(dto.getFirstName());
        child.setLastName(dto.getLastName());
        child.setChineseName(dto.getChineseName());

        // Personal details
        child.setDateOfBirth(dto.getDateOfBirth());
        child.setGender(dto.getGender());

        // Identity documents
        child.setHkid(dto.getHkid());
        child.setPassport(dto.getPassport());
        child.setNationality(dto.getNationality());

        // Audit fields
        child.setCreatedBy("SYSTEM");
        child.setUserstamp("SYSTEM");

        return child;
    }

    /**
     * Validate child data
     *
     * @param child PersonChild model to validate
     * @return true if valid, false otherwise
     */
    public boolean validate(PersonChild child) {
        if (child == null) {
            return false;
        }

        // Check required fields
        if (child.getOfferId() == null || child.getOfferId().isEmpty()) {
            return false;
        }

        if (child.getReqNumber() == null || child.getReqNumber().isEmpty()) {
            return false;
        }

        if (child.getChildSeq() == null) {
            return false;
        }

        return true;
    }
}
