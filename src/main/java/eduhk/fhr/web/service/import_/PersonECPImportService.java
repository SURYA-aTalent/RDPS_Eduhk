package eduhk.fhr.web.service.import_;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eduhk.fhr.web.dao.PersonECPDAO;
import eduhk.fhr.web.dto.talentlink.TalentLinkEmergencyContactDTO;
import eduhk.fhr.web.mapper.PersonECPMapper;
import eduhk.fhr.web.model.PersonECP;

/**
 * Person Emergency Contact Import Service
 *
 * Handles importing emergency contact information data from onboarding questionnaire.
 */
@Service
@Transactional
public class PersonECPImportService {

    private static final Logger logger = LoggerFactory.getLogger(PersonECPImportService.class);

    @Autowired
    private PersonECPDAO personECPDAO;

    @Autowired
    private PersonECPMapper personECPMapper;

    /**
     * Import emergency contact from onboarding questionnaire
     *
     * @param dto TalentLink emergency contact DTO
     * @param offerId Offer ID
     * @param reqNumber Requisition number
     */
    public void importEmergencyContact(TalentLinkEmergencyContactDTO dto, String offerId, String reqNumber) {
        if (dto == null) {
            logger.debug("No emergency contact data to import for offer: {}", offerId);
            return;
        }

        logger.info("Importing PersonECP for offer: {}", offerId);

        // Map DTO to model
        PersonECP ecp = personECPMapper.mapToModel(dto, offerId, reqNumber);

        // Validate
        if (!personECPMapper.validate(ecp)) {
            logger.warn("PersonECP validation failed for offer: {}", offerId);
            return;
        }

        // Delete existing and insert new
        personECPDAO.deleteEmergencyContact(offerId, reqNumber);
        personECPDAO.insertEmergencyContact(ecp);

        logger.info("Successfully imported PersonECP for offer: {}", offerId);
    }
}
