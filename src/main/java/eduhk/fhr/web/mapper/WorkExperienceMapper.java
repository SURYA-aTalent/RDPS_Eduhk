package eduhk.fhr.web.mapper;

// File Path: src/main/java/eduhk/fhr/web/mapper/WorkExperienceMapper.java
// Purpose: Map TalentLink API work experience DTO to RDPS database model

import org.springframework.stereotype.Component;

import eduhk.fhr.web.dto.talentlink.TalentLinkWorkExperienceDTO;
import eduhk.fhr.web.model.WorkExperience;

/**
 * Work Experience Mapper
 *
 * Transforms TalentLink API work experience DTOs to RDPS database models.
 */
@Component
public class WorkExperienceMapper {

    /**
     * Map TalentLink work experience DTO to RDPS WorkExperience model
     *
     * @param dto TalentLink work experience DTO
     * @param candidateId Candidate ID
     * @param reqNumber Requisition number
     * @param seq Sequence number
     * @return RDPS WorkExperience model
     */
    public WorkExperience mapToModel(TalentLinkWorkExperienceDTO dto, String candidateId, String reqNumber, int seq) {
        // TODO: Implement mapping logic
        // TODO: Map all fields from DTO to Model
        // TODO: Set candidate ID, req number, and sequence
        // TODO: Handle date formatting
        // TODO: Handle null/missing values

        WorkExperience workExp = new WorkExperience();

        if (dto != null) {
            workExp.setCandidateId(candidateId);
            workExp.setReqNumber(reqNumber);
            workExp.setSeq(seq);
            workExp.setEmployerName(dto.getEmployerName());
            workExp.setPositionTitle(dto.getPositionTitle());
            workExp.setStartDate(dto.getStartDate());
            workExp.setEndDate(dto.getEndDate());
            // TODO: Map remaining fields
        }

        return workExp;
    }

    /**
     * Validate work experience data
     *
     * @param workExp WorkExperience model to validate
     * @return true if valid, false otherwise
     */
    public boolean validate(WorkExperience workExp) {
        // TODO: Implement validation logic
        return true;
    }
}
