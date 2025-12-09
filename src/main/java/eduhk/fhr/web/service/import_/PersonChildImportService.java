package eduhk.fhr.web.service.import_;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eduhk.fhr.web.dao.PersonChildDAO;
import eduhk.fhr.web.dto.talentlink.TalentLinkChildDTO;
import eduhk.fhr.web.mapper.PersonChildMapper;
import eduhk.fhr.web.model.PersonChild;

/**
 * Person Child Import Service
 *
 * Handles importing children information data from onboarding questionnaire.
 * Supports multiple children (repeating section).
 */
@Service
@Transactional
public class PersonChildImportService {

    private static final Logger logger = LoggerFactory.getLogger(PersonChildImportService.class);

    @Autowired
    private PersonChildDAO personChildDAO;

    @Autowired
    private PersonChildMapper personChildMapper;

    /**
     * Import children from onboarding questionnaire
     *
     * @param children List of TalentLink child DTOs
     * @param offerId Offer ID
     * @param reqNumber Requisition number
     */
    public void importChildren(List<TalentLinkChildDTO> children, String offerId, String reqNumber) {
        if (children == null || children.isEmpty()) {
            logger.debug("No children data to import for offer: {}", offerId);
            return;
        }

        logger.info("Importing {} children for offer: {}", children.size(), offerId);

        // Delete existing children
        personChildDAO.deleteChildrenByOfferId(offerId, reqNumber);

        // Insert each child
        int importedCount = 0;
        for (TalentLinkChildDTO dto : children) {
            // Map DTO to model
            PersonChild child = personChildMapper.mapToModel(dto, offerId, reqNumber);

            // Validate
            if (!personChildMapper.validate(child)) {
                logger.warn("PersonChild validation failed for offer: {}, childSeq: {}",
                    offerId, dto.getChildSeq());
                continue;
            }

            // Insert child
            personChildDAO.insertChild(child);
            importedCount++;
        }

        logger.info("Successfully imported {} children for offer: {}", importedCount, offerId);
    }
}
