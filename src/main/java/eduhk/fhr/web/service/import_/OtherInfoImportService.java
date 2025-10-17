package eduhk.fhr.web.service.import_;

// File Path: src/main/java/eduhk/fhr/web/service/import_/OtherInfoImportService.java
// Purpose: Service for importing other candidate information (salary, skills, etc.)

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eduhk.fhr.web.dto.talentlink.TalentLinkCandidateDTO;
import eduhk.fhr.web.mapper.OtherInfoMapper;
import eduhk.fhr.web.model.OtherInfo;

/**
 * Other Info Import Service
 *
 * Handles importing other candidate information.
 */
@Service
public class OtherInfoImportService {

    private static final Logger logger = LoggerFactory.getLogger(OtherInfoImportService.class);

    @Autowired
    private OtherInfoMapper otherInfoMapper;

    @Autowired
    private eduhk.fhr.web.dao.OtherInfoDAO otherInfoDAO;

    /**
     * Import other info for a candidate
     *
     * @param candidateDTO Candidate DTO containing other info
     * @param candidateId Candidate ID
     * @param reqNumber Requisition number
     */
    public void importOtherInfo(TalentLinkCandidateDTO candidateDTO, String candidateId, String reqNumber) {
        // logger.info("Importing other info for candidate: {}", candidateId);

        // if (candidateDTO.getOtherInfo() != null) {
        //     OtherInfo otherInfo = otherInfoMapper.mapToModel(candidateDTO.getOtherInfo(), candidateId, reqNumber);

        //     // Validate and insert/update
        //     if (otherInfoMapper.validate(otherInfo)) {
        //         otherInfoDAO.upsertOtherInfo(otherInfo);
        //         logger.info("Imported other info for candidate: {}", candidateId);
        //     } else {
        //         logger.warn("Invalid other info record for candidate {}", candidateId);
        //     }
        // }

        // logger.info("Completed other info import for candidate: {}", candidateId);
    }
}
