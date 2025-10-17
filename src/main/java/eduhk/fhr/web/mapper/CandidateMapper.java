package eduhk.fhr.web.mapper;

// File Path: src/main/java/eduhk/fhr/web/mapper/CandidateMapper.java
// Purpose: Map TalentLink API candidate DTO to RDPS database model

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eduhk.fhr.web.dto.talentlink.TalentLinkCandidateDTO;
import eduhk.fhr.web.model.Candidate;

/**
 * Candidate Mapper
 *
 * Transforms TalentLink API candidate DTOs to RDPS database models.
 * Handles field mapping, data type conversion, and default values.
 */
@Component
public class CandidateMapper {

    @Autowired
    private LookupValueResolver lookupValueResolver;

    /**
     * Map TalentLink candidate DTO to RDPS Candidate model
     *
     * @param dto TalentLink candidate DTO
     * @return RDPS Candidate model
     */
    public Candidate mapToModel(TalentLinkCandidateDTO dto) {
        Candidate candidate = new Candidate();

        if (dto == null) {
            return candidate;
        }

        // Basic ID fields
        candidate.setCandidateId(dto.getId());
        candidate.setReqNumber(dto.getRequisitionNumber());

        // Personal information
        // candidate.setPostAppliedFor(dto.getPostAppliedFor());
        // candidate.setTitle(dto.getTitle());
        candidate.setFirstName(dto.getFirstname());
        candidate.setLastName(dto.getLastname());
        // candidate.setChineseName(dto.getChineseName());
        // candidate.setDateOfBirth(dto.getDateOfBirth());
        // candidate.setPermHK(dto.getPermanentHK());
        // candidate.setGender(dto.getGender());

        // Identity documents
        // candidate.setHkid(dto.getHkid());
        // candidate.setPassportNo(dto.getPassportNo());
        // candidate.setVisaRequired(dto.getVisaRequired());

        // Address information
        // candidate.setAddressLine1(dto.getAddressLine1());
        // candidate.setAddressLine2(dto.getAddressLine2());

        // Resolve district from lookup
        // NOTE: District is a REQUIRED field in database (NOT NULL constraint)
        // if (dto.getDistrict() != null && !dto.getDistrict().isEmpty()) {
        //     Integer districtKey = lookupValueResolver.resolveDistrict(dto.getDistrict());
        //     candidate.setDistrict(districtKey);
        // } else {
        //     // Set default district value to avoid NULL constraint violation
        //     // This should be validated and flagged as an error by CandidateValidationService
        //     candidate.setDistrict(null);  // Will be caught by validation
        // }

        // TEMPORARY FIX: Set district to null - validation service will catch this
        // TODO: Uncomment above code and implement proper district lookup once available
        candidate.setDistrict(null);

        // Contact information
        // candidate.setPhoneNumber(dto.getPhoneNumber());
        candidate.setEmail(dto.getEmail());

        // Audit fields
        candidate.setCreatedBy("SYSTEM");
        candidate.setUserstamp("SYSTEM");

        return candidate;
    }

    /**
     * Validate candidate data before database insertion
     *
     * @param candidate Candidate model to validate
     * @return true if valid, false otherwise
     */
    public boolean validate(Candidate candidate) {
        if (candidate == null) {
            return false;
        }

        // Check required fields
        if (candidate.getCandidateId() == null || candidate.getCandidateId().isEmpty()) {
            return false;
        }

        if (candidate.getReqNumber() == null || candidate.getReqNumber().isEmpty()) {
            return false;
        }

        // Optional: Add more validation rules
        // - Email format validation
        // - Date range validation
        // - Field length validation

        return true;
    }
}
