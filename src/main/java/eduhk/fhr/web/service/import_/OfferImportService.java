package eduhk.fhr.web.service.import_;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eduhk.fhr.web.dao.OfferDAO;
import eduhk.fhr.web.mapper.OfferMapper;
import eduhk.fhr.web.model.Offer;
import eduhk.fhr.web.soap.candidate.ApplicationDto;

/**
 * Offer Import Service
 *
 * Handles importing job offer data from TalentLink to RDPS database.
 * Processes ApplicationDto objects and persists them as Offer records.
 */
@Service
@Transactional
public class OfferImportService {

    private static final Logger logger = LoggerFactory.getLogger(OfferImportService.class);

    @Autowired
    private OfferDAO offerDAO;

    @Autowired
    private OfferMapper offerMapper;

    /**
     * Import offer from TalentLink ApplicationDto
     *
     * @param application TalentLink application DTO
     * @param candidateId Candidate ID
     * @param reqNumber Requisition number
     * @return true if import successful, false otherwise
     */
    public boolean importOffer(ApplicationDto application, String candidateId, String reqNumber) {
        try {
            if (application == null) {
                logger.warn("Cannot import null application for candidate: {}", candidateId);
                return false;
            }

            // Map application to offer model
            Offer offer = offerMapper.mapToModel(application, candidateId, reqNumber);

            // Validate offer data
            if (!offerMapper.validate(offer)) {
                logger.warn("Offer validation failed for candidate: {}, application: {}",
                        candidateId, application.getId());
                return false;
            }

            // Upsert offer (insert or update if exists)
            offerDAO.upsertOffer(offer);

            logger.info("Successfully imported offer: {} for candidate: {}",
                    offer.getOfferId(), candidateId);

            return true;

        } catch (Exception e) {
            logger.error("Error importing offer for candidate: {}, application: {}",
                    candidateId, application.getId(), e);
            return false;
        }
    }

    /**
     * Import offer if application status indicates an offer was made
     *
     * @param application TalentLink application DTO
     * @param candidateId Candidate ID
     * @param reqNumber Requisition number
     * @return true if import successful or not needed, false if import failed
     */
    public boolean importOfferIfEligible(ApplicationDto application, String candidateId, String reqNumber) {
        if (application == null || application.getStatus() == null) {
            return true; // No offer to import
        }

        String status = application.getStatus().toUpperCase();

        // Only import if status indicates an offer
        // Common offer statuses: "OFFERED", "OFFER APPROVED", "OFFER ACCEPTED", "HIRED"
        if (status.contains("OFFER") || status.contains("HIRED")) {
            return importOffer(application, candidateId, reqNumber);
        }

        logger.debug("Application status '{}' does not warrant offer import for candidate: {}",
                status, candidateId);
        return true; // Not an error, just not eligible for offer import
    }
}
