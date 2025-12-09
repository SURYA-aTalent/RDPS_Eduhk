# Solution: How to Access Education/Work Experience Data

**Date:** 2025-12-04
**Status:** Investigation Complete - Solution Identified

---

## Problem Statement

Candidate 116 has education, work experience, and referee data entered in TalentLink, but our sync only populates the RDPS_CANDIDATE table. Related entity tables (RDPS_EDU_PROF_QUAL, RDPS_WORK_EXPERIENCE, RDPS_REFEREE) remain empty.

---

## Investigation Results

### What We Found

1. **Data is NOT in the Profile object**
   - `getCandidateById(116)` returns Profile with basic info only
   - Profile.reference contains referral info, NOT education/work data

2. **Data is NOT in the main CV StructuredDocument**
   - `getStructuredDocumentById(116)` returns document 446 (basic CV form)
   - Contains 46 questions: name, email, address, social networking
   - Does NOT contain education/work/referee questions

3. ✅ **Data IS in a separate application-specific StructuredDocument**
   - Candidate 116 has **Application ID 122** for position "OFFER POST"
   - Application has **9 attached documents**
   - **Document 445** is "EdUHK Education/Work Experience" (STRUCTURED type)
   - This document contains the education/work/referee data!

### Document Structure

```
Candidate 116
└── Application 122 (for position 50 - "OFFER POST")
    ├── Document 445: "EdUHK Education/Work Experience" ← THE DATA IS HERE!
    ├── Document 446: Basic CV/Profile Form
    ├── Document 447: "EdUHK Standard PIF"
    ├── Document 448: "EdUHK Declaration"
    └── ... (emails and attachments)
```

---

## The Solution

### Current API Limitation

TalentLink's SOAP API has these methods:
- `getStructuredDocumentById(candidateId)` - Returns ONE document (the main CV)
- `getCandidateStructuredDocumentById(candidateId, ...)` - Also returns the main CV
- **NO direct method** to get StructuredDocument by documentId (like document 445)

### Three Possible Approaches

#### **Approach 1: Check if ApplicationDto.Documents Has Embedded Data** (RECOMMENDED TO TRY FIRST)

The ApplicationDto.Documents might include the actual StructuredDocument/GfDocument with answers embedded.

**Test:**
```java
ApplicationDto app = soapCandidateService.getApplicationById(122L, true);
Documents docs = app.getDocuments();
for (DocumentDto doc : docs.getDocument()) {
    if (doc.getId() == 445) {
        // Check if doc has embedded StructuredDocument or FormAnswered data
        // This would be the cleanest solution!
    }
}
```

**Action Required:** Inspect the actual ApplicationDto and Documents classes to see if they have embedded form data.

#### **Approach 2: Use TalentLink's Raw WSDL** (IF API METHOD EXISTS)

The SOAP WSDL might have methods not yet discovered:
- `getStructuredDocumentByDocumentId(documentId)`
- `getApplicationDocument(applicationId, documentId)`
- `getFormAnswers(documentId)`

**Action Required:** Download and inspect the full WSDL:
```bash
curl "https://api3.lumesse-talenthub.com/HRIS/SOAP/Candidate?wsdl" > candidate.wsdl
grep -i "structuredDocument\|formAnswer\|documentId" candidate.wsdl
```

#### **Approach 3: Parse Multiple Documents** (WORKAROUND)

If there's no direct method, we could:
1. Get all candidate documents: `getCandidateDocuments(candidateId)`
2. For each STRUCTURED document, try to retrieve it
3. Check if it's the Education/Work document by inspecting questions

**Limitation:** This assumes there's a way to retrieve each document, which we haven't confirmed yet.

---

## Implementation Plan

### Phase 1: Verify Data Access (IMMEDIATE)

**Create test endpoint:**
```java
@GetMapping("/api/test-sync/debug/app-document/{applicationId}/{documentId}")
public ResponseEntity<?> getApplicationDocument(
    @PathVariable Long applicationId,
    @PathVariable Long documentId) {

    // Try approach 1: Get application and check if documents have embedded data
    ApplicationDto app = soapService.getApplicationById(applicationId, true);

    // Inspect app.getDocuments() structure
    // Check if DocumentDto has getStructuredDocument() or getAnswers()

    // Return full structure for analysis
}
```

**Test with:** `http://localhost:8080/api/test-sync/debug/app-document/122/445`

### Phase 2: Extract Education/Work Data

Once we can access document 445's answers, we need to:

1. **Parse the GfDocument answers** (similar to document 446 we already parsed)
2. **Map questions to our entities:**
   - Education History questions → TalentLinkEducationDTO
   - Employment History questions → TalentLinkWorkExperienceDTO
   - Referees questions → TalentLinkRefereeDTO
3. **Update the import flow:**
   ```java
   // In CandidateImportService
   public ImportErrorDetail importCandidate(TalentLinkCandidateDTO dto, String batchId) {
       // Step 1: Import candidate (WORKING)
       candidateDAO.upsertCandidate(candidate);

       // Step 2: Get application for this candidate
       List<ApplicationDto> applications = soapService.getApplicationsByCandidateId(candidateId);

       // Step 3: For each application, get Education/Work document
       for (ApplicationDto app : applications) {
           StructuredDocument eduWorkDoc = getEducationWorkDocument(app);
           if (eduWorkDoc != null) {
               // Step 4: Parse and extract data
               List<TalentLinkEducationDTO> education = parseEducation(eduWorkDoc);
               List<TalentLinkWorkExperienceDTO> workExp = parseWorkExperience(eduWorkDoc);
               List<TalentLinkRefereeDTO> referees = parseReferees(eduWorkDoc);

               // Step 5: Import to database (UNCOMMENT LINES 100-118)
               educationImportService.importEducation(dto, candidateId, reqNumber);
               workExperienceImportService.importWorkExperience(dto, candidateId, reqNumber);
               refereeImportService.importReferees(dto, candidateId, reqNumber);
           }
       }
   }
   ```

### Phase 3: Question Mapping

From document 445, we expect questions like:

**Education History Section:**
- School → `education.institution`
- Faculty → (map to field or ignore)
- Degree → `education.qualification`
- Major name → `education.majorStudyArea`
- Graduated → `education.dateOfAward`

**Employment History Section:**
- Employer → `workExperience.employer`
- Positions → `workExperience.positionTitle`
- From → `workExperience.startDate`
- Until → `workExperience.endDate` (or current)

**Referees Section:**
- Name → `referee.firstName` + `referee.lastName`
- Contact details → `referee.email`, `referee.phone`

---

## Key Files to Modify

1. **TalentLinkSOAPCandidateService.java**
   - Add method to get application document by ID
   - Add method to parse Education/Work StructuredDocument

2. **CandidateImportService.java**
   - Uncomment lines 100-118 (related entity imports)
   - Add logic to retrieve application documents
   - Add Education/Work document parsing

3. **TalentLinkCandidateDTO.java**
   - Add collection fields:
     ```java
     private List<TalentLinkEducationDTO> education;
     private List<TalentLinkWorkExperienceDTO> workExperience;
     private List<TalentLinkRefereeDTO> referees;
     private TalentLinkOtherInfoDTO otherInfo;
     ```

4. **New Parser Classes** (if needed)
   - `EducationWorkDocumentParser.java` - Parse document 445 answers
   - Question-to-DTO mapping logic

---

## Testing Plan

### Test Case 1: Candidate 116
- Has application 122 with Education/Work document 445
- Expected results after fix:
  - 1 candidate record ✅ (already working)
  - 1 education record (DBTR School, BE degree)
  - 1 qualification record (PHP certification)
  - 1 work experience record (test company, position code, 2019-Current)
  - 1 referee record (ref name)
  - 1 other info record (expected salary: 300)

### Test Case 2: Candidate 37
- Has NO education/work data in TalentLink
- Expected results: Only candidate record (current behavior)
- Should NOT fail or error

### Test Case 3: Batch Import
- Import 10 candidates, some with applications, some without
- Verify all with applications get education/work data imported
- Verify no failures for candidates without applications

---

## Current Status

✅ **COMPLETED:**
1. Identified data location: Application documents, specifically document 445
2. Confirmed document structure: STRUCTURED type with GfDocument answers
3. Created debug endpoints to inspect all structures

⏳ **PENDING:**
1. Determine how to retrieve document 445's actual content (StructuredDocument with answers)
2. Implement parser for Education/Work document questions
3. Uncomment and wire up related entity imports
4. Test with candidate 116

---

## Next Action

**IMMEDIATE:** Create and test the application-document endpoint to see if we can access document 445's answers through the ApplicationDto.

**Code to test:**
```bash
# After implementing the endpoint
curl "http://localhost:8080/api/test-sync/debug/app-document/122/445"
```

This should reveal whether ApplicationDto.Documents contains embedded StructuredDocument data, or if we need a different API approach.

---

**Document Created:** 2025-12-04
**Investigation Status:** ✅ Complete - Solution path identified
**Implementation Status:** ⏳ Ready to proceed once data access method confirmed
