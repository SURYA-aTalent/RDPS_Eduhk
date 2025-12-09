# TalentLink to SharePoint Document Sync - Implementation Changes

**Project:** RDPS (Recruitment Data Processing System)
**Feature:** Automatic Document Sync from TalentLink to SharePoint
**Version:** 1.0
**Date:** 2025-11-26
**Author:** Development Team

---

## Table of Contents

1. [Overview](#overview)
2. [Feature Requirements](#feature-requirements)
3. [Architecture Changes](#architecture-changes)
4. [Files Created](#files-created)
5. [Files Modified](#files-modified)
6. [Database Changes](#database-changes)
7. [Configuration](#configuration)
8. [Testing Instructions](#testing-instructions)
9. [Deployment Checklist](#deployment-checklist)
10. [Rollback Instructions](#rollback-instructions)

---

## Overview

This document details all changes made to implement automatic synchronization of candidate documents from TalentLink to SharePoint.

### What This Feature Does

- **Fetches documents** from TalentLink via SOAP API for each candidate
- **Uploads documents** to SharePoint in candidate-specific folders
- **Tracks sync status** in database for audit trail and duplicate prevention
- **Scheduled execution** (currently disabled - manual trigger only)
- **Error handling** with comprehensive logging

### Key Benefits

- Automated document management
- Candidate-specific folder organization in SharePoint
- Full audit trail of all syncs (success/failure)
- Duplicate prevention
- Graceful error recovery

---

## Feature Requirements

### User Requirements (from planning)

| Requirement | Implementation |
|------------|----------------|
| **Trigger** | Automatic scheduled job (currently disabled) |
| **Tracking** | Full database tracking for audit compliance |
| **Organization** | By candidate (folder per candidate in SharePoint) |
| **Duplicates** | Skip if already uploaded (database check) |

### Technical Requirements

- Java 21
- Spring Boot 4.0.0-SNAPSHOT
- Oracle Database with RDPS schema
- TalentLink SOAP API credentials
- SharePoint via MS Graph API (existing integration)
- ShedLock for distributed locking

---

## Architecture Changes

### Document Retrieval Flow

```
Candidate ID
    ↓
getApplicationsByCandidateId() → List<ApplicationDto>
    ↓
Extract documents → List<DocumentBaseDto> (metadata)
    ↓
downloadAttachedFile(docId) → AttachedFileDto (binary data)
    ↓
Convert DataHandler to byte[] → Upload to SharePoint
    ↓
Record in database → RDPS_CANDIDATE_DOCUMENT
```

### Component Integration

```
ScheduleTask (Scheduler)
    ↓
CandidateDocumentSyncService (Orchestration)
    ├─→ TalentLinkSOAPCandidateService (Fetch documents)
    ├─→ SharePointUploadService (Upload files)
    ├─→ CandidateDocumentDAO (Track sync)
    └─→ CandidateDAO (Get candidate list)
```

---

## Files Created

### 1. Database Schema

**File:** `db_scripts/17_CREATE_CANDIDATE_DOCUMENT.sql`
**Purpose:** Create table to track document sync operations
**Lines:** 61

**Key Objects:**
- Table: `RDPS_CANDIDATE_DOCUMENT`
- Sequence: `RDPS_CANDIDATE_DOCUMENT_SEQ`
- Indexes: `IDX_CAND_DOC_CANDIDATE`, `IDX_CAND_DOC_STATUS`, `IDX_CAND_DOC_SYNC_DATE`
- Constraints: `FK_CAND_DOC_CANDIDATE`, `UK_CAND_DOC_TLK_ID` (unique)

**Table Structure:**
```sql
RDPS_CANDIDATE_DOCUMENT (
    DOC_ID NUMBER PRIMARY KEY,
    CANDIDATE_ID NUMBER NOT NULL,
    TALENTLINK_APP_ID NUMBER,
    TALENTLINK_DOC_ID NUMBER NOT NULL UNIQUE,
    FILE_NAME VARCHAR2(500) NOT NULL,
    FILE_SIZE NUMBER,
    DOC_TYPE VARCHAR2(100),
    SHAREPOINT_FILE_ID VARCHAR2(100),
    SHAREPOINT_WEB_URL VARCHAR2(1000),
    SHAREPOINT_FOLDER_PATH VARCHAR2(500),
    SYNC_STATUS VARCHAR2(50) NOT NULL,
    SYNC_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ERROR_MESSAGE VARCHAR2(4000),
    CREATED_BY VARCHAR2(100) DEFAULT 'SYSTEM',
    CREATED_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)
```

### 2. Model Class

**File:** `src/main/java/eduhk/fhr/web/model/CandidateDocument.java`
**Purpose:** Java model representing RDPS_CANDIDATE_DOCUMENT table
**Lines:** 165

**Key Features:**
- Complete getter/setter methods for all fields
- toString() method for debugging
- Default constructor

### 3. Data Access Object

**File:** `src/main/java/eduhk/fhr/web/dao/CandidateDocumentDAO.java`
**Purpose:** Database operations for candidate documents
**Lines:** 210

**Key Methods:**
| Method | Purpose |
|--------|---------|
| `isDocumentSynced(Long)` | Check if document already synced (duplicate detection) |
| `insert(CandidateDocument)` | Insert new document record |
| `updateSyncStatus(Long, String, String)` | Update sync status and error message |
| `findByCandidateId(Long)` | Get all documents for a candidate |
| `findFailedDocuments()` | Get failed documents for retry |
| `findSyncedDocuments()` | Get successfully synced documents |
| `countByStatus(String)` | Count documents by status |

**RowMapper:** `CandidateDocumentRowMapper` for result set mapping

### 4. Sync Orchestration Service

**File:** `src/main/java/eduhk/fhr/web/service/CandidateDocumentSyncService.java`
**Purpose:** Main service to orchestrate document sync from TalentLink to SharePoint
**Lines:** 243

**Key Methods:**
| Method | Purpose | Return Type |
|--------|---------|-------------|
| `syncCandidateDocuments(Long)` | Sync documents for single candidate | `int` (count synced) |
| `syncAllCandidateDocuments()` | Sync documents for all candidates | `Map<String, Integer>` (stats) |
| `syncCandidateDocuments(List<Long>)` | Sync documents for specific candidates | `Map<String, Integer>` (stats) |

**Logic Flow:**
1. Fetch document metadata from TalentLink
2. Check if already synced (skip duplicates)
3. Download document binary data
4. Upload to SharePoint in candidate folder
5. Record success/failure in database
6. Continue on errors (doesn't stop batch)

---

## Files Modified

### 1. TalentLinkSOAPCandidateService.java

**File:** `src/main/java/eduhk/fhr/web/service/TalentLinkSOAPCandidateService.java`
**Changes:** Added 3 new methods (89 lines added)

**New Methods Added:**

#### a. getApplicationsByCandidateId()
```java
public List<ApplicationDto> getApplicationsByCandidateId(Long candidateId) throws Exception
```
- **Purpose:** Fetch all applications for a candidate
- **Returns:** List of ApplicationDto (contains document lists)
- **Lines:** 232-252

#### b. downloadAttachedFile()
```java
public AttachedFileDto downloadAttachedFile(Long documentId) throws Exception
```
- **Purpose:** Download document binary data from TalentLink
- **Returns:** AttachedFileDto with binary data
- **Lines:** 254-279

#### c. getCandidateDocuments()
```java
public List<DocumentBaseDto> getCandidateDocuments(Long candidateId) throws Exception
```
- **Purpose:** Extract all document metadata from candidate's applications
- **Returns:** List of DocumentBaseDto
- **Lines:** 281-312
- **Logic:** Iterates through applications and extracts documents from each

### 2. SharePointUploadService.java

**File:** `src/main/java/eduhk/fhr/web/service/SharePointUploadService.java`
**Changes:** Added 1 new method (52 lines added)

**New Method Added:**

#### uploadFileToFolder()
```java
public DriveItem uploadFileToFolder(byte[] fileBytes, String fileName, String folderPath) throws Exception
```
- **Purpose:** Upload file to specific folder path in SharePoint
- **Parameters:**
  - `fileBytes` - File content as byte array
  - `fileName` - File name
  - `folderPath` - Relative path (e.g., "Candidate_123")
- **Returns:** DriveItem with SharePoint details
- **Lines:** 167-218
- **Key Feature:** MS Graph API automatically creates folders if they don't exist

**Upload Path Format:**
```
SharePoint Drive Root
  └── Candidate_{candidateId}
       └── {sanitized_filename}
```

### 3. CandidateDAO.java

**File:** `src/main/java/eduhk/fhr/web/dao/CandidateDAO.java`
**Changes:** Added 1 new method (9 lines added)

**New Method Added:**

#### getAllCandidateIds()
```java
public java.util.List<Long> getAllCandidateIds()
```
- **Purpose:** Get all candidate IDs from database
- **Returns:** List of candidate IDs (as Long)
- **Lines:** 163-171
- **SQL:** Converts CANDIDATE_ID to NUMBER and orders by ID

### 4. ScheduleTask.java

**File:** `src/main/java/eduhk/fhr/web/scheduler/ScheduleTask.java`
**Changes:** Added autowired service and scheduled method (31 lines added)

**Autowired Service:**
```java
@Autowired
private eduhk.fhr.web.service.CandidateDocumentSyncService candidateDocumentSyncService;
```
- **Lines:** 24-25

**New Scheduled Method:**

#### syncCandidateDocuments()
```java
@SchedulerLock(name = "CandidateDocumentSyncLock", lockAtMostFor = "2h", lockAtLeastFor = "5m")
//@Scheduled(cron = "0 0 3 * * ?")  // Daily at 3:00 AM - CURRENTLY DISABLED
public void syncCandidateDocuments()
```
- **Purpose:** Scheduled job to sync all candidate documents
- **Schedule:** Daily at 3:00 AM (currently disabled)
- **Lock:** Prevents concurrent execution across multiple instances
- **Lock Times:** Max 2 hours, min 5 minutes
- **Lines:** 64-89
- **Status:** **DISABLED** (commented out @Scheduled annotation)

**Statistics Logged:**
- Total candidates in database
- Candidates successfully processed
- Total documents synced
- Failed candidates count

---

## Database Changes

### New Table: RDPS_CANDIDATE_DOCUMENT

**Schema:** RDPS
**Purpose:** Track candidate document sync operations

**Columns:**

| Column Name | Data Type | Nullable | Description |
|------------|-----------|----------|-------------|
| DOC_ID | NUMBER | NO | Primary key (from sequence) |
| CANDIDATE_ID | NUMBER | NO | Foreign key to RDPS_CANDIDATE |
| TALENTLINK_APP_ID | NUMBER | YES | Application ID from TalentLink |
| TALENTLINK_DOC_ID | NUMBER | NO | Document ID in TalentLink (UNIQUE) |
| FILE_NAME | VARCHAR2(500) | NO | Original file name |
| FILE_SIZE | NUMBER | YES | File size in bytes |
| DOC_TYPE | VARCHAR2(100) | YES | Document type from TalentLink |
| SHAREPOINT_FILE_ID | VARCHAR2(100) | YES | SharePoint DriveItem ID |
| SHAREPOINT_WEB_URL | VARCHAR2(1000) | YES | SharePoint web URL |
| SHAREPOINT_FOLDER_PATH | VARCHAR2(500) | YES | Folder path in SharePoint |
| SYNC_STATUS | VARCHAR2(50) | NO | Status: SYNCED, FAILED, SKIPPED |
| SYNC_DATE | TIMESTAMP | NO | Date/time of sync attempt |
| ERROR_MESSAGE | VARCHAR2(4000) | YES | Error message if failed |
| CREATED_BY | VARCHAR2(100) | NO | User/system that created record |
| CREATED_DATE | TIMESTAMP | NO | Record creation timestamp |

**Constraints:**
- Primary Key: `DOC_ID`
- Foreign Key: `CANDIDATE_ID` → `RDPS_CANDIDATE(CANDIDATE_ID)`
- Unique: `TALENTLINK_DOC_ID` (prevents duplicates)

**Indexes:**
- `IDX_CAND_DOC_CANDIDATE` on `CANDIDATE_ID` (performance)
- `IDX_CAND_DOC_STATUS` on `SYNC_STATUS` (query optimization)
- `IDX_CAND_DOC_SYNC_DATE` on `SYNC_DATE` (date range queries)

**Sequence:**
- Name: `RDPS_CANDIDATE_DOCUMENT_SEQ`
- Start: 1
- Increment: 1

### Deployment SQL

```sql
-- Run as RDPS user
sqlplus RDPS/rdps_password123@FREEPDB1 @db_scripts/17_CREATE_CANDIDATE_DOCUMENT.sql
```

---

## Configuration

### No New Configuration Required

This feature reuses existing configuration:
- **TalentLink SOAP:** Existing credentials in `RDPS_PARAMETER` table
- **SharePoint:** Existing configuration in `application-local.properties`
- **ShedLock:** Existing `RDPS_SHEDLOCK` table

### Scheduled Job Configuration

**Current Status:** DISABLED (manual trigger only)

**To Enable Automatic Scheduling:**

Edit `src/main/java/eduhk/fhr/web/scheduler/ScheduleTask.java` line 70:

```java
// Change from:
//@Scheduled(cron = "0 0 3 * * ?")  // Daily at 3:00 AM - DISABLED

// To:
@Scheduled(cron = "0 0 3 * * ?")  // Daily at 3:00 AM
```

**Schedule Options:**
```java
// Production: Daily at 3:00 AM
@Scheduled(cron = "0 0 3 * * ?")

// Testing: Every 10 minutes
@Scheduled(cron = "0 */10 * * * ?")

// Testing: Every 5 minutes
@Scheduled(cron = "0 */5 * * * ?")
```

---

## Testing Instructions

### 1. Database Setup

```bash
# Connect to Oracle database
sqlplus RDPS/rdps_password123@FREEPDB1

# Run table creation script
@db_scripts/17_CREATE_CANDIDATE_DOCUMENT.sql

# Verify table created
SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME = 'RDPS_CANDIDATE_DOCUMENT';
-- Expected: 1
```

### 2. Build Application

```bash
# Clean build
mvn clean package -DskipTests

# Verify WAR file created
ls -lh target/RDPS-0.0.1-SNAPSHOT.war
```

### 3. Start Application

```bash
# Start with Spring Boot
mvn spring-boot:run

# Or run as standalone WAR
java -jar target/RDPS-0.0.1-SNAPSHOT.war
```

### 4. Verify Initialization

**Check logs for:**
```
INFO - Initializing TalentLink SOAP Candidate Service
INFO - TalentLink SOAP Candidate Service initialized successfully
INFO - Initializing SharePoint Upload Service
INFO - SharePoint Upload Service initialized successfully
```

### 5. Manual Testing (Option A: Direct Service Call)

Create a test controller endpoint:

```java
@RestController
@RequestMapping("/test")
public class DocumentSyncTestController {

    @Autowired
    private CandidateDocumentSyncService syncService;

    @GetMapping("/sync-candidate/{candidateId}")
    public Map<String, Object> testSync(@PathVariable Long candidateId) {
        int synced = syncService.syncCandidateDocuments(candidateId);
        Map<String, Object> result = new HashMap<>();
        result.put("candidateId", candidateId);
        result.put("documentsSynced", synced);
        return result;
    }
}
```

**Test URL:**
```
http://localhost:8080/RDPS/test/sync-candidate/12345
```

### 6. Manual Testing (Option B: Enable Test Schedule)

Edit `ScheduleTask.java` line 71:

```java
@Scheduled(cron = "0 */10 * * * ?") // Run every 10 minutes for testing
```

**Monitor logs:**
```bash
tail -f catalina.base_IS_UNDEFINED/logs/server.log_RDPS.log | grep -i "document sync"
```

### 7. Verify Database Records

```sql
-- Check synced documents
SELECT
    DOC_ID,
    CANDIDATE_ID,
    FILE_NAME,
    SYNC_STATUS,
    SYNC_DATE,
    SHAREPOINT_WEB_URL
FROM RDPS_CANDIDATE_DOCUMENT
WHERE SYNC_STATUS = 'SYNCED'
ORDER BY SYNC_DATE DESC
FETCH FIRST 10 ROWS ONLY;

-- Check failed documents
SELECT
    CANDIDATE_ID,
    FILE_NAME,
    ERROR_MESSAGE,
    SYNC_DATE
FROM RDPS_CANDIDATE_DOCUMENT
WHERE SYNC_STATUS = 'FAILED'
ORDER BY SYNC_DATE DESC;

-- Summary statistics
SELECT
    SYNC_STATUS,
    COUNT(*) as COUNT,
    SUM(FILE_SIZE) as TOTAL_SIZE_BYTES,
    MIN(SYNC_DATE) as FIRST_SYNC,
    MAX(SYNC_DATE) as LAST_SYNC
FROM RDPS_CANDIDATE_DOCUMENT
GROUP BY SYNC_STATUS;
```

### 8. Verify SharePoint

**Navigate to SharePoint site and check:**
1. Folders created: `Candidate_{candidateId}`
2. Files uploaded with correct names
3. Files can be opened/downloaded
4. Folder structure is organized

**Example SharePoint structure:**
```
Document Library Root
  ├── Candidate_12345/
  │   ├── resume.pdf
  │   ├── certificate.pdf
  │   └── reference_letter.docx
  ├── Candidate_12346/
  │   └── cv.pdf
  └── Candidate_12347/
      ├── diploma.pdf
      └── transcript.pdf
```

### 9. Test Error Scenarios

**Test cases:**
1. Invalid candidate ID → Should log error and continue
2. Document without binary data → Should skip and log warning
3. SharePoint upload failure → Should record as FAILED in database
4. TalentLink service down → Should log error and continue with next candidate

---

## Deployment Checklist

### Pre-Deployment

- [ ] Code review completed
- [ ] All files committed to version control
- [ ] Database script reviewed and tested
- [ ] Build successful: `mvn clean package -DskipTests`
- [ ] Unit tests pass (if applicable)
- [ ] Documentation complete

### Deployment Steps

1. **Backup Database**
   ```sql
   -- Create backup of RDPS schema
   expdp RDPS/password DIRECTORY=backup_dir DUMPFILE=rdps_backup_20251126.dmp
   ```

2. **Deploy Database Changes**
   ```bash
   sqlplus RDPS/password@PROD @db_scripts/17_CREATE_CANDIDATE_DOCUMENT.sql
   ```

3. **Verify Database**
   ```sql
   SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME = 'RDPS_CANDIDATE_DOCUMENT';
   SELECT COUNT(*) FROM USER_SEQUENCES WHERE SEQUENCE_NAME = 'RDPS_CANDIDATE_DOCUMENT_SEQ';
   ```

4. **Deploy Application**
   ```bash
   # Stop application
   # Deploy new WAR file
   # Start application
   ```

5. **Verify Deployment**
   - Check application logs for initialization
   - Verify services initialized successfully
   - Test manual sync for one candidate

### Post-Deployment

- [ ] Verify all services initialized
- [ ] Test manual document sync
- [ ] Monitor logs for errors
- [ ] Check database records
- [ ] Verify SharePoint uploads
- [ ] Document any issues

### Enabling Automatic Sync (Optional)

**Only after successful testing:**

1. Uncomment `@Scheduled` annotation in `ScheduleTask.java`
2. Rebuild and redeploy
3. Verify scheduled job runs at configured time
4. Monitor first automated execution

---

## Rollback Instructions

### If Issues Occur

### 1. Disable Scheduled Job (Quick Fix)

**Edit:** `src/main/java/eduhk/fhr/web/scheduler/ScheduleTask.java`

```java
//@Scheduled(cron = "0 0 3 * * ?")  // DISABLED
```

**Rebuild and restart** application.

### 2. Complete Rollback

**Step 1: Stop Application**

**Step 2: Rollback Database**

```sql
-- Drop new objects
DROP TABLE RDPS_CANDIDATE_DOCUMENT CASCADE CONSTRAINTS PURGE;
DROP SEQUENCE RDPS_CANDIDATE_DOCUMENT_SEQ;
```

**Step 3: Revert Code Changes**

```bash
# Revert to previous commit
git revert HEAD

# Or restore previous version
git checkout <previous-commit-hash>
```

**Step 4: Rebuild and Deploy**

```bash
mvn clean package -DskipTests
# Deploy previous WAR file
```

**Step 5: Verify Rollback**

- Application starts successfully
- No errors in logs
- Existing functionality works

### 3. Partial Rollback (Keep Database, Disable Feature)

**Option:** Keep database table for future use, but disable all sync functionality

1. Comment out `@Autowired CandidateDocumentSyncService` in `ScheduleTask.java`
2. Comment out scheduled method
3. Rebuild and restart

**Data Retention:**
- Database table remains
- Historical sync records preserved
- Can re-enable feature later

---

## Monitoring & Maintenance

### Log Monitoring

**Key log messages:**
```
=== Starting Candidate Document Sync Job ===
Starting document sync for candidate {id}
Found {n} documents for candidate {id}
Successfully synced document {docId} for candidate {id}
Document sync completed for candidate {id}. Synced: {n}/{total}
=== Candidate Document Sync Job Finished ===
```

**Error patterns:**
```
Failed to sync document {docId} for candidate {id}
Error syncing documents for candidate {id}
Document sync job failed
```

### Database Maintenance Queries

```sql
-- Cleanup old records (older than 1 year)
DELETE FROM RDPS_CANDIDATE_DOCUMENT
WHERE SYNC_DATE < ADD_MONTHS(SYSDATE, -12);

-- Re-sync failed documents
SELECT CANDIDATE_ID, TALENTLINK_DOC_ID, FILE_NAME, ERROR_MESSAGE
FROM RDPS_CANDIDATE_DOCUMENT
WHERE SYNC_STATUS = 'FAILED'
AND SYNC_DATE > SYSDATE - 7  -- Last 7 days
ORDER BY SYNC_DATE DESC;

-- Performance check
SELECT
    TO_CHAR(SYNC_DATE, 'YYYY-MM-DD') as SYNC_DAY,
    COUNT(*) as TOTAL_DOCS,
    SUM(CASE WHEN SYNC_STATUS = 'SYNCED' THEN 1 ELSE 0 END) as SYNCED,
    SUM(CASE WHEN SYNC_STATUS = 'FAILED' THEN 1 ELSE 0 END) as FAILED
FROM RDPS_CANDIDATE_DOCUMENT
WHERE SYNC_DATE > SYSDATE - 30
GROUP BY TO_CHAR(SYNC_DATE, 'YYYY-MM-DD')
ORDER BY SYNC_DAY DESC;
```

---

## Summary of Changes

### Statistics

| Category | Count |
|----------|-------|
| **Files Created** | 4 |
| **Files Modified** | 4 |
| **Lines Added** | ~700 |
| **Database Tables** | 1 |
| **Database Sequences** | 1 |
| **New Methods** | 12 |
| **Scheduled Jobs** | 1 (disabled) |

### Key Achievements

✅ Automatic document sync from TalentLink to SharePoint
✅ Full database tracking and audit trail
✅ Duplicate prevention via unique constraints
✅ Candidate-specific folder organization
✅ Comprehensive error handling and logging
✅ Distributed lock support (ShedLock)
✅ Manual and automatic trigger options
✅ Graceful error recovery (continues on failures)

### Current Status

- **Implementation:** ✅ Complete
- **Testing:** ⏳ Pending
- **Automatic Scheduling:** ❌ Disabled (manual trigger only)
- **Database:** ✅ Schema created
- **Documentation:** ✅ Complete

---

## Support & Troubleshooting

### Common Issues

**Issue 1: "SOAP Candidate service not initialized"**
- Check TalentLink credentials in `RDPS_PARAMETER` table
- Verify SOAP endpoint URL is accessible
- Review application startup logs

**Issue 2: "SharePoint service not initialized"**
- Check SharePoint configuration in `application-local.properties`
- Verify Azure AD credentials are valid
- Test MS Graph API connectivity

**Issue 3: No documents found for candidate**
- Verify candidate has applications in TalentLink
- Check application has documents attached
- Review TalentLink SOAP response

**Issue 4: Upload fails with "File too large"**
- Default limit: 50 MB
- Configure in `sharepoint.max.file.size.mb`
- Large files (>250 MB) not supported

### Contact

For issues or questions:
- Review logs: `catalina.base_IS_UNDEFINED/logs/server.log_RDPS.log`
- Check database: Query `RDPS_CANDIDATE_DOCUMENT` table
- Verify SharePoint: Check folder structure

---

**END OF DOCUMENT**
