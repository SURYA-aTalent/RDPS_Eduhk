package eduhk.fhr.web.service.import_;

// File Path: src/main/java/eduhk/fhr/web/service/import_/WorkExperienceImportService.java
// Purpose: Service for importing work experience records

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eduhk.fhr.web.dto.talentlink.TalentLinkCandidateDTO;
import eduhk.fhr.web.dto.talentlink.TalentLinkWorkExperienceDTO;
import eduhk.fhr.web.mapper.WorkExperienceMapper;
import eduhk.fhr.web.model.WorkExperience;

/**
 * Work Experience Import Service
 *
 * Handles importing work experience records for a candidate.
 */
@Service
public class WorkExperienceImportService {

    private static final Logger logger = LoggerFactory.getLogger(WorkExperienceImportService.class);

    @Autowired
    private WorkExperienceMapper workExperienceMapper;

    @Autowired
    private eduhk.fhr.web.dao.WorkExperienceDAO workExperienceDAO;

    /**
     * Import work experience records for a candidate
     *
     * @param candidateDTO Candidate DTO containing work experience list
     * @param candidateId Candidate ID
     * @param reqNumber Requisition number
     */
    public void importWorkExperience(TalentLinkCandidateDTO candidateDTO, String candidateId, String reqNumber) {
        // logger.info("Importing work experience for candidate: {}", candidateId);

        // // Delete existing work experience records first (replace strategy)
        // workExperienceDAO.deleteWorkExperienceByCandidateId(candidateId, reqNumber);

        // if (candidateDTO.getWorkExperience() != null) {
        //     int seq = 1;
        //     for (TalentLinkWorkExperienceDTO dto : candidateDTO.getWorkExperience()) {
        //         WorkExperience workExp = workExperienceMapper.mapToModel(dto, candidateId, reqNumber, seq);

        //         // Validate and insert
        //         if (workExperienceMapper.validate(workExp)) {
        //             workExperienceDAO.insertWorkExperience(workExp);
        //         } else {
        //             logger.warn("Invalid work experience record for candidate {}, seq {}", candidateId, seq);
        //         }

        //         seq++;
        //     }
        //     logger.info("Imported {} work experience records for candidate: {}",
        //         candidateDTO.getWorkExperience().size(), candidateId);
        // }

        // logger.info("Completed work experience import for candidate: {}", candidateId);
    }
}
