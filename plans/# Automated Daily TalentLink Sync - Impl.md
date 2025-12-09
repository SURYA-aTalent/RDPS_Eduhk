# Automated Daily TalentLink Sync - Implementation Plan

## Overview

Implement automated daily sync process that fetches candidates with status "Offered" or "Hired" from TalentLink and syncs all related data to RDPS database. Runs daily at 2:00 AM with retry logic and error handling.

## User Requirements

- **Entities to Sync:** Candidate, Education, Work Experience, Referee, Other Info, Offer Details
- **Filter:** Candidates with status "Offered" or "Hired"
- **Schedule:** Daily at 2:00 AM
- **Update Strategy:** Upsert (insert new + update existing)
- **Error Handling:** Log errors + email notification + continue on error + retry failed candidates
- **Skip Logic:** Skip already synced candidates

## Current State Analysis

### ✅ Already Implemented
- ImportOrchestrationService (manual import)
- TalentLinkSOAPCandidateService (SOAP integration)
- LookupValueResolver (with caching for LOV fields)
- All DAO classes with upsert support
- ShedLock integration for scheduled jobs
- Email notification service

### ⚠️ Partially Complete
- Field mappers (35-70% complete, many fields commented out)
- Import orchestration (missing status filtering)

### ❌ Missing
- Offer management system (table, model, DAO, mapper, service)
- Status filtering in SOAP calls
- Sync tracking table for retry logic
- Scheduled daily job

---

## Implementation Phases

## Phase 1: Complete Field Mappers (3-4 days)

**Priority:** 🔴 CRITICAL - Prevents data loss

### Task 1.1: Complete CandidateMapper (1 day)
**File:** `src/main/java/eduhk/fhr/web/mapper/CandidateMapper.java`

**Problem:** Only 7 of 20 fields mapped (65% data loss!)

**Steps:**
1. Extend `TalentLinkCandidateDTO` with missing fields
2. Update `TalentLinkSOAPCandidateService.convertProfileToDTO()` to populate missing fields from SOAP Profile
3. Uncomment all 13 missing field mappings in `CandidateMapper.mapToModel()`
4. Enable district LOV resolution (LookupValueResolver already exists)

**Missing Fields to Add:**
```java
candidate.setPostAppliedFor(dto.getPostAppliedFor());
candidate.setTitle(dto.getTitle());
candidate.setChineseName(dto.getChineseName());
candidate.setDateOfBirth(dto.getDateOfBirth());
candidate.setPermHK(dto.getPermanentHK());
candidate.setGender(dto.getGender());
candidate.setHkid(dto.getHkid());
candidate.setPassport(dto.getPassportNo());
candidate.setVisaRequired(dto.getVisaRequired());
candidate.setAddrLine1(dto.getAddressLine1());
candidate.setAddrLine2(dto.getAddressLine2());
candidate.setPhoneNo(dto.getPhoneNumber());

// District LOV resolution
Integer districtKey = lookupValueResolver.resolveDistrict(dto.getDistrict());
candidate.setDistrict(districtKey != null ? districtKey : 1);
```

### Task 1.2: Complete EducationMapper (1 day)
**File:** `src/main/java/eduhk/fhr/web/mapper/EducationMapper.java`

**Missing:** 8 fields including 4 LOV resolutions

**Steps:**
1. Add LOV resolutions using existing LookupValueResolver:
```java
education.setEduLevel(lookupValueResolver.resolveEducationLevel(dto.getEducationLevel()));
education.setStudyMode(lookupValueResolver.resolveStudyMode(dto.getStudyMode()));
education.setQualAwardDesc(lookupValueResolver.resolveQualAwardDesc(dto.getQualAwardDesc()));
education.setQualAwardClass(lookupValueResolver.resolveQualAwardClass(dto.getQualAwardClass()));
```

2. Add remaining fields:
```java
education.setOthers(dto.getOthers());
education.setMajorStudyArea(dto.getMajorStudyArea());
education.setStartDate(dto.getStartDate());
education.setDateOfAward(dto.getDateOfAward());
```

### Task 1.3: Complete WorkExperienceMapper (0.5 day)
**File:** `src/main/java/eduhk/fhr/web/mapper/WorkExperienceMapper.java`

**Missing:** 5 fields (58% complete - easiest to fix)

```java
workExp.setNatureOfBusiness(dto.getNatureOfBusiness());
workExp.setCurrentJob(dto.getCurrentJob()); // Y/N
workExp.setModeOfEmployment(dto.getModeOfEmployment());
workExp.setHoursPerWeek(dto.getHoursPerWeek());
workExp.setNatureOfDuties(dto.getNatureOfDuties());
```

### Task 1.4: Complete RefereeMapper (0.5 day)
**File:** `src/main/java/eduhk/fhr/web/mapper/RefereeMapper.java`

**Missing:** 3 fields (70% complete - quick win!)

```java
referee.setPositionTitle(dto.getPositionTitle());
referee.setPhoneNumber(dto.getPhoneNumber());
referee.setRelationship(dto.getRelationship());
```

### Task 1.5: Complete OtherInfoMapper (1 day)
**File:** `src/main/java/eduhk/fhr/web/mapper/OtherInfoMapper.java`

**Missing:** 16 fields (27% complete - most complex)

Add all compensation, career, and source tracking fields (see field mapping report for complete list)

---

## Phase 2: Offer Management System (2-3 days)

**Priority:** 🔴 CRITICAL - Required for syncing offered/hired candidates

### Task 2.1: Create Database Schema (0.5 day)

**New File:** `db_scripts/rdps_offer.sql`

```sql
CREATE TABLE RDPS_OFFER (
    OFFER_ID            VARCHAR2(20) NOT NULL,
    CANDIDATE_ID        VARCHAR2(20) NOT NULL,
    REQ_NUMBER          VARCHAR2(20) NOT NULL,
    OFFER_STATUS        VARCHAR2(15),

    -- Employment details (7 fields)
    PAY_BASIS           CHAR(1),
    EMP_DEP             VARCHAR2(240),
    TOA                 VARCHAR2(5),
    FUNC_TITLE          VARCHAR2(20),
    BAND                CHAR(2),
    GRADE               VARCHAR2(20),
    POST                VARCHAR2(20),

    -- Contract dates
    CONTRACT_START_DATE DATE,
    CONTRACT_END_DATE   DATE,

    -- Probation/Notice (4 fields)
    PROBATION_LENGTH    NUMBER,
    PROBATION_UNITS     CHAR(1),
    NOTICE_LENGTH       NUMBER,
    NOTICE_UNITS        CHAR(1),

    -- Compensation
    SALARY_AMOUNT       NUMBER(38,2),
    GRATUITY_PCT        NUMBER(5,2),

    -- Employment mode
    EMP_MODE            CHAR(1),
    PLAN_HOURS          NUMBER,

    -- Benefits flags
    MPF                 CHAR(1),
    SUPERANNUATION      CHAR(1),
    PENSION             CHAR(1),

    -- Other
    PROJECT_TITLE       VARCHAR2(50),
    OFFER_REMARKS       VARCHAR2(100),

    -- Audit
    CREATED_BY          VARCHAR2(32) DEFAULT USER NOT NULL,
    CREATION_DATE       DATE DEFAULT SYSDATE NOT NULL,
    USERSTAMP           VARCHAR2(32) DEFAULT USER NOT NULL,
    TIMESTAMP           DATE DEFAULT SYSDATE NOT NULL,

    CONSTRAINT PK_RDPS_OFFER PRIMARY KEY (OFFER_ID),
    CONSTRAINT FK_OFFER_CANDIDATE FOREIGN KEY (CANDIDATE_ID, REQ_NUMBER)
        REFERENCES RDPS_CANDIDATE(CANDIDATE_ID, REQ_NUMBER)
);

CREATE INDEX IDX_OFFER_CANDIDATE ON RDPS_OFFER(CANDIDATE_ID);
CREATE INDEX IDX_OFFER_STATUS ON RDPS_OFFER(OFFER_STATUS);
```

### Task 2.2: Create Model Class (0.5 day)

**New File:** `src/main/java/eduhk/fhr/web/model/Offer.java`

Create POJO with 30 fields matching database schema + getters/setters

### Task 2.3: Create DAO Class (0.5 day)

**New File:** `src/main/java/eduhk/fhr/web/dao/OfferDAO.java`

```java
@Repository
public class OfferDAO extends BaseDao {
    public void insertOffer(Offer offer) { /* INSERT */ }
    public void updateOffer(Offer offer) { /* UPDATE */ }
    public boolean offerExists(String offerId) { /* Check existence */ }
    public void upsertOffer(Offer offer) { /* Insert or update */ }
    public Offer findByOfferId(String offerId) { /* Find by PK */ }
    public List<Offer> findByCandidateId(String candidateId) { /* Find by candidate */ }
}
```

### Task 2.4: Create Mapper Class (0.5 day)

**New File:** `src/main/java/eduhk/fhr/web/mapper/OfferMapper.java`

Map from TalentLink Application object to Offer model

### Task 2.5: Create Import Service (0.5 day)

**New File:** `src/main/java/eduhk/fhr/web/service/import_/OfferImportService.java`

```java
@Service
@Transactional
public class OfferImportService {
    @Autowired private OfferDAO offerDAO;
    @Autowired private OfferMapper offerMapper;

    public void importOffer(TalentLinkOfferDTO offerDto, String candidateId) {
        Offer offer = offerMapper.mapToModel(offerDto, candidateId);
        offerDAO.upsertOffer(offer);
    }
}
```

---

## Phase 3: Status Filtering & Sync Tracking (2-3 days)

**Priority:** 🟡 HIGH - Core sync functionality

### Task 3.1: Create Sync Status Table (0.5 day)

**New File:** `db_scripts/rdps_sync_status.sql`

```sql
CREATE TABLE RDPS_SYNC_STATUS (
    SYNC_ID             NUMBER NOT NULL,
    CANDIDATE_ID        VARCHAR2(20) NOT NULL,
    REQ_NUMBER          VARCHAR2(20) NOT NULL,
    SYNC_STATUS         VARCHAR2(20) NOT NULL,  -- SYNCED, FAILED, PENDING
    LAST_SYNC_DATE      TIMESTAMP,
    SYNC_ATTEMPT_COUNT  NUMBER DEFAULT 0,
    LAST_ERROR_MESSAGE  VARCHAR2(4000),
    CREATED_BY          VARCHAR2(32) DEFAULT USER NOT NULL,
    CREATION_DATE       DATE DEFAULT SYSDATE NOT NULL,
    USERSTAMP           VARCHAR2(32) DEFAULT USER NOT NULL,
    TIMESTAMP           DATE DEFAULT SYSDATE NOT NULL,

    CONSTRAINT PK_RDPS_SYNC_STATUS PRIMARY KEY (SYNC_ID),
    CONSTRAINT UK_SYNC_CANDIDATE UNIQUE (CANDIDATE_ID, REQ_NUMBER)
);

CREATE SEQUENCE RDPS_SYNC_STATUS_SEQ START WITH 1 INCREMENT BY 1;
```

### Task 3.2: Create SyncStatusDAO (0.5 day)

**New File:** `src/main/java/eduhk/fhr/web/dao/SyncStatusDAO.java`

```java
@Repository
public class SyncStatusDAO extends BaseDao {
    public boolean isCandidateSynced(String candidateId, String reqNumber);
    public void markAsSynced(String candidateId, String reqNumber);
    public void markAsFailed(String candidateId, String reqNumber, String errorMsg);
    public List<SyncStatus> getFailedSyncsForRetry(int maxAttempts);
    public void incrementAttemptCount(String candidateId, String reqNumber);
}
```

### Task 3.3: Implement Status Filtering (1 day)

**File:** `src/main/java/eduhk/fhr/web/service/TalentLinkSOAPCandidateService.java`

**Add new method:**
```java
public List<Profile> getCandidatesByStatus(Integer page, int limit, List<String> statusFilter) {
    CandidateSearchCriteriaDTO searchDto = new CandidateSearchCriteriaDTO();

    // Add status filter
    for (String statusStr : statusFilter) {
        Status status = Status.fromValue(statusStr);
        searchDto.getStatus().add(status);
    }

    // Fetch candidates
    CandidateIdsDTO candidateIds = candidateService.getCandidates(page, searchDto);
    List<Profile> profiles = new ArrayList<>();

    for (Long candidateId : candidateIds.getCandidateIds()) {
        Profile profile = candidateService.getCandidateById(candidateId, false, false);
        profiles.add(profile);
        if (profiles.size() >= limit) break;
    }

    return profiles;
}
```

**⚠️ IMPORTANT:** Need to verify actual status values in TalentLink API. The enum shows: Active, Archive, Inactive, New, UnderHiring - but user requirement is "Offered" and "Hired". These may be Application status values, not Candidate status values.

### Task 3.4: Create FilteredImportOrchestrationService (1 day)

**New File:** `src/main/java/eduhk/fhr/web/service/import_/FilteredImportOrchestrationService.java`

```java
@Service
public class FilteredImportOrchestrationService {

    @Autowired private TalentLinkSOAPCandidateService soapService;
    @Autowired private SyncStatusDAO syncStatusDAO;
    @Autowired private CandidateImportService candidateImportService;
    @Autowired private EducationImportService educationImportService;
    @Autowired private WorkExperienceImportService workExperienceImportService;
    @Autowired private RefereeImportService refereeImportService;
    @Autowired private OtherInfoImportService otherInfoImportService;
    @Autowired private OfferImportService offerImportService;
    @Autowired private ParameterService parameterService;

    public ImportResultSummary importFilteredCandidates() {
        // Get configuration
        List<String> statusFilter = getStatusFilterFromConfig();
        int batchSize = getBatchSize();
        int maxRetryAttempts = getMaxRetryAttempts();

        // Fetch candidates with status filter
        List<Profile> profiles = soapService.getCandidatesByStatus(0, batchSize, statusFilter);

        int successCount = 0;
        int failedCount = 0;

        for (Profile profile : profiles) {
            String candidateId = profile.getCandidateId();
            String reqNumber = profile.getRequisitionNumber();

            // Skip if already synced
            if (syncStatusDAO.isCandidateSynced(candidateId, reqNumber)) {
                continue;
            }

            try {
                // Import all entities
                importCandidateWithRelatedData(profile);

                // Mark as synced
                syncStatusDAO.markAsSynced(candidateId, reqNumber);
                successCount++;

            } catch (Exception e) {
                // Mark as failed for retry
                syncStatusDAO.markAsFailed(candidateId, reqNumber, e.getMessage());
                failedCount++;
            }
        }

        // Retry previously failed candidates
        retryFailedCandidates(maxRetryAttempts);

        return new ImportResultSummary(successCount, failedCount);
    }

    private void importCandidateWithRelatedData(Profile profile) {
        // Convert and import
        TalentLinkCandidateDTO dto = soapService.convertProfileToDTO(profile);

        candidateImportService.importCandidate(dto);
        educationImportService.importEducation(dto.getCandidateId(), dto.getEducations());
        workExperienceImportService.importWorkExperience(dto.getCandidateId(), dto.getWorkExperiences());
        refereeImportService.importReferee(dto.getCandidateId(), dto.getReferees());
        otherInfoImportService.importOtherInfo(dto);

        // Import offer (fetch from applications)
        List<Application> applications = soapService.getApplicationsByCandidateId(
            Long.parseLong(dto.getCandidateId())
        );
        for (Application app : applications) {
            if ("Offered".equals(app.getStatus()) || "Hired".equals(app.getStatus())) {
                offerImportService.importOffer(app, dto.getCandidateId());
            }
        }
    }

    private void retryFailedCandidates(int maxAttempts) {
        List<SyncStatus> failedSyncs = syncStatusDAO.getFailedSyncsForRetry(maxAttempts);

        for (SyncStatus failedSync : failedSyncs) {
            try {
                // Fetch candidate again
                Profile profile = soapService.getCandidateById(
                    Long.parseLong(failedSync.getCandidateId()), false, false
                );

                // Retry import
                importCandidateWithRelatedData(profile);

                // Mark as synced
                syncStatusDAO.markAsSynced(failedSync.getCandidateId(), failedSync.getReqNumber());

            } catch (Exception e) {
                // Increment attempt count
                syncStatusDAO.incrementAttemptCount(failedSync.getCandidateId(), failedSync.getReqNumber());
            }
        }
    }
}
```

---

## Phase 4: Scheduled Job Integration (1-2 days)

**Priority:** 🟡 MEDIUM - Automation

### Task 4.1: Create DailyTalentLinkSyncService (0.5 day)

**New File:** `src/main/java/eduhk/fhr/web/service/sync/DailyTalentLinkSyncService.java`

```java
@Service
public class DailyTalentLinkSyncService {

    @Autowired private FilteredImportOrchestrationService filteredImportService;
    @Autowired private ImportNotificationService notificationService;
    @Autowired private ParameterService parameterService;

    public String executeDailySync() {
        // Check if enabled
        String enabled = parameterService.getParameterValue("DAILY_SYNC_ENABLED");
        if (!"Y".equals(enabled)) {
            return "Daily sync is disabled";
        }

        // Execute sync
        ImportResultSummary summary = filteredImportService.importFilteredCandidates();

        // Send notification
        notificationService.sendImportNotification(summary);

        return String.format("Total: %d, Success: %d, Failed: %d",
            summary.getTotalProcessed(),
            summary.getSuccessCount(),
            summary.getFailedCount()
        );
    }
}
```

### Task 4.2: Add Scheduled Job to ScheduleTask (0.5 day)

**File:** `src/main/java/eduhk/fhr/web/scheduler/ScheduleTask.java`

**Add new method:**
```java
@Autowired
private DailyTalentLinkSyncService dailyTalentLinkSyncService;

@SchedulerLock(
    name = "DailyTalentLinkSyncLock",
    lockAtMostFor = "2h",
    lockAtLeastFor = "5m"
)
@Scheduled(cron = "0 0 2 * * ?")  // Daily at 2:00 AM
public void syncOfferedHiredCandidates() {
    writeLog("=== Daily TalentLink Sync Start ===");

    try {
        String result = dailyTalentLinkSyncService.executeDailySync();
        writeLog("Sync Result: " + result);
    } catch (Exception e) {
        writeErrorLog("Daily sync failed: " + e.getMessage(), e);
    }

    writeLog("=== Daily TalentLink Sync End ===");
}
```

**Cron Schedules:**
- Production: `"0 0 2 * * ?"` (Daily at 2:00 AM)
- Testing: `"0 */5 * * * ?"` (Every 5 minutes)

### Task 4.3: Add Configuration Parameters (0.5 day)

**Execute SQL:**
```sql
-- Enable/disable sync
INSERT INTO RDPS_PARAMETER (PARAM_CODE, PARAM_VALUE, REMARKS)
VALUES ('DAILY_SYNC_ENABLED', 'Y', 'Enable daily sync (Y/N)');

-- Batch size
INSERT INTO RDPS_PARAMETER (PARAM_CODE, PARAM_VALUE, REMARKS)
VALUES ('DAILY_SYNC_BATCH_SIZE', '100', 'Max candidates per sync run');

-- Retry attempts
INSERT INTO RDPS_PARAMETER (PARAM_CODE, PARAM_VALUE, REMARKS)
VALUES ('DAILY_SYNC_MAX_RETRY_ATTEMPTS', '3', 'Max retry attempts for failures');

-- Status filter
INSERT INTO RDPS_PARAMETER (PARAM_CODE, PARAM_VALUE, REMARKS)
VALUES ('DAILY_SYNC_STATUS_FILTER', 'Offered,Hired', 'Comma-separated status values');

COMMIT;
```

---

## Phase 5: Testing & Deployment (2-3 days)

### Task 5.1: Unit Tests (1 day)

**Create test files:**
- `CandidateMapperTest` - Verify all 20 fields mapped
- `OfferMapperTest` - Verify BigDecimal conversion
- `SyncStatusDAOTest` - Test retry logic
- `FilteredImportOrchestrationServiceTest` - Test skip/retry logic

### Task 5.2: Integration Tests (1 day)

**Test scenarios:**
1. End-to-end sync with mock SOAP responses
2. Status filtering works correctly
3. Skip already synced candidates
4. Retry failed candidates (max 3 attempts)
5. Upsert updates existing candidates
6. Email notifications sent

### Task 5.3: Deployment Checklist (0.5 day)

**Pre-deployment:**
- [ ] Run database scripts (RDPS_OFFER, RDPS_SYNC_STATUS tables)
- [ ] Insert configuration parameters
- [ ] Verify TalentLink SOAP credentials in RDPS_PARAMETER
- [ ] Back up production database

**Deployment:**
- [ ] Build WAR: `mvn clean package -DskipTests`
- [ ] Stop application server
- [ ] Deploy new WAR
- [ ] Start application server

**Post-deployment:**
- [ ] Verify scheduled job registered (check logs)
- [ ] Verify ShedLock entry created in RDPS_SHEDLOCK
- [ ] Test manual sync trigger
- [ ] Monitor first automatic run at 2:00 AM
- [ ] Check RDPS_IMPORT_HISTORY for results
- [ ] Verify email notification received

---

## Critical Files Summary

### Files to Modify:
1. `src/main/java/eduhk/fhr/web/mapper/CandidateMapper.java` - Complete 13 missing fields
2. `src/main/java/eduhk/fhr/web/mapper/EducationMapper.java` - Add LOV resolutions + 4 fields
3. `src/main/java/eduhk/fhr/web/mapper/WorkExperienceMapper.java` - Add 5 fields
4. `src/main/java/eduhk/fhr/web/mapper/RefereeMapper.java` - Add 3 fields
5. `src/main/java/eduhk/fhr/web/mapper/OtherInfoMapper.java` - Add 16 fields
6. `src/main/java/eduhk/fhr/web/service/TalentLinkSOAPCandidateService.java` - Add status filtering
7. `src/main/java/eduhk/fhr/web/scheduler/ScheduleTask.java` - Add daily sync job

### Files to Create:
1. `db_scripts/rdps_offer.sql` - Offer table schema
2. `db_scripts/rdps_sync_status.sql` - Sync tracking table
3. `src/main/java/eduhk/fhr/web/model/Offer.java` - Offer model
4. `src/main/java/eduhk/fhr/web/dao/OfferDAO.java` - Offer DAO
5. `src/main/java/eduhk/fhr/web/dao/SyncStatusDAO.java` - Sync status DAO
6. `src/main/java/eduhk/fhr/web/mapper/OfferMapper.java` - Offer mapper
7. `src/main/java/eduhk/fhr/web/service/import_/OfferImportService.java` - Offer import service
8. `src/main/java/eduhk/fhr/web/service/import_/FilteredImportOrchestrationService.java` - Main sync orchestrator
9. `src/main/java/eduhk/fhr/web/service/sync/DailyTalentLinkSyncService.java` - Scheduled job wrapper

---

## Risk Assessment & Mitigation

### 🔴 Critical Risks

**1. Status Value Mismatch**
- **Issue:** TalentLink Candidate Status enum shows: Active, Archive, Inactive, New, UnderHiring
- **User Requirement:** "Offered" and "Hired" status
- **Risk:** These status values may not exist in Candidate status
- **Mitigation:**
  - Verify with TalentLink API documentation
  - May need to use Application status instead of Candidate status
  - Alternative: Fetch all candidates and filter by Application status

**2. SOAP Field Mapping Assumptions**
- **Issue:** Many DTO field names are assumptions, not verified against actual SOAP response
- **Risk:** Runtime errors, null values, missing data
- **Mitigation:**
  - Capture real SOAP response in test environment
  - Verify all field names before implementation
  - Add comprehensive null checks

**3. Offer Data Location**
- **Issue:** Offer details likely in Application object, not Profile
- **Risk:** May need additional SOAP calls per candidate (performance impact)
- **Mitigation:**
  - Use `getApplicationsByCandidateId()` to fetch applications
  - Filter applications by status
  - Extract offer details from Application

### 🟡 Medium Risks

**1. Performance with Large Volumes**
- **Issue:** 1000+ candidates may exceed 2-hour execution window
- **Mitigation:** Pagination, batch processing, monitor execution time

**2. Failed Candidate Accumulation**
- **Issue:** Failed candidates may pile up if issues not resolved
- **Mitigation:** Max 3 retry attempts, admin UI for manual review

---

## Success Criteria

### Functional Requirements:
- ✅ Daily sync runs automatically at 2:00 AM
- ✅ Only candidates with "Offered" or "Hired" status synced
- ✅ All 6 entities imported (Candidate, Education, Work Exp, Referee, Other Info, Offer)
- ✅ Upsert strategy: insert new + update existing
- ✅ Skip already synced candidates
- ✅ Retry failed candidates up to 3 times
- ✅ Errors logged to RDPS_IMPORT_VALIDATION_ERROR
- ✅ Email notification sent on completion
- ✅ Import history tracked in RDPS_IMPORT_HISTORY
- ✅ Sync status tracked in RDPS_SYNC_STATUS

### Non-Functional Requirements:
- ✅ ShedLock prevents concurrent execution
- ✅ Transaction management for data consistency
- ✅ Performance: Complete within 2-hour window
- ✅ Configurable via RDPS_PARAMETER table

---

## Estimated Effort

| Phase | Tasks | Effort |
|-------|-------|--------|
| Phase 1: Complete Mappers | 5 tasks | 3-4 days |
| Phase 2: Offer Management | 5 tasks | 2-3 days |
| Phase 3: Sync Tracking | 4 tasks | 2-3 days |
| Phase 4: Scheduled Job | 3 tasks | 1-2 days |
| Phase 5: Testing | 3 tasks | 2-3 days |
| **Total** | **20 tasks** | **10-15 days** |

---

## Next Steps

1. Verify TalentLink status values ("Offered", "Hired" exist in API?)
2. Capture real SOAP response to validate field names
3. Confirm offer data is in Application object
4. Schedule deployment window for database schema changes
5. Begin Phase 1: Complete field mappers (highest priority)
