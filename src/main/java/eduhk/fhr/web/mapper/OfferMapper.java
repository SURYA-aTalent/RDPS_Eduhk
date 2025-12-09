package eduhk.fhr.web.mapper;

import org.springframework.stereotype.Component;

import eduhk.fhr.web.model.Offer;
import eduhk.fhr.web.soap.candidate.ApplicationDto;

/**
 * Offer Mapper
 *
 * Transforms TalentLink ApplicationDto to RDPS Offer model.
 * Maps application/offer data from TalentLink SOAP API to database model.
 */
@Component
public class OfferMapper {

    /**
     * Map TalentLink ApplicationDto to RDPS Offer model
     *
     * NOTE: ApplicationDto only provides 4/30 offer fields. Remaining 26 fields are missing
     * and require investigation into Contract API, Position API, or structured documents.
     * See OFFER_API_DETAILS.md for complete analysis.
     *
     * @param application TalentLink application DTO
     * @param candidateId Candidate ID
     * @param reqNumber Requisition number
     * @return RDPS Offer model (partially populated)
     */
    public Offer mapToModel(ApplicationDto application, String candidateId, String reqNumber) {
        Offer offer = new Offer();

        if (application == null) {
            return offer;
        }

        // ========== AVAILABLE FIELDS (4/30) ==========

        // Primary key - use application ID as offer ID
        if (application.getId() != null) {
            offer.setOfferId(String.valueOf(application.getId()));
        }

        // Reference fields
        offer.setCandidateId(candidateId);
        offer.setReqNumber(reqNumber);

        // Offer status from application status (truncate to 15 chars - DB limit)
        if (application.getStatus() != null) {
            String status = application.getStatus();
            if (status.length() > 15) {
                status = status.substring(0, 15);
            }
            offer.setOfferStatus(status);
        }

        // Optional: Add memo to remarks if available (truncate to 100 chars - DB limit)
        String remarks = null;
        if (application.getMemo() != null && !application.getMemo().isEmpty()) {
            remarks = application.getMemo();
        } else if (application.getStatusComment() != null) {
            remarks = application.getStatusComment();
        }
        if (remarks != null && remarks.length() > 100) {
            remarks = remarks.substring(0, 100);
        }
        offer.setOfferRemarks(remarks);

        // ========== MISSING FIELDS (26/30) ==========
        // TODO: Investigate data sources for the following fields:
        //
        // EMPLOYMENT DETAILS (7 fields):
        // - payBasis: Y=Yearly/H=Hourly/M=Monthly
        // - empDep: Department
        // - toa: Terms of Appointment (IC/IS/TMP/RSS)
        // - funcTitle: Functional Title
        // - band: Band
        // - grade: Job Grade
        // - post: Post
        //
        // CONTRACT DATES (2 fields):
        // - contractStartDate: Contract start date
        // - contractEndDate: Contract end date
        //
        // PROBATION PERIOD (2 fields):
        // - probationLength: Probation period length
        // - probationUnits: Y=Year/H=Hour/M=Month
        //
        // NOTICE PERIOD (2 fields):
        // - noticeLength: Notice period length
        // - noticeUnits: M=Months/D=Days
        //
        // COMPENSATION (2 fields):
        // - salaryAmount: Salary amount
        // - gratuityPct: Gratuity percentage
        //
        // EMPLOYMENT MODE (2 fields):
        // - empMode: F=Full-time/H=Half-time/P=Part-time
        // - planHours: Planned hours/days
        //
        // BENEFITS ELIGIBILITY (3 fields):
        // - mpf: Y/N (Mandatory Provident Fund)
        // - superannuation: Y/N
        // - pension: Y/N
        //
        // OTHER (2 fields):
        // - projectTitle: Project title (if applicable)
        //
        // POSSIBLE DATA SOURCES:
        // 1. Contract API - if application.getHasContracts() == true
        //    - Check for getContractByApplicationId() method in TalentLink SOAP API
        //    - Contract might contain: salary, dates, benefits, probation/notice periods
        //
        // 2. Position API - application.getPositionId() available
        //    - Check for getPositionById() method in TalentLink SOAP API
        //    - Position might contain: title, department, band, grade, employment mode
        //
        // 3. Structured Documents - Similar to Education/Work/PIF parsing
        //    - Look for documents like "Offer Letter", "Employment Contract", "Contract Details"
        //    - Parse structured document to extract offer details
        //
        // 4. Custom Fields - Check if ApplicationDto exposes custom field accessors
        //    - application.getCustomField("SALARY")
        //    - application.getCustomField("CONTRACT_START_DATE")
        //
        // See OFFER_API_DETAILS.md for complete investigation notes.

        // Audit fields
        offer.setCreatedBy("SYSTEM");
        offer.setUserstamp("SYSTEM");

        return offer;
    }

    /**
     * Validate offer data
     *
     * @param offer Offer model to validate
     * @return true if valid, false otherwise
     */
    public boolean validate(Offer offer) {
        if (offer == null) {
            return false;
        }

        // Check required fields
        if (offer.getOfferId() == null || offer.getOfferId().isEmpty()) {
            return false;
        }

        if (offer.getCandidateId() == null || offer.getCandidateId().isEmpty()) {
            return false;
        }

        return true;
    }
}
