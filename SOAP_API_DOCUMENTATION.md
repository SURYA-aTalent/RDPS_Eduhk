# SOAP API Documentation and Gap Analysis for EDUHK RDPS

**Generated:** 2025-12-03
**Purpose:** Comprehensive inventory of current SOAP APIs and identification of new APIs needed for missing entities

---

## Executive Summary

This document provides:
1. Complete list of all SOAP APIs currently implemented in the project
2. Files where each SOAP API is used/referenced
3. Identification of new SOAP APIs needed for missing entities (Offer, PersonInfo, PersonSpouse, PersonChild, PersonECP, PhotoUpload, BankInfo, BenefitMain, BenefitSpouse, BenefitChild, ToEBS)
4. Data source mapping and implementation guidance

**Key Finding:** TalentLink SOAP API already provides Contract/Offer operations that are NOT yet implemented. The missing entities (PersonInfo, Benefits, etc.) can be accessed via Contract free-form fields, which are custom fields configured in TalentLink.

---

## Part 1: Currently Implemented SOAP APIs

### 1.1 Candidate SOAP Service

**Service Class:** `TalentLinkSOAPCandidateService`
**Location:** `/src/main/java/eduhk/fhr/web/service/TalentLinkSOAPCandidateService.java`
**SOAP Interface:** `eduhk.fhr.web.soap.candidate.CandidateWebService`
**Endpoint Configuration:** `TALENTLINK_CANDIDATE_SOAP_URL` parameter
**Default URL:** `https://api3.lumesse-talenthub.com/HRIS/SOAP/Candidate`

#### 1.1.1 getCandidates()

**SOAP Signature:**
```java
CandidateIdsDTO getCandidates(Integer page, CandidateSearchCriteriaDTO criteria)
```

**Service Method:**
```java
public CandidateIdsDTO getCandidates(Integer page, int limit)
```

**Returns:** List of candidate IDs with pagination

**Used In:**
- `ImportOrchestrationService.importNewCandidates()` (line 81)
  - **File:** `src/main/java/eduhk/fhr/web/service/import_/ImportOrchestrationService.java`
  - **Context:** Main entry point for bulk candidate retrieval during scheduled imports
  - **Workflow:** Fetches candidate IDs → loops through each → calls getCandidateById()

**Purpose:** Retrieve paginated list of candidate IDs from TalentLink for bulk import operations

---

#### 1.1.2 getCandidateById()

**SOAP Signature:**
```java
Profile getCandidateById(Long candidateId, Boolean displayConsents, Boolean displayBoTags)
```

**Service Method:**
```java
public Profile getCandidateById(Long candidateId)
```

**Returns:** Complete `Profile` object containing:
- Basic info: id, firstname, lastname, middlename, email
- Personal data: dateOfBirth, citizenship, maritalStatus, sex, numberOfChildren (via `personalData` object)
- Address: address object
- Position: position object
- References: reference object
- Tags: candidate tags

**Used In:**
- `ImportOrchestrationService.importNewCandidates()` (line 166)
  - **File:** `src/main/java/eduhk/fhr/web/service/import_/ImportOrchestrationService.java`
  - **Context:** Called for each candidate ID returned from getCandidates()
  - **Data Flow:** Profile → CandidateMapper → Candidate model → Database

**Purpose:** Fetch complete candidate profile data including personal information, education, work experience, and references

---

#### 1.1.3 getApplicationsByCandidateId()

**SOAP Signature:**
```java
List<ApplicationDto> getApplicationsByCandidateId(Long candidateId, Boolean displayConsents)
```

**Service Method:**
```java
public List<ApplicationDto> getApplicationsByCandidateId(Long candidateId)
```

**Returns:** List of `ApplicationDto` objects containing:
- Application details: id, candidateId, positionId, applicationDate, status
- Document list: documents collection with document metadata
- Contract indicator: hasContracts (boolean)

**Used In:**
1. `TalentLinkSOAPCandidateService.getCandidateDocuments()` (line 657)
   - **File:** `src/main/java/eduhk/fhr/web/service/TalentLinkSOAPCandidateService.java`
   - **Purpose:** Extract document list from applications

2. `CandidateDocumentSyncService.syncCandidateDocuments()` (line 60)
   - **File:** `src/main/java/eduhk/fhr/web/service/CandidateDocumentSyncService.java`
   - **Context:** Document sync to SharePoint
   - **Data Flow:** Applications → Documents → Download → SharePoint upload

**Purpose:** Retrieve candidate's job applications and associated document metadata for document sync operations

**IMPORTANT:** This operation returns `hasContracts` flag which indicates if offer/contract data exists for the application. This is the key indicator for when to fetch contract/offer details.

---

#### 1.1.4 downloadAttachedFile()

**SOAP Signature:**
```java
AttachedFileDto downloadAttachedFile(Long documentId)
```

**Service Implementation:** **Uses REST API instead of SOAP**
```java
public AttachedFileDto downloadAttachedFile(Long documentId)
```

**REST Endpoint:** `https://apiproxy.shared.lumessetalentlink.com/tlk/rest/candidate/attachment/{documentId}`

**Returns:** `AttachedFileDto` with:
- Binary file data (Base64 encoded)
- File metadata (name, type, size)

**Used In:**
- `CandidateDocumentSyncService.syncCandidateDocuments()` (line 95)
  - **File:** `src/main/java/eduhk/fhr/web/service/CandidateDocumentSyncService.java`
  - **Context:** Downloads document binary data for SharePoint upload
  - **Data Flow:** Document ID → Download binary → Upload to SharePoint → Save metadata to RDPS_CANDIDATE_DOCUMENT

**Purpose:** Download binary file content for candidate documents

**Technical Note:** Switched to REST API to avoid GZIP decompression issues in SOAP response handling

---

#### 1.1.5 createCandidateViaFolder()

**SOAP Signature:**
```java
void createCandidateViaFolder(Holder<Profile> candidate, Long folderId)
```

**Service Method:**
```java
public void createCandidateViaFolder(Holder<Profile> candidate, Long folderId)
```

**Returns:** Updates candidate holder with created candidate ID

**Used In:** **AVAILABLE BUT NOT CURRENTLY USED**

**Purpose:** Create new candidate in TalentLink within a specific folder/pool (for future use in candidate creation workflows)

---

### 1.2 User SOAP Service

**Service Class:** `TalentLinkUserSyncService`
**Location:** `/src/main/java/eduhk/fhr/web/service/TalentLinkUserSyncService.java`
**SOAP Interface:** `eduhk.fhr.web.soap.user.UserWebService`
**Endpoint Configuration:** `TALENTLINK_USER_SOAP_URL` parameter
**Default URL:** `https://api3.lumesse-talenthub.com/User/SOAP/User`

#### 1.2.1 getUsers()

**SOAP Signature:**
```java
List<UserDto> getUsers(RequestByGetUsersDto request)
```

**Service Method:**
```java
public List<UserDto> getUserByEmailOrLogin(String email)
```

**Returns:** List of `UserDto` objects matching search criteria

**Used In:**
- `TalentLinkUserSyncService.syncUsersToTalentLink()` (line 53)
  - **File:** `src/main/java/eduhk/fhr/web/service/TalentLinkUserSyncService.java`
  - **Context:** Checks if user already exists before create/update decision
  - **Scheduled Job:** Runs hourly via `ScheduleTask.syncUsersToTalentLink()` (cron: `0 0 * * * ?`)
  - **Manual Trigger:** `AdminUserSyncController.triggerSync()` endpoint

**Purpose:** Search for existing users in TalentLink by email or login to determine if create or update operation is needed

---

#### 1.2.2 createUser()

**SOAP Signature:**
```java
void createUser(Holder<UserExtDto> user, String activationType)
```

**Service Method:**
```java
public Long createUser(TalentLinkUserStaging user)
```

**Parameters Mapped:**
- firstName, lastName, email, login
- language, timezone, userType, password
- weekStartDay, active

**Returns:** Created user ID

**Used In:**
- `TalentLinkUserSyncService.syncUsersToTalentLink()` (line 76)
  - **File:** `src/main/java/eduhk/fhr/web/service/TalentLinkUserSyncService.java`
  - **Context:** Creates new user when not found in TalentLink
  - **Data Source:** Reads from `RDPS_TALENTLINK_USER_STAGING` table (where `syncedToTalentlink = 'N'`)
  - **Post-Action:** Updates staging record with `syncedToTalentlink = 'Y'` and `syncedAt` timestamp

**Purpose:** Create new user account in TalentLink from staging table

---

#### 1.2.3 updateUser()

**SOAP Signature:**
```java
void updateUser(UserExtDto user)
```

**Service Method:**
```java
public void updateUser(Long userId, TalentLinkUserStaging user)
```

**Parameters:** Same fields as createUser

**Used In:**
- `TalentLinkUserSyncService.syncUsersToTalentLink()` (line 68)
  - **File:** `src/main/java/eduhk/fhr/web/service/TalentLinkUserSyncService.java`
  - **Context:** Updates existing user in TalentLink when user found and status is 'Active'
  - **Data Source:** `RDPS_TALENTLINK_USER_STAGING` table

**Purpose:** Update existing user account in TalentLink

---

#### 1.2.4 deactivateUser()

**SOAP Signature:**
```java
void deactivateUser(Long userId)
```

**Service Method:**
```java
public void deactivateUser(Long userId)
```

**Used In:**
- `TalentLinkUserSyncService.syncUsersToTalentLink()` (line 61)
  - **File:** `src/main/java/eduhk/fhr/web/service/TalentLinkUserSyncService.java`
  - **Context:** Deactivates user when status in staging table is 'Inactive'

**Purpose:** Deactivate user account in TalentLink

---

### 1.3 SOAP Handler (Authentication)

**Class:** `TalentLinkSOAPHandler`
**Location:** `/src/main/java/eduhk/fhr/web/soap/handler/TalentLinkSOAPHandler.java`

**Purpose:** Adds WS-Security authentication headers to all outbound SOAP requests

**Authentication Method:**
- Uses `<wsse:UsernameToken>` with plaintext password
- Security namespace: `http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd`

**Credentials Configuration:**
- Loaded from `RDPS_PARAMETER` table at service initialization (`@PostConstruct`)
- Required parameters:
  - `TALENTLINK_USERNAME`
  - `TALENTLINK_PASSWORD`
  - `TALENTLINK_API_KEY` (appended to endpoint URL)
- Credentials stored in static variables (not thread-safe)
- **IMPORTANT:** Application must be restarted after updating credentials

**Usage:** Automatically attached to all SOAP service ports via handler chain configuration

---

## Part 2: Available But UNUSED SOAP Operations

### 2.1 Candidate Service (32 unused operations)

These operations are defined in `CandidateWebService` interface but not currently used:

**Tagging Operations:**
- `addTag` - Add tags to candidates
- `editTagByCandidateID` - Edit candidate tags
- `removeTagByCandidateID` - Remove candidate tags
- `getTagTypes` - Get available tag types
- `getTagsForType` - Get tags for type
- `getTaggedProfiles` - Get tagged profiles

**Application Operations:**
- `getApplicationById` - Get single application by ID
- `getApplications` - Get applications with filters
- `getApplicationsByJob` - Get applications for a job
- `getApplicationsByStatus` - Get applications by status

**Candidate Management:**
- `createCandidateViaJobNumber` - Create candidate via job number
- `createCandidateViaOpening` - Create candidate via opening ID
- `getArchivedCandidates` - Get archived candidate list
- `getCandidateHistory` - Get candidate history
- `updateCandidatePif` - Update candidate personal information

**Contract Operations (CRITICAL - NEEDED FOR OFFER ENTITY):**
- `getContracts` - **Get contracts/offers with pagination**
- `getContractsByApplicationId` - **Get contracts for specific application**

**Document Operations:**
- `getCandidateStructuredDocumentById` - Get structured document
- `uploadAttachedFile` - Upload file to candidate profile

**Consent Operations:**
- `getConsents` - Get data privacy consents
- `acceptCandidateConsent` - Accept consent
- `revokeCandidateConsent` - Revoke consent

**Template Operations:**
- `getCurrentBoCpTemplateByType` - Get template by type

---

### 2.2 User Service (31 unused operations)

These operations are defined in `UserWebService` interface but not currently used:

**User Management:**
- `activateUser` - Activate user account
- `getUserById` - Get user by ID
- `getUserExtById` - Get extended user by ID
- `getUserData` - Get user data
- `getInactiveUsers` - Get inactive users
- `deleteUser` - Delete user permanently
- `changePassword` - Change user password
- `generatePassword` - Generate random password
- `generateTemporaryPassword` - Generate temp password

**Role Management:**
- `assignRole` - Assign role to user
- `assignRoles` - Assign multiple roles
- `assignRolesWithTag` - Assign roles with tag
- `clearAllRoleAssignments` - Clear all role assignments
- `removeRoleAssignment` - Remove single role assignment
- `removeRolesWithTag` - Remove roles with tag
- `getRolesAssignedForUser` - Get assigned roles
- `getRightsForDepartmentAndCurrentUser` - Get user rights
- `getRightsWithLabels` - Get rights with labels
- `deleteRoles` - Delete roles

**Tag Operations:**
- `addTag` - Add tags to users
- `removeTag` - Remove user tag
- `getTagTypes` - Get tag types
- `getTaggedUsers` - Get tagged users
- `getTagsForType` - Get tags by type

**Configuration:**
- `configureUser` - Configure user parameters
- `getPasswordRules` - Get password rules
- `updatePasswordRules` - Update password rules

**Federation:**
- `removeFederationData` - Remove federation data

**Pool Management:**
- `getGrantedPools` - Get granted pools

---

## Part 3: New SOAP APIs Needed for Missing Entities

### 3.1 Offer Entity (HIGH PRIORITY)

**Implementation Status:** 0% - Completely missing
**Required SOAP Operations:** **ALREADY EXIST BUT NOT IMPLEMENTED**

#### SOAP Operations Available:

**1. getContracts()**

**SOAP Signature:**
```java
List<ContractDto> getContracts(Integer page, Boolean displayConsents)
```

**Location in WSDL:** `CandidateWebService.getContracts()`

**Returns:** `List<ContractDto>` with extensive offer/contract data:

**Key Fields for Offer Entity:**
- `id` - Contract/Offer ID
- `applicationId` - Links to application
- `status` - Offer status (Offered, Accepted, Declined, etc.)
- `type` - Contract type
- `offerSentOn` - Offer sent date
- `acceptedDate` - Offer accepted date
- `accepted` - Boolean flag
- `approved` - Boolean approval flag
- `plannedStartDate` - Contract start date
- `plannedEndDate` - Contract end date
- `probationaryPeriod` - Probation period (string format)
- `length` - Contract length (float)
- `lengthUnit` - Contract length unit (months/years)
- `permanentContract` - Complex object with employment details
- `freeFormFields` - **CRITICAL:** Custom fields configured in TalentLink
  - **PersonInfo fields** (employee onboarding data)
  - **Benefits fields** (medical, dental, housing)
  - **Bank info fields** (account details)
  - **Additional offer terms**
- `lovs` - List of values for dropdown fields
- `candidate` - ProfileDto (candidate information)

**Implementation Needed:**
1. Add method to `TalentLinkSOAPCandidateService`:
```java
public List<ContractDto> getContracts(Integer page) {
    return candidateService.getContracts(page, false);
}
```

2. Create `ContractMapper` to map `ContractDto` → `Offer` model
3. Create `OfferImportService` to orchestrate import
4. Integrate into `ImportOrchestrationService`

---

**2. getContractsByApplicationId()**

**SOAP Signature:**
```java
List<ContractDto> getContractsByApplicationId(Long applicationId, Boolean displayConsents)
```

**Location in WSDL:** `CandidateWebService.getContractsByApplicationId()`

**Returns:** Same `ContractDto` structure as getContracts(), but filtered by application ID

**Use Case:** Fetch offers/contracts for a specific job application

**When to Use:** When `ApplicationDto.hasContracts == true` (returned from getApplicationsByCandidateId())

**Implementation Needed:**
1. Add method to `TalentLinkSOAPCandidateService`:
```java
public List<ContractDto> getContractsByApplicationId(Long applicationId) {
    return candidateService.getContractsByApplicationId(applicationId, false);
}
```

2. Call this method when `hasContracts == true` during candidate import

---

#### ContractDto Structure (Detailed Breakdown)

**File:** `src/main/java/eduhk/fhr/web/soap/candidate/ContractDto.java`

**Core Offer Fields:**
```java
protected Long id;                          // Contract/Offer ID → OFFER_ID
protected Long applicationId;               // Application ID → Links to application
protected String status;                    // Offer status → OFFER_STATUS
protected String type;                      // Contract type
protected XMLGregorianCalendar offerSentOn; // Offer sent date
protected Boolean accepted;                 // Accepted flag
protected XMLGregorianCalendar acceptedDate;// Accepted date
protected Boolean approved;                 // Approval flag
protected XMLGregorianCalendar approval;    // Approval date
```

**Contract Period:**
```java
protected XMLGregorianCalendar plannedStartDate;  // → CONTRACT_START_DATE
protected XMLGregorianCalendar plannedEndDate;    // → CONTRACT_END_DATE
protected Float length;                           // Contract length → PROBATION_LENGTH
protected String lengthUnit;                      // Length unit → PROBATION_UNITS
protected String probationaryPeriod;              // Probation period string
```

**Employment Details (via permanentContract object):**
```java
protected PermanentContract permanentContract;
// Contains:
//   - department
//   - jobTitle
//   - salary
//   - benefits flags
//   - employment mode
```

**Custom Fields (CRITICAL):**
```java
protected ContractDto.FreeFormFields freeFormFields;
// Contains: List<FreeFormFieldContract>
// Each FreeFormFieldContract has:
//   - name: Field name (e.g., "PAY_BASIS", "EMP_DEP", "BANK_CODE")
//   - value: Field value (String)
//   - type: Field type (TEXT, NUMBER, DATE, etc.)
```

**Lookups:**
```java
protected ContractDto.Lovs lovs;
// Contains: List<GenericLov>
// Dropdown values for fields like:
//   - TOA (Terms of Appointment)
//   - Employment modes
//   - Benefit options
```

---

### 3.2 PersonInfo, PersonSpouse, PersonChild, PersonECP (Onboarding Entities)

**Implementation Status:** 0% - Completely missing
**Data Source:** TalentLink SOAP via Contract free-form fields

#### Access Method:

**NO separate SOAP operations exist for these entities.** Instead, access via:

1. **Call:** `getContractsByApplicationId(applicationId)`
2. **Navigate to:** `ContractDto.freeFormFields`
3. **Parse:** Extract person-related custom fields

#### Expected Custom Field Names (configured in TalentLink):

**PersonInfo Fields (34 fields):**
```
- PERSON_TITLE
- PERSON_FIRST_NAME
- PERSON_LAST_NAME
- PERSON_SUFFIX_NAME
- PERSON_CHINESE_NAME
- PERSON_GENDER
- PERSON_DATE_OF_BIRTH
- PERSON_HKID
- PERSON_PASSPORT
- PERSON_COUNTRY_OF_ISSUE
- PERSON_MARITAL_STATUS
- PERSON_PERM_RESIDENT
- PERSON_NATIONALITY
- PERSON_PLACE_OF_ORIGIN
- PERSON_ADDRESS_LINE1
- PERSON_ADDRESS_LINE2
- PERSON_ADDRESS_LINE3
- PERSON_DISTRICT
- PERSON_AREA_CODE
- PERSON_HOME_NUMBER
- PERSON_MOBILE_NUMBER
- PERSON_EMAIL
- PERSON_PERMIT_NUMBER
- PERSON_PERMIT_WORK_EXPIRY
- PERSON_PERMIT_ONEWAY_EXPIRY
... etc
```

**PersonSpouse Fields (20 fields):**
```
- SPOUSE_TITLE
- SPOUSE_FIRST_NAME
- SPOUSE_LAST_NAME
- SPOUSE_CHINESE_NAME
- SPOUSE_HKID
- SPOUSE_PASSPORT
- SPOUSE_DATE_OF_BIRTH
- SPOUSE_PLACE_OF_BIRTH
- SPOUSE_NATIONALITY
- SPOUSE_EDUCATION_LEVEL
- SPOUSE_OCCUPATION
- SPOUSE_EMPLOYER_NAME
- SPOUSE_OFFICE_TEL
... etc
```

**PersonChild Fields (21 fields per child, supports 6 children):**
```
- CHILD_1_FIRST_NAME
- CHILD_1_LAST_NAME
- CHILD_1_CHINESE_NAME
- CHILD_1_GENDER
- CHILD_1_DATE_OF_BIRTH
- CHILD_1_HKID
- CHILD_1_NATIONALITY
... repeated for CHILD_2 through CHILD_6
```

**PersonECP (Emergency Contact) Fields (11 fields):**
```
- ECP_NAME
- ECP_RELATIONSHIP
- ECP_HOME_TEL
- ECP_OFFICE_TEL
- ECP_MOBILE
- ECP_ADDRESS_LINE1
- ECP_ADDRESS_LINE2
- ECP_ADDRESS_LINE3
- ECP_DISTRICT
- ECP_AREA_CODE
```

#### Implementation Approach:

**1. Create Custom Field Parser:**
```java
@Component
public class ContractFreeFormFieldParser {

    public PersonInfo extractPersonInfo(List<FreeFormFieldContract> freeFormFields) {
        PersonInfo personInfo = new PersonInfo();

        for (FreeFormFieldContract field : freeFormFields) {
            switch (field.getName()) {
                case "PERSON_TITLE":
                    personInfo.setTitle(field.getValue());
                    break;
                case "PERSON_FIRST_NAME":
                    personInfo.setFirstName(field.getValue());
                    break;
                // ... map all 34 fields
            }
        }

        return personInfo;
    }

    public PersonSpouse extractPersonSpouse(List<FreeFormFieldContract> freeFormFields) {
        // Similar extraction logic
    }

    public List<PersonChild> extractPersonChildren(List<FreeFormFieldContract> freeFormFields) {
        // Extract CHILD_1 through CHILD_6 fields
    }

    public PersonECP extractPersonECP(List<FreeFormFieldContract> freeFormFields) {
        // Extract emergency contact fields
    }
}
```

**2. Integrate into Import Flow:**
```java
// In ImportOrchestrationService or new OnboardingImportService:

public void importOnboardingData(Long applicationId) {
    // 1. Fetch contract
    List<ContractDto> contracts = soapCandidateService.getContractsByApplicationId(applicationId);

    if (contracts.isEmpty()) return;

    ContractDto contract = contracts.get(0); // Latest contract
    List<FreeFormFieldContract> freeFormFields =
        contract.getFreeFormFields().getFreeFormField();

    // 2. Parse custom fields
    PersonInfo personInfo = freeFormFieldParser.extractPersonInfo(freeFormFields);
    PersonSpouse spouse = freeFormFieldParser.extractPersonSpouse(freeFormFields);
    List<PersonChild> children = freeFormFieldParser.extractPersonChildren(freeFormFields);
    PersonECP ecp = freeFormFieldParser.extractPersonECP(freeFormFields);

    // 3. Save to database
    personInfoDAO.insert(personInfo);
    if (spouse != null) personSpouseDAO.insert(spouse);
    children.forEach(child -> personChildDAO.insert(child));
    if (ecp != null) personECPDAO.insert(ecp);
}
```

**3. Prerequisites:**
- TalentLink must be configured with these custom fields in the Contract form
- Field names must match exactly (case-sensitive)
- Free-form fields must be enabled for the contract type being used

---

### 3.3 BankInfo and Benefit Entities

**Implementation Status:** 0% - Completely missing
**Data Source:** TalentLink SOAP via Contract free-form fields

#### Access Method:

Same as PersonInfo entities - via `ContractDto.freeFormFields`

#### Expected Custom Field Names:

**BankInfo Fields (9 fields):**
```
- BANK_CODE
- BRANCH_CODE
- ACCOUNT_NO
- ACCOUNT_NAME
- ACCOUNT_TYPE
- PAYMENT_METHOD
- BANK_NAME
- BRANCH_NAME
```

**BenefitMain Fields (9 fields):**
```
- BENEFIT_MEDICAL
- BENEFIT_DENTAL
- BENEFIT_HOUSING
- BENEFIT_PREVENTIVE_CHECKUP
- BENEFIT_START_DATE
- BENEFIT_END_DATE
```

**BenefitSpouse Fields (17 fields):**
```
- SPOUSE_BENEFIT_MEDICAL
- SPOUSE_BENEFIT_DENTAL
- SPOUSE_BENEFIT_HOUSING
- SPOUSE_BENEFIT_PREVENTIVE_CHECKUP
- SPOUSE_BENEFIT_START_DATE
- SPOUSE_BENEFIT_END_DATE
... (similar structure to BenefitMain)
```

**BenefitChild Fields (12 fields per child, supports 6 children):**
```
- CHILD_1_BENEFIT_MEDICAL
- CHILD_1_BENEFIT_DENTAL
- CHILD_1_BENEFIT_START_DATE
- CHILD_1_BENEFIT_END_DATE
... repeated for CHILD_2 through CHILD_6
```

#### Implementation Approach:

**Add to ContractFreeFormFieldParser:**
```java
public BankInfo extractBankInfo(List<FreeFormFieldContract> freeFormFields) {
    BankInfo bankInfo = new BankInfo();

    for (FreeFormFieldContract field : freeFormFields) {
        switch (field.getName()) {
            case "BANK_CODE":
                bankInfo.setBankCode(field.getValue());
                break;
            case "BRANCH_CODE":
                bankInfo.setBranchCode(field.getValue());
                break;
            // ... map all 9 fields
        }
    }

    return bankInfo;
}

public BenefitMain extractBenefitMain(List<FreeFormFieldContract> freeFormFields) {
    // Extract benefit fields
}

public BenefitSpouse extractBenefitSpouse(List<FreeFormFieldContract> freeFormFields) {
    // Extract spouse benefit fields
}

public List<BenefitChild> extractBenefitChildren(List<FreeFormFieldContract> freeFormFields) {
    // Extract CHILD_1 through CHILD_6 benefit fields
}
```

---

### 3.4 PhotoUpload Entity

**Implementation Status:** 0% - Completely missing
**Data Source:** TalentLink documents via existing API

#### Access Method:

**Use existing document APIs** that are already implemented:

1. **Call:** `getApplicationsByCandidateId(candidateId)`
2. **Navigate to:** `ApplicationDto.documents.document` list
3. **Filter by:** Document type = "PHOTO" or document name contains "photo"
4. **Download:** Use existing `downloadAttachedFile(documentId)` (REST API)
5. **Store:** Save BLOB to `RDPS_PHOTO_UPLOAD` table

#### Implementation Approach:

**Extend CandidateDocumentSyncService:**
```java
@Service
public class CandidatePhotoSyncService {

    @Autowired
    private TalentLinkSOAPCandidateService talentLinkService;

    @Autowired
    private PhotoUploadDAO photoUploadDAO;

    public void syncCandidatePhoto(String candidateId, Long tlCandidateId) {
        // 1. Get applications
        List<ApplicationDto> applications =
            talentLinkService.getApplicationsByCandidateId(tlCandidateId);

        for (ApplicationDto app : applications) {
            List<DocumentBaseDto> documents = app.getDocuments().getDocument();

            // 2. Find photo documents
            for (DocumentBaseDto doc : documents) {
                if (isPhotoDocument(doc)) {
                    // 3. Download photo
                    AttachedFileDto file =
                        talentLinkService.downloadAttachedFile(doc.getId());

                    // 4. Save to database
                    PhotoUpload photo = new PhotoUpload();
                    photo.setCandidateId(candidateId);
                    photo.setEmpPhoto(file.getData()); // BLOB
                    photo.setFileName(file.getName());
                    photo.setFileType(file.getContentType());

                    photoUploadDAO.insert(photo);
                }
            }
        }
    }

    private boolean isPhotoDocument(DocumentBaseDto doc) {
        String docType = doc.getType();
        String docName = doc.getName();

        return "PHOTO".equalsIgnoreCase(docType) ||
               (docName != null && docName.toLowerCase().contains("photo"));
    }
}
```

**NO NEW SOAP API NEEDED** - uses existing document operations

---

### 3.5 ToEBS Entity (Integration Staging Table)

**Implementation Status:** 0% - Completely missing
**Data Source:** Internal RDPS system (not from TalentLink)

#### Purpose:

`RDPS_TOEBS` is a **staging table** for exporting data to Oracle E-Business Suite (EBS). It does NOT require SOAP API integration.

#### Fields (9 fields):
- `CANDIDATE_ID` - RDPS candidate ID
- `REQ_NUMBER` - Requisition number
- `EBS_READY` - Flag indicating ready for EBS export (Y/N)
- `EMPLOYEE_NUMBER` - EBS employee number (generated by EBS)
- `ASSIGNMENT_NUMBER` - EBS assignment number (generated by EBS)
- `STAGE` - Export stage (PENDING, IN_PROGRESS, COMPLETED, ERROR)
- `ERROR_MESSAGE` - Error message if export fails
- `EXPORT_DATE` - Date exported to EBS
- `LAST_UPDATE_DATE` - Last update timestamp

#### Implementation Approach:

**NO SOAP API NEEDED** - This is purely internal:

1. **Create database table:** `RDPS_TOEBS`
2. **Create model class:** `ToEBS.java`
3. **Create DAO class:** `ToEBSDAO.java`
4. **Create export service:**
```java
@Service
public class EBSExportService {

    public void markCandidateReadyForEBS(String candidateId, String reqNumber) {
        ToEBS toEBS = new ToEBS();
        toEBS.setCandidateId(candidateId);
        toEBS.setReqNumber(reqNumber);
        toEBS.setEbsReady("Y");
        toEBS.setStage("PENDING");

        toEBSDAO.insert(toEBS);
    }

    public void exportToEBS(String candidateId) {
        // 1. Fetch all candidate data from RDPS tables
        // 2. Call EBS integration (SOAP/REST/File)
        // 3. Update RDPS_TOEBS with EBS-generated IDs and status
    }
}
```

5. **EBS Integration:** Separate integration module needed (outside scope of TalentLink SOAP)

---

## Part 4: Implementation Summary

### 4.1 Required SOAP API Implementations

| Entity | SOAP Operation | Status | Complexity | Priority |
|--------|----------------|--------|------------|----------|
| **Offer** | `getContracts()` | NOT IMPLEMENTED | Medium | HIGH |
| **Offer** | `getContractsByApplicationId()` | NOT IMPLEMENTED | Medium | HIGH |
| **PersonInfo** | Via `ContractDto.freeFormFields` | NOT IMPLEMENTED | High | MEDIUM |
| **PersonSpouse** | Via `ContractDto.freeFormFields` | NOT IMPLEMENTED | Medium | MEDIUM |
| **PersonChild** | Via `ContractDto.freeFormFields` | NOT IMPLEMENTED | Medium | MEDIUM |
| **PersonECP** | Via `ContractDto.freeFormFields` | NOT IMPLEMENTED | Low | MEDIUM |
| **BankInfo** | Via `ContractDto.freeFormFields` | NOT IMPLEMENTED | Low | MEDIUM |
| **BenefitMain** | Via `ContractDto.freeFormFields` | NOT IMPLEMENTED | Medium | MEDIUM |
| **BenefitSpouse** | Via `ContractDto.freeFormFields` | NOT IMPLEMENTED | Medium | LOW |
| **BenefitChild** | Via `ContractDto.freeFormFields` | NOT IMPLEMENTED | Medium | LOW |
| **PhotoUpload** | Via existing `downloadAttachedFile()` | NOT IMPLEMENTED | Low | LOW |
| **ToEBS** | NO SOAP - Internal staging | NOT IMPLEMENTED | Low | LOW |

---

### 4.2 SOAP Service Methods to Add

**To `TalentLinkSOAPCandidateService`:**

```java
/**
 * Fetch contracts/offers from TalentLink
 * @param page Page number for pagination
 * @return List of contract DTOs
 */
public List<ContractDto> getContracts(Integer page) {
    try {
        return candidateService.getContracts(page, false);
    } catch (Exception e) {
        logger.error("Error fetching contracts", e);
        throw new RuntimeException("Failed to fetch contracts from TalentLink", e);
    }
}

/**
 * Fetch contracts for a specific application
 * @param applicationId Application ID
 * @return List of contract DTOs for the application
 */
public List<ContractDto> getContractsByApplicationId(Long applicationId) {
    try {
        return candidateService.getContractsByApplicationId(applicationId, false);
    } catch (Exception e) {
        logger.error("Error fetching contracts for application {}", applicationId, e);
        throw new RuntimeException("Failed to fetch contracts from TalentLink", e);
    }
}
```

---

### 4.3 New Services to Create

**1. ContractFreeFormFieldParser**
- **Location:** `src/main/java/eduhk/fhr/web/parser/ContractFreeFormFieldParser.java`
- **Purpose:** Parse free-form fields from ContractDto
- **Methods:**
  - `extractPersonInfo(List<FreeFormFieldContract> fields) → PersonInfo`
  - `extractPersonSpouse(List<FreeFormFieldContract> fields) → PersonSpouse`
  - `extractPersonChildren(List<FreeFormFieldContract> fields) → List<PersonChild>`
  - `extractPersonECP(List<FreeFormFieldContract> fields) → PersonECP`
  - `extractBankInfo(List<FreeFormFieldContract> fields) → BankInfo`
  - `extractBenefitMain(List<FreeFormFieldContract> fields) → BenefitMain`
  - `extractBenefitSpouse(List<FreeFormFieldContract> fields) → BenefitSpouse`
  - `extractBenefitChildren(List<FreeFormFieldContract> fields) → List<BenefitChild>`

**2. OfferImportService**
- **Location:** `src/main/java/eduhk/fhr/web/service/import_/OfferImportService.java`
- **Purpose:** Import offer/contract data from TalentLink
- **Methods:**
  - `importOffers(String candidateId, Long applicationId)`
  - Uses `ContractMapper` to map ContractDto → Offer model

**3. OnboardingImportService**
- **Location:** `src/main/java/eduhk/fhr/web/service/import_/OnboardingImportService.java`
- **Purpose:** Import person info, spouse, children, emergency contact
- **Methods:**
  - `importOnboardingData(String candidateId, Long applicationId)`
  - Uses `ContractFreeFormFieldParser` to extract data

**4. BenefitsImportService**
- **Location:** `src/main/java/eduhk/fhr/web/service/import_/BenefitsImportService.java`
- **Purpose:** Import bank and benefits data
- **Methods:**
  - `importBenefitsData(String candidateId, Long applicationId)`
  - Uses `ContractFreeFormFieldParser` to extract data

**5. CandidatePhotoSyncService**
- **Location:** `src/main/java/eduhk/fhr/web/service/CandidatePhotoSyncService.java`
- **Purpose:** Sync candidate photos from TalentLink documents
- **Methods:**
  - `syncCandidatePhoto(String candidateId, Long tlCandidateId)`
  - Uses existing `downloadAttachedFile()` REST API

---

### 4.4 Mappers to Create

**1. ContractMapper**
- **Location:** `src/main/java/eduhk/fhr/web/mapper/ContractMapper.java`
- **Purpose:** Map ContractDto → Offer model
- **Complexity:** Medium
- **Key Mappings:**
  - ContractDto.id → Offer.offerId
  - ContractDto.status → Offer.offerStatus
  - ContractDto.plannedStartDate → Offer.contractStartDate
  - ContractDto.plannedEndDate → Offer.contractEndDate
  - ContractDto.probationaryPeriod → Offer.probationLength + probationUnits
  - ContractDto.permanentContract.salary → Offer.salaryAmount
  - Extract offer fields from freeFormFields (PAY_BASIS, EMP_DEP, TOA, etc.)

---

### 4.5 Database Tables Already Defined

**All database tables are already defined in `db_scripts/` but need corresponding Java models:**

**Already Implemented:**
- ✅ RDPS_CANDIDATE
- ✅ RDPS_EDU_PROF_QUAL
- ✅ RDPS_WORK_EXPERIENCE
- ✅ RDPS_REFEREE
- ✅ RDPS_OTHER_INFO

**Need Implementation:**
- ❌ RDPS_OFFER (30 fields)
- ❌ RDPS_PERSON_INFO (34 fields)
- ❌ RDPS_PERSON_SPOUSE (20 fields)
- ❌ RDPS_PERSON_CHILD (21 fields, SEQ 1-6)
- ❌ RDPS_PERSON_ECP (11 fields)
- ❌ RDPS_BANK_INFO (9 fields)
- ❌ RDPS_BENEFIT_MAIN (9 fields)
- ❌ RDPS_BENEFIT_SPOUSE (17 fields)
- ❌ RDPS_BENEFIT_CHILD (12 fields, SEQ 1-6)
- ❌ RDPS_PHOTO_UPLOAD (7 fields with BLOB)
- ❌ RDPS_TOEBS (9 fields)

---

## Part 5: TalentLink Configuration Requirements

### 5.1 Free-Form Fields Configuration

**CRITICAL:** For PersonInfo, Benefits, and BankInfo entities to work, TalentLink must be configured with custom free-form fields in the Contract form.

**Configuration Steps (performed by TalentLink Administrator):**

1. **Navigate to:** TalentLink Admin → Contract Configuration → Free-Form Fields
2. **Create fields** for each entity with **exact field names** as specified in Part 3
3. **Field settings:**
   - Type: TEXT, NUMBER, DATE (as appropriate)
   - Required: Set based on business rules
   - Visible: Yes
   - Editable: Yes

**Example Configuration:**

```
Field Name: PERSON_TITLE
Type: TEXT
Max Length: 15
Required: Yes
Default Value: (empty)

Field Name: PERSON_DATE_OF_BIRTH
Type: DATE
Format: YYYY-MM-DD
Required: Yes

Field Name: BANK_CODE
Type: TEXT
Max Length: 10
Required: No

... etc for all 150+ fields
```

### 5.2 Field Name Conventions

**IMPORTANT:** Field names are **case-sensitive**. Parser expects EXACT matches:
- ✅ `PERSON_TITLE` (correct)
- ❌ `person_title` (won't match)
- ❌ `PersonTitle` (won't match)

### 5.3 Validation

**Before Implementation:**
1. Export sample ContractDto from TalentLink
2. Verify freeFormFields contains expected field names
3. Confirm field values are populated correctly
4. Test with real candidate data

**Verification Query:**
```java
ContractDto contract = soapService.getContractsByApplicationId(applicationId).get(0);
List<FreeFormFieldContract> fields = contract.getFreeFormFields().getFreeFormField();

// Print all fields
for (FreeFormFieldContract field : fields) {
    System.out.println("Field: " + field.getName() + " = " + field.getValue());
}
```

---

## Part 6: Complete SOAP Operations Reference

### 6.1 Currently Implemented (9 operations)

| Operation | Service | Status | Used In |
|-----------|---------|--------|---------|
| `getCandidates()` | Candidate | ✅ IMPLEMENTED | ImportOrchestrationService |
| `getCandidateById()` | Candidate | ✅ IMPLEMENTED | ImportOrchestrationService |
| `getApplicationsByCandidateId()` | Candidate | ✅ IMPLEMENTED | CandidateDocumentSyncService |
| `downloadAttachedFile()` | Candidate | ✅ IMPLEMENTED (REST) | CandidateDocumentSyncService |
| `createCandidateViaFolder()` | Candidate | ✅ AVAILABLE | (Not currently used) |
| `getUsers()` | User | ✅ IMPLEMENTED | TalentLinkUserSyncService |
| `createUser()` | User | ✅ IMPLEMENTED | TalentLinkUserSyncService |
| `updateUser()` | User | ✅ IMPLEMENTED | TalentLinkUserSyncService |
| `deactivateUser()` | User | ✅ IMPLEMENTED | TalentLinkUserSyncService |

---

### 6.2 Need Implementation (2 operations for Offer entity)

| Operation | Service | Status | Priority | For Entity |
|-----------|---------|--------|----------|------------|
| `getContracts()` | Candidate | ❌ NOT IMPLEMENTED | HIGH | Offer |
| `getContractsByApplicationId()` | Candidate | ❌ NOT IMPLEMENTED | HIGH | Offer |

---

### 6.3 Available But Not Needed (61 operations)

These operations are available in the SOAP stubs but not required for current functionality:

**Candidate Service (30):**
- Tag operations (6): addTag, editTag, removeTag, getTagTypes, getTagsForType, getTaggedProfiles
- Application operations (4): getApplicationById, getApplications, getApplicationsByJob, getApplicationsByStatus
- Candidate management (7): createCandidateViaJobNumber, createCandidateViaOpening, getArchivedCandidates, getCandidateHistory, updateCandidatePif, getCandidateStructuredDocumentById, uploadAttachedFile
- Consent operations (3): getConsents, acceptCandidateConsent, revokeCandidateConsent
- Template operations (1): getCurrentBoCpTemplateByType

**User Service (31):**
- User management (10): activateUser, getUserById, getUserExtById, getUserData, getInactiveUsers, deleteUser, changePassword, generatePassword, generateTemporaryPassword, configureUser
- Role management (10): assignRole, assignRoles, assignRolesWithTag, clearAllRoleAssignments, removeRoleAssignment, removeRolesWithTag, getRolesAssignedForUser, getRightsForDepartmentAndCurrentUser, getRightsWithLabels, deleteRoles
- Tag operations (5): addTag, removeTag, getTagTypes, getTaggedUsers, getTagsForType
- Password operations (2): getPasswordRules, updatePasswordRules
- Other (4): removeFederationData, getGrantedPools, getCurrentUserExt

---

## Part 7: Conclusion

### 7.1 Summary

**Current State:**
- 9 SOAP operations implemented (Candidate: 5, User: 4)
- Focus on candidate import and user synchronization
- Document sync infrastructure in place

**What's Missing:**
- **2 SOAP operations** for Offer entity (already exist in stubs, just need implementation)
- **Parser/mapper infrastructure** for extracting data from Contract free-form fields
- **Database models and DAOs** for 11 entities

**Data Source Strategy:**
- **Offer:** Direct from ContractDto standard fields
- **PersonInfo/Spouse/Child/ECP:** From Contract free-form fields
- **BankInfo/Benefits:** From Contract free-form fields
- **PhotoUpload:** From existing document API
- **ToEBS:** Internal staging (no SOAP)

### 7.2 Key Insights

1. **No New SOAP Operations Needed:** TalentLink already provides all necessary SOAP operations. Only 2 operations (`getContracts`, `getContractsByApplicationId`) need to be implemented.

2. **Free-Form Fields Are Critical:** Most missing entities (PersonInfo, Benefits, Bank) depend on TalentLink being configured with appropriate custom fields in the Contract form.

3. **Configuration Over Code:** Success depends heavily on proper TalentLink configuration. Field names must match exactly.

4. **Incremental Implementation:** Can be built incrementally:
   - Phase 1: Basic offer data (standard ContractDto fields)
   - Phase 2: Onboarding data (free-form fields)
   - Phase 3: Benefits/Bank (free-form fields)
   - Phase 4: Photos and EBS staging

### 7.3 Next Steps

**For Documentation Review:**
1. Review this document with TalentLink administrator
2. Verify free-form field naming conventions
3. Confirm which entities are stored in TalentLink vs. RDPS

**For Implementation (if proceeding):**
1. Start with Phase 1 (Offer Management)
2. Coordinate with TalentLink admin for free-form field configuration
3. Test with sample data before full implementation
4. Build incrementally with thorough testing at each phase

---

## Appendix: File Reference

### Current SOAP Service Classes

| File | Purpose | Lines |
|------|---------|-------|
| `src/main/java/eduhk/fhr/web/service/TalentLinkSOAPCandidateService.java` | Candidate SOAP operations | ~700 |
| `src/main/java/eduhk/fhr/web/service/TalentLinkSOAPUserService.java` | User SOAP operations | ~200 |
| `src/main/java/eduhk/fhr/web/soap/handler/TalentLinkSOAPHandler.java` | WS-Security authentication | ~100 |

### Current Import Services

| File | Purpose | Lines |
|------|---------|-------|
| `src/main/java/eduhk/fhr/web/service/import_/ImportOrchestrationService.java` | Main import coordinator | ~250 |
| `src/main/java/eduhk/fhr/web/service/import_/CandidateImportService.java` | Candidate import | ~100 |
| `src/main/java/eduhk/fhr/web/service/import_/EducationImportService.java` | Education import | ~80 |
| `src/main/java/eduhk/fhr/web/service/import_/WorkExperienceImportService.java` | Work experience import | ~80 |
| `src/main/java/eduhk/fhr/web/service/import_/RefereeImportService.java` | Referee import | ~70 |
| `src/main/java/eduhk/fhr/web/service/import_/OtherInfoImportService.java` | Other info import | ~70 |

### SOAP Stub Files

| Directory | Files | Purpose |
|-----------|-------|---------|
| `src/main/java/eduhk/fhr/web/soap/candidate/` | 200+ files | Candidate service stubs |
| `src/main/java/eduhk/fhr/web/soap/user/` | 100+ files | User service stubs |

### Key SOAP DTOs

| File | Purpose |
|------|---------|
| `src/main/java/eduhk/fhr/web/soap/candidate/Profile.java` | Candidate profile |
| `src/main/java/eduhk/fhr/web/soap/candidate/PersonalData.java` | Personal information |
| `src/main/java/eduhk/fhr/web/soap/candidate/ApplicationDto.java` | Job application |
| `src/main/java/eduhk/fhr/web/soap/candidate/ContractDto.java` | Contract/Offer (1640 lines) |
| `src/main/java/eduhk/fhr/web/soap/candidate/CandidateWebService.java` | Service interface |

---

**End of Documentation**
