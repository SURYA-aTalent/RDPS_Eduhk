package eduhk.fhr.web.mapper;

import org.springframework.stereotype.Component;

import eduhk.fhr.web.dto.talentlink.TalentLinkPersonInfoDTO;
import eduhk.fhr.web.model.PersonInfo;

/**
 * Person Info Mapper
 *
 * Transforms TalentLink PersonInfo DTOs to RDPS database models.
 */
@Component
public class PersonInfoMapper {

    /**
     * Map TalentLink PersonInfo DTO to RDPS PersonInfo model
     *
     * @param dto TalentLink person info DTO
     * @param offerId Offer ID
     * @param reqNumber Requisition number
     * @return RDPS PersonInfo model
     */
    public PersonInfo mapToModel(TalentLinkPersonInfoDTO dto, String offerId, String reqNumber) {
        PersonInfo personInfo = new PersonInfo();

        if (dto == null) {
            return personInfo;
        }

        // Primary keys
        personInfo.setOfferId(offerId);
        personInfo.setReqNumber(reqNumber);

        // Personal identification
        personInfo.setPersonNumber(dto.getPersonNumber());
        personInfo.setNationality(dto.getNationality());
        personInfo.setPlaceOfOrigin(dto.getPlaceOfOrigin());

        // Education and status
        personInfo.setHighestEducation(dto.getHighestEducation());
        personInfo.setMaritalStatus(dto.getMaritalStatus());
        personInfo.setStatusDate(dto.getStatusDate());

        // Immigration and visa
        personInfo.setVisaIssueDate(dto.getVisaIssueDate());
        personInfo.setVisaExpiryDate(dto.getVisaExpiryDate());
        personInfo.setImmigrationStatus(dto.getImmigrationStatus());
        personInfo.setHkEntryDate(dto.getHkEntryDate());

        // Audit fields
        personInfo.setCreatedBy("SYSTEM");
        personInfo.setUserstamp("SYSTEM");

        return personInfo;
    }

    /**
     * Validate person info data
     *
     * @param personInfo PersonInfo model to validate
     * @return true if valid, false otherwise
     */
    public boolean validate(PersonInfo personInfo) {
        if (personInfo == null) {
            return false;
        }

        // Check required fields
        if (personInfo.getOfferId() == null || personInfo.getOfferId().isEmpty()) {
            return false;
        }

        if (personInfo.getReqNumber() == null || personInfo.getReqNumber().isEmpty()) {
            return false;
        }

        return true;
    }
}
