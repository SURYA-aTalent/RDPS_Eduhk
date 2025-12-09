package eduhk.fhr.web.service;

import java.util.ArrayList;
import java.util.List;

import eduhk.fhr.web.dto.talentlink.TalentLinkEducationDTO;
import eduhk.fhr.web.dto.talentlink.TalentLinkWorkExperienceDTO;
import eduhk.fhr.web.dto.talentlink.TalentLinkRefereeDTO;
import eduhk.fhr.web.dto.talentlink.TalentLinkOtherInfoDTO;

/**
 * Helper class to hold parsed candidate data from structured documents
 *
 * This class aggregates education, work experience, referee, and other info
 * extracted from TalentLink application-specific documents.
 */
public class ParsedCandidateData {

    private List<TalentLinkEducationDTO> education;
    private List<TalentLinkWorkExperienceDTO> workExperience;
    private List<TalentLinkRefereeDTO> referees;
    private TalentLinkOtherInfoDTO otherInfo;

    public ParsedCandidateData() {
        this.education = new ArrayList<>();
        this.workExperience = new ArrayList<>();
        this.referees = new ArrayList<>();
    }

    // Getters
    public List<TalentLinkEducationDTO> getEducation() {
        return education;
    }

    public List<TalentLinkWorkExperienceDTO> getWorkExperience() {
        return workExperience;
    }

    public List<TalentLinkRefereeDTO> getReferees() {
        return referees;
    }

    public TalentLinkOtherInfoDTO getOtherInfo() {
        return otherInfo;
    }

    // Setters
    public void setEducation(List<TalentLinkEducationDTO> education) {
        this.education = education;
    }

    public void setWorkExperience(List<TalentLinkWorkExperienceDTO> workExperience) {
        this.workExperience = workExperience;
    }

    public void setReferees(List<TalentLinkRefereeDTO> referees) {
        this.referees = referees;
    }

    public void setOtherInfo(TalentLinkOtherInfoDTO otherInfo) {
        this.otherInfo = otherInfo;
    }

    // Helper methods to add individual items
    public void addEducation(TalentLinkEducationDTO edu) {
        if (edu != null) {
            this.education.add(edu);
        }
    }

    public void addWorkExperience(TalentLinkWorkExperienceDTO workExp) {
        if (workExp != null) {
            this.workExperience.add(workExp);
        }
    }

    public void addReferee(TalentLinkRefereeDTO referee) {
        if (referee != null) {
            this.referees.add(referee);
        }
    }

    // Helper methods to check if data exists
    public boolean hasEducation() {
        return education != null && !education.isEmpty();
    }

    public boolean hasWorkExperience() {
        return workExperience != null && !workExperience.isEmpty();
    }

    public boolean hasReferees() {
        return referees != null && !referees.isEmpty();
    }

    public boolean hasOtherInfo() {
        return otherInfo != null;
    }

    @Override
    public String toString() {
        return String.format("ParsedCandidateData[education=%d, workExperience=%d, referees=%d, hasOtherInfo=%s]",
            education != null ? education.size() : 0,
            workExperience != null ? workExperience.size() : 0,
            referees != null ? referees.size() : 0,
            hasOtherInfo());
    }
}
