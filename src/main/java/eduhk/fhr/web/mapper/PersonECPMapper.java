package eduhk.fhr.web.mapper;

import org.springframework.stereotype.Component;

import eduhk.fhr.web.dto.talentlink.TalentLinkEmergencyContactDTO;
import eduhk.fhr.web.model.PersonECP;

/**
 * Person Emergency Contact Mapper
 *
 * Transforms TalentLink Emergency Contact DTOs to RDPS database models.
 */
@Component
public class PersonECPMapper {

    /**
     * Map TalentLink Emergency Contact DTO to RDPS PersonECP model
     *
     * @param dto TalentLink emergency contact DTO
     * @param offerId Offer ID
     * @param reqNumber Requisition number
     * @return RDPS PersonECP model
     */
    public PersonECP mapToModel(TalentLinkEmergencyContactDTO dto, String offerId, String reqNumber) {
        PersonECP ecp = new PersonECP();

        if (dto == null) {
            return ecp;
        }

        // Primary keys
        ecp.setOfferId(offerId);
        ecp.setReqNumber(reqNumber);

        // Contact person details
        ecp.setFullName(dto.getFullName());
        ecp.setPhoneNo(dto.getPhoneNo());
        ecp.setRelationship(dto.getRelationship());

        // Audit fields
        ecp.setCreatedBy("SYSTEM");
        ecp.setUserstamp("SYSTEM");

        return ecp;
    }

    /**
     * Validate emergency contact data
     *
     * @param ecp PersonECP model to validate
     * @return true if valid, false otherwise
     */
    public boolean validate(PersonECP ecp) {
        if (ecp == null) {
            return false;
        }

        // Check required fields
        if (ecp.getOfferId() == null || ecp.getOfferId().isEmpty()) {
            return false;
        }

        if (ecp.getReqNumber() == null || ecp.getReqNumber().isEmpty()) {
            return false;
        }

        return true;
    }
}
