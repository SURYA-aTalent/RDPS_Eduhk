# Education/Work Experience Sync Workflow Documentation

**Last Updated:** December 4, 2025
**Status:** Implemented - Data Mapping Issue in Progress

---

## Table of Contents
1. [Overview](#overview)
2. [Complete Data Flow](#complete-data-flow)
3. [Detailed Component Breakdown](#detailed-component-breakdown)
4. [Files Modified/Created](#files-modifiedcreated)
5. [Fixes Applied](#fixes-applied)
6. [Current Status](#current-status)
7. [Flow Diagram](#flow-diagram)

---

## Overview

This document describes the end-to-end workflow for syncing candidate education, work experience, referee, and other information from TalentLink to the RDPS database.

### Problem Solved
Previously, candidate sync only imported basic candidate information to `RDPS_CANDIDATE` table. Education, work experience, and other related data existed in TalentLink application-specific documents but was not being synced.

### Solution Implemented
Integrated TalentLink Document SOAP API to retrieve structured documents attached to candidate applications, parse the education/work/referee data, and import to respective database tables.

---

## Complete Data Flow

### Flow Sequence

```
1. HTTP Request (Test Endpoint)
   ↓
2. TestSyncController.syncCandidate()
   ↓
3. CandidateImportService.importCandidate()
   │
   ├─→ Step 1: Import Candidate Basic Info
   │   └─→ CandidateMapper → CandidateDAO → RDPS_CANDIDATE ✅
   │
   ├─→ Step 2: Enrich with Document Data (NEW)
   │   │
   │   ├─→ 2.1: Get Applications
   │   │   └─→ TalentLinkSOAPCandidateService.getApplicationsByCandidateId()
   │   │       └─→ TalentLink Candidate SOAP API
   │   │
   │   ├─→ 2.2: Get Documents per Application
   │   │   └─→ TalentLinkSOAPDocumentService.getDocumentsByApplicationId()
   │   │       └─→ TalentLink Document SOAP API
   │   │
   │   ├─→ 2.3: Find Education/Work Document
   │   │   └─→ CandidateImportService.findEducationWorkDocumentId()
   │   │       └─→ Searches for "EdUHK Education/Work Experience"
   │   │
   │   ├─→ 2.4: Retrieve Structured Document
   │   │   └─→ TalentLinkSOAPDocumentService.getStructuredDocumentByDocumentId()
   │   │       └─→ TalentLink Document SOAP API
   │   │
   │   └─→ 2.5: Parse Document Answers
   │       └─→ EducationWorkDocumentParser.parseEducationWorkDocument()
   │           └─→ Returns ParsedCandidateData
   │
   ├─→ Step 3: Import Education
   │   └─→ EducationImportService → EducationMapper → EducationDAO → RDPS_EDU_PROF_QUAL ⚠️
   │
   ├─→ Step 4: Import Work Experience
   │   └─→ WorkExperienceImportService → WorkExperienceMapper → WorkExperienceDAO → RDPS_WORK_EXPERIENCE ⚠️
   │
   ├─→ Step 5: Import Referees
   │   └─→ RefereeImportService → RefereeMapper → RefereeDAO → RDPS_REFEREE
   │
   └─→ Step 6: Import Other Info
       └─→ OtherInfoImportService → OtherInfoMapper → OtherInfoDAO → RDPS_OTHER_INFO
```

**Legend:**
- ✅ Working correctly
- ⚠️ Executing but failing due to NULL EDU_LEVEL field

---

## Detailed Component Breakdown

### 1. Entry Point

#### TestSyncController
**File:** `src/main/java/eduhk/fhr/web/controller/TestSyncController.java`

**Purpose:** HTTP endpoint to trigger candidate sync for testing

**Endpoint:** `GET /api/test-sync/candidate/{candidateId}`

**Usage:**
```bash
curl http://localhost:8080/api/test-sync/candidate/116
```

**Response:** JSON with sync status, batch ID, errors (if any)

---

### 2. Orchestration Layer

#### CandidateImportService
**File:** `src/main/java/eduhk/fhr/web/service/import_/CandidateImportService.java`

**Purpose:** Main orchestrator for complete candidate data import

**Key Methods:**

1. **`importCandidate(candidateDTO, batchId)`**
   - Entry point for candidate import
   - Coordinates all import steps
   - Handles validation and error logging

2. **`enrichCandidateWithDocumentData(candidateDTO, candidateId)` ⭐ NEW**
   - Fetches applications for candidate
   - Retrieves documents for each application
   - Finds Education/Work Experience document
   - Parses document and enriches DTO with data

3. **`findEducationWorkDocumentId(documents)` ⭐ NEW**
   - Searches document list for structured document
   - Matches by: `objectType="STRUCTURED"` AND name contains "Education" or "Work Experience"
   - Returns document ID if found

**Dependencies:**
- `TalentLinkSOAPCandidateService` - Get applications
- `TalentLinkSOAPDocumentService` - Get/retrieve documents
- `EducationWorkDocumentParser` - Parse document
- `EducationImportService` - Import education
- `WorkExperienceImportService` - Import work experience
- `RefereeImportService` - Import referees
- `OtherInfoImportService` - Import other info

---

### 3. SOAP Services (NEW)

#### TalentLinkSOAPDocumentService
**File:** `src/main/java/eduhk/fhr/web/service/TalentLinkSOAPDocumentService.java`

**Purpose:** Service to interact with TalentLink Document SOAP API

**Configuration:**
- Endpoint URL from database parameter: `TALENTLINK_DOCUMENT_SOAP_URL`
- Uses WS-Security authentication via `TalentLinkSOAPHandler`
- Credentials from `RDPS_PARAMETER` table

**Methods:**

1. **`getDocumentsByApplicationId(Long applicationId)`**
   - Retrieves all documents attached to an application
   - Returns: `List<Object>` - XML elements representing documents

2. **`getDocumentsByCandidateId(Long candidateId)`**
   - Retrieves all documents for a candidate across all applications
   - Returns: `List<Object>` - XML elements

3. **`getStructuredDocumentByDocumentId(Long documentId)`**
   - Retrieves full structured document with all question answers
   - Parameters:
     - `showLocalizedValues = true`
     - `langCode = LangCode.UK` (English)
   - Returns: `StructuredDocument` with nested `GfDocument` containing answers

#### TalentLinkSOAPCandidateService (Extended)
**File:** `src/main/java/eduhk/fhr/web/service/TalentLinkSOAPCandidateService.java`

**New Method Added:**

**`getApplicationsByCandidateId(Long candidateId)`**
- Retrieves all job applications for a candidate
- Returns: `List<ApplicationDto>` - Each application with ID, requisition, status, etc.

---

### 4. Document Parser (NEW)

#### EducationWorkDocumentParser
**File:** `src/main/java/eduhk/fhr/web/service/EducationWorkDocumentParser.java`

**Purpose:** Parses structured document answers to extract education, work, referee data

**Method:** `parseEducationWorkDocument(StructuredDocument document)`

**Process:**
1. Extracts `GfDocument` containing all answers
2. Iterates through each `Answer` object
3. Identifies question type by text matching:
   - Education questions (institution, degree, dates)
   - Work experience questions (company, title, dates)
   - Referee questions (name, contact, relationship)
   - Other info questions
4. Maps answers to appropriate DTO objects
5. Returns `ParsedCandidateData` with all collections populated

**Returns:** `ParsedCandidateData` containing:
- `List<TalentLinkEducationDTO>` - Education/qualification records
- `List<TalentLinkWorkExperienceDTO>` - Work experience records
- `List<TalentLinkRefereeDTO>` - Referee/reference records
- `TalentLinkOtherInfoDTO` - Additional information

**Current Issue:** Not correctly extracting `EDU_LEVEL` field, resulting in NULL value

---

### 5. Data Container (NEW)

#### ParsedCandidateData
**File:** `src/main/java/eduhk/fhr/web/service/ParsedCandidateData.java`

**Purpose:** Simple POJO to hold parsed data from documents

**Fields:**
- `List<TalentLinkEducationDTO> education`
- `List<TalentLinkWorkExperienceDTO> workExperience`
- `List<TalentLinkRefereeDTO> referees`
- `TalentLinkOtherInfoDTO otherInfo`

---

### 6. Import Services

#### EducationImportService
**File:** `src/main/java/eduhk/fhr/web/service/import_/EducationImportService.java`

**Purpose:** Imports education/qualification records for a candidate

**Method:** `importEducation(candidateDTO, candidateId, reqNumber)`

**Process:**
1. Delete existing education records for candidate (replace strategy)
2. For each education DTO in list:
   - Map to `Education` model using `EducationMapper`
   - Validate using mapper's `validate()` method
   - Insert to database using `EducationDAO.insertEducation()`
   - Increment sequence number
3. Log success/failure

**Database Table:** `RDPS_EDU_PROF_QUAL`

**Fix Applied:** All code was commented out - uncommented to enable functionality

---

#### WorkExperienceImportService
**File:** `src/main/java/eduhk/fhr/web/service/import_/WorkExperienceImportService.java`

**Purpose:** Imports work experience records for a candidate

**Method:** `importWorkExperience(candidateDTO, candidateId, reqNumber)`

**Process:**
1. Delete existing work experience records for candidate
2. For each work experience DTO in list:
   - Map to `WorkExperience` model using `WorkExperienceMapper`
   - Validate using mapper
   - Insert to database using `WorkExperienceDAO.insertWorkExperience()`
   - Increment sequence number
3. Log success/failure

**Database Table:** `RDPS_WORK_EXPERIENCE`

**Fix Applied:** All code was commented out - uncommented to enable functionality

---

#### RefereeImportService
**File:** `src/main/java/eduhk/fhr/web/service/import_/RefereeImportService.java`

**Purpose:** Imports referee/reference records

**Database Table:** `RDPS_REFEREE`

---

#### OtherInfoImportService
**File:** `src/main/java/eduhk/fhr/web/service/import_/OtherInfoImportService.java`

**Purpose:** Imports additional candidate information

**Database Table:** `RDPS_OTHER_INFO`

---

### 7. Data Mapping Layer

#### EducationMapper
**File:** `src/main/java/eduhk/fhr/web/mapper/EducationMapper.java`

**Purpose:** Maps `TalentLinkEducationDTO` to `Education` model

**Key Operations:**
- Uses `LookupValueResolver` to convert text values to LOV codes:
  - Education Level → `EDU_LEVEL_CODE` (from `RDPS_LOV_EDU_LEVEL`)
  - Study Mode → `STUDY_MODE_CODE` (from `RDPS_LOV_STUDY_MODE`)
  - Award Class → `QUAL_AWARD_CLASS_CODE` (from `RDPS_LOV_QUAL_AWARD_CLASS`)
  - Award Description → `QUAL_AWARD_DESC_CODE` (from `RDPS_LOV_QUAL_AWARD_DESC`)
- Parses dates (start date, award date)
- Sets candidate_id, requisition number, sequence
- Validates all required fields

**Dependencies:**
- `LookupValueResolver` - LOV code resolution

---

#### WorkExperienceMapper
**File:** `src/main/java/eduhk/fhr/web/mapper/WorkExperienceMapper.java`

**Purpose:** Maps `TalentLinkWorkExperienceDTO` to `WorkExperience` model

**Key Operations:**
- Maps job title, company name, responsibilities
- Parses employment dates (start, end)
- Calculates duration if needed
- Sets candidate_id, requisition number, sequence
- Validates required fields

---

#### LookupValueResolver
**File:** `src/main/java/eduhk/fhr/web/mapper/LookupValueResolver.java`

**Purpose:** Resolves text values to database LOV (List of Values) codes

**Initialization:**
- Loads LOV data from database on startup (`@PostConstruct`)
- Caches values in memory for fast lookup

**Cached Tables:**
- `RDPS_LOV_EDU_LEVEL` - Education levels (19 values)
- `RDPS_LOV_STUDY_MODE` - Study modes (7 values)
- `RDPS_LOV_QUAL_AWARD_CLASS` - Award classes (16 values)
- `RDPS_LOV_QUAL_AWARD_DESC` - Award descriptions (35 values)
- `RDPS_LOV_DISTRICT` - Hong Kong districts (19 values)

**Resolution Methods:**
- `resolveEducationLevel(String text)` → Returns `EDU_LEVEL_CODE`
- `resolveStudyMode(String text)` → Returns `STUDY_MODE_CODE`
- `resolveAwardClass(String text)` → Returns `QUAL_AWARD_CLASS_CODE`
- `resolveAwardDescription(String text)` → Returns `QUAL_AWARD_DESC_CODE`

**Matching Logic:**
- Case-insensitive partial matching
- Handles common variations and abbreviations

---

### 8. Database Access Layer (DAOs)

#### EducationDAO
**File:** `src/main/java/eduhk/fhr/web/dao/EducationDAO.java`

**Methods:**
- `insertEducation(Education education)` - INSERT statement
- `deleteEducationByCandidateId(candidateId, reqNumber)` - DELETE by candidate

**SQL Table:** `RDPS_EDU_PROF_QUAL`

**Required Fields:**
- `CANDIDATE_ID` - NOT NULL
- `REQ_NUMBER` - NOT NULL
- `SEQ` - NOT NULL
- `EDU_LEVEL` - NOT NULL ⚠️ Currently causing insert failure

---

#### WorkExperienceDAO
**File:** `src/main/java/eduhk/fhr/web/dao/WorkExperienceDAO.java`

**Methods:**
- `insertWorkExperience(WorkExperience workExp)` - INSERT statement
- `deleteWorkExperienceByCandidateId(candidateId, reqNumber)` - DELETE by candidate

**SQL Table:** `RDPS_WORK_EXPERIENCE`

---

#### RefereeDAO
**File:** `src/main/java/eduhk/fhr/web/dao/RefereeDAO.java`

**SQL Table:** `RDPS_REFEREE`

---

#### OtherInfoDAO
**File:** `src/main/java/eduhk/fhr/web/dao/OtherInfoDAO.java`

**SQL Table:** `RDPS_OTHER_INFO`

---

### 9. SOAP Stubs (Generated)

#### Document Service Stubs
**Location:** `src/main/java/eduhk/fhr/web/soap/document/`

**Total Files:** 59 Java files

**Key Files:**
- `DocumentWebService.java` - Service interface (14 methods)
- `DocumentWebService_Service.java` - Service factory
- `StructuredDocument.java` - Document structure
- `GfDocument.java` - Document answers container
- `Answer.java` - Individual question answer
- `LangCode.java` - Language codes enum
- `AttachedFileDto.java` - File attachment DTO
- `FormAnsweredDto.java` - Form answer DTO
- Plus 51 other supporting classes

**Generation Source:**
- Copied from DHLTLKConnector project
- Package changed: `com.mrted.ws.documents` → `eduhk.fhr.web.soap.document`
- Converted from `javax.*` to `jakarta.*` packages (Jakarta EE 4.0)

---

### 10. Database Configuration

#### New Parameter Added
**File:** `db_scripts/20_ADD_DOCUMENT_SOAP_URL.sql`

**SQL:**
```sql
INSERT INTO RDPS_PARAMETER (PARAM_CODE, PARAM_VALUE, ACTIVE, TIMESTAMP, USERSTAMP)
VALUES (
    'TALENTLINK_DOCUMENT_SOAP_URL',
    'https://api3.lumesse-talenthub.com/HRIS/SOAP/Document',
    'Y',
    SYSTIMESTAMP,
    'SYSTEM'
);
```

**Table:** `RDPS_PARAMETER`

**Purpose:** Stores Document SOAP API endpoint URL

**Usage:** Loaded by `TalentLinkSOAPDocumentService` on initialization

---

### 11. Database Tables (Destination)

#### RDPS_CANDIDATE
- **Purpose:** Basic candidate information
- **Key:** `CANDIDATE_ID` (Primary Key)
- **Status:** ✅ Working

#### RDPS_EDU_PROF_QUAL
- **Purpose:** Education and professional qualifications
- **Key:** `QUAL_ID` (Primary Key), `CANDIDATE_ID` (Foreign Key)
- **Status:** ⚠️ Insert failing - EDU_LEVEL is NULL

#### RDPS_WORK_EXPERIENCE
- **Purpose:** Work experience records
- **Key:** `EXP_ID` (Primary Key), `CANDIDATE_ID` (Foreign Key)
- **Status:** ⚠️ Not yet tested (education fails first)

#### RDPS_REFEREE
- **Purpose:** Referee/reference information
- **Key:** `REFEREE_ID` (Primary Key), `CANDIDATE_ID` (Foreign Key)
- **Status:** ⏸️ Not yet executed

#### RDPS_OTHER_INFO
- **Purpose:** Additional candidate information
- **Key:** `INFO_ID` (Primary Key), `CANDIDATE_ID` (Foreign Key)
- **Status:** ⏸️ Not yet executed

---

## Files Modified/Created

### New Files Created (62 total)

#### Services (3)
1. `src/main/java/eduhk/fhr/web/service/TalentLinkSOAPDocumentService.java`
2. `src/main/java/eduhk/fhr/web/service/EducationWorkDocumentParser.java`
3. `src/main/java/eduhk/fhr/web/service/ParsedCandidateData.java`

#### SOAP Stubs (59)
4-62. `src/main/java/eduhk/fhr/web/soap/document/*.java` (59 files)
   - Service interfaces, DTOs, request/response wrappers
   - Document structures, answer containers
   - Supporting classes for SOAP operations

#### Database Scripts (1)
63. `db_scripts/20_ADD_DOCUMENT_SOAP_URL.sql`

---

### Files Modified (6)

#### 1. CandidateImportService.java
**File:** `src/main/java/eduhk/fhr/web/service/import_/CandidateImportService.java`

**Changes:**
- Added autowired dependencies:
  - `TalentLinkSOAPDocumentService documentService`
  - `EducationWorkDocumentParser documentParser`

- Added new method: `enrichCandidateWithDocumentData()`
  - Fetches applications for candidate
  - Retrieves documents per application
  - Finds Education/Work document
  - Parses and enriches DTO

- Added new method: `findEducationWorkDocumentId()`
  - Searches document list for structured document
  - **FIX:** Changed from `org.apache.xerces.dom.ElementNSImpl` to `org.w3c.dom.Element`

- Modified `importCandidate()` method:
  - Added call to `enrichCandidateWithDocumentData()`
  - Uncommented calls to related entity import services

---

#### 2. EducationImportService.java
**File:** `src/main/java/eduhk/fhr/web/service/import_/EducationImportService.java`

**Changes:**
- **FIX:** Uncommented entire `importEducation()` method body (lines 40-63)
- Method was completely commented out, preventing any education import

**Before:**
```java
public void importEducation(...) {
    // logger.info("Importing education...");
    // // Delete existing records
    // educationDAO.deleteEducationByCandidateId(...);
    // ... all logic commented
}
```

**After:**
```java
public void importEducation(...) {
    logger.info("Importing education...");
    // Delete existing records
    educationDAO.deleteEducationByCandidateId(...);
    // ... all logic active
}
```

---

#### 3. WorkExperienceImportService.java
**File:** `src/main/java/eduhk/fhr/web/service/import_/WorkExperienceImportService.java`

**Changes:**
- **FIX:** Uncommented entire `importWorkExperience()` method body (lines 40-63)
- Method was completely commented out, preventing any work experience import

---

#### 4. TalentLinkCandidateDTO.java
**File:** `src/main/java/eduhk/fhr/web/dto/talentlink/TalentLinkCandidateDTO.java`

**Changes:**
- Added collection fields:
  ```java
  private List<TalentLinkEducationDTO> education;
  private List<TalentLinkWorkExperienceDTO> workExperience;
  private List<TalentLinkRefereeDTO> referees;
  private TalentLinkOtherInfoDTO otherInfo;
  ```
- Added corresponding getters and setters

**Purpose:** Allow DTO to hold parsed data from documents

---

#### 5. DocumentWebService_Service.java
**File:** `src/main/java/eduhk/fhr/web/soap/document/DocumentWebService_Service.java`

**Changes:**
- **FIX:** Disabled WSDL loading at initialization
  - Set `DOCUMENTWEBSERVICE_WSDL_LOCATION = null`
  - Commented out `@HandlerChain` annotation
  - Modified `__getWsdlLocation()` to return null

**Reason:** WSDL requires authentication; loaded programmatically with credentials instead

---

#### 6. DocumentWebService.java
**File:** `src/main/java/eduhk/fhr/web/soap/document/DocumentWebService.java`

**Changes:**
- **FIX:** Updated all `@RequestWrapper` and `@ResponseWrapper` className attributes
- Changed package: `com.mrted.ws.documents.*` → `eduhk.fhr.web.soap.document.*`

**Example:**
```java
// Before
@RequestWrapper(className = "com.mrted.ws.documents.GetDocumentsByApplicationId")

// After
@RequestWrapper(className = "eduhk.fhr.web.soap.document.GetDocumentsByApplicationId")
```

**Reason:** Fixed JAXB annotation conflicts (28 IllegalAnnotationsExceptions)

---

## Fixes Applied

### Fix 1: Document Type Mismatch
**Problem:** `instanceof` check failing for document objects

**Root Cause:**
- Expected type: `org.apache.xerces.dom.ElementNSImpl` (external Xerces)
- Actual type: `com.sun.org.apache.xerces.internal.dom.ElementNSImpl` (JDK internal)

**Solution:**
Changed to use W3C DOM standard interface that both implement:
```java
// Before
if (docObj instanceof org.apache.xerces.dom.ElementNSImpl) {

// After
if (docObj instanceof org.w3c.dom.Element) {
```

**File:** `CandidateImportService.java:236`

**Result:** ✅ Documents now found and parsed successfully

---

### Fix 2: Import Services Disabled
**Problem:** Education and work experience not being imported despite data being parsed

**Root Cause:**
- `EducationImportService.importEducation()` - 100% commented out
- `WorkExperienceImportService.importWorkExperience()` - 100% commented out

**Solution:** Uncommented all import logic in both services

**Files:**
- `EducationImportService.java:40-63`
- `WorkExperienceImportService.java:40-63`

**Result:** ✅ Import services now executing and attempting database inserts

---

### Fix 3: WSDL Loading Error
**Problem:** Application failed to start - DocumentWebService trying to load WSDL at initialization

**Root Cause:** Generated SOAP stub tries to fetch WSDL from TalentLink URL which requires authentication

**Solution:**
- Set WSDL location to `null` in static initializer
- Commented out `@HandlerChain` annotation
- WSDL loaded programmatically with credentials instead

**File:** `DocumentWebService_Service.java:35`

**Result:** ✅ Application starts successfully

---

### Fix 4: JAXB Package Conflicts
**Problem:** 28 IllegalAnnotationsExceptions - "Two classes have the same XML type name"

**Root Cause:** Package references in annotations still using old package from source project

**Solution:**
Updated all className attributes in `@RequestWrapper` and `@ResponseWrapper` annotations:
```bash
find src/main/java/eduhk/fhr/web/soap/document -name '*.java' \
  -exec sed -i '' 's/className = "com\.mrted\.ws\.documents\./className = "eduhk.fhr.web.soap.document./g' {} \;
```

**File:** `DocumentWebService.java` (all methods)

**Result:** ✅ No JAXB conflicts, application builds successfully

---

### Fix 5: Jakarta Package Migration
**Problem:** Compilation errors - generated stubs using deprecated `javax.*` packages

**Root Cause:** SOAP stubs generated for older Jakarta EE version

**Solution:**
Replaced all package references:
```bash
# javax.jws → jakarta.jws
# javax.xml.bind → jakarta.xml.bind
# javax.xml.ws → jakarta.xml.ws
```

**Files:** All 59 SOAP stub files

**Result:** ✅ Clean compilation with Jakarta EE 4.0

---

## Current Status

### ✅ Working Components

1. **HTTP Endpoint**
   - `GET /api/test-sync/candidate/{id}` accessible
   - Returns JSON response with status

2. **Candidate Basic Import**
   - Candidate data inserted to `RDPS_CANDIDATE`
   - Validation working
   - DAO operations successful

3. **Application Retrieval**
   - `TalentLinkSOAPCandidateService.getApplicationsByCandidateId()` working
   - Candidate 116: Found 1 application (ID: 122)

4. **Document Retrieval**
   - `TalentLinkSOAPDocumentService.getDocumentsByApplicationId()` working
   - Application 122: Found 3 documents

5. **Document Identification**
   - `findEducationWorkDocumentId()` correctly identifying target document
   - Found: "EdUHK Education/Work Experience" (Document ID: 445)

6. **Structured Document Retrieval**
   - `getStructuredDocumentByDocumentId()` working
   - Document 445 retrieved with full answer structure

7. **Document Parsing**
   - `EducationWorkDocumentParser.parseEducationWorkDocument()` executing
   - Extracting data from answers
   - **Result:** 2 education records, 2 work experience records parsed

8. **Import Service Invocation**
   - `EducationImportService.importEducation()` called with 2 records
   - `WorkExperienceImportService.importWorkExperience()` called with 2 records
   - Both services attempting database insert

---

### ❌ Current Issue

**Error:**
```
ORA-01400: cannot insert NULL into ("RDPS"."RDPS_EDU_PROF_QUAL"."EDU_LEVEL")
```

**Location:** Database insert in `EducationDAO.insertEducation()`

**Root Cause:**
- `EDU_LEVEL` column is NOT NULL in database
- `EducationWorkDocumentParser` not correctly extracting education level from document
- `Education` model has `eduLevel = null`
- `EducationMapper` unable to map NULL value

**Impact:**
- Education import transaction rolls back
- Work experience not attempted (education fails first)
- Referees not attempted
- Other info not attempted

**Next Steps:**
1. Investigate document structure to find education level field
2. Update `EducationWorkDocumentParser` to extract education level
3. Test with debug endpoint to see raw document data
4. Verify mapping logic in `EducationMapper`

---

### 📊 Test Results Summary

**Test Case:** Candidate 116 sync
**Date:** December 4, 2025

**Results:**
```
✅ Candidate basic data: SUCCESS
✅ Applications retrieved: 1 found
✅ Documents retrieved: 3 found
✅ Education/Work document identified: Document 445
✅ Document retrieved: StructuredDocument with answers
✅ Document parsed: 2 education + 2 work records
✅ Import services called: Both executed
❌ Database insert: FAILED - EDU_LEVEL is NULL
```

**Database State:**
```sql
-- Candidate exists
SELECT COUNT(*) FROM RDPS_CANDIDATE WHERE candidate_id = 116;
-- Result: 1

-- Education records (expected: 2)
SELECT COUNT(*) FROM RDPS_EDU_PROF_QUAL WHERE candidate_id = 116;
-- Result: 0

-- Work experience records (expected: 2)
SELECT COUNT(*) FROM RDPS_WORK_EXPERIENCE WHERE candidate_id = 116;
-- Result: 0
```

---

## Flow Diagram

### Visual Representation

```
┌─────────────────────────────────────────────────────────────┐
│                     HTTP REQUEST                             │
│          GET /api/test-sync/candidate/116                    │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ↓
┌─────────────────────────────────────────────────────────────┐
│              TestSyncController                              │
│               syncCandidate(116)                             │
└───────────────────────┬─────────────────────────────────────┘
                        │
                        ↓
┌─────────────────────────────────────────────────────────────┐
│          CandidateImportService                              │
│           importCandidate(candidateDTO, batchId)             │
└───────────────────────┬─────────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┐
        │               │               │
        ↓               ↓               ↓
┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐
│   Candidate │  │  Enrich     │  │  Import Related     │
│   Basic     │  │  Document   │  │  Entities           │
│   Import    │  │  Data       │  │  (Education, Work)  │
└─────────────┘  └─────────────┘  └─────────────────────┘
      │                │                      │
      ↓                ↓                      ↓
  ✅ SUCCESS      [See Below]              ❌ FAILED
                                        (EDU_LEVEL NULL)


┌────────────────────────────────────────────────────────────┐
│         DOCUMENT ENRICHMENT FLOW (NEW)                      │
└────────────────────────────────────────────────────────────┘

enrichCandidateWithDocumentData(candidateDTO, "116")
│
├─→ TalentLinkSOAPCandidateService.getApplicationsByCandidateId(116)
│   │
│   └─→ TalentLink Candidate SOAP API
│       │
│       └─→ Returns: [Application{id=122, requisition=...}]
│           ✅ Found 1 application
│
├─→ For each application (122):
│   │
│   └─→ TalentLinkSOAPDocumentService.getDocumentsByApplicationId(122)
│       │
│       └─→ TalentLink Document SOAP API
│           │
│           └─→ Returns: [Doc1, Doc2, Doc3] (3 documents)
│               ✅ Found 3 documents
│
├─→ findEducationWorkDocumentId([Doc1, Doc2, Doc3])
│   │
│   ├─→ Check Doc1: objectType="?", name="?" → Skip
│   ├─→ Check Doc2: objectType="?", name="?" → Skip
│   └─→ Check Doc3: objectType="STRUCTURED",
│       │          name="EdUHK Education/Work Experience"
│       └─→ ✅ MATCH! Return documentId=445
│
├─→ TalentLinkSOAPDocumentService.getStructuredDocumentByDocumentId(445)
│   │
│   └─→ TalentLink Document SOAP API
│       │
│       └─→ Returns: StructuredDocument{
│           │   id=445,
│           │   name="EdUHK Education/Work Experience",
│           │   answers=GfDocument{
│           │     answer=[Answer1, Answer2, ..., AnswerN]
│           │   }
│           │ }
│           ✅ Retrieved structured document
│
└─→ EducationWorkDocumentParser.parseEducationWorkDocument(document)
    │
    ├─→ Extract GfDocument from StructuredDocument
    │
    ├─→ For each Answer in answers:
    │   │
    │   ├─→ Get question text
    │   ├─→ Get answer value(s)
    │   │
    │   ├─→ If education question:
    │   │   └─→ Create TalentLinkEducationDTO
    │   │       ⚠️  eduLevel = null (NOT EXTRACTED)
    │   │
    │   ├─→ If work experience question:
    │   │   └─→ Create TalentLinkWorkExperienceDTO
    │   │
    │   ├─→ If referee question:
    │   │   └─→ Create TalentLinkRefereeDTO
    │   │
    │   └─→ If other info question:
    │       └─→ Set in TalentLinkOtherInfoDTO
    │
    └─→ Return ParsedCandidateData{
          education=[EducDTO1, EducDTO2],     (2 records)
          workExperience=[WorkDTO1, WorkDTO2], (2 records)
          referees=[],                         (0 records)
          otherInfo=OtherInfoDTO
        }
        ✅ Parsing complete


┌────────────────────────────────────────────────────────────┐
│         IMPORT SERVICES FLOW                                │
└────────────────────────────────────────────────────────────┘

candidateDTO.setEducation([EducDTO1, EducDTO2])
candidateDTO.setWorkExperience([WorkDTO1, WorkDTO2])
│
├─→ EducationImportService.importEducation(candidateDTO, "116", "REQ123")
│   │
│   ├─→ educationDAO.deleteEducationByCandidateId("116", "REQ123")
│   │   └─→ ✅ Existing records deleted
│   │
│   ├─→ For EducDTO1 (seq=1):
│   │   │
│   │   ├─→ EducationMapper.mapToModel(EducDTO1, "116", "REQ123", 1)
│   │   │   │
│   │   │   └─→ Education{
│   │   │       candidateId=116,
│   │   │       reqNumber=REQ123,
│   │   │       seq=1,
│   │   │       institution=...,
│   │   │       eduLevel=null,  ⚠️  NULL VALUE
│   │   │       ...
│   │   │     }
│   │   │
│   │   ├─→ educationMapper.validate(education)
│   │   │   └─→ ✅ Validation passes (doesn't check DB constraints)
│   │   │
│   │   └─→ educationDAO.insertEducation(education)
│   │       │
│   │       └─→ SQL: INSERT INTO RDPS_EDU_PROF_QUAL (..., EDU_LEVEL, ...)
│   │           │     VALUES (..., NULL, ...)
│   │           │
│   │           └─→ ❌ ORA-01400: cannot insert NULL into EDU_LEVEL
│   │               │
│   │               └─→ DataIntegrityViolationException thrown
│   │                   └─→ Transaction rolled back
│   │
│   └─→ ❌ IMPORT FAILED
│
└─→ WorkExperienceImportService.importWorkExperience(...)
    └─→ ⏸️  NOT EXECUTED (education failed first)
```

---

## Appendix: Key Code Locations

### Critical Methods

1. **Document Enrichment Entry Point**
   - File: `CandidateImportService.java`
   - Method: `enrichCandidateWithDocumentData(TalentLinkCandidateDTO, String)`
   - Lines: 148-216

2. **Document Finder**
   - File: `CandidateImportService.java`
   - Method: `findEducationWorkDocumentId(List<Object>)`
   - Lines: 224-271
   - **Fix Location:** Line 236 (Element interface)

3. **Document Parser**
   - File: `EducationWorkDocumentParser.java`
   - Method: `parseEducationWorkDocument(StructuredDocument)`
   - **Issue Location:** Education level extraction logic

4. **Education Import**
   - File: `EducationImportService.java`
   - Method: `importEducation(TalentLinkCandidateDTO, String, String)`
   - Lines: 39-64
   - **Fix Location:** Lines 40-63 (uncommented)

5. **Work Experience Import**
   - File: `WorkExperienceImportService.java`
   - Method: `importWorkExperience(TalentLinkCandidateDTO, String, String)`
   - Lines: 39-64
   - **Fix Location:** Lines 40-63 (uncommented)

---

## Testing Commands

### Start Application
```bash
mvn clean package -DskipTests
mvn spring-boot:run
```

### Test Candidate Sync
```bash
curl http://localhost:8080/api/test-sync/candidate/116
```

### Check Database Results
```bash
# Education records
docker exec oracle-db-free bash -c \
  "echo \"SELECT COUNT(*) FROM RDPS_EDU_PROF_QUAL WHERE candidate_id = 116;\" | \
   sqlplus -s RDPS/rdps_password123@FREEPDB1"

# Work experience records
docker exec oracle-db-free bash -c \
  "echo \"SELECT COUNT(*) FROM RDPS_WORK_EXPERIENCE WHERE candidate_id = 116;\" | \
   sqlplus -s RDPS/rdps_password123@FREEPDB1"
```

### View Logs
```bash
tail -f catalina.base_IS_UNDEFINED/logs/server.log_RDPS.log
```

---

## Related Documentation

- `CLAUDE.md` - Project overview and setup
- `DATABASE_FIELDS_MAPPING.md` - Field mapping reference
- `SOAP_API_DOCUMENTATION.md` - TalentLink SOAP API details
- `FIXES_APPLIED_DOCUMENT_SERVICE.md` - Initial setup fixes

---

**End of Documentation**
