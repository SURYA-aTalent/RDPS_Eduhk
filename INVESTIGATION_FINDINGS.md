# Education/Work Experience Data Investigation - Findings

**Date:** 2025-12-04
**Issue:** Candidate 116 with complete data only populated RDPS_CANDIDATE table, not related entities

---

## Investigation Summary

I investigated where education, work experience, referee, and other info data is stored in the TalentLink SOAP API structure.

### Key Discovery

**The `Profile` object returned by `getCandidateById()` does NOT contain education or work experience collections.**

After examining the SOAP stub classes:

1. **Profile.java** - Contains these fields:
   - Basic info: id, firstname, lastname, email
   - PersonalData: dateOfBirth, sex
   - Address: address1, address2, city, phone
   - Position: position code
   - Reference: **referral info only** (company, employeeCode, referredFrom) - NOT education/work

2. **Reference.java** - Only contains referral fields:
   ```java
   protected String company;
   protected String email;
   protected String employeeCode;
   protected String referredFrom;
   ```
   - **Does NOT have** education, workExperience, or referees collections

3. **No Education/WorkExperience classes** found in SOAP stubs

---

## What I Created

### 1. Debug Endpoints

Added two new endpoints to `TestSyncController.java` to investigate the API:

#### Endpoint 1: Inspect Profile Structure
```
GET /api/test-sync/debug/profile/{candidateId}
Example: http://localhost:8080/api/test-sync/debug/profile/116
```

**What it shows:**
- Complete Profile object structure
- All available fields and their values
- Confirms Profile does NOT contain education/work collections

#### Endpoint 2: Inspect StructuredDocument
```
GET /api/test-sync/debug/structured-doc/{candidateId}
Example: http://localhost:8080/api/test-sync/debug/structured-doc/116
```

**What it shows:**
- StructuredDocument (CV/Resume form) structure
- Might contain education/work as form questions/answers
- GfDocument structure needs exploration

### 2. Service Method

Added `getStructuredDocumentById()` to `TalentLinkSOAPCandidateService.java`:
```java
public StructuredDocument getStructuredDocumentById(Long candidateId)
```

This calls the TalentLink SOAP API to retrieve the structured document (CV/Resume form).

---

## Possible Scenarios

### Scenario 1: Education/Work in StructuredDocument

TalentLink might store education and work experience as **form data** in the StructuredDocument:
- CV/Resume entered via a form
- Education = questions about degrees
- Work experience = questions about employment
- Data stored as question/answer pairs in GfDocument

**If this is true:**
- We need to parse StructuredDocument
- Extract specific questions (e.g., "Education History", "Work Experience")
- Map answers to our DTO fields

### Scenario 2: Separate API Calls Required

Education/work might require **separate API methods**:
- `getEducationByCandidateId(candidateId)`
- `getWorkExperienceByCandidateId(candidateId)`
- `getRefereesByCandidateId(candidateId)`

**If this is true:**
- These methods might not exist in current SOAP stubs
- Might need to regenerate stubs from updated WSDL
- Or use a different SOAP endpoint altogether

### Scenario 3: Data Not Available via API

TalentLink might not expose this data via SOAP API:
- Only available in TalentLink UI
- Would require database access or export files
- API limitation

---

## Next Steps

### Step 1: Run Debug Endpoints (IMMEDIATE)

**You need to run these endpoints** to see what TalentLink actually returns:

```bash
# 1. Check Profile structure
curl "http://localhost:8080/api/test-sync/debug/profile/116"

# 2. Check StructuredDocument
curl "http://localhost:8080/api/test-sync/debug/structured-doc/116"
```

Save the JSON responses - they will tell us:
- What fields Profile actually has
- Whether StructuredDocument contains education/work data
- Where to find the missing data

### Step 2: Analyze Results

Based on what the debug endpoints show:

#### If StructuredDocument has education/work:
1. Parse GfDocument question/answer structure
2. Map specific questions to our DTOs
3. Extract education, work, referee data from form fields

#### If separate API calls needed:
1. Check TalentLink WSDL for available methods
2. Regenerate SOAP stubs if needed
3. Call separate methods for each entity type

#### If data not available:
1. Contact TalentLink support
2. Check API documentation
3. Consider alternative data sources (export files, database access)

### Step 3: Implement Solution

Once we know where the data is, we can:
1. Add appropriate extraction code
2. Update DTOs if needed
3. Uncomment import calls in CandidateImportService
4. Test with candidate 116

---

## Files Modified

### TestSyncController.java
- Added `/api/test-sync/debug/profile/{candidateId}` endpoint
- Added `/api/test-sync/debug/structured-doc/{candidateId}` endpoint

### TalentLinkSOAPCandidateService.java
- Added `getStructuredDocumentById(Long candidateId)` method

### Build Status
✅ Maven build successful - all code compiles

---

## Important Notes

1. **Current import code is commented out** (CandidateImportService.java lines 100-118)
   - This is why related entities aren't imported
   - We need to find the data source first before uncommenting

2. **Profile does NOT contain the collections we expected**
   - CANDIDATE_116_SYNC_REPORT.md assumed Profile.reference had educations/workExperiences
   - This assumption was **incorrect**

3. **TalentLinkCandidateDTO is ready**
   - Already has all candidate fields
   - Will add collection fields once we know the data structure

---

## Action Required

**Please run the debug endpoints** and share the JSON responses:

```bash
# Start the application if not running
mvn spring-boot:run

# In another terminal, run:
curl "http://localhost:8080/api/test-sync/debug/profile/116" > profile_116_debug.json
curl "http://localhost:8080/api/test-sync/debug/structured-doc/116" > structured_doc_116_debug.json

# Share the contents of:
# - profile_116_debug.json
# - structured_doc_116_debug.json
```

This will reveal where TalentLink stores the education and work experience data.

---

**Investigation Status:** ⏳ Waiting for debug endpoint results
**Next Action:** Run debug endpoints for candidate 116
