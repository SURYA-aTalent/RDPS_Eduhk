package eduhk.fhr.web.dto.talentlink;

// File Path: src/main/java/eduhk/fhr/web/dto/talentlink/TalentLinkPIFDTO.java
// Purpose: DTO representing Personal Information Form (PIF) data from TalentLink structured documents

import java.util.Date;

/**
 * TalentLink PIF DTO
 *
 * Data Transfer Object for Personal Information Form (Standard PIF) data
 * extracted from TalentLink structured documents.
 *
 * Maps fields from "EdUHK Standard PIF" document to candidate profile fields.
 */
public class TalentLinkPIFDTO {

    // Personal identifiers
    private String title;
    private String lastName;
    private String firstName;
    private String chineseName;
    private Date dateOfBirth;

    // Residency and demographics
    private String permanentResident;  // Yes/No
    private String gender;             // Male/Female

    // Identity documents
    private String hkidNumber;
    private String passportNumber;
    private String nationality;

    // Personal status
    private String maritalStatus;

    // Contact information
    private String residentialAddress;
    private String correspondenceAddress;
    private String homeTelephone;
    private String mobilePhone;
    private String email;

    // Getters and Setters

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getChineseName() {
        return chineseName;
    }

    public void setChineseName(String chineseName) {
        this.chineseName = chineseName;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPermanentResident() {
        return permanentResident;
    }

    public void setPermanentResident(String permanentResident) {
        this.permanentResident = permanentResident;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getHkidNumber() {
        return hkidNumber;
    }

    public void setHkidNumber(String hkidNumber) {
        this.hkidNumber = hkidNumber;
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getResidentialAddress() {
        return residentialAddress;
    }

    public void setResidentialAddress(String residentialAddress) {
        this.residentialAddress = residentialAddress;
    }

    public String getCorrespondenceAddress() {
        return correspondenceAddress;
    }

    public void setCorrespondenceAddress(String correspondenceAddress) {
        this.correspondenceAddress = correspondenceAddress;
    }

    public String getHomeTelephone() {
        return homeTelephone;
    }

    public void setHomeTelephone(String homeTelephone) {
        this.homeTelephone = homeTelephone;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "TalentLinkPIFDTO{" +
                "title='" + title + '\'' +
                ", lastName='" + lastName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", chineseName='" + chineseName + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", permanentResident='" + permanentResident + '\'' +
                ", gender='" + gender + '\'' +
                ", nationality='" + nationality + '\'' +
                ", maritalStatus='" + maritalStatus + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
