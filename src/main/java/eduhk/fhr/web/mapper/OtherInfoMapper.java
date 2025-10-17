package eduhk.fhr.web.mapper;

// File Path: src/main/java/eduhk/fhr/web/mapper/OtherInfoMapper.java
// Purpose: Map TalentLink API other info DTO to RDPS database model

import org.springframework.stereotype.Component;

import eduhk.fhr.web.dto.talentlink.TalentLinkOtherInfoDTO;
import eduhk.fhr.web.model.OtherInfo;

/**
 * Other Info Mapper
 *
 * Transforms TalentLink API other info DTOs to RDPS database models.
 * Handles salary, skills, availability, and other candidate information.
 */
@Component
public class OtherInfoMapper {

    /**
     * Map TalentLink other info DTO to RDPS OtherInfo model
     *
     * @param dto TalentLink other info DTO
     * @param candidateId Candidate ID
     * @param reqNumber Requisition number
     * @return RDPS OtherInfo model
     */
    public OtherInfo mapToModel(TalentLinkOtherInfoDTO dto, String candidateId, String reqNumber) {
        // TODO: Implement mapping logic
        // TODO: Map all fields from DTO to Model
        // TODO: Handle numeric conversions (BigDecimal, Integer)
        // TODO: Handle null/missing values

        OtherInfo otherInfo = new OtherInfo();

        if (dto != null) {
            otherInfo.setCandidateId(candidateId);
            otherInfo.setReqNumber(reqNumber);
            otherInfo.setSalary(dto.getSalary());
            otherInfo.setExpectedSalary(dto.getExpectedSalary());
            otherInfo.setSkills(dto.getSkills());
            otherInfo.setNoticePeriod(dto.getNoticePeriod());
            // TODO: Map remaining fields
        }

        return otherInfo;
    }

    /**
     * Validate other info data
     *
     * @param otherInfo OtherInfo model to validate
     * @return true if valid, false otherwise
     */
    public boolean validate(OtherInfo otherInfo) {
        // TODO: Implement validation logic
        return true;
    }
}
