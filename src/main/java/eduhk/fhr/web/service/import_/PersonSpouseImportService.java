package eduhk.fhr.web.service.import_;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eduhk.fhr.web.dao.PersonSpouseDAO;
import eduhk.fhr.web.dto.talentlink.TalentLinkSpouseDTO;
import eduhk.fhr.web.mapper.PersonSpouseMapper;
import eduhk.fhr.web.model.PersonSpouse;

/**
 * Person Spouse Import Service
 *
 * Handles importing spouse information data from onboarding questionnaire.
 */
@Service
@Transactional
public class PersonSpouseImportService {

    private static final Logger logger = LoggerFactory.getLogger(PersonSpouseImportService.class);

    @Autowired
    private PersonSpouseDAO personSpouseDAO;

    @Autowired
    private PersonSpouseMapper personSpouseMapper;

    /**
     * Import spouse info from onboarding questionnaire
     *
     * @param dto TalentLink spouse DTO
     * @param offerId Offer ID
     * @param reqNumber Requisition number
     */
    public void importSpouse(TalentLinkSpouseDTO dto, String offerId, String reqNumber) {
        if (dto == null) {
            logger.debug("No spouse data to import for offer: {}", offerId);
            return;
        }

        logger.info("Importing PersonSpouse for offer: {}", offerId);

        // Map DTO to model
        PersonSpouse spouse = personSpouseMapper.mapToModel(dto, offerId, reqNumber);

        // Validate
        if (!personSpouseMapper.validate(spouse)) {
            logger.warn("PersonSpouse validation failed for offer: {}", offerId);
            return;
        }

        // Delete existing and insert new
        personSpouseDAO.deleteSpouse(offerId, reqNumber);
        personSpouseDAO.insertSpouse(spouse);

        logger.info("Successfully imported PersonSpouse for offer: {}", offerId);
    }
}
