# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Run Commands

### Prerequisites
- Java 21 (required - application compiled with class version 61.0)
- Maven 3.8+
- Docker (for Oracle Database)

### Database Setup

**Automated setup (recommended):**
```bash
./setup_db.sh
```

**Manual Oracle setup:**
```bash
# Start Oracle container
docker run -d --name oracle-db-free -p 1521:1521 -e ORACLE_PWD=password123 container-registry.oracle.com/database/free

# Wait for ready (2-5 minutes)
docker logs -f oracle-db-free  # Wait for "DATABASE IS READY TO USE!"

# Install schema
docker cp db_scripts oracle-db-free:/tmp/db_scripts/
docker exec oracle-db-free bash -c "cd /tmp/db_scripts && sqlplus RDPS/rdps_password123@FREEPDB1 @00_INSTALL_ALL.sql"
```

### Build and Run

```bash
# Build WAR (skip tests)
mvn clean package -DskipTests

# Run with Maven (recommended for development)
mvn spring-boot:run

# Run as standalone WAR (requires Java 21)
java -jar target/RDPS-0.0.1-SNAPSHOT.war

# Access application
# http://localhost:8080/RDPS
# Default credentials: fhr-dev-uacct01 / password123
```

### Testing and Verification

```bash
# Verify database tables (expects 20)
docker exec oracle-db-free bash -c "echo 'SELECT COUNT(*) FROM USER_TABLES;' | sqlplus -s RDPS/rdps_password123@FREEPDB1"

# Check application logs
tail -f catalina.base_IS_UNDEFINED/logs/server.log_RDPS.log

# Verify system parameters
docker exec oracle-db-free bash -c "echo 'SELECT PARAM_CODE, PARAM_VALUE FROM RDPS_PARAMETER ORDER BY PARAM_CODE;' | sqlplus -s RDPS/rdps_password123@FREEPDB1"
```

### Database Operations

```bash
# Reset database (drop tables and reinstall)
./remove_db.sh --tables
./setup_db.sh --skip-docker

# Export configuration data
cd db_scripts
./DOCKER_EXPORT.sh

# Import configuration data
docker cp rdps_export.sql oracle-db-free:/tmp/
docker exec oracle-db-free sqlplus RDPS/rdps_password123@FREEPDB1 @/tmp/rdps_export.sql
```

## High-Level Architecture

### Technology Stack
- **Framework:** Spring Boot 4.0.0-SNAPSHOT (Java 21, WAR deployment)
- **Database:** Oracle Database (Raw JDBC with JdbcTemplate, no JPA/Hibernate)
- **Security:** Pre-authenticated SSO via HTTP headers (OAM_REMOTE_USER)
- **External Integration:** TalentLink SOAP API (JAX-WS with WS-Security)
- **Scheduling:** Spring @Scheduled with ShedLock (distributed task locking)
- **Frontend:** Thymeleaf 3.1.3 with Spring Security integration

### Layered Architecture Pattern

```
Controllers (HTTP request handling)
    ↓
Services (Business logic orchestration)
    ↓
DAOs (Raw JDBC operations via JdbcTemplate)
    ↓
Oracle Database
```

**Key Design Decision:** Raw JDBC instead of JPA for explicit SQL control, performance tuning, and reduced ORM complexity.

## Core Integration: TalentLink SOAP API

### Authentication Flow
1. **Initialization (@PostConstruct):**
   - `TalentLinkSOAPCandidateService` and `TalentLinkSOAPUserService` load credentials from `RDPS_PARAMETER` table
   - Credentials set in static `TalentLinkSOAPHandler` (⚠️ not thread-safe)

2. **Runtime (per SOAP request):**
   - `TalentLinkSOAPHandler` intercepts outbound SOAP messages
   - Adds WS-Security header with `<wsse:UsernameToken>` (plaintext password)
   - Sends to TalentLink endpoint: `{SOAP_URL}?api_key={API_KEY}`

### Required Parameters in Database
```sql
-- Must exist in RDPS_PARAMETER for SOAP services to initialize:
TALENTLINK_USERNAME
TALENTLINK_PASSWORD
TALENTLINK_API_KEY
TALENTLINK_CANDIDATE_SOAP_URL
TALENTLINK_USER_SOAP_URL
```

**Critical:** Application must be restarted after updating these parameters (loaded at startup).

## Import Orchestration Flow

**Entry Point:** `ImportOrchestrationService.importNewCandidates()`

**Workflow:**
1. Retrieve `LAST_IMPORTED_CANDIDATE_ID` from parameters
2. Fetch candidates from TalentLink via SOAP (paginated)
3. For each candidate:
   - Validate required fields (`CandidateValidationService`)
   - If invalid → log to `RDPS_IMPORT_VALIDATION_ERROR`
   - If valid → import cascade:
     - `CandidateImportService` → `RDPS_CANDIDATE`
     - `EducationImportService` → `RDPS_EDU_PROF_QUAL`
     - `WorkExperienceImportService` → `RDPS_WORK_EXPERIENCE`
     - `RefereeImportService` → `RDPS_REFEREE`
     - `OtherInfoImportService` → `RDPS_OTHER_INFO`
4. Track history in `RDPS_IMPORT_HISTORY`
5. Send email notification
6. Update `LAST_IMPORTED_CANDIDATE_ID`

**Key Services:**
- **ImportOrchestrationService** - Main coordinator
- **CandidateMapper** - Transforms SOAP `Profile` → `TalentLinkCandidateDTO` → `Candidate` model
- **ImportErrorHandler** - Persists validation errors for user review

## Scheduled Jobs (ShedLock)

**Configuration:** `ShedLockConfig` uses `RDPS_SHEDLOCK` table for distributed locks

**Jobs in `ScheduleTask`:**

1. **performTask()** - Cron: `0 30 14,22 * * ?` (2:30 PM & 10:30 PM daily)
   - Lock: "RDPSTaskLock" (5m max, 2m min)
   - Inserts audit record to `RDPS_SCHEDULER`

2. **syncUsersToTalentLink()** - Cron: `0 0 * * * ?` (Hourly)
   - Lock: "TalentLinkUserSyncLock" (10m max, 1m min)
   - Reads `RDPS_TALENTLINK_USER_STAGING` (where `syncedToTalentlink = 'N'`)
   - Creates/updates/deactivates users in TalentLink via SOAP
   - Marks synced records with timestamp and log

**Purpose of ShedLock:** Prevents duplicate job execution across multiple application instances in distributed deployments.

## Security Architecture

### Authentication
- **Pre-authenticated filter:** `HeaderAuthenticationFilter` extracts `OAM_REMOTE_USER` header
- **Local development fallback:** If `request.getLocalAddr()` starts with `0:0:0:0:0:0:0:1`, uses `fhr-dev-uacct01`
- **Guest fallback:** Missing header → `RDPS_GUEST` user

### Authorization (Role-Based)
```
/makeReservation     → ROLE_USE_RDPS
/viewReservation     → ROLE_USE_RDPS
/downloadLogs        → ROLE_ACCESS_RIGHT_SETUP
/setUserProfile      → ROLE_ACCESS_RIGHT_SETUP
/log/**              → ROLE_USE_RDPS
/main/**             → ROLE_USE_RDPS
/report/**           → ROLE_USE_RDPS
/                    → authenticated
/public/**           → permitAll
```

**User data loaded from:** `RDPS_USER_PROFILE` + `RDPS_USER_ROLE` tables via `UserDetailsServiceImpl`

## Database Strategy

### No ORM - Raw JDBC
- All DAOs extend `BaseDao` with `JdbcTemplate` and `NamedParameterJdbcTemplate`
- Manual SQL in DAO classes
- `PreparedStatementSetter` for parameter binding
- No JPA annotations on models

### Data Source Configuration
**Development (local profile):**
```properties
spring.datasource.url=jdbc:oracle:thin:@//localhost:1521/FREEPDB1
spring.datasource.username=RDPS
spring.datasource.password=rdps_password123
```

**Production (JNDI):**
- Configured in `AppConfig.dataSource()` with JNDI lookup: `java:comp/env/jdbc/datasources/rdpsDS`
- Falls back to H2 in-memory if JNDI unavailable

### Schema Management
- **No auto-DDL:** `spring.jpa.hibernate.ddl-auto=none`
- **Manual scripts:** `db_scripts/` directory contains all schema definitions
- **Master installer:** `00_INSTALL_ALL.sql` creates 20 tables + LOV data

## Key Domain Entities

**Candidate Hierarchy (Parent → Children):**
```
RDPS_CANDIDATE (candidate_id PK)
├── RDPS_EDU_PROF_QUAL (qual_id PK, candidate_id FK)
├── RDPS_WORK_EXPERIENCE (exp_id PK, candidate_id FK)
├── RDPS_REFEREE (referee_id PK, candidate_id FK)
└── RDPS_OTHER_INFO (info_id PK, candidate_id FK)
```

**System Tables:**
- `RDPS_PARAMETER` - System configuration (credentials, feature flags, last import ID)
- `RDPS_USER_PROFILE` + `RDPS_USER_ROLE` - User authentication/authorization
- `RDPS_IMPORT_HISTORY` - Import operation audit trail
- `RDPS_IMPORT_VALIDATION_ERROR` - Pre-import validation failures
- `RDPS_SHEDLOCK` - Distributed scheduler locks
- `RDPS_TALENTLINK_USER_STAGING` - User sync staging table
- `RDPS_EMAIL_JOB` + `RDPS_EMAIL_TEMPLATE` - Email notification system

**Lookup Tables (LOV):**
- `RDPS_LOV_DISTRICT` - Hong Kong districts (19 records)
- `RDPS_LOV_EDU_LEVEL` - Education levels (14 records)
- `RDPS_LOV_QUAL_AWARD_CLASS` - Award classes (16 records)
- `RDPS_LOV_QUAL_AWARD_DESC` - Award descriptions (35 records)
- `RDPS_LOV_STUDY_MODE` - Study modes (7 records)

## Important Configuration Files

**Application Properties:**
- `application.properties` - Base config (sets `spring.profiles.active=local`)
- `application-local.properties` - Development database connection
- `parameters.properties` - System metadata (version, date formats, SSO config)

**Logging:**
- `logback.xml` - Log appenders and levels
- File location: `${catalina.base}/logs/server.log_RDPS.log`
- In development: `./catalina.base_IS_UNDEFINED/logs/server.log_RDPS.log`

**Database Scripts:**
- `db_scripts/00_INSTALL_ALL.sql` - Master installation script
- `db_scripts/*.sql` - Individual table creation scripts
- `db_scripts/DOCKER_EXPORT.sh` - Export configuration data

## Common Development Scenarios

### Adding a New SOAP Operation to TalentLink
1. Check if stubs exist in `src/main/java/eduhk/fhr/web/soap/candidate/` or `soap/user/`
2. If missing, regenerate from WSDL using `wsimport` (see reference stubs in `src/main/java-reference/`)
3. Add method to `TalentLinkSOAPCandidateService` or `TalentLinkSOAPUserService`
4. Ensure `TalentLinkSOAPHandler` is in the handler chain (already configured)
5. Test credential loading from `RDPS_PARAMETER` table

### Adding a New Scheduled Job
1. Add method to `ScheduleTask` component
2. Annotate with `@Scheduled(cron = "...")` and `@SchedulerLock(name = "...", lockAtMostFor = "...", lockAtLeastFor = "...")`
3. Choose unique lock name to avoid conflicts
4. Test in distributed environment (multiple instances)

### Updating Import Validation Rules
1. Modify `CandidateValidationService.validate(Candidate)`
2. Add validation errors to returned `ValidationResult`
3. Errors automatically logged to `RDPS_IMPORT_VALIDATION_ERROR` via `ImportErrorHandler`
4. UI displays errors at `/import/validation-errors`

### Fixing Database Connection Issues
**Common error: "Incorrect result size: expected 1, actual 2"**
- Cause: Duplicate parameters in `RDPS_PARAMETER` table
- Check: `SELECT PARAM_CODE, COUNT(*) FROM RDPS_PARAMETER GROUP BY PARAM_CODE HAVING COUNT(*) > 1`
- Fix: Delete older duplicates, keeping most recent `TIMESTAMP`

**Common error: "UnsupportedClassVersionError: class file version 61.0"**
- Cause: Running with Java 8 instead of Java 21
- Fix: `java -version` should show "21.x.x" - install Java 21 if not

### Context Path Issues in Templates
- Application runs with context path `/` (root) when using `mvn spring-boot:run`
- Use Thymeleaf URL expressions: `th:href="@{/admin/user-sync}"`
- Avoid hardcoded paths like `/RDPS/admin/...` in JavaScript fetch calls
- Correct: `fetch('[[@{/admin/user-sync/trigger}]]', {...})`

## Security Considerations

⚠️ **Known Issues:**
1. SOAP passwords transmitted in plaintext (requires HTTPS)
2. Static credential storage in `TalentLinkSOAPHandler` (not thread-safe)
3. Database passwords visible in `application-local.properties`
4. No credential rotation mechanism
5. Credentials loaded at startup only (requires restart to update)

**When modifying SOAP credentials:**
1. Update in `RDPS_PARAMETER` table
2. Restart application (credentials cached at `@PostConstruct`)
3. Verify in logs: `grep "SOAP credentials configured" catalina.base_IS_UNDEFINED/logs/server.log_RDPS.log`

## Frontend Notes

**Template Engine:** Thymeleaf 3.1.3 with layout dialect
- Master layout: `templates/fragments/template.html`
- Navigation: `templates/fragments/nav.html` (role-based menu via `sec:authorize`)

**Server-Side Pagination:** DataTables with `DatatableServerSideRequestDto` / `DatatableServerSideResponseDto`

**AJAX Endpoints:**
- Use `@ResponseBody` annotation in controllers
- Return JSON via Jackson (auto-configured)
- CSRF token required for POST requests (Thymeleaf provides via meta tags)

## Troubleshooting

**Application won't start:**
```bash
# Check Java version
java -version  # Must be 21+

# Check Oracle container
docker ps | grep oracle-db-free
docker logs oracle-db-free

# Verify database connection
docker exec oracle-db-free bash -c "echo 'SELECT 1 FROM DUAL;' | sqlplus -s RDPS/rdps_password123@FREEPDB1"
```

**Import fails silently:**
```bash
# Check validation errors
docker exec oracle-db-free bash -c "echo 'SELECT COUNT(*) FROM RDPS_IMPORT_VALIDATION_ERROR;' | sqlplus -s RDPS/rdps_password123@FREEPDB1"

# Check import history
docker exec oracle-db-free bash -c "echo 'SELECT * FROM RDPS_IMPORT_HISTORY ORDER BY IMPORT_START_TIME DESC FETCH FIRST 5 ROWS ONLY;' | sqlplus -s RDPS/rdps_password123@FREEPDB1"

# View application logs
tail -100 catalina.base_IS_UNDEFINED/logs/server.log_RDPS.log
```

**SOAP service returns 403 Forbidden:**
- Check credentials in `RDPS_PARAMETER` table
- Verify `TalentLinkSOAPHandler.setCredentials()` was called
- Restart application to reload credentials
- Check endpoint URL includes API key parameter

**User sync not running:**
- Check `RDPS_SHEDLOCK` table for stuck locks
- Verify cron expression: `0 0 * * * ?` (runs hourly)
- Check `RDPS_TALENTLINK_USER_STAGING` has `syncedToTalentlink = 'N'` records
- Review scheduler logs for exceptions
