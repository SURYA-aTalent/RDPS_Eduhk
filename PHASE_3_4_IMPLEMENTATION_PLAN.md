# Phase 3 & 4 Implementation Plan

**Status:** Ready to Implement
**Scope:** Onboarding Questionnaire Parser + Offer API Integration

---

## Phase 3: Onboarding Questionnaire Parser

### Database Tables (All Exist ✅)
- RDPS_PERSON_INFO (PK: OFFER_ID, REQ_NUMBER)
- RDPS_PERSON_SPOUSE (PK: OFFER_ID, REQ_NUMBER)
- RDPS_PERSON_CHILD (PK: OFFER_ID, REQ_NUMBER, CHILD_SEQ)
- RDPS_PERSON_ECP (PK: OFFER_ID, REQ_NUMBER)
- RDPS_PHOTO_UPLOAD (exists but schema not checked yet)

### Key Insight
All Person tables are linked to **OFFER_ID**, not CANDIDATE_ID. This means:
1. Onboarding data should only be imported for candidates who have accepted offers
2. Import flow: Candidate → Offer → Person Info/Spouse/Child/ECP
3. Need to fetch OFFER_ID before importing onboarding data

### Components to Create

#### 1. DTOs (4 files)
- `TalentLinkPersonInfoDTO.java` - Personal information from onboarding
- `TalentLinkSpouseDTO.java` - Spouse information
- `TalentLinkChildDTO.java` - Child information (repeating)
- `TalentLinkEmergencyContactDTO.java` - Emergency contact

#### 2. Container
- `ParsedOnboardingData.java` - Container for all parsed onboarding data

#### 3. Parser
- `OnboardingQuestionnaireParser.java` - Parses "EdUHK Onboarding Data Submission Questionnaire (Candidate)"

#### 4. Models (simplified - use existing pattern)
Can reuse existing model pattern or create minimal models

#### 5. DAOs (4 files - simplified)
- `PersonInfoDAO.java`
- `PersonSpouseDAO.java`
- `PersonChildDAO.java`
- `PersonECPDAO.java`

#### 6. Import Services (4 files)
- `PersonInfoImportService.java`
- `PersonSpouseImportService.java`
- `PersonChildImportService.java`
- `PersonECPImportService.java`

### Implementation Strategy
Given the dependency on OFFER_ID, the implementation order should be:
1. Create Phase 4 (Offer) first to get OFFER_ID
2. Then implement Phase 3 (Onboarding) using the OFFER_ID

---

## Phase 4: Offer API Integration

### Database Table
- RDPS_OFFER (exists with 29 fields!)

### Schema Analysis
```
OFFER_ID (PK)
REQ_NUMBER
CANDIDATE_ID
OFFER_STATUS
PAY_BASIS
EMP_DEP (Employee Department)
TOA (Type of Appointment)
FUNC_TITLE
BAND, GRADE, POST
CONTRACT_START_DATE, CONTRACT_END_DATE
PROBATION_LENGTH, PROBATION_UNITS
NOTICE_LENGTH, NOTICE_UNITS
OFFER_REMARKS
PROJECT_TITLE
MPF, SUPERANNUATION, PENSION (Y/N flags)
SALARY_AMOUNT
GRATUITY_PCT
EMP_MODE (Employment Mode)
PLAN_HOURS
+ Audit fields
```

### Components Needed

#### 1. SOAP Service (CRITICAL - Need to verify API exists)
- `TalentLinkSOAPOfferService.java`
- Need to check if TalentLink has Offer API
- May need to generate SOAP stubs from WSDL

#### 2. DTO
- `TalentLinkOfferDTO.java`

#### 3. Mapper
- `OfferMapper.java`

#### 4. DAO
- `OfferDAO.java` (already exists based on git status!)

#### 5. Model
- `Offer.java` (already exists based on git status!)

#### 6. Import Service
- `OfferImportService.java` (already exists based on git status!)

### Key Question
**Does TalentLink have an Offer API?**
Need to investigate:
1. Check existing WSDL files
2. Check TalentLink API documentation
3. May need to fetch offer data from a different source

---

## Recommended Implementation Order

### Step 1: Verify Offer API Availability
```bash
# Check if Offer SOAP stubs exist
find src/main/java -name "*Offer*.java" -path "*/soap/*"

# Check existing SOAP services for clues
grep -r "getOffer" src/main/java/eduhk/fhr/web/service/
```

### Step 2: Implement Basic Offer Infrastructure
Since Offer.java, OfferDAO.java, and OfferImportService.java already exist (untracked files), we may already have some infrastructure!

### Step 3: Create Onboarding DTOs and Parser
Once we can get OFFER_ID, implement the onboarding parser.

### Step 4: Wire Everything Together
Update CandidateImportService or create separate OfferImportOrchestration.

---

## Simplified Implementation Approach

Given the complexity and the fact that some files already exist, I recommend:

### Phase 3 (Minimal Viable Implementation)
1. **Create DTOs only** - Start with data structures
2. **Create basic parser** - Parse the document structure
3. **Skip full import** - Just log the parsed data for now
4. **Defer database inserts** - Until Offer API is confirmed

### Phase 4 (Investigation First)
1. **Check existing files** - Review Offer.java, OfferDAO.java, OfferImportService.java
2. **Investigate API** - Determine if TalentLink has Offer API
3. **Document findings** - Create requirements document
4. **Implement if API exists** - Otherwise note as future work

---

## Next Steps

1. ✅ Check database schemas (DONE)
2. 🔄 Check existing Offer files
3. 🔄 Create Onboarding DTOs
4. 🔄 Create Onboarding Parser (basic structure)
5. ⏭ Investigate Offer API availability
6. ⏭ Full integration (after API confirmation)

---

**End of Plan**
