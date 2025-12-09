package eduhk.fhr.web.mapper;

import org.springframework.stereotype.Component;

import eduhk.fhr.web.dto.talentlink.TalentLinkSpouseDTO;
import eduhk.fhr.web.model.PersonSpouse;

/**
 * Person Spouse Mapper
 *
 * Transforms TalentLink Spouse DTOs to RDPS database models.
 */
@Component
public class PersonSpouseMapper {

    /**
     * Map TalentLink Spouse DTO to RDPS PersonSpouse model
     *
     * @param dto TalentLink spouse DTO
     * @param offerId Offer ID
     * @param reqNumber Requisition number
     * @return RDPS PersonSpouse model
     */
    public PersonSpouse mapToModel(TalentLinkSpouseDTO dto, String offerId, String reqNumber) {
        PersonSpouse spouse = new PersonSpouse();

        if (dto == null) {
            return spouse;
        }

        // Primary keys
        spouse.setOfferId(offerId);
        spouse.setReqNumber(reqNumber);

        // Name fields
        spouse.setFullName(dto.getFullName());
        spouse.setFirstName(dto.getFirstName());
        spouse.setLastName(dto.getLastName());
        spouse.setChineseName(dto.getChineseName());

        // Personal details
        spouse.setDateOfBirth(dto.getDateOfBirth());
        spouse.setGender(dto.getGender());

        // Identity documents
        spouse.setHkid(dto.getHkid());
        spouse.setPassport(dto.getPassport());
        spouse.setNationality(dto.getNationality());

        // Contact information
        spouse.setEmail(dto.getEmail());
        spouse.setPhoneNo(dto.getPhoneNo());

        // Audit fields
        spouse.setCreatedBy("SYSTEM");
        spouse.setUserstamp("SYSTEM");

        return spouse;
    }

    /**
     * Validate spouse data
     *
     * @param spouse PersonSpouse model to validate
     * @return true if valid, false otherwise
     */
    public boolean validate(PersonSpouse spouse) {
        if (spouse == null) {
            return false;
        }

        // Check required fields
        if (spouse.getOfferId() == null || spouse.getOfferId().isEmpty()) {
            return false;
        }

        if (spouse.getReqNumber() == null || spouse.getReqNumber().isEmpty()) {
            return false;
        }

        return true;
    }
}
