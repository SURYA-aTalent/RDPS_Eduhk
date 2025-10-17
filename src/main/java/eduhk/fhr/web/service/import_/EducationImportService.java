package eduhk.fhr.web.service.import_;

// File Path: src/main/java/eduhk/fhr/web/service/import_/EducationImportService.java
// Purpose: Service for importing education/qualification records

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eduhk.fhr.web.dto.talentlink.TalentLinkCandidateDTO;
import eduhk.fhr.web.dto.talentlink.TalentLinkEducationDTO;
import eduhk.fhr.web.mapper.EducationMapper;
import eduhk.fhr.web.model.Education;

/**
 * Education Import Service
 *
 * Handles importing education and professional qualification records for a candidate.
 */
@Service
public class EducationImportService {

    private static final Logger logger = LoggerFactory.getLogger(EducationImportService.class);

    @Autowired
    private EducationMapper educationMapper;

    @Autowired
    private eduhk.fhr.web.dao.EducationDAO educationDAO;

    /**
     * Import education records for a candidate
     *
     * @param candidateDTO Candidate DTO containing education list
     * @param candidateId Candidate ID
     * @param reqNumber Requisition number
     */
    public void importEducation(TalentLinkCandidateDTO candidateDTO, String candidateId, String reqNumber) {
        // logger.info("Importing education for candidate: {}", candidateId);

        // // Delete existing education records first (replace strategy)
        // educationDAO.deleteEducationByCandidateId(candidateId, reqNumber);

        // if (candidateDTO.getEducation() != null) {
        //     int seq = 1;
        //     for (TalentLinkEducationDTO dto : candidateDTO.getEducation()) {
        //         Education education = educationMapper.mapToModel(dto, candidateId, reqNumber, seq);

        //         // Validate and insert
        //         if (educationMapper.validate(education)) {
        //             educationDAO.insertEducation(education);
        //         } else {
        //             logger.warn("Invalid education record for candidate {}, seq {}", candidateId, seq);
        //         }

        //         seq++;
        //     }
        //     logger.info("Imported {} education records for candidate: {}",
        //         candidateDTO.getEducation().size(), candidateId);
        // }

        // logger.info("Completed education import for candidate: {}", candidateId);
    }
}
