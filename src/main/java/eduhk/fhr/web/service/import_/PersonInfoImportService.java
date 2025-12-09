package eduhk.fhr.web.service.import_;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eduhk.fhr.web.dao.PersonInfoDAO;
import eduhk.fhr.web.dto.talentlink.TalentLinkPersonInfoDTO;
import eduhk.fhr.web.mapper.PersonInfoMapper;
import eduhk.fhr.web.model.PersonInfo;

/**
 * Person Info Import Service
 *
 * Handles importing personal information data from onboarding questionnaire.
 */
@Service
@Transactional
public class PersonInfoImportService {

    private static final Logger logger = LoggerFactory.getLogger(PersonInfoImportService.class);

    @Autowired
    private PersonInfoDAO personInfoDAO;

    @Autowired
    private PersonInfoMapper personInfoMapper;

    /**
     * Import person info from onboarding questionnaire
     *
     * @param dto TalentLink person info DTO
     * @param offerId Offer ID
     * @param reqNumber Requisition number
     */
    public void importPersonInfo(TalentLinkPersonInfoDTO dto, String offerId, String reqNumber) {
        if (dto == null) {
            logger.debug("No person info data to import for offer: {}", offerId);
            return;
        }

        logger.info("Importing PersonInfo for offer: {}", offerId);

        // Map DTO to model
        PersonInfo personInfo = personInfoMapper.mapToModel(dto, offerId, reqNumber);

        // Validate
        if (!personInfoMapper.validate(personInfo)) {
            logger.warn("PersonInfo validation failed for offer: {}", offerId);
            return;
        }

        // Delete existing and insert new
        personInfoDAO.deletePersonInfo(offerId, reqNumber);
        personInfoDAO.insertPersonInfo(personInfo);

        logger.info("Successfully imported PersonInfo for offer: {}", offerId);
    }
}
