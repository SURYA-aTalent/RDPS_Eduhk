# TalentLink Offer/Application API Details

## Overview

This document explains how to retrieve job offer and application details from TalentLink SOAP API and what data is available.

---

## API Method: getApplicationsByCandidateId()

### Method Signature
```java
CandidateWebService.getApplicationsByCandidateId(
    Long candidateId,
    boolean includeDocuments
)
→ Returns: List<ApplicationDto>
```

### Usage Example
```java
// Get all applications for candidate 37
List<ApplicationDto> applications =
    soapCandidateService.getApplicationsByCandidateId(37L, true);
```

---

## ApplicationDto Structure

### Available Fields in ApplicationDto

| Field Name | Type | Description | Maps to RDPS_OFFER |
|------------|------|-------------|-------------------|
| **id** | Long | Application ID (unique) | ✅ OFFER_ID |
| **candidateId** | Long | Candidate ID | ✅ CANDIDATE_ID |
| **positionId** | Long | Position/Requisition ID | ✅ REQ_NUMBER |
| **status** | String | Application status | ✅ OFFER_STATUS |
| **statusComment** | String | Status notes | ✅ OFFER_REMARKS |
| **applicationDate** | XMLGregorianCalendar | Application submitted date | ⚠️ Could use for CONTRACT_START_DATE |
| **creation** | XMLGregorianCalendar | Record creation date | ❌ Not mapped |
| **update** | XMLGregorianCalendar | Last update date | ❌ Not mapped |
| **activeApplication** | Boolean | Is active? | ❌ Not mapped |
| **archived** | Boolean | Is archived? | ❌ Not mapped |
| **shortListed** | Boolean | Is shortlisted? | ❌ Not mapped |
| **hasContracts** | Boolean | Has contract data? | ⚠️ Indicator for contract lookup |
| **memo** | String | Application notes | ✅ Could add to OFFER_REMARKS |
| **completionReason** | String | Reason for completion | ❌ Not mapped |
| **srcChannelName** | String | Source channel (website/referral) | ❌ Not mapped |
| **srcChannelType** | String | Source type | ❌ Not mapped |
| **srcMedium** | String | Source medium | ❌ Not mapped |
| **applicationHistory** | ApplicationHistory | History of status changes | ❌ Not mapped |
| **documents** | Documents | Attached documents list | ❌ Not mapped (separate feature) |
| **candidateConsents** | CandidateConsents | GDPR consents | ❌ Not mapped |

### Status Values

Common application status values that indicate an offer:
- `"OFFERED"` - Offer made
- `"OFFER APPROVED"` - Offer approved internally
- `"OFFER ACCEPTED"` - Candidate accepted offer
- `"HIRED"` - Candidate hired
- `"CONTRACT_SENT"` - Contract sent to candidate
- `"CONTRACT_SIGNED"` - Contract signed

---

## Missing Offer Details in ApplicationDto

### ❌ Not Available in ApplicationDto

The following RDPS_OFFER fields are **NOT** available in ApplicationDto:

| Field Category | Missing Fields |
|----------------|----------------|
| **Employment Details** | PAY_BASIS, EMP_DEP, TOA, FUNC_TITLE, BAND, GRADE, POST |
| **Contract Dates** | CONTRACT_START_DATE, CONTRACT_END_DATE |
| **Probation** | PROBATION_LENGTH, PROBATION_UNITS |
| **Notice Period** | NOTICE_LENGTH, NOTICE_UNITS |
| **Compensation** | SALARY_AMOUNT, GRATUITY_PCT |
| **Employment Mode** | EMP_MODE, PLAN_HOURS |
| **Benefits** | MPF, SUPERANNUATION, PENSION |
| **Project** | PROJECT_TITLE |

### Where to Find Missing Data

These fields might be available in:

#### 1. **Contract Object (if hasContracts = true)**
```java
// If application.hasContracts() returns true
// There should be a separate API to get contract details
// Check TalentLink API documentation for:
ContractDto contract = getContractByApplicationId(applicationId);
```

**Contract might contain:**
- Salary amount
- Contract start/end dates
- Employment terms
- Benefits eligibility

#### 2. **Position/Requisition Details**
```java
// Get position details by positionId
PositionDto position = getPositionById(application.getPositionId());
```

**Position might contain:**
- Job title, band, grade
- Department
- Terms of appointment (TOA)
- Full-time/part-time indicator

#### 3. **Custom Fields in Application**
```java
// TalentLink supports custom fields
// Check if offer details are stored in custom fields
application.getCustomFields()
```

#### 4. **Application Follow-up Records**
```java
// Check applicationHistory for offer-related updates
ApplicationHistory history = application.getApplicationHistory();
List<ApplicationFollowup> followups = history.getApplicationFollowup();

// Follow-up records might contain:
// - Salary discussions
// - Offer details
// - Contract negotiation notes
```

---

## Current Implementation Status

### ✅ Currently Mapped (4 fields)

```java
// OfferMapper.java - current implementation
Offer offer = new Offer();
offer.setOfferId(String.valueOf(application.getId()));           // ✅
offer.setCandidateId(candidateId);                                // ✅
offer.setReqNumber(String.valueOf(application.getPositionId()));  // ✅
offer.setOfferStatus(application.getStatus());                    // ✅
```

### ⚠️ TODO: Additional Mappings (26 fields)

```java
// TODO: Map from ApplicationDto
offer.setOfferRemarks(application.getMemo());
// TODO: Parse applicationDate for contract start date estimation

// TODO: Fetch from Contract API (if hasContracts = true)
if (application.getHasContracts()) {
    ContractDto contract = getContract(application.getId());
    offer.setSalaryAmount(contract.getSalary());
    offer.setContractStartDate(contract.getStartDate());
    offer.setContractEndDate(contract.getEndDate());
    // ... etc
}

// TODO: Fetch from Position API
PositionDto position = getPosition(application.getPositionId());
offer.setFuncTitle(position.getTitle());
offer.setEmpDep(position.getDepartment());
offer.setBand(position.getBand());
offer.setGrade(position.getGrade());
// ... etc
```

---

## Complete Offer Data Retrieval Flow

### Step 1: Get Application
```java
List<ApplicationDto> applications =
    candidateService.getApplicationsByCandidateId(candidateId, true);
```

**Returns:**
```json
{
  "id": 12345,
  "candidateId": 37,
  "positionId": 789,
  "status": "OFFERED",
  "applicationDate": "2025-01-15T10:00:00Z",
  "hasContracts": true,
  "memo": "Excellent candidate, approved by hiring manager"
}
```

### Step 2: Get Position Details (Optional)
```java
PositionDto position = positionService.getPositionById(789L);
```

**Returns:**
```json
{
  "id": 789,
  "title": "Senior Lecturer",
  "department": "Computer Science",
  "band": "Academic",
  "grade": "AL3",
  "employmentType": "Full-time"
}
```

### Step 3: Get Contract Details (if available)
```java
if (application.getHasContracts()) {
    ContractDto contract = contractService.getContract(12345L);
}
```

**Returns:**
```json
{
  "applicationId": 12345,
  "salary": 85000,
  "currency": "HKD",
  "startDate": "2025-02-01",
  "endDate": "2027-01-31",
  "probationMonths": 6,
  "noticeMonths": 3,
  "benefits": {
    "mpf": true,
    "medicalInsurance": true
  }
}
```

### Step 4: Combine All Data
```java
Offer offer = new Offer();

// From Application
offer.setOfferId(application.getId());
offer.setOfferStatus(application.getStatus());
offer.setOfferRemarks(application.getMemo());

// From Position
offer.setFuncTitle(position.getTitle());
offer.setEmpDep(position.getDepartment());
offer.setBand(position.getBand());
offer.setGrade(position.getGrade());

// From Contract
offer.setSalaryAmount(contract.getSalary());
offer.setContractStartDate(contract.getStartDate());
offer.setContractEndDate(contract.getEndDate());
offer.setProbationLength(contract.getProbationMonths());
offer.setNoticeLength(contract.getNoticeMonths());
offer.setMpf(contract.getBenefits().getMpf() ? "Y" : "N");
```

---

## Testing: Check Actual API Response

### Test Code
```java
// Add this to TestSyncController or create a separate test endpoint
@GetMapping("/test-sync/application-details/{candidateId}")
public ResponseEntity<Map<String, Object>> getApplicationDetails(@PathVariable Long candidateId) {
    try {
        List<ApplicationDto> applications =
            soapCandidateService.getApplicationsByCandidateId(candidateId);

        Map<String, Object> response = new HashMap<>();
        response.put("candidateId", candidateId);
        response.put("applicationCount", applications.size());

        List<Map<String, Object>> appDetails = new ArrayList<>();
        for (ApplicationDto app : applications) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("id", app.getId());
            detail.put("positionId", app.getPositionId());
            detail.put("status", app.getStatus());
            detail.put("applicationDate", app.getApplicationDate());
            detail.put("hasContracts", app.getHasContracts());
            detail.put("memo", app.getMemo());
            detail.put("statusComment", app.getStatusComment());
            appDetails.add(detail);
        }

        response.put("applications", appDetails);
        return ResponseEntity.ok(response);

    } catch (Exception e) {
        return ResponseEntity.status(500)
            .body(Map.of("error", e.getMessage()));
    }
}
```

### Test URL
```bash
curl "http://localhost:8080/api/test-sync/application-details/37"
```

---

## Recommendations

### 1. **Investigate TalentLink API Documentation**
- Check if there's a `getContractByApplicationId()` method
- Check if there's a `getPositionById()` method
- Review custom field definitions for offer data

### 2. **Check ApplicationDto Custom Fields**
```java
// Check if ApplicationDto has custom field accessors
// Some SOAP APIs expose custom fields via:
application.getCustomField("SALARY")
application.getCustomField("CONTRACT_START_DATE")
```

### 3. **Inspect ApplicationHistory**
```java
ApplicationHistory history = application.getApplicationHistory();
if (history != null) {
    List<ApplicationFollowup> followups =
        history.getApplicationFollowup();

    for (ApplicationFollowup followup : followups) {
        // Check followup comments for offer details
        String comment = followup.getComment();
        // Might contain: "Salary: $85,000" or "Start: 2025-02-01"
    }
}
```

### 4. **Use hasContracts Flag**
```java
if (application.getHasContracts() != null &&
    application.getHasContracts()) {
    // There is contract data available
    // Find the API method to retrieve it
}
```

---

## API Methods to Investigate

Check if these methods exist in TalentLink SOAP API:

```java
// Contract retrieval
ContractDto getContractByApplicationId(Long applicationId);
List<ContractDto> getContractsByCandidateId(Long candidateId);

// Position retrieval
PositionDto getPositionById(Long positionId);
PositionDetailDto getPositionDetails(Long positionId);

// Offer retrieval
OfferDto getOfferByApplicationId(Long applicationId);
```

---

## SOAP WSDL Inspection

To find all available methods:

### 1. Check the WSDL
```bash
curl "https://api3.lumesse-talenthub.com/HRIS/SOAP/Candidate?wsdl" > candidate.wsdl
```

### 2. Search for Contract/Offer Methods
```bash
grep -i "contract\|offer" candidate.wsdl
```

### 3. Check Method Signatures
Look for methods like:
- `getContract`
- `getContracts`
- `getOffer`
- `getOfferDetails`
- `getEmploymentDetails`

---

## Summary

### Available in ApplicationDto (4/30 fields)
- ✅ Offer ID, Candidate ID, Requisition Number
- ✅ Offer Status

### Missing in ApplicationDto (26/30 fields)
- ❌ Employment details (department, title, band, grade)
- ❌ Contract dates
- ❌ Compensation details
- ❌ Benefits eligibility
- ❌ Probation/notice periods

### Next Steps
1. ✅ Document ApplicationDto structure (this file)
2. 🔄 Test actual API call with candidate who has applications
3. 🔄 Investigate contract/position API methods
4. 🔄 Check for custom fields
5. 🔄 Update OfferMapper with complete field extraction

---

**Document Created:** 2025-12-03
**Status:** Investigation in progress
**API Version:** TalentLink SOAP API v3
