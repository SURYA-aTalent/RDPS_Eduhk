# Comprehensive Field Mapping Implementation Plan

**Generated:** 2025-12-06
**Excel Source:** All fields in iRecruitment - inHouse_20251013_TLK_Andy Update 20251205.xlsx

## Executive Summary

This plan maps all fields from 11 sheets (Candidate, EduProfQual, WorkExp, OtherInfo, Referee, Offer, PersonInfo, PersonSpouse, PersonChild, PersonECP, PhotoUpload) to their respective data sources in TalentLink.

### Data Source Categories:

1. **Candidate API** ✅ - Already available
2. **EdUHK Education/Work Experience Form** ✅ - Already implemented
3. **EdUHK Standard PIF Form** 🔄 - Need to implement
4. **EdUHK Onboarding Data Submission Questionnaire (Candidate)** 🔄 - Need to implement
5. **EdUHK Onboarding (HR)** 🔄 - Need to implement
6. **Offer Form API** 🔄 - Need to implement
7. **Condition Form API** 🔄 - Need to implement
8. **Job Requisition** ✅ - Available via Candidate API
9. **N/A Fields** - Skip or derive

---

## Sheet 1: Candidate (21 fields)

### Data Sources Breakdown:

#### 1.1 From Candidate API (3 fields) ✅
**Status:** Already implemented via `TalentLinkCandidateDTO`

| Field Name | API Source | Notes |
|------------|-----------|-------|
| Candidate ID | `Profile.id` | Primary key |
| Job Number | `Profile.requisitionNumber` | FK to requisition |
| Post applied for | `Requisition.jobTitle` | Join via requisitionNumber |

#### 1.2 From EdUHK Standard PIF Form (16 fields) 🔄
**Status:** Need to implement structured document parser

**Document Name:** "EdUHK Standard PIF"

| Field Name | Form Question | Mapping Notes |
|------------|---------------|---------------|
| Title | Title | Dropdown selection |
| Last Name | Last Name | Free text |
| First Name | First Name | Free text |
| Name in Chinese (If any) | Chinese Name | Free text |
| Date of Birth | Date of Birth | Date field |
| Permanent Resident in HK | Permanent Resident | Yes/No dropdown |
| Gender | Gender | Male/Female dropdown |
| HKID No. | HKID Number | Free text (masked) |
| Passport No. | Passport Number | Free text |
| Nationality | Nationality | Dropdown |
| Marital Status | Marital Status | Dropdown |
| Residential Address | Residential Address | Free text (multi-line) |
| Correspondence Address | Correspondence Address | Free text |
| Home Tel. No. | Home Telephone | Free text |
| Mobile Phone No. | Mobile Phone | Free text |
| Email | Email Address | Free text |

**Implementation:** Similar to `EducationWorkDocumentParser`

#### 1.3 From N/A (1 field) ⏭
Skip or derive from other sources

---

## Sheet 2: EduProfQual (13 fields)

### Data Sources Breakdown:

#### 2.1 From Candidate API (2 fields) ✅
| Field Name | API Source |
|------------|-----------|
| Candidate ID | `Profile.id` |
| Requisition Number | `Profile.requisitionNumber` |

#### 2.2 From EdUHK Education/Work Experience (6 fields) ✅
**Status:** Already implemented!

| Field Name | Form Question | Current Mapping |
|------------|---------------|-----------------|
| School or Institution Name | School Name | `institution` |
| Country | Country | `country` |
| Degree Name | Degree Name | `qualificationAwardDesc` |
| Is Full Time? | Study Mode | `studyMode` |
| Major | Sub-School / Department | `majorStudyArea` |
| Date of Award | Degree Date | `dateOfAward` |

#### 2.3 From N/A (4 fields) 🔄
**Need LOV resolution or derivation:**

| Field Name | Source | Implementation |
|------------|--------|----------------|
| Qualification Award Description | Derive from Degree Type | Map "Advanced or honors diploma" → LOV |
| Qualification Award Classification | Derive from awards | Map to LOV |
| Others | Free text field | Optional |
| Start Date | Calculate or form | Optional field |

**Action:** Update `EducationMapper` to handle these derivations

---

## Sheet 3: WorkExp (12 fields)

### Data Sources Breakdown:

#### 3.1 From Candidate API (2 fields) ✅
Same as EduProfQual

#### 3.2 From EdUHK Education/Work Experience (7 fields) ✅
**Status:** Already implemented!

| Field Name | Form Question | Current Mapping |
|------------|---------------|-----------------|
| Company | Employer Organization Name | `employerName` |
| Position Title | Position Title | `positionTitle` |
| Current | Current | Checkbox/Yes-No |
| Type | Employment Type | Need to add |
| From | Start Date | `startDate` |
| Until | End Date | `endDate` |
| Position Type | Position Type | DirectHire/Contract |

**Action:** Update `WorkExperienceMapper` to include Type and Position Type

#### 3.3 From N/A (2 fields) 🔄
| Field Name | Implementation |
|------------|----------------|
| Nature of Business | Optional - can derive from industry |
| Number of hour engaged per week (if part-time) | Optional field |

---

## Sheet 4: OtherInfo (23 fields)

### Data Sources Breakdown:

#### 4.1 From Candidate API (2 fields) ✅
Same as EduProfQual

#### 4.2 From EdUHK Education/Work Experience (2 fields) ✅
| Field Name | Form Question | Current Mapping |
|------------|---------------|-----------------|
| Present / Last Basic Monthly Salary | Compensation / Ending Compensation | `salary` |
| Expected Salary | Expected Salary | `expectedSalary` ✅ (already working!) |

#### 4.3 From N/A (18 fields) 📝
**Status:** Most are optional/calculated fields

| Field Name | Type | Source |
|------------|------|--------|
| Number of months | Calculated | Duration calculation |
| Allowance / Bonus Amount | Optional | N/A |
| Allowance / Bonus - % of Basic Salary | Optional | N/A |
| Allowance / Bonus - By Month / Year | Optional | N/A |
| Nature of Allowance / Bonus | Optional | Free text |
| Contract-end Gratuity Amount | Optional | N/A |
| Contract-end Gratuity - % of Basic Monthly Salary | Optional | N/A |
| Other Allowances and Fringe Benefits | Optional | Free text |
| ... (10 more optional fields) | Optional | N/A |

**Action:** Mark as NULL-able in database or provide defaults

---

## Sheet 5: Referee (13 fields)

### Data Sources Breakdown:

#### 5.1 From Candidate API (2 fields) ✅
Same as EduProfQual

#### 5.2 From EdUHK Education/Work Experience (5 fields) ✅
**Status:** Parser extracts but NOT inserting (needs investigation)

| Field Name | Form Question | Current Status |
|------------|---------------|----------------|
| Referee : Name | Referee 1: Name, Referee 2: Name | Parsed but count=0 |
| Referee : Position | Referee 1: Position | Parsed |
| Referee : Tel. No. | Referee 1: Tel. No. | Parsed |
| Referee :E-Mail Address | Referee 2:E-Mail Address | Parsed |
| Relationship | Referee 1: Relationship | Parsed |

**Current Issue:** Debug endpoint shows `"refereesCount": 0` even though data exists in document

**Action:**
1. Investigate `isRefereeQuestion()` matching
2. Update parser to handle "Referee 1: Name" pattern
3. Split into 2 records (Referee 1 and Referee 2)

#### 5.3 From N/A (2 fields) 🔄
| Field Name | Implementation |
|------------|----------------|
| Title | Derive from Name or separate field |
| Last Name | Parse from full name |

**Note:** Excel shows instructions:
- "Please transfer into 2 records"
- "TalentLink Keep 2 set of Referee data"
- Must split Referee 1 and Referee 2 into separate database records

---

## Sheet 6: Offer (11 fields)

### Data Sources Breakdown:

#### 6.1 From Offer Form API (3 fields) 🔄
**Status:** Need to implement Offer API access

**API Endpoint:** `getOfferById()` or similar

| Field Name | API Field | Notes |
|------------|-----------|-------|
| Job Number | `Offer.jobNumber` | FK to requisition |
| Related Candidate ID | `Offer.candidateId` | FK to candidate |
| Application Status | `Offer.status` | Enum: Accepted/Rejected/Pending |

**Implementation Required:**
```java
// New service
TalentLinkSOAPOfferService.getOfferByCandidateId(candidateId)
```

#### 6.2 From Condition Form API (6 fields) 🔄
**Status:** Need to implement Condition/Offer details API

| Field Name | API Field | Notes |
|------------|-----------|-------|
| Salary Per | `Condition.salaryPer` | Monthly/Annual |
| Planned Start Date | `Condition.startDate` | Date |
| Planned End Date | `Condition.endDate` | Date (if contract) |
| Comments | `Condition.comments` | Free text |
| Salary Amount | `Condition.salaryAmount` | Decimal |
| Schedule Type | `Condition.scheduleType` | Full-time/Part-time |

#### 6.3 From Job Requisition (1 field) ✅
| Field Name | API Source |
|------------|-----------|
| Organisation | `Requisition.department` or `organizationUnit` |

**Action:**
1. Implement `OfferImportService` (similar to `CandidateImportService`)
2. Create `OfferMapper`
3. Implement SOAP API calls

---

## Sheet 7: PersonInfo (28 fields)

### Data Sources Breakdown:

#### 7.1 From Candidate API (2 fields) ✅
| Field Name | API Source |
|------------|-----------|
| Related Offer ID | Derived from offer sync |
| Related Requisition Number | `Profile.requisitionNumber` |

#### 7.2 From EdUHK Onboarding Data Submission Questionnaire (20 fields) 🔄
**Status:** Need to implement structured document parser

**Document Name:** "EdUHK Onboarding Data Submission Questionnaire (Candidate)"

| Field Name | Form Question |
|------------|---------------|
| Title | Title |
| Last Name | Last Name |
| First Name | First Name |
| Suffix Name | Suffix Name |
| Suffix Name on HKID/Passport | Suffix on ID |
| Chinese Name | Chinese Name |
| Gender | Gender |
| Date of Birth | Date of Birth |
| HKID/Passport No. | HKID Card No. |
| Passport No. | Passport No. |
| Issuing Country | Issuing Country |
| Nationality | Nationality |
| Place of Birth | Place of Birth |
| Marital Status | Marital Status |
| Permanent Resident | Permanent Resident |
| Residential / Correspondence Address | Residential Address |
| Tel No. | Telephone Number |
| Mobile Phone No. | Mobile Phone |
| Personal Email | Personal Email |
| Work Permit No. (If applicable) | Work Permit Number |

**Implementation:** Create `OnboardingDataParser` (similar to `EducationWorkDocumentParser`)

#### 7.3 From EdUHK Onboarding (HR) Form (3 fields) 🔄
**Status:** Need to implement HR form parser

**Document Name:** "EdUHK Onboarding (HR)"

| Field Name | Form Question |
|------------|---------------|
| Work Permit Number | Work Permit No. |
| Work Permit Expiry Date | Expiry Date |
| One-way Permit Expiry Date | One-way Permit Date |

#### 7.4 From EdUHK Standard PIF (1 field) ✅
| Field Name | Form Question |
|------------|---------------|
| Email | Email Address |

**Note:** Can reuse PIF parser from Candidate sheet

#### 7.5 From N/A (2 fields) 🔄
| Field Name | Implementation |
|------------|----------------|
| District | Derive from address or LOV |
| Country | Derive from address or LOV |

---

## Sheet 8: PersonSpouse (12 fields)

### Data Sources Breakdown:

#### 8.1 From Candidate API (2 fields) ✅
Same as PersonInfo

#### 8.2 From EdUHK Onboarding Data Submission Questionnaire (9 fields) 🔄
**Document Name:** "EdUHK Onboarding Data Submission Questionnaire (Candidate)"

**Section:** Spouse Information

| Field Name | Form Question |
|------------|---------------|
| Spouse Surname Name | Spouse Surname |
| Spouse First Name | Spouse First Name |
| Spouse Chinese Name | Spouse Chinese Name |
| Spouse Date of Birth | Spouse DOB |
| Spouse HKID Card No. | Spouse HKID |
| Date of Marriage | Marriage Date |
| Spouse Residential Address | Spouse Address (if different) |
| Spouse Tel No. | Spouse Telephone |
| Spouse Mobile Phone No. | Spouse Mobile |

**Implementation:** Extend `OnboardingDataParser` to handle spouse section

#### 8.3 From N/A (1 field) ⏭
Skip Suffix Name

---

## Sheet 9: PersonChild (16 fields)

### Data Sources Breakdown:

#### 9.1 From Candidate API (2 fields) ✅
Same as PersonInfo

#### 9.2 From EdUHK Onboarding Data Submission Questionnaire (9 fields) 🔄
**Document Name:** "EdUHK Onboarding Data Submission Questionnaire (Candidate)"

**Section:** Child Information (Repeating)

| Field Name | Form Question |
|------------|---------------|
| Child Surname | Child Surname |
| Child First Name | Child First Name |
| Child Chinese Name | Child Chinese Name |
| Child Sex | Child Gender |
| Child 1 Date of Birth | Child DOB |
| Child 1 Married or not? | Is Married |
| Child 1 Studying or not? | Is Studying |
| Child 1 Living with employee or not? | Living Together |
| Child 1 If Living with employee, whether... | Additional conditions |

**Note:** This is a REPEATING section - may have multiple children

**Implementation:** Handle as repeating section like Education/Work records

#### 9.3 From N/A (5 fields) 🔄
| Field Name | Implementation |
|------------|----------------|
| Suffix Name | Optional |
| HKID No. | May be in form |
| Passport No. | May be in form |
| Issuing Country | May be in form |
| Place of Birth | May be in form |

---

## Sheet 10: PersonECP (5 fields)

### Data Sources Breakdown:

#### 10.1 From EdUHK Onboarding Data Submission Questionnaire (5 fields) 🔄
**Document Name:** "EdUHK Onboarding Data Submission Questionnaire (Candidate)"

**Section:** Emergency Contact Person

| Field Name | Form Question |
|------------|---------------|
| Emergency Contact English Name | Emergency Contact Name |
| Emergency Contact Chinese Name | Emergency Contact Chinese Name |
| Emergency Contact Relationship | Relationship |
| Emergency Contact Residential Address | Emergency Contact Address |
| Emergency Contact Mobile Phone No. | Emergency Contact Mobile |

**Implementation:** Single record - part of `OnboardingDataParser`

---

## Sheet 11: PhotoUpload (1 field)

### Data Sources Breakdown:

#### 11.1 From EdUHK Onboarding Data Submission Questionnaire (1 field) 🔄
**Document Name:** "EdUHK Onboarding Data Submission Questionnaire (Candidate)"

| Field Name | Form Question | Type |
|------------|---------------|------|
| Employee Photo | Photo Upload | File attachment |

**Implementation:**
- Retrieve file attachment ID from structured document
- Use Document API to download file
- Store in database as BLOB or file path

---

## Implementation Plan

### Phase 1: Complete Current Implementation (Week 1)

#### 1.1 Fix Existing Issues ✅ DONE
- [x] Fix Education/Work parser for nested fields
- [x] Resolve EDU_LEVEL NULL issue
- [ ] Fix STUDY_MODE NULL issue
- [ ] Fix Referee parsing (currently returning 0 records)

#### 1.2 Enhance Existing Parsers
**File:** `EducationWorkDocumentParser.java`

Tasks:
- [ ] Add STUDY_MODE field extraction
- [ ] Fix referee question matching (`isRefereeQuestion()`)
- [ ] Implement referee name parsing (split into First/Last)
- [ ] Handle 2 referee records separately

**Files to modify:**
- `EducationWorkDocumentParser.java` - Add referee parsing
- `RefereeMapper.java` - Update mapping logic
- `RefereeImportService.java` - Verify insert logic

---

### Phase 2: Implement Standard PIF Form Parser (Week 2)

#### 2.1 Create New Parser
**New File:** `StandardPIFDocumentParser.java`

**Pattern:** Copy from `EducationWorkDocumentParser.java`

```java
@Service
public class StandardPIFDocumentParser {

    public ParsedPIFData parseStandardPIFDocument(StructuredDocument document) {
        // Similar structure to parseEducationWorkDocument()
    }

    private TalentLinkPIFDTO extractPIFData(Answer answer) {
        // Exact question name matching
        if ("Title".equalsIgnoreCase(question)) {
            dto.setTitle(value);
        } else if ("Last Name".equalsIgnoreCase(question)) {
            dto.setLastName(value);
        }
        // ... etc
    }
}
```

#### 2.2 Create DTO
**New File:** `TalentLinkPIFDTO.java`

Fields:
- title, lastName, firstName, chineseName
- dateOfBirth, permanentResident, gender
- hkidNumber, passportNumber, nationality
- maritalStatus, residentialAddress, correspondenceAddress
- homeTelephone, mobilePhone, email

#### 2.3 Update CandidateMapper
**File:** `CandidateMapper.java`

Add method:
```java
public void enrichCandidateFromPIF(Candidate candidate, TalentLinkPIFDTO pifData) {
    // Map PIF fields to Candidate model
}
```

#### 2.4 Update CandidateImportService
**File:** `CandidateImportService.java`

Add after Education/Work parsing:
```java
// Fetch and parse Standard PIF document
TalentLinkPIFDTO pifData = fetchAndParsePIFDocument(candidateId);
candidateMapper.enrichCandidateFromPIF(candidate, pifData);
```

---

### Phase 3: Implement Onboarding Questionnaire Parser (Week 3)

#### 3.1 Create Onboarding Parser
**New File:** `OnboardingQuestionnaireParser.java`

Sections to parse:
1. **Personal Info** → PersonInfo table
2. **Spouse Info** → PersonSpouse table
3. **Child Info** (repeating) → PersonChild table (multiple records)
4. **Emergency Contact** → PersonECP table
5. **Photo** → PhotoUpload table

**Pattern:** Similar to Education/Work with multiple DTOs

```java
@Service
public class OnboardingQuestionnaireParser {

    public ParsedOnboardingData parseOnboardingDocument(StructuredDocument document) {
        ParsedOnboardingData data = new ParsedOnboardingData();

        // Parse each section
        data.setPersonInfo(parsePersonInfo(answers));
        data.setSpouse(parseSpouse(answers));
        data.setChildren(parseChildren(answers));  // List<ChildDTO>
        data.setEmergencyContact(parseEmergencyContact(answers));
        data.setPhoto(parsePhoto(answers));

        return data;
    }
}
```

#### 3.2 Create DTOs
**New Files:**
- `TalentLinkPersonInfoDTO.java`
- `TalentLinkSpouseDTO.java`
- `TalentLinkChildDTO.java`
- `TalentLinkEmergencyContactDTO.java`
- `TalentLinkPhotoDTO.java`

#### 3.3 Create Import Services
**New Files:**
- `PersonInfoImportService.java`
- `PersonSpouseImportService.java`
- `PersonChildImportService.java`
- `PersonECPImportService.java`
- `PhotoUploadImportService.java`

**Pattern:** Copy from `EducationImportService.java`

#### 3.4 Integrate with Candidate Import
**File:** `CandidateImportService.java`

Add to `importCandidate()`:
```java
// After work/education/referees
if (hasOffer) {  // Only if candidate has accepted offer
    ParsedOnboardingData onboarding = fetchAndParseOnboarding(candidateId);

    personInfoImportService.importPersonInfo(onboarding.getPersonInfo(), candidateId, offerId);
    personSpouseImportService.importSpouse(onboarding.getSpouse(), candidateId, offerId);
    personChildImportService.importChildren(onboarding.getChildren(), candidateId, offerId);
    personECPImportService.importEmergencyContact(onboarding.getEmergencyContact(), candidateId, offerId);
    photoUploadImportService.importPhoto(onboarding.getPhoto(), candidateId, offerId);
}
```

---

### Phase 4: Implement Offer API Integration (Week 4)

#### 4.1 Create Offer SOAP Service
**New File:** `TalentLinkSOAPOfferService.java`

**Pattern:** Copy from `TalentLinkSOAPCandidateService.java`

```java
@Service
public class TalentLinkSOAPOfferService {

    @PostConstruct
    public void initialize() {
        // Load SOAP credentials
        // Initialize offer service port
    }

    public OfferDto getOfferByCandidateId(Long candidateId) {
        // SOAP call to get offer details
    }

    public List<ConditionDto> getOfferConditions(Long offerId) {
        // SOAP call to get offer conditions/terms
    }
}
```

#### 4.2 Create Offer DTO and Mapper
**New Files:**
- `TalentLinkOfferDTO.java`
- `OfferMapper.java`

Fields from Excel:
- jobNumber, candidateId, applicationStatus
- salaryPer, salaryAmount, plannedStartDate, plannedEndDate
- scheduleType, comments, organisation

#### 4.3 Create Offer Import Service
**New File:** `OfferImportService.java`

```java
@Service
public class OfferImportService {

    public ImportErrorDetail importOffer(TalentLinkOfferDTO offerDTO, String candidateId) {
        // Map DTO to Offer model
        // Validate
        // Insert into RDPS_OFFER table
    }
}
```

#### 4.4 Update Orchestration
**File:** `ImportOrchestrationService.java`

Add offer import after candidate:
```java
if (candidate.hasAcceptedOffer()) {
    TalentLinkOfferDTO offer = offerService.getOfferByCandidateId(candidateId);
    offerImportService.importOffer(offer, candidateId);
}
```

---

### Phase 5: Database Schema Updates

#### 5.1 Check Existing Tables
Verify these tables exist:
- [x] RDPS_CANDIDATE
- [x] RDPS_EDU_PROF_QUAL
- [x] RDPS_WORK_EXPERIENCE
- [x] RDPS_OTHER_INFO
- [x] RDPS_REFEREE
- [ ] RDPS_OFFER
- [ ] RDPS_PERSON_INFO
- [ ] RDPS_PERSON_SPOUSE
- [ ] RDPS_PERSON_CHILD
- [ ] RDPS_PERSON_ECP
- [ ] RDPS_PHOTO_UPLOAD

#### 5.2 Create Missing Tables
If tables don't exist, create SQL scripts:

**File:** `db_scripts/11_CREATE_OFFER.sql`
```sql
CREATE TABLE RDPS_OFFER (
    OFFER_ID NUMBER PRIMARY KEY,
    CANDIDATE_ID VARCHAR2(50) NOT NULL,
    JOB_NUMBER VARCHAR2(50),
    APPLICATION_STATUS VARCHAR2(50),
    SALARY_PER VARCHAR2(20),
    SALARY_AMOUNT NUMBER(15,2),
    PLANNED_START_DATE DATE,
    PLANNED_END_DATE DATE,
    SCHEDULE_TYPE VARCHAR2(50),
    COMMENTS CLOB,
    ORGANISATION VARCHAR2(200),
    CREATED_BY VARCHAR2(100),
    CREATION_DATE DATE,
    USERSTAMP VARCHAR2(100),
    TIMESTAMP DATE,
    CONSTRAINT FK_OFFER_CANDIDATE FOREIGN KEY (CANDIDATE_ID)
        REFERENCES RDPS_CANDIDATE(CANDIDATE_ID)
);
```

Repeat for other tables...

#### 5.3 Update LOV Tables
Add missing values discovered during testing:
- [x] RDPS_LOV_EDU_LEVEL - Added "Advanced or honors diploma"
- [ ] RDPS_LOV_STUDY_MODE - Add values as needed
- [ ] Others as discovered

---

### Phase 6: Testing and Validation

#### 6.1 Unit Tests
Create test classes:
- `StandardPIFDocumentParserTest.java`
- `OnboardingQuestionnaireParserTest.java`
- `OfferMapperTest.java`

#### 6.2 Integration Tests
Test full flow:
1. Fetch candidate 116
2. Parse all forms (PIF, Education/Work, Onboarding)
3. Map to database models
4. Insert into database
5. Verify all fields populated

#### 6.3 Debug Endpoints
Create additional endpoints:
- `/api/test-sync/debug/pif-data/{candidateId}`
- `/api/test-sync/debug/onboarding-data/{candidateId}`
- `/api/test-sync/debug/offer-data/{candidateId}`

---

## Summary of Work Required

### New Components to Create:

1. **Parsers (3 files)**
   - `StandardPIFDocumentParser.java`
   - `OnboardingQuestionnaireParser.java`
   - `OnboardingHRParser.java`

2. **DTOs (10 files)**
   - `TalentLinkPIFDTO.java`
   - `TalentLinkPersonInfoDTO.java`
   - `TalentLinkSpouseDTO.java`
   - `TalentLinkChildDTO.java`
   - `TalentLinkEmergencyContactDTO.java`
   - `TalentLinkPhotoDTO.java`
   - `TalentLinkOfferDTO.java`
   - `ParsedPIFData.java`
   - `ParsedOnboardingData.java`

3. **Import Services (7 files)**
   - `PersonInfoImportService.java`
   - `PersonSpouseImportService.java`
   - `PersonChildImportService.java`
   - `PersonECPImportService.java`
   - `PhotoUploadImportService.java`
   - `OfferImportService.java`

4. **Mappers (6 files)**
   - `PersonInfoMapper.java`
   - `PersonSpouseMapper.java`
   - `PersonChildMapper.java`
   - `PersonECPMapper.java`
   - `PhotoUploadMapper.java`
   - `OfferMapper.java`

5. **SOAP Services (1 file)**
   - `TalentLinkSOAPOfferService.java`

6. **DAOs (7 files)**
   - `PersonInfoDAO.java`
   - `PersonSpouseDAO.java`
   - `PersonChildDAO.java`
   - `PersonECPDAO.java`
   - `PhotoUploadDAO.java`
   - `OfferDAO.java`

7. **Database Models (6 files)**
   - `PersonInfo.java`
   - `PersonSpouse.java`
   - `PersonChild.java`
   - `PersonECP.java`
   - `PhotoUpload.java`
   - `Offer.java`

### Files to Modify:

1. **Existing Parsers**
   - `EducationWorkDocumentParser.java` - Fix referee parsing, add STUDY_MODE

2. **Existing Services**
   - `CandidateImportService.java` - Add PIF and Onboarding parsing
   - `ImportOrchestrationService.java` - Add offer import

3. **Existing Mappers**
   - `CandidateMapper.java` - Add PIF enrichment
   - `EducationMapper.java` - Handle qualification LOV mappings
   - `WorkExperienceMapper.java` - Add Type and Position Type
   - `RefereeMapper.java` - Handle name splitting

### Estimated Effort:

- **Phase 1:** 1-2 days (fix current issues)
- **Phase 2:** 2-3 days (Standard PIF parser)
- **Phase 3:** 5-7 days (Onboarding parser + 5 import services)
- **Phase 4:** 3-4 days (Offer API integration)
- **Phase 5:** 1-2 days (Database updates)
- **Phase 6:** 2-3 days (Testing)

**Total:** 14-21 days (3-4 weeks)

---

## Document Structure Names

Based on the Excel mapping, these structured documents need to be retrieved:

1. ✅ **"EdUHK Education/Work Experience"** - Already implemented
2. 🔄 **"EdUHK Standard PIF"** - Need to implement
3. 🔄 **"EdUHK Onboarding Data Submission Questionnaire (Candidate)"** - Need to implement
4. 🔄 **"EdUHK Onboarding (HR)"** - Need to implement

**Note:** Use same document retrieval pattern as current implementation:
```java
List<ApplicationDto> applications = soapCandidateService.getApplicationsByCandidateId(candidateId);
List<Object> documents = documentService.getDocumentsByApplicationId(applicationId);
Long docId = findDocumentByName(documents, "EdUHK Standard PIF");
StructuredDocument doc = documentService.getStructuredDocumentByDocumentId(docId);
```

---

## Key Reuse Patterns

### 1. Document Retrieval (✅ Already working)
```java
// From CandidateImportService.enrichCandidateWithDocumentData()
List<ApplicationDto> applications = soapCandidateService.getApplicationsByCandidateId(candidateId);
for (ApplicationDto app : applications) {
    List<Object> documents = documentService.getDocumentsByApplicationId(app.getId());
    Long docId = findEducationWorkDocumentId(documents);  // Reuse this pattern
}
```

### 2. Document Parsing (✅ Pattern established)
```java
// From EducationWorkDocumentParser
Map<String, String> questionValueMap = new HashMap<>();
collectQuestionValues(answer, questionValueMap);  // Recursive collection

// Exact name matching (priority)
if ("School Name".equalsIgnoreCase(question)) {
    dto.setInstitution(value);
}
// Keyword matching (fallback)
else if (dto.getInstitution() == null && questionLower.contains("school")) {
    dto.setInstitution(value);
}
```

### 3. Import Service Pattern (✅ Established)
```java
// From EducationImportService
public void importEducation(TalentLinkCandidateDTO candidateDTO, String candidateId, String reqNumber) {
    // Delete existing records (replace strategy)
    educationDAO.deleteEducationByCandidateId(candidateId, reqNumber);

    // Insert new records
    int seq = 1;
    for (TalentLinkEducationDTO dto : candidateDTO.getEducation()) {
        Education education = educationMapper.mapToModel(dto, candidateId, reqNumber, seq);
        if (educationMapper.validate(education)) {
            educationDAO.insertEducation(education);
        }
        seq++;
    }
}
```

---

## Next Steps

1. ✅ Complete current Education/Work parser fixes (STUDY_MODE, Referee)
2. Create detailed field mapping for Standard PIF
3. Create detailed field mapping for Onboarding Questionnaire
4. Implement parsers one by one
5. Test with candidate 116
6. Validate all fields are correctly populated

---

## Appendix: Field Count by Sheet

| Sheet | Total Fields | From API | From Forms | From N/A |
|-------|--------------|----------|------------|----------|
| Candidate | 21 | 3 | 16 | 1 |
| EduProfQual | 13 | 2 | 6 | 4 |
| WorkExp | 12 | 2 | 7 | 2 |
| OtherInfo | 23 | 2 | 2 | 18 |
| Referee | 13 | 2 | 5 | 2 |
| Offer | 11 | 1 | 9 | 0 |
| PersonInfo | 28 | 2 | 24 | 2 |
| PersonSpouse | 12 | 2 | 9 | 1 |
| PersonChild | 16 | 2 | 9 | 5 |
| PersonECP | 5 | 0 | 5 | 0 |
| PhotoUpload | 1 | 0 | 1 | 0 |
| **TOTAL** | **155** | **18** | **93** | **35** |

---

**End of Plan**
