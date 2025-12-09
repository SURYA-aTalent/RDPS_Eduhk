# Database Tables and Fields - Implementation Mapping

This document describes all database tables and fields that are populated by the TalentLink to RDPS data synchronization implementation.

## Overview

The implementation imports candidate data from TalentLink SOAP API into 6 RDPS database tables, mapping 111 fields in total.

---

## 1. RDPS_CANDIDATE (Main Candidate Table)

**Purpose:** Stores core candidate information including personal details, contact information, and identity documents.

**Relationship:** Parent table for all other candidate-related tables.

### Fields (22 total)

| Field Name | Type | NOT NULL | Source | Notes |
|------------|------|----------|--------|-------|
| CANDIDATE_ID | VARCHAR2(20) | ✓ | TalentLink Profile.id | Primary Key |
| REQ_NUMBER | VARCHAR2(20) | ✓ | Position.requisitionId or "POOL" | Primary Key |
| POST_APPLIED_FOR | VARCHAR2(100) | | Position.position | Last/current position title |
| TITLE | VARCHAR2(15) | | Profile.formOfAddress or academicTitle | Mr/Ms/Dr/Prof |
| FIRST_NAME | VARCHAR2(32) | | Profile.firstname | |
| LAST_NAME | VARCHAR2(32) | | Profile.lastname | |
| CHINESE_NAME | VARCHAR2(60) | | Profile (custom field) | May be null if not available |
| DATE_OF_BIRTH | DATE | | PersonalData.dateOfBirth | Converted from XMLGregorianCalendar |
| PERM_HK | CHAR(1) | | Profile (custom field) | Y/N - May be null |
| GENDER | CHAR(1) | | PersonalData.sex.valueAttribute | F=Female, M=Male |
| HKID | VARCHAR2(30) | | Profile.socialSecurityNumber | HK ID card number |
| PASSPORT | VARCHAR2(20) | | Profile (custom field) | May be null if not available |
| VISA_REQUIRED | CHAR(1) | | Profile (custom field) | Y/N - May be null |
| ADDR_LINE1 | VARCHAR2(60) | | Address.address1 | |
| ADDR_LINE2 | VARCHAR2(60) | | Address.address2 | |
| DISTRICT | NUMBER | ✓ | Address.city or regionName → LOV | Resolved to RDPS_LOV_DISTRICT key |
| PHONE_NO | VARCHAR2(20) | | Address.mobilePhone | |
| EMAIL | VARCHAR2(50) | | Profile.email | |
| CREATED_BY | VARCHAR2(32) | ✓ | "SYSTEM" | Audit field |
| CREATION_DATE | DATE | ✓ | SYSDATE | Audit field |
| USERSTAMP | VARCHAR2(32) | ✓ | "SYSTEM" | Audit field |
| TIMESTAMP | DATE | ✓ | SYSDATE | Audit field |

### Data Source Mapping
```
TalentLink Profile (SOAP)
    ├─ Profile.id → CANDIDATE_ID
    ├─ Profile.firstname → FIRST_NAME
    ├─ Profile.lastname → LAST_NAME
    ├─ Profile.email → EMAIL
    ├─ Profile.formOfAddress → TITLE
    ├─ Profile.socialSecurityNumber → HKID
    ├─ Profile.personalData.dateOfBirth → DATE_OF_BIRTH
    ├─ Profile.personalData.sex → GENDER
    ├─ Profile.address.address1 → ADDR_LINE1
    ├─ Profile.address.address2 → ADDR_LINE2
    ├─ Profile.address.city/regionName → DISTRICT (via LOV)
    ├─ Profile.address.mobilePhone → PHONE_NO
    └─ Profile.position.position → POST_APPLIED_FOR
```

---

## 2. RDPS_EDU_PROF_QUAL (Education & Qualifications)

**Purpose:** Stores candidate's educational background and professional qualifications.

**Relationship:** Many-to-one with RDPS_CANDIDATE (one candidate can have multiple qualifications).

### Fields (16 total)

| Field Name | Type | NOT NULL | Source | Notes |
|------------|------|----------|--------|-------|
| QUAL_ID | NUMBER | ✓ | Auto-generated sequence | Primary Key |
| CANDIDATE_ID | VARCHAR2(20) | ✓ | Parent candidate | Foreign Key |
| REQ_NUMBER | VARCHAR2(20) | ✓ | Parent candidate | Foreign Key |
| SEQ | NUMBER | ✓ | Sequence counter | Part of composite key |
| INSTITUTION | VARCHAR2(100) | | TalentLinkEducationDTO.institution | University/school name |
| COUNTRY | VARCHAR2(50) | | TalentLinkEducationDTO.country | Country of study |
| EDU_LEVEL | NUMBER | | TalentLinkEducationDTO.educationLevel → LOV | Resolved to RDPS_LOV_EDU_LEVEL |
| STUDY_MODE | NUMBER | | TalentLinkEducationDTO.studyMode → LOV | Resolved to RDPS_LOV_STUDY_MODE |
| QUAL_AWARD_DESC | NUMBER | | TalentLinkEducationDTO.qualificationAwardDesc → LOV | Resolved to RDPS_LOV_QUAL_AWARD_DESC |
| QUAL_AWARD_CLASS | NUMBER | | TalentLinkEducationDTO.qualificationAwardClass → LOV | Resolved to RDPS_LOV_QUAL_AWARD_CLASS |
| OTHERS | VARCHAR2(200) | | TalentLinkEducationDTO.others | Additional details |
| MAJOR_STUDY_AREA | VARCHAR2(100) | | TalentLinkEducationDTO.majorStudyArea | Field of study |
| START_DATE | DATE | | TalentLinkEducationDTO.startDate | Study start date |
| DATE_OF_AWARD | DATE | | TalentLinkEducationDTO.dateOfAward | Graduation/award date |
| CREATED_BY | VARCHAR2(32) | ✓ | "SYSTEM" | Audit field |
| USERSTAMP | VARCHAR2(32) | ✓ | "SYSTEM" | Audit field |

### LOV Resolutions
- **EDU_LEVEL:** Bachelor's, Master's, PhD, Diploma, etc. (14 values)
- **STUDY_MODE:** Full-time, Part-time, Distance, etc. (7 values)
- **QUAL_AWARD_DESC:** Degree types (35 values)
- **QUAL_AWARD_CLASS:** First Class, Upper Second, etc. (16 values)

---

## 3. RDPS_WORK_EXPERIENCE (Employment History)

**Purpose:** Stores candidate's work experience and employment history.

**Relationship:** Many-to-one with RDPS_CANDIDATE (one candidate can have multiple work experiences).

### Fields (15 total)

| Field Name | Type | NOT NULL | Source | Notes |
|------------|------|----------|--------|-------|
| EXP_ID | NUMBER | ✓ | Auto-generated sequence | Primary Key |
| CANDIDATE_ID | VARCHAR2(20) | ✓ | Parent candidate | Foreign Key |
| REQ_NUMBER | VARCHAR2(20) | ✓ | Parent candidate | Foreign Key |
| SEQ | NUMBER | ✓ | Sequence counter | Part of composite key |
| EMPLOYER_NAME | VARCHAR2(100) | | TalentLinkWorkExperienceDTO.employerName | Company name |
| NATURE_OF_BUSINESS | VARCHAR2(100) | | TalentLinkWorkExperienceDTO.natureOfBusiness | Industry/sector |
| POSITION_TITLE | VARCHAR2(100) | | TalentLinkWorkExperienceDTO.positionTitle | Job title |
| CURRENT_JOB | CHAR(1) | | TalentLinkWorkExperienceDTO.currentJob | Y/N |
| MODE_OF_EMPLOYMENT | VARCHAR2(50) | | TalentLinkWorkExperienceDTO.modeOfEmployment | Full-time/Part-time/Contract |
| HOURS_PER_WEEK | VARCHAR2(10) | | TalentLinkWorkExperienceDTO.hoursPerWeek | Working hours |
| NATURE_OF_DUTIES | VARCHAR2(4000) | | TalentLinkWorkExperienceDTO.natureOfDuties | Job description/responsibilities |
| START_DATE | DATE | | TalentLinkWorkExperienceDTO.startDate | Employment start |
| END_DATE | DATE | | TalentLinkWorkExperienceDTO.endDate | Employment end (null if current) |
| CREATED_BY | VARCHAR2(32) | ✓ | "SYSTEM" | Audit field |
| USERSTAMP | VARCHAR2(32) | ✓ | "SYSTEM" | Audit field |

---

## 4. RDPS_REFEREE (References)

**Purpose:** Stores information about candidate's references/referees.

**Relationship:** Many-to-one with RDPS_CANDIDATE (one candidate can have multiple referees).

### Fields (13 total)

| Field Name | Type | NOT NULL | Source | Notes |
|------------|------|----------|--------|-------|
| REFEREE_ID | NUMBER | ✓ | Auto-generated sequence | Primary Key |
| CANDIDATE_ID | VARCHAR2(20) | ✓ | Parent candidate | Foreign Key |
| REQ_NUMBER | VARCHAR2(20) | ✓ | Parent candidate | Foreign Key |
| SEQ | NUMBER | ✓ | Sequence counter | Part of composite key |
| TITLE | VARCHAR2(15) | | TalentLinkRefereeDTO.title | Mr/Ms/Dr/Prof |
| FIRST_NAME | VARCHAR2(32) | | TalentLinkRefereeDTO.firstname | Referee first name |
| LAST_NAME | VARCHAR2(32) | | TalentLinkRefereeDTO.lastname | Referee last name |
| POSITION_TITLE | VARCHAR2(100) | | TalentLinkRefereeDTO.positionTitle | Referee's job title |
| PHONE_NUMBER | VARCHAR2(20) | | TalentLinkRefereeDTO.phoneNumber | Contact phone |
| EMAIL | VARCHAR2(50) | | TalentLinkRefereeDTO.email | Contact email |
| RELATIONSHIP | VARCHAR2(50) | | TalentLinkRefereeDTO.relationship | Supervisor/Colleague/Academic |
| CREATED_BY | VARCHAR2(32) | ✓ | "SYSTEM" | Audit field |
| USERSTAMP | VARCHAR2(32) | ✓ | "SYSTEM" | Audit field |

---

## 5. RDPS_OTHER_INFO (Additional Information)

**Purpose:** Stores compensation, benefits, skills, and other miscellaneous candidate information.

**Relationship:** One-to-one with RDPS_CANDIDATE (each candidate has one other info record).

### Fields (25 total)

| Field Name | Type | NOT NULL | Source | Notes |
|------------|------|----------|--------|-------|
| INFO_ID | NUMBER | ✓ | Auto-generated sequence | Primary Key |
| CANDIDATE_ID | VARCHAR2(20) | ✓ | Parent candidate | Foreign Key |
| REQ_NUMBER | VARCHAR2(20) | ✓ | Parent candidate | Foreign Key |
| SALARY | NUMBER(10,2) | | TalentLinkOtherInfoDTO.salary | Current salary |
| NUMBER_OF_MONTHS | NUMBER | | TalentLinkOtherInfoDTO.numberOfMonths | Salary months per year (12/13/14) |
| EXPECTED_SALARY | NUMBER(10,2) | | TalentLinkOtherInfoDTO.expectedSalary | Desired salary |
| NEXT_SALARY_REVIEW_DATE | DATE | | TalentLinkOtherInfoDTO.nextSalaryReviewDate | Next review date |
| ALLOWANCE_AMOUNT | NUMBER(10,2) | | TalentLinkOtherInfoDTO.allowanceAmount | Allowance amount |
| ALLOWANCE_PCT | NUMBER(5,2) | | TalentLinkOtherInfoDTO.allowancePercentage | Allowance as % of salary |
| ALLOWANCE_TYPE | VARCHAR2(50) | | TalentLinkOtherInfoDTO.allowanceType | Type of allowance |
| ALLOWANCE_NATURE | VARCHAR2(100) | | TalentLinkOtherInfoDTO.allowanceNature | Allowance description |
| GRATUITY_AMOUNT | NUMBER(10,2) | | TalentLinkOtherInfoDTO.gratuityAmount | Gratuity payment |
| GRATUITY_BASIC | NUMBER(10,2) | | TalentLinkOtherInfoDTO.gratuityBasic | Gratuity basic amount |
| OTHER_BENEFITS | VARCHAR2(500) | | TalentLinkOtherInfoDTO.otherBenefits | Medical/housing/etc. |
| SKILLS | VARCHAR2(4000) | | TalentLinkOtherInfoDTO.skills | Skills and competencies |
| NOTICE_PERIOD | VARCHAR2(50) | | TalentLinkOtherInfoDTO.noticePeriod | Notice period required |
| DATE_OF_AVAILABILITY | DATE | | TalentLinkOtherInfoDTO.dateOfAvailability | Available to start from |
| EDUHK_EMPLOYEE | CHAR(1) | | TalentLinkOtherInfoDTO.eduhkEmployee | Y/N previous EdUHK employee |
| STAFF_NUMBER | VARCHAR2(20) | | TalentLinkOtherInfoDTO.staffNumber | Previous staff number |
| SOURCE_TYPE | VARCHAR2(50) | | TalentLinkOtherInfoDTO.sourceType | How found job (website/referral) |
| SOURCE_TYPE_OTHER | VARCHAR2(100) | | TalentLinkOtherInfoDTO.sourceTypeOther | Other source details |
| DCCPR | CHAR(1) | | TalentLinkOtherInfoDTO.dccpr | Y/N DCCPR flag |
| DCCPR_DETAILS | VARCHAR2(500) | | TalentLinkOtherInfoDTO.dccprDetails | DCCPR details |
| CREATED_BY | VARCHAR2(32) | ✓ | "SYSTEM" | Audit field |
| USERSTAMP | VARCHAR2(32) | ✓ | "SYSTEM" | Audit field |

---

## 6. RDPS_OFFER (Job Offers)

**Purpose:** Stores job offer details including contract terms, compensation, and benefits eligibility.

**Relationship:** Many-to-one with RDPS_CANDIDATE (one candidate can have multiple offers).

### Fields (30 total)

| Field Name | Type | NOT NULL | Source | Notes |
|------------|------|----------|--------|-------|
| OFFER_ID | VARCHAR2(20) | ✓ | ApplicationDto.id | Primary Key |
| CANDIDATE_ID | VARCHAR2(20) | ✓ | Parent candidate | Foreign Key |
| REQ_NUMBER | VARCHAR2(20) | ✓ | ApplicationDto.positionId | Foreign Key |
| OFFER_STATUS | VARCHAR2(50) | | ApplicationDto.status | "OFFERED", "ACCEPTED", "HIRED" |
| PAY_BASIS | CHAR(1) | | **TODO** | Y=Yearly, H=Hourly, M=Monthly |
| EMP_DEP | VARCHAR2(100) | | **TODO** | Department |
| TOA | VARCHAR2(20) | | **TODO** | Terms of Appointment (IC/IS/TMP/RSS) |
| FUNC_TITLE | VARCHAR2(100) | | **TODO** | Functional title |
| BAND | VARCHAR2(20) | | **TODO** | Band |
| GRADE | VARCHAR2(20) | | **TODO** | Job grade |
| POST | VARCHAR2(100) | | **TODO** | Post |
| CONTRACT_START_DATE | DATE | | **TODO** | Contract start date |
| CONTRACT_END_DATE | DATE | | **TODO** | Contract end date |
| PROBATION_LENGTH | NUMBER | | **TODO** | Probation period length |
| PROBATION_UNITS | CHAR(1) | | **TODO** | Y=Year, M=Month, D=Day |
| NOTICE_LENGTH | NUMBER | | **TODO** | Notice period length |
| NOTICE_UNITS | CHAR(1) | | **TODO** | M=Months, D=Days |
| SALARY_AMOUNT | NUMBER(10,2) | | **TODO** | Offered salary |
| GRATUITY_PCT | NUMBER(5,2) | | **TODO** | Gratuity percentage |
| EMP_MODE | CHAR(1) | | **TODO** | F=Full-time, H=Half-time, P=Part-time |
| PLAN_HOURS | NUMBER | | **TODO** | Planned hours/days |
| MPF | CHAR(1) | | **TODO** | Y/N MPF eligibility |
| SUPERANNUATION | CHAR(1) | | **TODO** | Y/N Superannuation |
| PENSION | CHAR(1) | | **TODO** | Y/N Pension |
| PROJECT_TITLE | VARCHAR2(200) | | **TODO** | Project title (for research positions) |
| OFFER_REMARKS | VARCHAR2(500) | | **TODO** | Additional remarks |
| CREATED_BY | VARCHAR2(32) | ✓ | "SYSTEM" | Audit field |
| CREATION_DATE | DATE | ✓ | SYSDATE | Audit field |
| USERSTAMP | VARCHAR2(32) | ✓ | "SYSTEM" | Audit field |
| TIMESTAMP | DATE | ✓ | SYSDATE | Audit field |

### Implementation Status

**Currently Implemented (4 fields):**
- OFFER_ID → ApplicationDto.id
- CANDIDATE_ID → From candidate
- REQ_NUMBER → From candidate
- OFFER_STATUS → ApplicationDto.status

**TODO (26 fields):**
- All employment details, contract terms, compensation, and benefits fields
- These require extraction from TalentLink contract/offer objects or custom fields

---

## Summary Statistics

### Overall Coverage

| Table | Total Fields | Implemented | Status |
|-------|--------------|-------------|--------|
| RDPS_CANDIDATE | 22 | 22 | ✅ 100% |
| RDPS_EDU_PROF_QUAL | 16 | 16 | ✅ 100% |
| RDPS_WORK_EXPERIENCE | 15 | 15 | ✅ 100% |
| RDPS_REFEREE | 13 | 13 | ✅ 100% |
| RDPS_OTHER_INFO | 25 | 25 | ✅ 100% |
| RDPS_OFFER | 30 | 4 | ⚠️ 13% |
| **TOTAL** | **121** | **95** | **78.5%** |

### Data Flow Diagram

```
┌─────────────────────────────────────────┐
│   TalentLink SOAP API                   │
│   (Profile + nested objects)            │
└──────────────┬──────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────┐
│   TalentLinkSOAPCandidateService        │
│   - getCandidates()                     │
│   - convertProfileToDTO()               │
└──────────────┬──────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────┐
│   DTOs (Data Transfer Objects)          │
│   - TalentLinkCandidateDTO              │
│   - TalentLinkEducationDTO              │
│   - TalentLinkWorkExperienceDTO         │
│   - TalentLinkRefereeDTO                │
│   - TalentLinkOtherInfoDTO              │
└──────────────┬──────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────┐
│   Mappers (DTO → Model)                 │
│   - CandidateMapper                     │
│   - EducationMapper (with LOV)          │
│   - WorkExperienceMapper                │
│   - RefereeMapper                       │
│   - OtherInfoMapper                     │
│   - OfferMapper                         │
└──────────────┬──────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────┐
│   DAOs (Database Access)                │
│   - CandidateDAO                        │
│   - EducationDAO                        │
│   - WorkExperienceDAO                   │
│   - RefereeDAO                          │
│   - OtherInfoDAO                        │
│   - OfferDAO                            │
└──────────────┬──────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────┐
│   Oracle Database (RDPS Schema)         │
│   - RDPS_CANDIDATE                      │
│   - RDPS_EDU_PROF_QUAL                  │
│   - RDPS_WORK_EXPERIENCE                │
│   - RDPS_REFEREE                        │
│   - RDPS_OTHER_INFO                     │
│   - RDPS_OFFER                          │
└─────────────────────────────────────────┘
```

---

## Lookup Value (LOV) Tables

The following LOV tables are used for reference data resolution:

### RDPS_LOV_DISTRICT (Hong Kong Districts)
- **Purpose:** Maps district names to numeric keys
- **Records:** 19 districts
- **Used in:** RDPS_CANDIDATE.DISTRICT

### RDPS_LOV_EDU_LEVEL (Education Levels)
- **Purpose:** Maps education levels to numeric keys
- **Records:** 14 levels (Primary, Secondary, Bachelor's, Master's, PhD, etc.)
- **Used in:** RDPS_EDU_PROF_QUAL.EDU_LEVEL

### RDPS_LOV_STUDY_MODE (Study Modes)
- **Purpose:** Maps study modes to numeric keys
- **Records:** 7 modes (Full-time, Part-time, Distance, etc.)
- **Used in:** RDPS_EDU_PROF_QUAL.STUDY_MODE

### RDPS_LOV_QUAL_AWARD_DESC (Qualification Descriptions)
- **Purpose:** Maps qualification types to numeric keys
- **Records:** 35 types (Bachelor of Arts, Master of Science, PhD, etc.)
- **Used in:** RDPS_EDU_PROF_QUAL.QUAL_AWARD_DESC

### RDPS_LOV_QUAL_AWARD_CLASS (Award Classifications)
- **Purpose:** Maps award classes to numeric keys
- **Records:** 16 classes (First Class, Upper Second, Merit, Distinction, etc.)
- **Used in:** RDPS_EDU_PROF_QUAL.QUAL_AWARD_CLASS

---

## Testing Queries

### Verify Data Import

```sql
-- Count records in each table
SELECT
    (SELECT COUNT(*) FROM RDPS_CANDIDATE) as CANDIDATES,
    (SELECT COUNT(*) FROM RDPS_EDU_PROF_QUAL) as EDUCATION,
    (SELECT COUNT(*) FROM RDPS_WORK_EXPERIENCE) as WORK_EXP,
    (SELECT COUNT(*) FROM RDPS_REFEREE) as REFEREES,
    (SELECT COUNT(*) FROM RDPS_OTHER_INFO) as OTHER_INFO,
    (SELECT COUNT(*) FROM RDPS_OFFER) as OFFERS
FROM DUAL;
```

### View Recent Imports

```sql
-- Latest 5 candidates with their related records
SELECT
    c.CANDIDATE_ID,
    c.FIRST_NAME,
    c.LAST_NAME,
    c.EMAIL,
    c.CREATION_DATE,
    (SELECT COUNT(*) FROM RDPS_EDU_PROF_QUAL e WHERE e.CANDIDATE_ID = c.CANDIDATE_ID) as EDU_COUNT,
    (SELECT COUNT(*) FROM RDPS_WORK_EXPERIENCE w WHERE w.CANDIDATE_ID = c.CANDIDATE_ID) as WORK_COUNT,
    (SELECT COUNT(*) FROM RDPS_REFEREE r WHERE r.CANDIDATE_ID = c.CANDIDATE_ID) as REF_COUNT
FROM RDPS_CANDIDATE c
ORDER BY c.CREATION_DATE DESC
FETCH FIRST 5 ROWS ONLY;
```

### Check LOV Resolutions

```sql
-- View education records with resolved LOV values
SELECT
    e.CANDIDATE_ID,
    e.INSTITUTION,
    el.LOV_VALUE as EDUCATION_LEVEL,
    sm.LOV_VALUE as STUDY_MODE,
    qd.LOV_VALUE as QUALIFICATION,
    qc.LOV_VALUE as AWARD_CLASS
FROM RDPS_EDU_PROF_QUAL e
LEFT JOIN RDPS_LOV_EDU_LEVEL el ON e.EDU_LEVEL = el.LOV_KEY
LEFT JOIN RDPS_LOV_STUDY_MODE sm ON e.STUDY_MODE = sm.LOV_KEY
LEFT JOIN RDPS_LOV_QUAL_AWARD_DESC qd ON e.QUAL_AWARD_DESC = qd.LOV_KEY
LEFT JOIN RDPS_LOV_QUAL_AWARD_CLASS qc ON e.QUAL_AWARD_CLASS = qc.LOV_KEY
ORDER BY e.CANDIDATE_ID;
```

---

## API Testing

### Trigger Import

```bash
# Import 5 candidates
curl "http://localhost:8080/api/test-sync/trigger?batchSize=5"
```

### Sync Single Candidate

```bash
# Import specific candidate by ID
curl "http://localhost:8080/api/test-sync/candidate/12345"
```

---

## Notes and Limitations

### Current Limitations

1. **Offer Details Incomplete:** Only 13% of RDPS_OFFER fields are mapped. Employment details, contract terms, and compensation require additional TalentLink API integration.

2. **Custom Fields:** Some candidate fields (chineseName, permanentHK, passportNo, visaRequired) may not be available in standard SOAP Profile and require custom field extraction.

3. **LOV Resolution:** District resolution uses city/regionName from address, which may not perfectly match HK district names.

### Future Enhancements

1. **Complete Offer Mapping:** Extract offer details from TalentLink contract objects or custom fields
2. **Custom Field Integration:** Map TalentLink custom fields to RDPS fields
3. **Document Attachment:** Import candidate documents/attachments
4. **Application History:** Track application workflow history
5. **Validation Rules:** Enhanced validation for data quality

---

## Document Information

- **Created:** 2025-12-03
- **Version:** 1.0
- **Last Updated:** 2025-12-03
- **Author:** Claude Code Implementation
- **Status:** Active Development
