# RDPS Field Mapping Implementation Report

**Generated:** 2025-11-28
**Project:** EdUHK RDPS - Recruitment Data Processing System
**Source:** All fields in iRecruitment - inHouse_20251013_TLK_Andy Update 20251028.xlsx

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Current Implementation Status](#current-implementation-status)
3. [Completed/Partially Complete Components](#completedpartially-complete-components)
4. [Missing Components](#missing-components)
5. [Detailed Development Plan](#detailed-development-plan)
6. [Technical Debt & Risks](#technical-debt--risks)
7. [Appendix: Field Mapping Reference](#appendix-field-mapping-reference)

---

## Executive Summary

### Overall Status

| Metric | Count | Percentage |
|--------|-------|------------|
| **Total Entities in Spec** | 16 | 100% |
| **Implemented Entities** | 5 | 31.25% |
| **Missing Entities** | 11 | 68.75% |
| **Total Fields in Spec** | 277 | 100% |
| **Average Completion (Implemented)** | - | 45.7% |

### Key Findings

- ✅ Core candidate import is partially functional (Candidate, Education, Work Experience, Referee, Other Info)
- ❌ Onboarding system is completely missing (Person Info, Spouse, Child, Emergency Contact)
- ❌ Offer management system is not implemented
- ❌ Benefits management is not implemented
- ❌ Bank info and photo upload are missing
- ❌ EBS integration table is missing

### Estimated Effort to Complete

| Phase | Description | Effort |
|-------|-------------|--------|
| Phase 1 | Complete existing mappers | 3-4 days |
| Phase 2 | Offer management | 2-3 days |
| Phase 3 | Onboarding system | 6-8 days |
| Phase 4 | Benefits & payroll | 4-6 days |
| Phase 5 | Integration & attachments | 3-4 days |
| **TOTAL** | | **18-25 days** |

---

## Current Implementation Status

### Implementation Matrix

| Entity | DB Table | Model Class | DAO Class | Mapper Class | Import Service | Completion |
|--------|----------|-------------|-----------|--------------|----------------|------------|
| **Candidate** | ✅ | ✅ | ✅ | ✅ | ✅ | 35% |
| **EduProfQual** | ✅ | ✅ | ✅ | ✅ | ✅ | 38% |
| **WorkExp** | ✅ | ✅ | ✅ | ✅ | ✅ | 58% |
| **OtherInfo** | ✅ | ✅ | ✅ | ✅ | ✅ | 27% |
| **Referee** | ✅ | ✅ | ✅ | ✅ | ✅ | 70% |
| Offer | ❌ | ❌ | ❌ | ❌ | ❌ | 0% |
| PersonInfo | ❌ | ❌ | ❌ | ❌ | ❌ | 0% |
| PersonSpouse | ❌ | ❌ | ❌ | ❌ | ❌ | 0% |
| PersonChild | ❌ | ❌ | ❌ | ❌ | ❌ | 0% |
| PersonECP | ❌ | ❌ | ❌ | ❌ | ❌ | 0% |
| PhotoUpload | ❌ | ❌ | ❌ | ❌ | ❌ | 0% |
| BankInfo | ❌ | ❌ | ❌ | ❌ | ❌ | 0% |
| BenefitMain | ❌ | ❌ | ❌ | ❌ | ❌ | 0% |
| BenefitSpouse | ❌ | ❌ | ❌ | ❌ | ❌ | 0% |
| BenefitChild | ❌ | ❌ | ❌ | ❌ | ❌ | 0% |
| ToEBS | ❌ | ❌ | ❌ | ❌ | ❌ | 0% |

---

## Completed/Partially Complete Components

### 1. Candidate Entity (35% Complete)

#### Existing Files

**Database Schema:**
- **File:** `db_scripts/rdps_candidate.sql`
- **Status:** ✅ Complete - All 20 fields defined
- **Key fields:** CANDIDATE_ID (PK), REQ_NUMBER, FIRST_NAME, LAST_NAME, EMAIL, etc.

**Model Class:**
- **File:** `src/main/java/eduhk/fhr/web/model/Candidate.java`
- **Status:** ✅ Complete - All fields defined with getters/setters
- **Lines:** 217
- **Fields:** 20 properties matching database schema

**DAO Class:**
- **File:** `src/main/java/eduhk/fhr/web/dao/CandidateDAO.java`
- **Status:** ✅ Complete
- **Methods:**
  - `insert(Candidate)` - Insert candidate record
  - `findById(String)` - Find by candidate ID
  - `update(Candidate)` - Update candidate record
  - Database operations use JdbcTemplate

**Mapper Class:**
- **File:** `src/main/java/eduhk/fhr/web/mapper/CandidateMapper.java`
- **Status:** ⚠️ **PARTIAL** - Many fields commented out
- **Lines:** 113
- **Implemented Logic:**
  ```java
  public Candidate mapToModel(TalentLinkCandidateDTO dto) {
      Candidate candidate = new Candidate();

      // Currently mapped (ONLY 7 fields):
      candidate.setCandidateId(dto.getId());
      candidate.setReqNumber(dto.getRequisitionNumber());
      candidate.setFirstName(dto.getFirstname());
      candidate.setLastName(dto.getLastname());
      candidate.setEmail(dto.getEmail());
      candidate.setCreatedBy("SYSTEM");
      candidate.setUserstamp("SYSTEM");

      // COMMENTED OUT (13 fields):
      // candidate.setPostAppliedFor(dto.getPostAppliedFor());
      // candidate.setTitle(dto.getTitle());
      // candidate.setChineseName(dto.getChineseName());
      // candidate.setDateOfBirth(dto.getDateOfBirth());
      // candidate.setPermHK(dto.getPermanentHK());
      // candidate.setGender(dto.getGender());
      // candidate.setHkid(dto.getHkid());
      // candidate.setPassportNo(dto.getPassportNo());
      // candidate.setVisaRequired(dto.getVisaRequired());
      // candidate.setAddressLine1(dto.getAddressLine1());
      // candidate.setAddressLine2(dto.getAddressLine2());
      // candidate.setDistrict(lookupValueResolver.resolveDistrict(...));
      // candidate.setPhoneNumber(dto.getPhoneNumber());

      return candidate;
  }
  ```

**Import Service:**
- **File:** `src/main/java/eduhk/fhr/web/service/import_/CandidateImportService.java`
- **Status:** ✅ Complete
- **Methods:**
  - `importCandidate(TalentLinkCandidateDTO)` - Orchestrates import
  - Uses CandidateMapper and CandidateDAO

**Missing Field Mappings (13):**
1. `POST_APPLIED_FOR` ← Post applied for
2. `TITLE` ← Title
3. `CHINESE_NAME` ← Name in Chinese (If any)
4. `DATE_OF_BIRTH` ← Date of Birth
5. `PERM_HK` ← Permanent Resident in HK
6. `GENDER` ← Gender
7. `HKID` ← HKID No.
8. `PASSPORT` ← Passport No.
9. `VISA_REQUIRED` ← Need Visa to Work in HK
10. `ADDR_LINE1` ← Address (line 1)
11. `ADDR_LINE2` ← Address (line 2)
12. `DISTRICT` ← District
13. `PHONE_NO` ← Mobile No.

---

### 2. Education/Professional Qualifications Entity (38% Complete)

#### Existing Files

**Database Schema:**
- **File:** `db_scripts/rdps_edu_prof_qual.sql`
- **Status:** ✅ Complete - All 17 fields defined
- **Key fields:** CANDIDATE_ID, REQ_NUMBER, SEQ, INSTITUTION, COUNTRY, EDU_LEVEL, etc.

**Model Class:**
- **File:** `src/main/java/eduhk/fhr/web/model/Education.java`
- **Status:** ✅ Complete - All fields defined
- **Lines:** 171
- **Fields:** 17 properties including LOV references (eduLevel, studyMode, qualAwardDesc, qualAwardClass)

**DAO Class:**
- **File:** `src/main/java/eduhk/fhr/web/dao/EducationDAO.java`
- **Status:** ✅ Complete
- **Methods:**
  - `insert(Education)` - Insert education record
  - `findByCandidateId(String)` - Find all education records for candidate
  - `deleteByCandidate(String)` - Delete education records

**Mapper Class:**
- **File:** `src/main/java/eduhk/fhr/web/mapper/EducationMapper.java`
- **Status:** ⚠️ **PARTIAL** - Basic structure with TODOs
- **Lines:** 66
- **Implemented Logic:**
  ```java
  public Education mapToModel(TalentLinkEducationDTO dto, String candidateId,
                               String reqNumber, int seq) {
      Education education = new Education();

      // Currently mapped (ONLY 5 fields):
      education.setCandidateId(candidateId);
      education.setReqNumber(reqNumber);
      education.setSeq(seq);
      education.setInstitution(dto.getInstitution());
      education.setCountry(dto.getCountry());

      // TODO: Resolve LOV keys
      // education.setEduLevel(lookupValueResolver.resolveEducationLevel(...));
      // education.setStudyMode(lookupValueResolver.resolveStudyMode(...));
      // education.setQualAwardDesc(lookupValueResolver.resolveQualAwardDesc(...));
      // education.setQualAwardClass(lookupValueResolver.resolveQualAwardClass(...));

      // TODO: Map remaining fields
      // education.setOthers(dto.getOthers());
      // education.setMajorStudyArea(dto.getMajorStudyArea());
      // education.setStartDate(dto.getStartDate());
      // education.setDateOfAward(dto.getDateOfAward());

      return education;
  }
  ```

**Import Service:**
- **File:** `src/main/java/eduhk/fhr/web/service/import_/EducationImportService.java`
- **Status:** ✅ Complete
- **Methods:**
  - `importEducation(String candidateId, List<TalentLinkEducationDTO>)`

**Missing Field Mappings (8):**
1. `EDU_LEVEL` ← Education Level (requires LOV lookup)
2. `STUDY_MODE` ← Full Time or Part Time (requires LOV lookup)
3. `QUAL_AWARD_DESC` ← Qualification Award Description (requires LOV lookup)
4. `QUAL_AWARD_CLASS` ← Qualification Award Classification (requires LOV lookup)
5. `OTHERS` ← Others
6. `MAJOR_STUDY_AREA` ← Degree Information: Name
7. `START_DATE` ← Start Date
8. `DATE_OF_AWARD` ← Date of Award

---

### 3. Work Experience Entity (58% Complete)

#### Existing Files

**Database Schema:**
- **File:** `db_scripts/rdps_work_experience.sql`
- **Status:** ✅ Complete - All 16 fields defined

**Model Class:**
- **File:** `src/main/java/eduhk/fhr/web/model/WorkExperience.java`
- **Status:** ✅ Complete
- **Lines:** 162
- **Fields:** 16 properties

**DAO Class:**
- **File:** `src/main/java/eduhk/fhr/web/dao/WorkExperienceDAO.java`
- **Status:** ✅ Complete

**Mapper Class:**
- **File:** `src/main/java/eduhk/fhr/web/mapper/WorkExperienceMapper.java`
- **Status:** ⚠️ **PARTIAL**
- **Lines:** 62
- **Implemented Logic:**
  ```java
  public WorkExperience mapToModel(TalentLinkWorkExperienceDTO dto,
                                    String candidateId, String reqNumber, int seq) {
      WorkExperience workExp = new WorkExperience();

      // Currently mapped (7 fields):
      workExp.setCandidateId(candidateId);
      workExp.setReqNumber(reqNumber);
      workExp.setSeq(seq);
      workExp.setEmployerName(dto.getEmployerName());
      workExp.setPositionTitle(dto.getPositionTitle());
      workExp.setStartDate(dto.getStartDate());
      workExp.setEndDate(dto.getEndDate());

      // TODO: Map remaining fields

      return workExp;
  }
  ```

**Import Service:**
- **File:** `src/main/java/eduhk/fhr/web/service/import_/WorkExperienceImportService.java`
- **Status:** ✅ Complete

**Missing Field Mappings (5):**
1. `NATURE_OF_BUSINESS` ← Nature of Business
2. `CURRENT_JOB` ← Current
3. `MODE_OF_EMPLOYMENT` ← Type
4. `HOURS_PER_WEEK` ← Number of hour engaged per week
5. `NATURE_OF_DUTIES` ← Position Type

---

### 4. Other Info Entity (27% Complete)

#### Existing Files

**Database Schema:**
- **File:** `db_scripts/rdps_other_info.sql`
- **Status:** ✅ Complete - All 26 fields defined

**Model Class:**
- **File:** `src/main/java/eduhk/fhr/web/model/OtherInfo.java`
- **Status:** ✅ Complete
- **Lines:** 253
- **Fields:** 26 properties including BigDecimal for financial fields

**DAO Class:**
- **File:** `src/main/java/eduhk/fhr/web/dao/OtherInfoDAO.java`
- **Status:** ✅ Complete

**Mapper Class:**
- **File:** `src/main/java/eduhk/fhr/web/mapper/OtherInfoMapper.java`
- **Status:** ⚠️ **PARTIAL**
- **Lines:** 60
- **Implemented Logic:**
  ```java
  public OtherInfo mapToModel(TalentLinkOtherInfoDTO dto,
                               String candidateId, String reqNumber) {
      OtherInfo otherInfo = new OtherInfo();

      // Currently mapped (6 fields):
      otherInfo.setCandidateId(candidateId);
      otherInfo.setReqNumber(reqNumber);
      otherInfo.setSalary(dto.getSalary());
      otherInfo.setExpectedSalary(dto.getExpectedSalary());
      otherInfo.setSkills(dto.getSkills());
      otherInfo.setNoticePeriod(dto.getNoticePeriod());

      // TODO: Map remaining 16 fields

      return otherInfo;
  }
  ```

**Import Service:**
- **File:** `src/main/java/eduhk/fhr/web/service/import_/OtherInfoImportService.java`
- **Status:** ✅ Complete

**Missing Field Mappings (16):**
1. `NUMBER_OF_MONTHS` ← Number of months
2. `ALLOWANCE_AMOUNT` ← Allowance / Bonus Amount
3. `ALLOWANCE_PCT` ← Allowance / Bonus - % of Basic Salary
4. `ALLOWANCE_TYPE` ← Allowance / Bonus - By Month / Year
5. `ALLOWANCE_NATURE` ← Nature of Allowance / Bonus
6. `GRATUITY_AMOUNT` ← Contract-end Gratuity Amount
7. `GRATUITY_BASIC` ← Contract-end Gratuity - % of Basic Monthly Salary
8. `OTHER_BENEFITS` ← Other Allowances and Fringe Benefits
9. `NEXT_SALARY_REVIEW_DATE` ← Next Salary Review Date
10. `DATE_OF_AVAILABILITY` ← Date of Availability
11. `EDUHK_EMPLOYEE` ← EdUHK Employee
12. `STAFF_NUMBER` ← Staff Number
13. `SOURCE_TYPE` ← Source Type
14. `SOURCE_TYPE_OTHER` ← Source Type - Other
15. `DCCPR` ← DCCPR
16. `DCCPR_DETAILS` ← DCCPR - Details

---

### 5. Referee Entity (70% Complete) ⭐ BEST COMPLETION

#### Existing Files

**Database Schema:**
- **File:** `db_scripts/rdps_referee.sql`
- **Status:** ✅ Complete - All 14 fields defined

**Model Class:**
- **File:** `src/main/java/eduhk/fhr/web/model/Referee.java`
- **Status:** ✅ Complete
- **Lines:** 144
- **Fields:** 14 properties

**DAO Class:**
- **File:** `src/main/java/eduhk/fhr/web/dao/RefereeDAO.java`
- **Status:** ✅ Complete

**Mapper Class:**
- **File:** `src/main/java/eduhk/fhr/web/mapper/RefereeMapper.java`
- **Status:** ⚠️ **MOSTLY COMPLETE** (70%)
- **Lines:** 60
- **Implemented Logic:**
  ```java
  public Referee mapToModel(TalentLinkRefereeDTO dto,
                             String candidateId, String reqNumber, int seq) {
      Referee referee = new Referee();

      // Currently mapped (7 fields):
      referee.setCandidateId(candidateId);
      referee.setReqNumber(reqNumber);
      referee.setSeq(seq);
      referee.setTitle(dto.getTitle());
      referee.setFirstName(dto.getFirstname());
      referee.setLastName(dto.getLastname());
      referee.setEmail(dto.getEmail());

      // TODO: Map remaining 3 fields
      // referee.setPositionTitle(dto.getPositionTitle());
      // referee.setPhoneNumber(dto.getPhoneNumber());
      // referee.setRelationship(dto.getRelationship());

      return referee;
  }
  ```

**Import Service:**
- **File:** `src/main/java/eduhk/fhr/web/service/import_/RefereeImportService.java`
- **Status:** ✅ Complete

**Missing Field Mappings (3 - EASIEST TO COMPLETE):**
1. `POSITION_TITLE` ← Referee 1: Position
2. `PHONE_NUMBER` ← Referee 1: Tel. No.
3. `RELATIONSHIP` ← Relationship

---

### 6. Supporting Infrastructure

#### Lookup Value Resolver (CRITICAL - NOT IMPLEMENTED)

**File:** `src/main/java/eduhk/fhr/web/mapper/LookupValueResolver.java`
- **Status:** ⚠️ **PARTIAL/STUB** - Class exists but methods not implemented
- **Required Methods:**
  ```java
  public Integer resolveDistrict(String districtName);
  public Integer resolveEducationLevel(String eduLevelName);
  public Integer resolveStudyMode(String studyModeName);
  public Integer resolveQualAwardDesc(String qualAwardDescName);
  public Integer resolveQualAwardClass(String qualAwardClassName);
  ```
- **Required LOV Tables (Already exist in DB):**
  - `RDPS_LOV_DISTRICT` - 24 districts
  - `RDPS_LOV_EDU_LEVEL` - 14 education levels
  - `RDPS_LOV_STUDY_MODE` - 7 study modes
  - `RDPS_LOV_QUAL_AWARD_DESC` - 35 award descriptions
  - `RDPS_LOV_QUAL_AWARD_CLASS` - 16 award classes

#### Import Orchestration

**File:** `src/main/java/eduhk/fhr/web/service/import_/ImportOrchestrationService.java`
- **Status:** ✅ Complete
- **Flow:**
  1. Fetch candidates from TalentLink SOAP API
  2. Validate candidate data
  3. Import candidate → education → work experience → referee → other info
  4. Log import history
  5. Send notification email

#### Validation Service

**File:** `src/main/java/eduhk/fhr/web/service/import_/CandidateValidationService.java`
- **Status:** ✅ Complete
- **Validates:** Required fields, data types, foreign key references

---

## Missing Components

### 1. Offer Management System (0% - HIGH PRIORITY)

**Required Components:**

| Component | File Path | Status | Fields |
|-----------|-----------|--------|--------|
| Database Schema | `db_scripts/rdps_offer.sql` | ❌ Missing | 30 |
| Model Class | `src/main/java/eduhk/fhr/web/model/Offer.java` | ❌ Missing | - |
| DAO Class | `src/main/java/eduhk/fhr/web/dao/OfferDAO.java` | ❌ Missing | - |
| Mapper Class | `src/main/java/eduhk/fhr/web/mapper/OfferMapper.java` | ❌ Missing | - |
| Import Service | `src/main/java/eduhk/fhr/web/service/import_/OfferImportService.java` | ❌ Missing | - |

**Key Fields Required:**
- OFFER_ID, REQ_NUMBER, CANDIDATE_ID, OFFER_STATUS
- PAY_BASIS, EMP_DEP, TOA, FUNC_TITLE, BAND, GRADE, POST
- CONTRACT_START_DATE, CONTRACT_END_DATE
- PROBATION_LENGTH, PROBATION_UNITS, NOTICE_LENGTH, NOTICE_UNITS
- SALARY_AMOUNT, GRATUITY_PCT, EMP_MODE, PLAN_HOURS
- MPF, SUPERANNUATION, PENSION flags

---

### 2. Onboarding System (0% - MEDIUM PRIORITY)

#### Person Info Entity

**Required Components:**

| Component | File Path | Status | Fields |
|-----------|-----------|--------|--------|
| Database Schema | `db_scripts/rdps_person_info.sql` | ❌ Missing | 34 |
| Model Class | `src/main/java/eduhk/fhr/web/model/PersonInfo.java` | ❌ Missing | - |
| DAO Class | `src/main/java/eduhk/fhr/web/dao/PersonInfoDAO.java` | ❌ Missing | - |
| Mapper Class | `src/main/java/eduhk/fhr/web/mapper/PersonInfoMapper.java` | ❌ Missing | - |
| Import Service | `src/main/java/eduhk/fhr/web/service/import_/PersonInfoImportService.java` | ❌ Missing | - |

**Key Fields Required:**
- OFFER_ID, REQ_NUMBER
- TITLE, FIRST_NAME, LAST_NAME, SUFFIX_NAME, CHINESE_NAME
- GENDER, DATE_OF_BIRTH, HKID, PASSPORT, COUNTRY_OF_ISSUE
- MARITAL_STATUS, PERM_RESIDENT, NATIONALITY, PLACE_OF_ORIGIN
- ADDRESS_LINE1, ADDRESS_LINE2, ADDRESS_LINE3, DISTRICT, AREA_CODE
- HOME_NUMBER, MOBILE_NUMBER, EMAIL
- PERMIT_NUMBER, PERMIT_WORK_EXPIRY, PERMIT_ONEWAY_EXPIRY

#### Person Spouse Entity

**Required Components:**

| Component | File Path | Status | Fields |
|-----------|-----------|--------|--------|
| Database Schema | `db_scripts/rdps_person_spouse.sql` | ❌ Missing | 20 |
| Model Class | `src/main/java/eduhk/fhr/web/model/PersonSpouse.java` | ❌ Missing | - |
| DAO Class | `src/main/java/eduhk/fhr/web/dao/PersonSpouseDAO.java` | ❌ Missing | - |
| Mapper Class | `src/main/java/eduhk/fhr/web/mapper/PersonSpouseMapper.java` | ❌ Missing | - |

#### Person Child Entity

**Required Components:**

| Component | File Path | Status | Fields |
|-----------|-----------|--------|--------|
| Database Schema | `db_scripts/rdps_person_child.sql` | ❌ Missing | 21 |
| Model Class | `src/main/java/eduhk/fhr/web/model/PersonChild.java` | ❌ Missing | - |
| DAO Class | `src/main/java/eduhk/fhr/web/dao/PersonChildDAO.java` | ❌ Missing | - |
| Mapper Class | `src/main/java/eduhk/fhr/web/mapper/PersonChildMapper.java` | ❌ Missing | - |

**Note:** Supports up to 6 children per employee (CHILD_SEQ 1-6)

#### Person Emergency Contact (ECP) Entity

**Required Components:**

| Component | File Path | Status | Fields |
|-----------|-----------|--------|--------|
| Database Schema | `db_scripts/rdps_person_ecp.sql` | ❌ Missing | 11 |
| Model Class | `src/main/java/eduhk/fhr/web/model/PersonECP.java` | ❌ Missing | - |
| DAO Class | `src/main/java/eduhk/fhr/web/dao/PersonECPDAO.java` | ❌ Missing | - |
| Mapper Class | `src/main/java/eduhk/fhr/web/mapper/PersonECPMapper.java` | ❌ Missing | - |

---

### 3. Benefits Management System (0% - MEDIUM PRIORITY)

#### Bank Info Entity

**Required Components:**

| Component | File Path | Status | Fields |
|-----------|-----------|--------|--------|
| Database Schema | `db_scripts/rdps_bank_info.sql` | ❌ Missing | 9 |
| Model Class | `src/main/java/eduhk/fhr/web/model/BankInfo.java` | ❌ Missing | - |
| DAO Class | `src/main/java/eduhk/fhr/web/dao/BankInfoDAO.java` | ❌ Missing | - |
| Mapper Class | `src/main/java/eduhk/fhr/web/mapper/BankInfoMapper.java` | ❌ Missing | - |

**Key Fields:** BANK_CODE, BRANCH_CODE, ACCOUNT_NO, ACCOUNT_NAME

#### Benefit Main/Spouse/Child Entities

Similar structure for:
- `RDPS_BENEFIT_MAIN` (9 fields)
- `RDPS_BENEFIT_SPOUSE` (17 fields)
- `RDPS_BENEFIT_CHILD` (12 fields)

**Benefit Fields:** MEDICAL_BENEFIT, DENTAL_BENEFIT, HOUSING_BENEFIT, PREVENTIVE_CHECKUP

---

### 4. Attachments & Integration (0% - LOW-MEDIUM PRIORITY)

#### Photo Upload Entity

**Required Components:**

| Component | File Path | Status | Fields |
|-----------|-----------|--------|--------|
| Database Schema | `db_scripts/rdps_photo_upload.sql` | ❌ Missing | 7 |
| Model Class | `src/main/java/eduhk/fhr/web/model/PhotoUpload.java` | ❌ Missing | - |
| DAO Class | `src/main/java/eduhk/fhr/web/dao/PhotoUploadDAO.java` | ❌ Missing | - |

**Key Fields:** EMP_PHOTO (BLOB), DEP_PHOTO (BLOB)

**Note:** Requires BLOB handling and file upload infrastructure

#### ToEBS Integration Entity

**Required Components:**

| Component | File Path | Status | Fields |
|-----------|-----------|--------|--------|
| Database Schema | `db_scripts/rdps_toebs.sql` | ❌ Missing | 9 |
| Model Class | `src/main/java/eduhk/fhr/web/model/ToEBS.java` | ❌ Missing | - |
| DAO Class | `src/main/java/eduhk/fhr/web/dao/ToEBSDAO.java` | ❌ Missing | - |

**Key Fields:** CANDIDATE_ID, REQ_NUMBER, EBS_READY, EMPLOYEE_NUMBER, ASSIGNMENT_NUMBER, STAGE

**Purpose:** Staging table for transferring data to EBS (Oracle E-Business Suite)

---

## Detailed Development Plan

### Phase 1: Complete Existing Mappers (HIGH PRIORITY)

**Estimated Effort:** 3-4 days
**Priority:** 🔴 Critical
**Goal:** Achieve 100% field mapping for all 5 implemented entities

#### Task 1.1: Complete Referee Mapper ⭐ QUICK WIN
**Effort:** 1-2 hours
**File:** `src/main/java/eduhk/fhr/web/mapper/RefereeMapper.java`

**Steps:**
1. Add missing field mappings (3 fields):
   ```java
   referee.setPositionTitle(dto.getPositionTitle());
   referee.setPhoneNumber(dto.getPhoneNumber());
   referee.setRelationship(dto.getRelationship());
   ```
2. Test with sample data
3. Verify database insertion

**Acceptance Criteria:**
- All 10 Referee fields mapped
- Unit tests pass
- Integration test with TalentLink DTO succeeds

---

#### Task 1.2: Complete Work Experience Mapper
**Effort:** 2-3 hours
**File:** `src/main/java/eduhk/fhr/web/mapper/WorkExperienceMapper.java`

**Steps:**
1. Add missing field mappings (5 fields):
   ```java
   workExp.setNatureOfBusiness(dto.getNatureOfBusiness());
   workExp.setCurrentJob(dto.getCurrentJob()); // Y/N
   workExp.setModeOfEmployment(dto.getModeOfEmployment());
   workExp.setHoursPerWeek(dto.getHoursPerWeek());
   workExp.setNatureOfDuties(dto.getNatureOfDuties());
   ```
2. Verify DTO field names match TalentLink SOAP response
3. Test with sample data
4. Verify database insertion

**Acceptance Criteria:**
- All 12 WorkExperience fields mapped
- Unit tests pass
- Integration test succeeds

---

#### Task 1.3: Implement LookupValueResolver (CRITICAL DEPENDENCY)
**Effort:** 4-6 hours
**File:** `src/main/java/eduhk/fhr/web/mapper/LookupValueResolver.java`

**Steps:**

1. **Create DAO classes for LOV tables:**
   ```java
   // File: src/main/java/eduhk/fhr/web/dao/DistrictLOVDAO.java
   @Repository
   public class DistrictLOVDAO extends BaseDao {
       public Integer findKeyByValue(String districtName) {
           String sql = "SELECT KEY FROM RDPS_LOV_DISTRICT WHERE UPPER(VALUE) = UPPER(?)";
           try {
               return jdbcTemplate.queryForObject(sql, Integer.class, districtName);
           } catch (EmptyResultDataAccessException e) {
               return null; // Return null for "Not Specified" or default value
           }
       }
   }

   // Similar DAOs for:
   // - EducationLevelLOVDAO
   // - StudyModeLOVDAO
   // - QualAwardDescLOVDAO
   // - QualAwardClassLOVDAO
   ```

2. **Implement LookupValueResolver:**
   ```java
   @Component
   public class LookupValueResolver {

       @Autowired
       private DistrictLOVDAO districtLOVDAO;

       @Autowired
       private EducationLevelLOVDAO educationLevelLOVDAO;

       @Autowired
       private StudyModeLOVDAO studyModeLOVDAO;

       @Autowired
       private QualAwardDescLOVDAO qualAwardDescLOVDAO;

       @Autowired
       private QualAwardClassLOVDAO qualAwardClassLOVDAO;

       public Integer resolveDistrict(String districtName) {
           if (districtName == null || districtName.trim().isEmpty()) {
               return 1; // Default: "Not Specified"
           }
           Integer key = districtLOVDAO.findKeyByValue(districtName);
           return key != null ? key : 1; // Default if not found
       }

       public Integer resolveEducationLevel(String eduLevelName) {
           if (eduLevelName == null || eduLevelName.trim().isEmpty()) {
               return 1; // Default: "Not Specified"
           }
           Integer key = educationLevelLOVDAO.findKeyByValue(eduLevelName);
           return key != null ? key : 1;
       }

       // Similar methods for other LOVs...
   }
   ```

3. **Add caching for performance:**
   ```java
   @Cacheable("districtLOV")
   public Integer resolveDistrict(String districtName) {
       // ... implementation
   }
   ```

4. **Write unit tests:**
   - Test with valid values
   - Test with null/empty values
   - Test with invalid values (should return default)
   - Test case-insensitive matching

**Acceptance Criteria:**
- All 5 LOV resolution methods implemented
- Caching enabled for performance
- Unit tests coverage > 80%
- Integration tests with database pass

---

#### Task 1.4: Complete Education Mapper
**Effort:** 4-6 hours (depends on Task 1.3)
**File:** `src/main/java/eduhk/fhr/web/mapper/EducationMapper.java`

**Steps:**
1. Implement LOV field mappings (4 fields):
   ```java
   education.setEduLevel(lookupValueResolver.resolveEducationLevel(dto.getEducationLevel()));
   education.setStudyMode(lookupValueResolver.resolveStudyMode(dto.getStudyMode()));
   education.setQualAwardDesc(lookupValueResolver.resolveQualAwardDesc(dto.getQualAwardDesc()));
   education.setQualAwardClass(lookupValueResolver.resolveQualAwardClass(dto.getQualAwardClass()));
   ```

2. Add remaining field mappings (4 fields):
   ```java
   education.setOthers(dto.getOthers());
   education.setMajorStudyArea(dto.getMajorStudyArea());
   education.setStartDate(dto.getStartDate());
   education.setDateOfAward(dto.getDateOfAward());
   ```

3. Add validation logic:
   ```java
   public boolean validate(Education education) {
       if (education.getCandidateId() == null) return false;
       if (education.getInstitution() == null) return false;
       if (education.getEduLevel() == null) return false;
       // ... additional validation
       return true;
   }
   ```

4. Test with sample data
5. Verify LOV lookups work correctly

**Acceptance Criteria:**
- All 13 Education fields mapped
- LOV resolution working
- Validation logic implemented
- Unit tests pass
- Integration tests succeed

---

#### Task 1.5: Complete Candidate Mapper
**Effort:** 4-6 hours (depends on Task 1.3)
**File:** `src/main/java/eduhk/fhr/web/mapper/CandidateMapper.java`

**Steps:**
1. **Uncomment and verify all existing code (13 fields)**
2. **Implement district LOV resolution:**
   ```java
   if (dto.getDistrict() != null && !dto.getDistrict().isEmpty()) {
       Integer districtKey = lookupValueResolver.resolveDistrict(dto.getDistrict());
       candidate.setDistrict(districtKey);
   } else {
       candidate.setDistrict(1); // Default: "Not Specified"
   }
   ```

3. **Verify DTO field names:**
   - Check TalentLink SOAP response structure
   - Ensure DTO getters match actual field names
   - Update if necessary

4. **Add null safety:**
   ```java
   candidate.setPostAppliedFor(dto.getPostAppliedFor());
   candidate.setTitle(dto.getTitle());
   candidate.setChineseName(dto.getChineseName());
   candidate.setDateOfBirth(dto.getDateOfBirth());
   candidate.setPermHK(dto.getPermanentHK()); // Y/N
   candidate.setGender(dto.getGender()); // F/M
   candidate.setHkid(dto.getHkid());
   candidate.setPassportNo(dto.getPassportNo());
   candidate.setVisaRequired(dto.getVisaRequired()); // Y/N
   candidate.setAddressLine1(dto.getAddressLine1());
   candidate.setAddressLine2(dto.getAddressLine2());
   candidate.setPhoneNumber(dto.getPhoneNumber());
   ```

5. **Add validation logic:**
   ```java
   public boolean validate(Candidate candidate) {
       if (candidate.getCandidateId() == null || candidate.getCandidateId().isEmpty()) {
           return false;
       }
       if (candidate.getReqNumber() == null || candidate.getReqNumber().isEmpty()) {
           return false;
       }
       if (candidate.getEmail() == null || candidate.getEmail().isEmpty()) {
           return false;
       }
       // Email format validation
       if (!candidate.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
           return false;
       }
       return true;
   }
   ```

6. Test thoroughly with sample data

**Acceptance Criteria:**
- All 20 Candidate fields mapped
- District LOV resolution working
- Validation logic complete
- Unit tests pass
- Integration tests succeed
- No commented-out code remains

---

#### Task 1.6: Complete Other Info Mapper
**Effort:** 6-8 hours
**File:** `src/main/java/eduhk/fhr/web/mapper/OtherInfoMapper.java`

**Steps:**
1. Add all 16 missing field mappings:
   ```java
   // Salary/Compensation fields
   otherInfo.setNumberOfMonths(dto.getNumberOfMonths());
   otherInfo.setAllowanceAmount(dto.getAllowanceAmount());
   otherInfo.setAllowancePct(dto.getAllowancePct());
   otherInfo.setAllowanceType(dto.getAllowanceType()); // Year/Month
   otherInfo.setAllowanceNature(dto.getAllowanceNature()); // Mandatory/Profile-based/etc
   otherInfo.setGratuityAmount(dto.getGratuityAmount());
   otherInfo.setGratuityBasic(dto.getGratuityBasic());
   otherInfo.setOtherBenefits(dto.getOtherBenefits());

   // Career fields
   otherInfo.setNextSalaryReviewDate(dto.getNextSalaryReviewDate());
   otherInfo.setDateOfAvailability(dto.getDateOfAvailability());
   otherInfo.setEduhkEmployee(dto.getEduhkEmployee()); // No/Serving/Former
   otherInfo.setStaffNumber(dto.getStaffNumber());

   // Application source
   otherInfo.setSourceType(dto.getSourceType()); // EdUHK Internet/Intranet/etc
   otherInfo.setSourceTypeOther(dto.getSourceTypeOther());

   // DCCPR fields
   otherInfo.setDccpr(dto.getDccpr());
   otherInfo.setDccprDetails(dto.getDccprDetails());
   ```

2. Add data type conversion for BigDecimal fields:
   ```java
   private BigDecimal convertToBigDecimal(Object value) {
       if (value == null) return null;
       if (value instanceof BigDecimal) return (BigDecimal) value;
       if (value instanceof Number) {
           return BigDecimal.valueOf(((Number) value).doubleValue());
       }
       try {
           return new BigDecimal(value.toString());
       } catch (NumberFormatException e) {
           return null;
       }
   }
   ```

3. Add validation logic
4. Test with sample data

**Acceptance Criteria:**
- All 22 OtherInfo fields mapped
- BigDecimal conversion working correctly
- Validation logic complete
- Unit tests pass
- Integration tests succeed

---

### Phase 2: Offer Management System (HIGH PRIORITY)

**Estimated Effort:** 2-3 days
**Priority:** 🔴 Critical
**Goal:** Enable offer tracking and management

#### Task 2.1: Create Offer Database Schema
**Effort:** 2-3 hours
**File:** `db_scripts/rdps_offer.sql`

**Steps:**
1. **Create table script:**
   ```sql
   -- File: db_scripts/rdps_offer.sql

   CREATE TABLE RDPS.RDPS_OFFER (
       OFFER_ID        VARCHAR2(20)    NOT NULL,
       REQ_NUMBER      VARCHAR2(20)    NOT NULL,
       CANDIDATE_ID    VARCHAR2(20)    NOT NULL,
       OFFER_STATUS    VARCHAR2(15),   -- Offered/Offer Approved/etc

       -- Employment details
       PAY_BASIS       CHAR(1),        -- Y=Yearly/H=Hourly/M=Monthly
       EMP_DEP         VARCHAR2(10),   -- Department
       TOA             VARCHAR2(5),    -- Terms of Appointment (IC/IS/TMP/RSS)
       FUNC_TITLE      VARCHAR2(20),   -- Functional Title
       BAND            CHAR(2),        -- Band
       GRADE           VARCHAR2(20),   -- Job Grade
       POST            VARCHAR2(20),   -- Post

       -- Contract dates
       CONTRACT_START_DATE  DATE,
       CONTRACT_END_DATE    DATE,

       -- Probation
       PROBATION_LENGTH     NUMBER,
       PROBATION_UNITS      CHAR(1),   -- Y/H/M

       -- Notice period
       NOTICE_LENGTH        NUMBER,
       NOTICE_UNITS         CHAR(1),   -- M=Months/D=Days

       -- Compensation
       SALARY_AMOUNT        NUMBER(38,2),
       GRATUITY_PCT         NUMBER(5,2),

       -- Employment mode
       EMP_MODE            CHAR(1),     -- F=Full-time/H=Half-time/P=Part-time
       PLAN_HOURS          NUMBER,      -- Planned hours/days

       -- Benefits eligibility
       MPF                 CHAR(1),     -- Y/N
       SUPERANNUATION      CHAR(1),     -- Y/N
       PENSION             CHAR(1),     -- Y/N

       -- Other
       PROJECT_TITLE       VARCHAR2(50),
       OFFER_REMARKS       VARCHAR2(100),

       -- Audit fields
       CREATED_BY          VARCHAR2(32),
       CREATION_DATE       DATE,
       USERSTAMP           VARCHAR2(32),
       TIMESTAMP           DATE,

       CONSTRAINT PK_RDPS_OFFER PRIMARY KEY (OFFER_ID),
       CONSTRAINT FK_OFFER_CANDIDATE FOREIGN KEY (CANDIDATE_ID)
           REFERENCES RDPS.RDPS_CANDIDATE(CANDIDATE_ID)
   );

   -- Indexes
   CREATE INDEX IDX_OFFER_CANDIDATE ON RDPS.RDPS_OFFER(CANDIDATE_ID);
   CREATE INDEX IDX_OFFER_REQ_NUMBER ON RDPS.RDPS_OFFER(REQ_NUMBER);
   CREATE INDEX IDX_OFFER_STATUS ON RDPS.RDPS_OFFER(OFFER_STATUS);

   -- Comments
   COMMENT ON TABLE RDPS.RDPS_OFFER IS 'Job offers for candidates';
   ```

2. **Update installation script:**
   Add `@@rdps_offer.sql` to `db_scripts/00_INSTALL_ALL.sql`

3. **Test installation:**
   ```bash
   # In Docker container
   sqlplus RDPS/rdps_password123@FREEPDB1 @db_scripts/rdps_offer.sql
   ```

**Acceptance Criteria:**
- Table created successfully
- All constraints applied
- Indexes created
- Comments added

---

#### Task 2.2: Create Offer Model Class
**Effort:** 2-3 hours
**File:** `src/main/java/eduhk/fhr/web/model/Offer.java`

**Steps:**
1. Create model with all 30 fields
2. Add getters/setters
3. Add toString(), equals(), hashCode() methods
4. Add JavaDoc comments

**Implementation:**
```java
package eduhk.fhr.web.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Offer Model
 *
 * Represents a job offer record in the RDPS_OFFER table.
 */
public class Offer {

    private String offerId;
    private String reqNumber;
    private String candidateId;
    private String offerStatus;

    // Employment details
    private String payBasis; // Y/H/M
    private String empDep;
    private String toa; // IC/IS/TMP/RSS
    private String funcTitle;
    private String band;
    private String grade;
    private String post;

    // Contract dates
    private Date contractStartDate;
    private Date contractEndDate;

    // Probation
    private Integer probationLength;
    private String probationUnits; // Y/H/M

    // Notice period
    private Integer noticeLength;
    private String noticeUnits; // M/D

    // Compensation
    private BigDecimal salaryAmount;
    private BigDecimal gratuityPct;

    // Employment mode
    private String empMode; // F/H/P
    private Integer planHours;

    // Benefits eligibility
    private String mpf; // Y/N
    private String superannuation; // Y/N
    private String pension; // Y/N

    // Other
    private String projectTitle;
    private String offerRemarks;

    // Audit fields
    private String createdBy;
    private Date creationDate;
    private String userstamp;
    private Date timestamp;

    // Getters and Setters
    // ... (generate all getters/setters)
}
```

---

#### Task 2.3: Create Offer DAO Class
**Effort:** 3-4 hours
**File:** `src/main/java/eduhk/fhr/web/dao/OfferDAO.java`

**Steps:**
1. Create DAO extending BaseDao
2. Implement insert method
3. Implement findById method
4. Implement findByCandidateId method
5. Implement update method
6. Add error handling

**Implementation:**
```java
@Repository
public class OfferDAO extends BaseDao {

    public void insert(Offer offer) {
        String sql = "INSERT INTO RDPS_OFFER (" +
            "OFFER_ID, REQ_NUMBER, CANDIDATE_ID, OFFER_STATUS, " +
            "PAY_BASIS, EMP_DEP, TOA, FUNC_TITLE, BAND, GRADE, POST, " +
            "CONTRACT_START_DATE, CONTRACT_END_DATE, " +
            "PROBATION_LENGTH, PROBATION_UNITS, " +
            "NOTICE_LENGTH, NOTICE_UNITS, " +
            "SALARY_AMOUNT, GRATUITY_PCT, " +
            "EMP_MODE, PLAN_HOURS, " +
            "MPF, SUPERANNUATION, PENSION, " +
            "PROJECT_TITLE, OFFER_REMARKS, " +
            "CREATED_BY, CREATION_DATE, USERSTAMP, TIMESTAMP" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, SYSDATE, ?, SYSDATE)";

        jdbcTemplate.update(sql, offer.getOfferId(), offer.getReqNumber(), ...);
    }

    public Offer findById(String offerId) {
        // ... implementation
    }

    public List<Offer> findByCandidateId(String candidateId) {
        // ... implementation
    }

    public void update(Offer offer) {
        // ... implementation
    }
}
```

---

#### Task 2.4: Create Offer Mapper Class
**Effort:** 3-4 hours
**File:** `src/main/java/eduhk/fhr/web/mapper/OfferMapper.java`

---

#### Task 2.5: Create Offer Import Service
**Effort:** 2-3 hours
**File:** `src/main/java/eduhk/fhr/web/service/import_/OfferImportService.java`

---

#### Task 2.6: Integrate Offer Import into Orchestration
**Effort:** 2-3 hours
**File:** `src/main/java/eduhk/fhr/web/service/import_/ImportOrchestrationService.java`

**Steps:**
1. Add OfferImportService injection
2. Call offer import after candidate import
3. Add error handling
4. Update import history logging

---

### Phase 3: Onboarding System (MEDIUM PRIORITY)

**Estimated Effort:** 6-8 days
**Priority:** 🟡 Medium
**Goal:** Enable HR onboarding data capture

This phase follows the same pattern as Phase 2 for each entity:
1. Create database schema
2. Create model class
3. Create DAO class
4. Create mapper class
5. Create import service (if applicable)

**Entities to implement:**
- PersonInfo (34 fields) - 2-3 days
- PersonSpouse (20 fields) - 1-2 days
- PersonChild (21 fields) - 1-2 days
- PersonECP (11 fields) - 1 day

---

### Phase 4: Benefits & Payroll Support (MEDIUM PRIORITY)

**Estimated Effort:** 4-6 days
**Priority:** 🟡 Medium
**Goal:** Enable benefits selection and payroll processing

**Entities to implement:**
- BankInfo (9 fields) - 0.5-1 day
- BenefitMain (9 fields) - 1 day
- BenefitSpouse (17 fields) - 1-2 days
- BenefitChild (12 fields) - 1-2 days

---

### Phase 5: Integration & Attachments (LOW-MEDIUM PRIORITY)

**Estimated Effort:** 3-4 days
**Priority:** 🟢 Low-Medium
**Goal:** Enable photo storage and EBS integration

**Entities to implement:**
- PhotoUpload (7 fields, BLOB handling) - 1-2 days
- ToEBS (9 fields) - 1-2 days

---

## Technical Debt & Risks

### 1. LOV Resolution Not Implemented ⚠️ CRITICAL

**Impact:** Cannot properly map education levels, study modes, qualifications, districts
**Affected Components:**
- CandidateMapper (district lookup)
- EducationMapper (4 LOV fields)

**Risk Level:** 🔴 High
**Mitigation:** Complete Task 1.3 immediately

---

### 2. Many Fields Commented Out in CandidateMapper ⚠️ HIGH

**Impact:** Silent data loss - fields received from TalentLink but not saved
**Risk Level:** 🔴 High
**Current Status:** Only 7 out of 20 fields are mapped
**Data Loss:** 13 fields per candidate not being captured

**Mitigation:** Complete Task 1.5 immediately

---

### 3. No Validation Logic in Mappers ⚠️ MEDIUM

**Impact:** Invalid data may be inserted into database
**Risk:** Data integrity issues, foreign key violations, application crashes
**Risk Level:** 🟡 Medium

**Mitigation:**
- Implement validate() methods in all mapper classes
- Add null checks
- Add format validation (email, phone, dates)
- Add range validation (numeric fields)

---

### 4. Missing Database Indexes ⚠️ MEDIUM

**Impact:** Performance degradation as data grows
**Risk Level:** 🟡 Medium

**Current Status:**
- Primary keys have indexes (automatic)
- Foreign keys missing indexes on:
  - CANDIDATE_ID in all child tables
  - REQ_NUMBER in all tables

**Mitigation:**
```sql
CREATE INDEX IDX_EDU_CANDIDATE ON RDPS_EDU_PROF_QUAL(CANDIDATE_ID);
CREATE INDEX IDX_EDU_REQ ON RDPS_EDU_PROF_QUAL(REQ_NUMBER);
CREATE INDEX IDX_WORK_CANDIDATE ON RDPS_WORK_EXPERIENCE(CANDIDATE_ID);
CREATE INDEX IDX_WORK_REQ ON RDPS_WORK_EXPERIENCE(REQ_NUMBER);
-- ... etc for all child tables
```

---

### 5. No SOAP DTO Verification ⚠️ HIGH

**Impact:** Runtime errors if DTO field names don't match TalentLink SOAP response
**Risk Level:** 🔴 High

**Current Status:** DTO field names based on assumptions, not verified against actual SOAP response

**Mitigation:**
1. Capture actual SOAP response from TalentLink
2. Verify DTO field names match response XML
3. Update DTOs if necessary
4. Add logging to capture mismatches

---

### 6. No Transaction Management ⚠️ MEDIUM

**Impact:** Partial imports may leave database in inconsistent state
**Risk Level:** 🟡 Medium

**Example Scenario:**
1. Candidate inserted successfully
2. Education insert fails
3. Database left with candidate but no education data

**Mitigation:**
```java
@Transactional(rollbackFor = Exception.class)
public void importCandidate(TalentLinkCandidateDTO dto) {
    // All operations in single transaction
    // Rollback if any step fails
}
```

---

### 7. No Error Recovery Mechanism ⚠️ LOW

**Impact:** Failed imports require manual intervention
**Risk Level:** 🟢 Low

**Current Status:**
- Import errors logged to `RDPS_IMPORT_VALIDATION_ERROR`
- No automatic retry mechanism
- No admin UI to manually fix and retry failed imports

**Mitigation:**
- Add retry mechanism with exponential backoff
- Create admin UI for reviewing and retrying failed imports
- Add "fix data and re-import" functionality

---

## Appendix: Field Mapping Reference

### Candidate Entity Field Mapping

| Excel Field Name | RDPS Column | Data Type | Status | Notes |
|------------------|-------------|-----------|--------|-------|
| Candidate ID | CANDIDATE_ID | VARCHAR2(20) | ✅ Mapped | Primary key |
| Job Number | REQ_NUMBER | VARCHAR2(20) | ✅ Mapped | |
| Post applied for | POST_APPLIED_FOR | VARCHAR2(100) | ❌ Missing | |
| Title | TITLE | VARCHAR(15) | ❌ Missing | Prof/Dr/Mr/Mrs/Ms/Miss |
| Last Name | LAST_NAME | VARCHAR2(32) | ✅ Mapped | |
| First Name | FIRST_NAME | VARCHAR2(32) | ✅ Mapped | |
| Name in Chinese (If any) | CHINESE_NAME | VARCHAR2(60) | ❌ Missing | |
| Date of Birth | DATE_OF_BIRTH | DATE | ❌ Missing | |
| Permanent Resident in HK | PERM_HK | CHAR(1) | ❌ Missing | Y/N |
| Gender | GENDER | CHAR(1) | ❌ Missing | F/M |
| HKID No. | HKID | VARCHAR2(30) | ❌ Missing | |
| Passport No. | PASSPORT | VARCHAR2(20) | ❌ Missing | |
| Need Visa to Work in HK | VISA_REQUIRED | CHAR(1) | ❌ Missing | Y/N |
| Address (line 1) | ADDR_LINE1 | VARCHAR2(60) | ❌ Missing | |
| Address (line 2) | ADDR_LINE2 | VARCHAR2(60) | ❌ Missing | |
| District | DISTRICT | NUMBER | ❌ Missing | LOV lookup required |
| Mobile No. | PHONE_NO | VARCHAR2(20) | ❌ Missing | |
| Email Address | EMAIL | VARCHAR2(50) | ✅ Mapped | |
| Created by | CREATED_BY | VARCHAR2(32) | ✅ Mapped | Auto-set to SYSTEM |
| Creation date | CREATION_DATE | DATE | ✅ Mapped | Auto-set to SYSDATE |
| Userstamp | USERSTAMP | VARCHAR2(32) | ✅ Mapped | Auto-set to SYSTEM |
| Timestamp | TIMESTAMP | DATE | ✅ Mapped | Auto-set to SYSDATE |

### Education Entity Field Mapping

| Excel Field Name | RDPS Column | Data Type | Status | Notes |
|------------------|-------------|-----------|--------|-------|
| Candidate ID | CANDIDATE_ID | VARCHAR2(20) | ✅ Mapped | FK |
| Requisition Number | REQ_NUMBER | VARCHAR2(20) | ✅ Mapped | |
| Seq | SEQ | NUMBER | ✅ Mapped | Auto number |
| School Name | INSTITUTION | VARCHAR2(100) | ✅ Mapped | |
| Country | COUNTRY | VARCHAR2(100) | ✅ Mapped | |
| Education Level | EDU_LEVEL | NUMBER | ❌ Missing | LOV lookup required |
| Full Time or Part Time | STUDY_MODE | NUMBER | ❌ Missing | LOV lookup required |
| Qualification Award Description | QUAL_AWARD_DESC | NUMBER | ❌ Missing | LOV lookup required |
| Qualification Award Classification | QUAL_AWARD_CLASS | NUMBER | ❌ Missing | LOV lookup required |
| Others | OTHERS | VARCHAR2(100) | ❌ Missing | |
| Degree Information: Name | MAJOR_STUDY_AREA | VARCHAR2(100) | ❌ Missing | |
| Start Date | START_DATE | DATE | ❌ Missing | MMM-YYYY |
| Date of Award | DATE_OF_AWARD | DATE | ❌ Missing | MMM-YYYY |

### Work Experience Entity Field Mapping

| Excel Field Name | RDPS Column | Data Type | Status | Notes |
|------------------|-------------|-----------|--------|-------|
| Candidate ID | CANDIDATE_ID | VARCHAR2(20) | ✅ Mapped | FK |
| Requisition Number | REQ_NUMBER | VARCHAR2(20) | ✅ Mapped | |
| Seq | SEQ | NUMBER | ✅ Mapped | Auto number |
| Company | EMPLOYER_NAME | VARCHAR2(100) | ✅ Mapped | |
| Nature of Business | NATURE_OF_BUSINESS | VARCHAR2(50) | ❌ Missing | |
| Position Title | POSITION_TITLE | VARCHAR2(100) | ✅ Mapped | |
| Current | CURRENT_JOB | CHAR(1) | ❌ Missing | Y/N |
| Type | MODE_OF_EMPLOYMENT | VARCHAR2(50) | ❌ Missing | Full-time/Part-time/etc |
| Number of hour engaged per week | HOURS_PER_WEEK | VARCHAR2(20) | ❌ Missing | |
| From | START_DATE | DATE | ✅ Mapped | MMM-YYYY |
| Until | END_DATE | DATE | ✅ Mapped | MMM-YYYY |
| Position Type | NATURE_OF_DUTIES | VARCHAR2(1000) | ❌ Missing | |

### Referee Entity Field Mapping

| Excel Field Name | RDPS Column | Data Type | Status | Notes |
|------------------|-------------|-----------|--------|-------|
| Candidate ID | CANDIDATE_ID | VARCHAR2(20) | ✅ Mapped | FK |
| Requisition Number | REQ_NUMBER | VARCHAR2(20) | ✅ Mapped | |
| Seq | SEQ | NUMBER | ✅ Mapped | Auto number |
| Title | TITLE | VARCHAR2(15) | ✅ Mapped | |
| Referee 1: Name | FIRST_NAME | VARCHAR2(32) | ✅ Mapped | |
| Last Name | LAST_NAME | VARCHAR2(32) | ✅ Mapped | |
| Referee 1: Position | POSITION_TITLE | VARCHAR2(100) | ❌ Missing | |
| Referee 1: Tel. No. | PHONE_NUMBER | VARCHAR2(20) | ❌ Missing | |
| Referee 1:E-Mail Address | EMAIL | VARCHAR2(50) | ✅ Mapped | |
| Relationship | RELATIONSHIP | VARCHAR2(50) | ❌ Missing | |

### Other Info Entity Field Mapping

| Excel Field Name | RDPS Column | Data Type | Status | Notes |
|------------------|-------------|-----------|--------|-------|
| Candidate ID | CANDIDATE_ID | VARCHAR2(20) | ✅ Mapped | FK |
| Requisition Number | REQ_NUMBER | VARCHAR2(20) | ✅ Mapped | |
| Present / Last Basic Monthly Salary | SALARY | NUMBER | ✅ Mapped | |
| Number of months | NUMBER_OF_MONTHS | NUMBER | ❌ Missing | |
| Allowance / Bonus Amount | ALLOWANCE_AMOUNT | NUMBER | ❌ Missing | |
| Allowance / Bonus - % of Basic Salary | ALLOWANCE_PCT | NUMBER | ❌ Missing | |
| Allowance / Bonus - By Month / Year | ALLOWANCE_TYPE | VARCHAR2(15) | ❌ Missing | |
| Nature of Allowance / Bonus | ALLOWANCE_NATURE | VARCHAR2(20) | ❌ Missing | |
| Contract-end Gratuity Amount | GRATUITY_AMOUNT | NUMBER | ❌ Missing | |
| Contract-end Gratuity - % | GRATUITY_BASIC | NUMBER | ❌ Missing | |
| Other Allowances and Fringe Benefits | OTHER_BENEFITS | VARCHAR2(255) | ❌ Missing | |
| Skills | SKILLS | VARCHAR2(255) | ✅ Mapped | |
| Next Salary Review Date | NEXT_SALARY_REVIEW_DATE | DATE | ❌ Missing | |
| Expected Salary (per month) | EXPECTED_SALARY | NUMBER | ✅ Mapped | |
| Notice period | NOTICE_PERIOD | VARCHAR2(50) | ✅ Mapped | |
| Date of Availability | DATE_OF_AVAILABILITY | DATE | ❌ Missing | |
| EdUHK Employee | EDUHK_EMPLOYEE | VARCHAR2(10) | ❌ Missing | No/Serving/Former |
| Staff Number | STAFF_NUMBER | VARCHAR2(10) | ❌ Missing | |
| Source Type | SOURCE_TYPE | VARCHAR2(20) | ❌ Missing | |
| Source Type - Other | SOURCE_TYPE_OTHER | VARCHAR2(200) | ❌ Missing | |
| DCCPR | DCCPR | VARCHAR2(25) | ❌ Missing | |
| DCCPR - Details | DCCPR_DETAILS | VARCHAR2(255) | ❌ Missing | |

---

## Summary

### Quick Stats

- **Total Entities:** 16
- **Implemented:** 5 (31%)
- **Missing:** 11 (69%)
- **Total Fields:** 277
- **Mapped Fields:** ~55 (20%)
- **Unmapped Fields:** ~222 (80%)

### Priority Actions

1. ⚠️ **IMMEDIATE:** Implement LookupValueResolver (blocks Education and Candidate completion)
2. ⚠️ **IMMEDIATE:** Uncomment CandidateMapper fields (silent data loss)
3. ✅ **WEEK 1:** Complete all existing mappers (Referee, WorkExp, Education, Candidate, OtherInfo)
4. ✅ **WEEK 2:** Implement Offer management system
5. ✅ **WEEKS 3-4:** Implement onboarding system
6. ✅ **WEEKS 5-6:** Implement benefits and integration systems

### Success Criteria

**Phase 1 Complete:**
- All 5 existing entities at 100% field mapping
- LOV resolution working
- All unit tests passing
- Integration tests passing

**Project Complete:**
- All 16 entities implemented
- 277 fields fully mapped
- All validation logic in place
- All tests passing
- Production deployment successful

---

**Report End**
