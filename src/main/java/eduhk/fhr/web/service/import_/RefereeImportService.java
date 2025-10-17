package eduhk.fhr.web.service.import_;

// File Path: src/main/java/eduhk/fhr/web/service/import_/RefereeImportService.java
// Purpose: Service for importing referee records

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eduhk.fhr.web.dto.talentlink.TalentLinkCandidateDTO;
import eduhk.fhr.web.dto.talentlink.TalentLinkRefereeDTO;
import eduhk.fhr.web.mapper.RefereeMapper;
import eduhk.fhr.web.model.Referee;

/**
 * Referee Import Service
 *
 * Handles importing referee records for a candidate.
 */
@Service
public class RefereeImportService {

    private static final Logger logger = LoggerFactory.getLogger(RefereeImportService.class);

    @Autowired
    private RefereeMapper refereeMapper;

    @Autowired
    private eduhk.fhr.web.dao.RefereeDAO refereeDAO;

    /**
     * Import referee records for a candidate
     *
     * @param candidateDTO Candidate DTO containing referee list
     * @param candidateId Candidate ID
     * @param reqNumber Requisition number
     */
    public void importReferees(TalentLinkCandidateDTO candidateDTO, String candidateId, String reqNumber) {
        // logger.info("Importing referees for candidate: {}", candidateId);

        // // Delete existing referee records first (replace strategy)
        // refereeDAO.deleteRefereesByCandidateId(candidateId, reqNumber);

        // if (candidateDTO.getReferees() != null) {
        //     int seq = 1;
        //     for (TalentLinkRefereeDTO dto : candidateDTO.getReferees()) {
        //         Referee referee = refereeMapper.mapToModel(dto, candidateId, reqNumber, seq);

        //         // Validate and insert
        //         if (refereeMapper.validate(referee)) {
        //             refereeDAO.insertReferee(referee);
        //         } else {
        //             logger.warn("Invalid referee record for candidate {}, seq {}", candidateId, seq);
        //         }

        //         seq++;
        //     }
        //     logger.info("Imported {} referee records for candidate: {}",
        //         candidateDTO.getReferees().size(), candidateId);
        // }

        // logger.info("Completed referee import for candidate: {}", candidateId);
    }
}
