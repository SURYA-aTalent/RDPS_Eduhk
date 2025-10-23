# RDPS Technical Documentation

## Project Overview

**RDPS (Recruitment Data Processing System)** is a Spring Boot web application designed for The Education University of Hong Kong to manage and process recruitment candidate data from the TalentLink recruitment platform.

**Version:** 0.0.1-SNAPSHOT
**Java Version:** 21
**Framework:** Spring Boot 4.0.0-SNAPSHOT
**Database:** Oracle Database 23c Free (Development) / Oracle Database 11g+ (Production)
**Deployment:** WAR (Tomcat)

> **Note:** The development environment uses Oracle Database 23c Free in a Docker container. For production deployments, you can use any Oracle Database instance (11g or higher) by updating the database URL in `application.properties` or JNDI configuration - no Docker container required.

---

## Technology Stack

### Backend
- **Spring Boot** - Application framework
- **Spring Security** - Authentication and authorization
- **Spring JDBC** - Database operations
- **MyBatis** - SQL mapping framework
- **JAX-WS (Jakarta XML Web Services)** - SOAP web service client

### Frontend
- **Thymeleaf** - Server-side template engine
- **DataTables** - Interactive data tables
- **Bootstrap** - UI framework

### Integration
- **TalentLink SOAP API** - Candidate and user data synchronization
- **Apache POI** - Excel file processing
- **ShedLock** - Distributed task scheduling

### Database
- **Oracle Database 23c Free** - Primary data store
- **HikariCP** - Connection pooling

---

## Architecture

### Layered Architecture

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│   (Controllers, Templates, Static)      │
└─────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────┐
│          Service Layer                  │
│   (Business Logic, Orchestration)       │
└─────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────┐
│      Data Access Layer                  │
│        (DAOs, Mappers)                  │
└─────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────┐
│         Oracle Database                 │
│     (Tables, Sequences, Functions)      │
└─────────────────────────────────────────┘
```

---

## Package Structure

### Core Packages

#### `eduhk.fhr.web.config`
**Purpose:** Application configuration and security setup

| Class | Responsibility |
|-------|---------------|
| `fhrApplication` | Spring Boot main application entry point |
| `WebSecurityConfig` | Security configuration (authentication, authorization) |
| `HeaderAuthenticationFilter` | SSO authentication via HTTP headers |
| `SsoLogoutSuccessHandler` | Custom logout handler for SSO integration |
| `RestTemplateConfig` | HTTP client configuration for API calls |
| `Parameters` | Application parameter constants |

#### `eduhk.fhr.web.controller`
**Purpose:** HTTP request handling and view rendering

| Controller | Endpoints |
|-----------|-----------|
| `ImportController` | `/import/*` - TalentLink data import operations |
| `MainController` | `/` - Dashboard and main application pages |
| `AdminUserSyncController` | `/admin/userSync` - User synchronization management |
| `ReportController` | `/report/*` - Reporting functionality |
| `LogController` | `/logs/*` - System log viewing |
| `ErrorController` | `/error` - Error handling pages |

#### `eduhk.fhr.web.service`
**Purpose:** Business logic implementation

**Core Services:**
- `TalentLinkSOAPCandidateService` - SOAP integration for candidate data
- `TalentLinkSOAPUserService` - SOAP integration for user data
- `TalentLinkUserSyncService` - User synchronization orchestration
- `ValidationService` - Pre-import data validation
- `SearchService` - Candidate search functionality
- `EmailService` - Email notification management
- `SchedulerService` - Scheduled task management
- `ParameterService` - System parameter configuration
- `UserProfileService` - User account management

#### `eduhk.fhr.web.service.import_`
**Purpose:** Import orchestration and processing

| Service | Responsibility |
|---------|---------------|
| `ImportOrchestrationService` | Coordinates multi-step import process |
| `CandidateImportService` | Candidate data import logic |
| `ValidationReportService` | Validation error reporting |

#### `eduhk.fhr.web.dao`
**Purpose:** Database access layer

**Key DAOs:**
- `CandidateDAO` - Candidate CRUD operations
- `ImportHistoryDAO` - Import tracking and history
- `ImportValidationErrorDAO` - Validation error persistence
- `UserProfileDao` - User account data access
- `EducationDAO` - Education qualification records
- `WorkExperienceDAO` - Work experience records
- `RefereeDAO` - Reference information
- `EmailDao` - Email queue management
- `ParameterDao` - System configuration

#### `eduhk.fhr.web.model`
**Purpose:** Domain entities and data models

**Main Entities:**
- `Candidate` - Recruitment candidate information
- `Education` - Educational qualifications (RDPS_EDU_PROF_QUAL)
- `WorkExperience` - Employment history
- `Referee` - Professional references
- `OtherInfo` - Additional candidate details
- `ImportHistory` - Import operation records
- `ImportValidationError` - Validation failure tracking
- `UserProfile` - User account data
- `EmailTemplate` - Email notification templates
- `SystemLog` - Application logging

#### `eduhk.fhr.web.dto`
**Purpose:** Data Transfer Objects for API and view layer

- `ApplicationDto` - Application-level data structures
- `DatatableServerSideRequestDto` - Server-side pagination
- `EmailTemplateDto` - Email template data
- `SaveDataDto` - Form submission data
- `AjaxResponse` - AJAX response wrapper

#### `eduhk.fhr.web.soap`
**Purpose:** SOAP web service integration

**Subpackages:**
- `soap.candidate` - Auto-generated SOAP stubs for candidate service
- `soap.user` - Auto-generated SOAP stubs for user service
- `soap.handler` - SOAP message interceptors and authentication

#### `eduhk.fhr.web.api.talentlink`
**Purpose:** TalentLink API integration (GraphQL/REST)

- API client implementations
- Request/response mappers
- Error handling

#### `eduhk.fhr.web.scheduler`
**Purpose:** Scheduled background tasks

| Class | Schedule | Task |
|-------|----------|------|
| `ScheduleTask` | Configurable | Automated candidate import from TalentLink |

#### `eduhk.fhr.web.util`
**Purpose:** Utility classes and helpers

- `Common` - Common utility methods
- `CreateValidationTable` - Dynamic validation table generation
- `CreateEmailFunction` - Email function utilities
- `reporting.ReportUtil` - Report generation helpers

#### `eduhk.fhr.web.mapper`
**Purpose:** MyBatis XML mappers (database operations)

- `CandidateMapper.xml` - Candidate SQL queries
- `EducationMapper.xml` - Education SQL queries
- `WorkExperienceMapper.xml` - Work experience queries
- `RefereeMapper.xml` - Reference queries
- `OtherInfoMapper.xml` - Other info queries

#### `eduhk.fhr.web.exception`
**Purpose:** Custom application exceptions

- `NotFoundException` - Resource not found
- `WarningException` - Non-critical warnings
- `DangerException` - Critical errors
- `PaymentNotIdentifiedException` - Payment validation errors

---

## Database Schema

### Table Categories

**User Management**
- `RDPS_USER_PROFILE` - User accounts (2 records)
- `RDPS_USER_ROLE` - User roles and permissions

**Candidate Data**
- `RDPS_CANDIDATE` - Main candidate records
- `RDPS_EDU_PROF_QUAL` - Educational qualifications
- `RDPS_WORK_EXPERIENCE` - Employment history
- `RDPS_REFEREE` - Professional references
- `RDPS_OTHER_INFO` - Additional information

**Import System**
- `RDPS_IMPORT_HISTORY` - Import operation tracking
- `RDPS_IMPORT_VALIDATION_ERROR` - Validation error records
- `RDPS_TALENTLINK_USER_STAGING` - User sync staging table

**Lookup Tables (LOV)**
- `RDPS_LOV_DISTRICT` - Hong Kong districts (19 records)
- `RDPS_LOV_EDU_LEVEL` - Education levels (14 records)
- `RDPS_LOV_QUAL_AWARD_CLASS` - Qualification classes (16 records)
- `RDPS_LOV_QUAL_AWARD_DESC` - Qualification descriptions (35 records)
- `RDPS_LOV_STUDY_MODE` - Study modes (7 records)

**System Tables**
- `RDPS_PARAMETER` - System configuration (11 records)
- `RDPS_EMAIL_TEMPLATE` - Email templates
- `RDPS_EMAIL_JOB` - Email queue
- `RDPS_SYSTEM_LOG` - Application logs
- `RDPS_SHEDLOCK` - Distributed lock for scheduled tasks

**Total:** 20 tables + 1 PL/SQL function (`EXECUTE_EMAIL_JOB`)

---

## Key Workflows

### 1. Candidate Import from TalentLink

```
User triggers import
      ↓
Import Orchestration Service
      ↓
Fetch candidates via SOAP API
      ↓
Validation Service (pre-import checks)
      ↓
Store validation errors (if any)
      ↓
Import valid candidates to database
      ↓
Record import history
      ↓
Display results to user
```

**Key Classes:**
- `ImportController` - UI and request handling
- `ImportOrchestrationService` - Process coordination
- `TalentLinkSOAPCandidateService` - SOAP API calls
- `ValidationService` - Data validation
- `CandidateDAO` - Database persistence

### 2. User Synchronization

```
Admin initiates user sync
      ↓
TalentLink User Sync Service
      ↓
Fetch users via SOAP API
      ↓
Stage users in RDPS_TALENTLINK_USER_STAGING
      ↓
Validate and transform data
      ↓
Create/update user profiles
      ↓
Record synchronization result
```

**Key Classes:**
- `AdminUserSyncController` - Admin interface
- `TalentLinkUserSyncService` - Sync orchestration
- `TalentLinkSOAPUserService` - SOAP integration
- `UserProfileDao` - User data persistence

### 3. Email Notifications

```
System event triggers email
      ↓
Email Service creates email job
      ↓
Email job stored in RDPS_EMAIL_JOB
      ↓
Scheduled task processes queue
      ↓
EXECUTE_EMAIL_JOB function sends email
      ↓
Update job status (SENT/FAILED)
```

**Key Classes:**
- `EmailService` - Email creation
- `ScheduleTask` - Email queue processing
- Database function: `EXECUTE_EMAIL_JOB`

---

## Security and Authentication

### Authentication Architecture

RDPS implements a **hybrid SSO + local development authentication** system using Spring Security's pre-authentication mechanism.

#### Authentication Flow

```
┌──────────────────────────────────────────────────────┐
│  1. HTTP Request arrives                             │
└──────────────────────────────────────────────────────┘
                       ↓
┌──────────────────────────────────────────────────────┐
│  2. HeaderAuthenticationFilter intercepts            │
│     Reads HTTP header: "OAM_REMOTE_USER"             │
└──────────────────────────────────────────────────────┘
                       ↓
              ┌────────┴────────┐
              │                 │
┌─────────────▼─────┐  ┌───────▼──────────┐
│  Local Request    │  │ External Request │
│  (127.0.0.1)      │  │ (Production SSO) │
└─────────────┬─────┘  └───────┬──────────┘
              │                 │
    Uses local.username    Uses header or
    fhr-dev-uacct01        external.guestUserName
              │                 │
              └────────┬────────┘
                       ↓
┌──────────────────────────────────────────────────────┐
│  3. UserDetailsServiceImpl loads user                │
│     - Query SSO database (nds_user_info)             │
│     - Fallback: Local development user               │
└──────────────────────────────────────────────────────┘
                       ↓
┌──────────────────────────────────────────────────────┐
│  4. Load roles from RDPS_USER_PROFILE table          │
│     - Check CN (username) exists                     │
│     - Load permissions from RDPS_USER_ROLE           │
└──────────────────────────────────────────────────────┘
                       ↓
┌──────────────────────────────────────────────────────┐
│  5. Spring Security creates authenticated session    │
└──────────────────────────────────────────────────────┘
```

### Authentication Components

#### 1. HeaderAuthenticationFilter
**Location:** `eduhk.fhr.web.config.HeaderAuthenticationFilter`

**Purpose:** Extracts username from HTTP header for SSO authentication

**Configuration:**
- **SSO Header:** `OAM_REMOTE_USER` (Oracle Access Manager)
- **Local Username:** `fhr-dev-uacct01` (from `parameters.properties`)
- **Local IP:** `0:0:0:0:0:0:0:1` (IPv6 localhost)
- **Guest Username:** `RDPS_GUEST` (fallback for external users)

#### 2. UserDetailsServiceImpl
**Location:** `eduhk.fhr.web.config.UserDetailsServiceImpl`

**Purpose:** Spring Security integration point for loading user details

#### 3. UserDao (Multi-Tier User Lookup)
**Location:** `eduhk.fhr.web.dao.UserDao`

**Purpose:** Performs multi-tier user lookup from SSO and RDPS databases

> **⚠️ Important: External SSO Database Dependency**
>
> The `nds_user_info` table and related SSO tables (`staff`, `person`, `active_employment`) are **NOT part of the RDPS codebase**. They reside in **EdUHK's central SSO Oracle database**, which is managed by the university's IT infrastructure.
>
> **Production Environment:**
> - SSO database accessed via JNDI: `java:comp/env/jdbc/datasources/rdpsDS`
> - Configured in Tomcat's `context.xml`
> - Requires network connectivity to SSO database server
> - RDPS should have **read-only** access for security
>
> **Development Environment:**
> - JNDI lookup fails gracefully
> - Falls back to H2 in-memory database (RDPS tables only)
> - SSO queries catch exceptions and create mock users
> - No SSO database connection required for local development
>
> **Configuration Location:** `eduhk.fhr.web.config.AppConfig`
> - `ssoDataSource()` - Bean for SSO database connection
> - `ssoJdbcTemplate()` - JDBC template for SSO queries

**Tier 1: Guest User Check**
- Username: `RDPS_GUEST`
- Authority: `ROLE_GUEST`

**Tier 2: SSO Database Lookup (External)**
```sql
-- Query external EdUHK SSO database (via ssoJdbcTemplate)
SELECT nds.cn, nds.institute_id,
       UPPER(p.surname) || ' ' || INITCAP(p.othername) AS name,
       ae.department_code AS department
FROM nds_user_info nds                          -- SSO: User directory
LEFT OUTER JOIN staff sf ON sf.staff_number = nds.institute_id
LEFT OUTER JOIN person p ON p.pid = sf.pid     -- SSO: Personal information
LEFT OUTER JOIN active_employment ae ON ae.staff_number = nds.institute_id
WHERE UPPER(nds.cn) = UPPER(?)
  AND nds.institute_id LIKE '8%'               -- Staff only (8xxxx IDs)
ORDER BY ae.department_code NULLS LAST
```

**SSO Database Tables (External - EdUHK Central Database):**

| Table | Schema | Purpose |
|-------|--------|---------|
| `nds_user_info` | SSO DB | User directory with CN and institute_id |
| `staff` | SSO DB | Staff employment records |
| `person` | SSO DB | Personal information (names) |
| `active_employment` | SSO DB | Current employment and department |

**Lookup Results:**
- **Success:** User populated with name, department, institute_id + `ROLE_STAFF` authority
- **Failure (Dev):** Creates mock user with username, "Local Development User" name, "DEV001" institute ID, and `ROLE_STAFF` authority

**Tier 3: RDPS Permission Lookup**
```sql
-- Query RDPS database for application-specific permissions
SELECT up.role, ur.fcn
FROM RDPS_USER_PROFILE up
LEFT OUTER JOIN RDPS_USER_ROLE ur
  ON up.role = ur.role AND ur.active = 'Y'
WHERE UPPER(up.cn) = UPPER(?)
  AND up.active = 'Y'
  AND COALESCE(up.effective_date_from, CURRENT_DATE) <= CURRENT_DATE
  AND COALESCE(up.effective_date_to, CURRENT_DATE) >= CURRENT_DATE
```
- Returns function-level permissions (e.g., `USE_RDPS`, `ACCESS_RIGHT_SETUP`)

**Combined Authorities Example:**
```
User: fhr-dev-uacct01
Authorities:
  - ROLE_STAFF (from SSO lookup)
  - USE_RDPS (from RDPS_USER_ROLE)
  - ACCESS_RIGHT_SETUP (from RDPS_USER_ROLE)
```

#### 4. WebSecurityConfig
**Location:** `eduhk.fhr.web.config.WebSecurityConfig`

**Purpose:** Configures Spring Security with pre-authentication using HeaderAuthenticationFilter and PreAuthenticatedAuthenticationProvider

### Authorization

**Role-Based Access Control:**
- Uses Spring Security's `hasRole()` method
- Roles loaded from `RDPS_USER_ROLE.FCN` column
- Supports time-bound permissions via `effective_date_from` and `effective_date_to`

**Access Control Matrix:**

| Endpoint Pattern | Required Role |
|-----------------|---------------|
| `/makeReservation` | `USE_RDPS` |
| `/viewReservation` | `USE_RDPS` |
| `/downloadLogs` | `ACCESS_RIGHT_SETUP` |
| `/setUserProfile` | `ACCESS_RIGHT_SETUP` |
| `/log/**` | `USE_RDPS` |
| `/main/**` | `USE_RDPS` |
| `/report/**` | `USE_RDPS` |
| `/accessDenied` | Public |
| `/logout` | Public |

### Session Management

#### Logout Handler
**Location:** `eduhk.fhr.web.config.SsoLogoutSuccessHandler`

**Purpose:** Redirects to SSO logout URL after invalidating session

**Logout Flow:**
1. User accesses `/logout`
2. Spring Security invalidates HTTP session
3. Deletes `JSESSIONID` cookie
4. Redirects to SSO logout URL (from `RDPS_PARAMETER.SSO_LOGOUT_URL`)
5. Default: `https://dapplt.eduhk.hk/logout.jsp`

### Authentication Modes

#### Production Mode (SSO)
- **Trigger:** Non-localhost requests
- **Header:** `OAM_REMOTE_USER` populated by Oracle Access Manager
- **User Source:** External SSO database (`nds_user_info` table)
- **Permissions:** `RDPS_USER_PROFILE` + `RDPS_USER_ROLE`
- **Logout:** SSO logout page

#### Local Development Mode
- **Trigger:** Requests from `0:0:0:0:0:0:0:1` (localhost IPv6)
- **Username:** `fhr-dev-uacct01` (from `parameters.properties`)
- **User Source:** Mock user if SSO database unavailable
- **Permissions:** `RDPS_USER_PROFILE` + `RDPS_USER_ROLE`
- **Logout:** SSO logout page (configurable)

#### Guest Mode
- **Trigger:** External request with no SSO header
- **Username:** `RDPS_GUEST`
- **Role:** `ROLE_GUEST`
- **Access:** Limited (define in `WebSecurityConfig`)

### Database Schema for Authentication

**RDPS_USER_PROFILE:**
```sql
CREATE TABLE RDPS_USER_PROFILE (
    CN VARCHAR2(100),           -- Username (common name)
    ROLE VARCHAR2(50),          -- Role name (e.g., SYSTEM_ADMIN)
    ACTIVE CHAR(1),             -- Y/N
    EFFECTIVE_DATE_FROM DATE,   -- Permission start date
    EFFECTIVE_DATE_TO DATE,     -- Permission end date
    PRIMARY KEY (CN, ROLE)
);
```

**RDPS_USER_ROLE:**
```sql
CREATE TABLE RDPS_USER_ROLE (
    ROLE VARCHAR2(50),          -- Role name
    FCN VARCHAR2(100),          -- Function/permission name
    ACTIVE CHAR(1),             -- Y/N
    PRIMARY KEY (ROLE, FCN)
);
```

**Current Users:**
```
CN: fhr-dev-uacct01
Roles:
  - SYSTEM_ADMIN (with all associated permissions)
  - HRO_ADMIN (with all associated permissions)
Status: Active
```

### Security Features

- **No password storage** - Uses SSO authentication
- **Pre-authentication** - Username extracted from trusted header
- **Multi-tier fallback** - SSO → Local dev → Guest
- **Role-based access** - Fine-grained permission control
- **Time-bound permissions** - Automatic expiration support
- **Session management** - Automatic timeout and cleanup
- **Guest access** - Limited access for external users
- **Development mode** - Works without SSO infrastructure

---

## Configuration

### Database Configuration

**Dual DataSource Architecture:**

RDPS uses **two separate database connections**:

1. **Primary DataSource (`dataSource`)** - RDPS Application Database
   - Contains: Candidate data, import history, system parameters
   - Tables: `RDPS_CANDIDATE`, `RDPS_USER_PROFILE`, `RDPS_PARAMETER`, etc.
   - Configuration: `spring.datasource.*` properties

2. **SSO DataSource (`ssoDataSource`)** - EdUHK Central SSO Database
   - Contains: University-wide user directory
   - Tables: `nds_user_info`, `staff`, `person`, `active_employment`
   - Configuration: JNDI lookup in production, H2 in development

**Configuration Class:** `eduhk.fhr.web.config.AppConfig`

**Beans:**
- `dataSource()` - RDPS application database (JNDI lookup in production, application.properties in dev)
- `ssoDataSource()` - SSO database (JNDI lookup in production, H2 fallback in dev)

### Application Properties (Development)

**Location:** `src/main/resources/application.properties`

```properties
# RDPS Application Database
spring.datasource.url=jdbc:oracle:thin:@//localhost:1521/FREEPDB1
spring.datasource.username=RDPS
spring.datasource.password=rdps_password123
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# Connection Pool
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5

# Server
server.port=8080
server.servlet.context-path=/RDPS
```

> **Using an Actual Oracle Database Instance:**
>
> The Docker container setup (`localhost:1521/FREEPDB1`) is for local development only. To connect to an actual Oracle Database instance (11g, 12c, 19c, 21c, or higher), simply update the `spring.datasource.url` to point to your database server:
>
> **For Oracle Service Name:**
> ```properties
> spring.datasource.url=jdbc:oracle:thin:@//your-db-host:1521/YOUR_SERVICE_NAME
> ```
>
> **For Oracle SID:**
> ```properties
> spring.datasource.url=jdbc:oracle:thin:@your-db-host:1521:YOUR_SID
> ```
>
> No Docker container is required for production or when connecting to an existing Oracle database instance. The application works with any Oracle Database 11g or higher.

### Production JNDI Configuration

**Location:** Tomcat `context.xml` or `server.xml`

```xml
<Context>
    <!-- RDPS Application Database -->
    <Resource
        name="jdbc/datasources/rdpsDS"
        auth="Container"
        type="javax.sql.DataSource"
        driverClassName="oracle.jdbc.OracleDriver"
        url="jdbc:oracle:thin:@rdps-db-host:1521/RDPS_PROD"
        username="RDPS"
        password="**********"
        maxTotal="20"
        maxIdle="10"
        maxWaitMillis="10000"/>

    <!-- EdUHK SSO Database (Read-Only Access) -->
    <Resource
        name="jdbc/datasources/ssoDS"
        auth="Container"
        type="javax.sql.DataSource"
        driverClassName="oracle.jdbc.OracleDriver"
        url="jdbc:oracle:thin:@sso-db-host:1521/SSO_PROD"
        username="rdps_sso_readonly"
        password="**********"
        maxTotal="10"
        maxIdle="5"
        maxWaitMillis="10000"
        validationQuery="SELECT 1 FROM DUAL"/>
</Context>
```

**Production Configuration Requirements:**
- ✓ SSO database credentials must be **read-only** for security
- ✓ Both databases accessible from application server network
- ✓ Use secure credential management (vault/encrypted config)
- ✓ Configure firewall rules for database connectivity
- ✓ Verify JNDI names match `AppConfig` bean definitions

### System Parameters (RDPS_PARAMETER table)

**Runtime Configuration:**

| Parameter | Purpose | Example Value |
|-----------|---------|---------------|
| `TALENTLINK_API_URL` | TalentLink API base URL | `https://api3.lumesse-talenthub.com` |
| `TALENTLINK_API_KEY` | API authentication key | `10047a13-72fb-ad0a-...` |
| `TALENTLINK_USERNAME` | SOAP service username | `EdUHK UAT:user@domain:BO` |
| `TALENTLINK_PASSWORD` | SOAP service password | `********` |
| `TALENTLINK_CANDIDATE_SOAP_URL` | Candidate SOAP endpoint | `.../HRIS/SOAP/Candidate` |
| `TALENTLINK_USER_SOAP_URL` | User SOAP endpoint | `.../User/SOAP/User` |
| `ENVIRONMENT` | Deployment environment | `DEV`/`UAT`/`PROD` |
| `IMPORT_BATCH_SIZE` | Records per import batch | `100` |
| `SSO_LOGOUT_URL` | SSO logout redirect URL | `https://dapplt.eduhk.hk/logout.jsp` |
| `LAST_IMPORTED_CANDIDATE_ID` | Resume point for incremental imports | `12345` |
| `LAST_IMPORT_TIMESTAMP` | Timestamp of last successful import | `2025-01-01 00:00:00` |

**Query Parameters:**
```sql
SELECT PARAM_CODE, PARAM_VALUE, ACTIVE
FROM RDPS_PARAMETER
WHERE ACTIVE = 'Y'
ORDER BY PARAM_CODE;
```

### Local Development Configuration

**Location:** `src/main/resources/parameters.properties`

```properties
# Local Development User (bypasses SSO)
local.username=fhr-dev-uacct01
local.ip=0:0:0:0:0:0:0:1

# Guest User Fallback
external.guestUserName=RDPS_GUEST
external.guestName=Guest

# Database Schema Prefix
database.schema.prefix=rdps_

# System Information
system.name=Recruitment Data Processing System
system.version=v1.0.0
```

**Development Mode Behavior:**
- ✓ No SSO database connection required
- ✓ Mock user created automatically on authentication
- ✓ H2 in-memory database fallback for RDPS tables
- ✓ Localhost IP detection (`0:0:0:0:0:0:0:1` → auto-login)
- ✓ Default user: `fhr-dev-uacct01` with `ROLE_STAFF`

---

## API Integration

### TalentLink SOAP Services

**Candidate Service:**
- Endpoint: Configured in `TALENTLINK_CANDIDATE_SOAP_URL`
- Operations: `getCandidates`, `getCandidateDetails`
- Authentication: WS-Security with username/password

**User Service:**
- Endpoint: Configured in `TALENTLINK_USER_SOAP_URL`
- Operations: `getUsers`, `createUser`, `updateUser`
- Authentication: WS-Security with username/password

**Implementation:**
- SOAP stubs: `eduhk.fhr.web.soap.candidate`, `eduhk.fhr.web.soap.user`
- Handler: `TalentLinkSOAPHandler` (adds authentication headers)
- Services: `TalentLinkSOAPCandidateService`, `TalentLinkSOAPUserService`

---

## Deployment

### Build Process

```bash
# Clean and compile
mvn clean package -DskipTests

# Output: target/RDPS-0.0.1-SNAPSHOT.war
```

### Deployment Configuration

**Development:**
```bash
mvn spring-boot:run
```

**Production (Tomcat):**
1. Deploy WAR to Tomcat webapps directory
2. Configure database connections in `application.properties`
3. Set environment-specific parameters in `RDPS_PARAMETER` table
4. Configure SSO header integration
5. Start Tomcat

---

## Logging and Monitoring

### Application Logs

**Location:** `logs/rdps.log`

**Log Levels:**
- `ERROR` - Critical errors requiring immediate attention
- `WARN` - Warnings and recoverable errors
- `INFO` - General information and important events
- `DEBUG` - Detailed debugging information

### System Log Database

**Table:** `RDPS_SYSTEM_LOG`

**Stores:**
- User actions and access logs
- Import operations and results
- System events and state changes
- Errors and exceptions

**Query Example:**
```sql
SELECT LOG_ID, LOG_DATE, USER_ID, ACTION, DESCRIPTION, STATUS
FROM RDPS_SYSTEM_LOG
WHERE LOG_DATE > SYSDATE - 1
ORDER BY LOG_DATE DESC;
```

---

## Performance Architecture

### Database Optimization
- **Connection Pooling:** HikariCP (max 10 connections, min 5 idle)
- **Indexing Strategy:** All primary keys, foreign keys, and search columns indexed
- **Batch Processing:** Configurable batch size via `IMPORT_BATCH_SIZE` parameter (default: 100)
- **Query Optimization:** MyBatis XML mappers for complex queries

### Caching Strategy
- **System Parameters:** Loaded at application startup, refreshed on update
- **LOV Data:** Cached in memory for dropdown population
- **User Sessions:** Spring Security session management
- **Database Connections:** Pooled and reused via HikariCP

### Scheduled Task Management
- **Framework:** Spring `@Scheduled` with ShedLock
- **Concurrency Control:** ShedLock prevents duplicate execution across instances
- **Lock Storage:** `RDPS_SHEDLOCK` table
- **Default Lock Duration:** 10 minutes
- **Scheduler:** Cron-based scheduling for candidate imports (daily at 2:00 AM)
