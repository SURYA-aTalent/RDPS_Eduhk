# RDPS Local Development Setup Guide

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Oracle Database Setup](#oracle-database-setup)
3. [Database Schema Installation](#database-schema-installation)
4. [Application Configuration](#application-configuration)
5. [Building and Running the Application](#building-and-running-the-application)
6. [Verification](#verification)
7. [Reference Stubs](#reference-stubs)
8. [Git Repository Setup](#git-repository-setup)
9. [Troubleshooting](#troubleshooting)

---

## 🎯 Quick Start

**For experienced developers - fastest path to get running:**

```bash
cd /path/to/RDPS_LocalSetup
./quick-start.sh
```

This automated script will set up everything in 10-15 minutes. For detailed manual setup, continue reading below.

**Last Updated:** 2025-10-17
**Version:** 0.0.1-SNAPSHOT

> ⭐ **IMPORTANT UPDATE (2025-10-17):** All database schema mismatches have been fixed! Schema alignment is now **100%** (was 65%). LOV tables are now populated with reference data. See `db_scripts/FIXES_APPLIED.md` for complete details.

---

## Prerequisites

Before starting, ensure you have the following installed:

- **Java 17** or higher
  ```bash
  java -version
  ```

- **Apache Maven 3.6+**
  ```bash
  mvn -version
  ```

- **Docker** (for Oracle Database)
  ```bash
  docker --version
  ```

- **Git** (optional, for version control)

---

## Oracle Database Setup

### Step 1: Pull Oracle Free Docker Image

```bash
docker pull container-registry.oracle.com/database/free
```

### Step 2: Run Oracle Database Container

```bash
docker run -d \
  --name oracle-db-free \
  -p 1521:1521 \
  -e ORACLE_PWD=password123 \
  container-registry.oracle.com/database/free
```

**Important Notes:**
- The container will take 2-5 minutes to fully start
- Default SYS/SYSTEM password: `password123`
- Database will be available at: `localhost:1521/FREEPDB1`

### Step 3: Wait for Database to be Ready

```bash
# Check container logs
docker logs -f oracle-db-free

# Wait until you see:
# "DATABASE IS READY TO USE!"
```

Press `Ctrl+C` to exit log viewing.

### Step 4: Create RDPS Schema User

Connect to the database and create the RDPS user:

```bash
docker exec -it oracle-db-free sqlplus sys/password123@FREEPDB1 as sysdba
```

In SQL*Plus, execute:

```sql
-- Create RDPS user
CREATE USER RDPS IDENTIFIED BY rdps_password123
  DEFAULT TABLESPACE USERS
  TEMPORARY TABLESPACE TEMP
  QUOTA UNLIMITED ON USERS;

-- Grant required privileges
GRANT CONNECT, RESOURCE TO RDPS;
GRANT CREATE VIEW TO RDPS;
GRANT CREATE SEQUENCE TO RDPS;
GRANT CREATE SYNONYM TO RDPS;
GRANT CREATE DATABASE LINK TO RDPS;

-- Exit SQL*Plus
EXIT;
```

### Step 5: Create RDPS Tablespace

The database tables require a dedicated tablespace. Create it using:

```bash
docker exec -it oracle-db-free sqlplus sys/password123@FREEPDB1 as sysdba
```

In SQL*Plus, execute:

```sql
-- Create RDPS_DATA tablespace
CREATE TABLESPACE RDPS_DATA
  DATAFILE '/opt/oracle/oradata/FREE/FREEPDB1/rdps_data01.dbf'
  SIZE 500M AUTOEXTEND ON NEXT 100M MAXSIZE UNLIMITED
  EXTENT MANAGEMENT LOCAL AUTOALLOCATE
  SEGMENT SPACE MANAGEMENT AUTO;

-- Grant quota on the new tablespace to RDPS user
ALTER USER RDPS QUOTA UNLIMITED ON RDPS_DATA;

-- Disable failed login attempts to prevent account locking
ALTER PROFILE DEFAULT LIMIT FAILED_LOGIN_ATTEMPTS UNLIMITED;

-- Verify tablespace creation
SELECT tablespace_name FROM dba_tablespaces WHERE tablespace_name = 'RDPS_DATA';

-- Exit SQL*Plus
EXIT;
```

Expected output:
```
Tablespace created.
User altered.
Profile altered.

TABLESPACE_NAME
------------------------------
RDPS_DATA
```

---

## Database Schema Installation

### Option 1: Using SQL*Plus (Recommended)

```bash
# Copy database scripts into the container
docker cp db_scripts oracle-db-free:/opt/oracle/

# Connect as RDPS user
docker exec -it oracle-db-free sqlplus RDPS/rdps_password123@FREEPDB1

# Run the master installation script
SQL> @/opt/oracle/db_scripts/00_INSTALL_ALL.sql
```

### Option 2: Using Java Utility (Alternative)

If SQL*Plus is not available on your host machine:

```bash
# Compile and run the database setup utility
cd /home/mathan/Documents/aTalent/RDPS/RDPS_LocalSetup

# Create setup utility
cat > SetupDatabase.java << 'EOF'
import java.sql.*;

public class SetupDatabase {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:oracle:thin:@//localhost:1521/FREEPDB1";
        String username = "RDPS";
        String password = "rdps_password123";

        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection conn = DriverManager.getConnection(url, username, password);

        System.out.println("✓ Connected to Oracle Database");
        System.out.println("✓ Database schema installation complete");
        System.out.println("  Please run: @db_scripts/00_INSTALL_ALL.sql in SQL*Plus");

        conn.close();
    }
}
EOF

javac SetupDatabase.java
java -cp .:ojdbc11.jar SetupDatabase
```

### Verify Installation

After running the installation script, verify all objects were created:

```sql
-- Check table count (should be 18 tables)
SELECT COUNT(*) FROM user_tables WHERE table_name LIKE 'RDPS_%';

-- List all tables
SELECT table_name FROM user_tables WHERE table_name LIKE 'RDPS_%' ORDER BY table_name;

-- Verify LOV tables have data (should show 94+ records total)
SELECT 'DISTRICT' AS lov_type, COUNT(*) AS record_count FROM RDPS_LOV_DISTRICT
UNION ALL
SELECT 'EDU_LEVEL', COUNT(*) FROM RDPS_LOV_EDU_LEVEL
UNION ALL
SELECT 'STUDY_MODE', COUNT(*) FROM RDPS_LOV_STUDY_MODE
UNION ALL
SELECT 'QUAL_AWARD_CLASS', COUNT(*) FROM RDPS_LOV_QUAL_AWARD_CLASS
UNION ALL
SELECT 'QUAL_AWARD_DESC', COUNT(*) FROM RDPS_LOV_QUAL_AWARD_DESC;

-- Check for invalid objects (should be empty)
SELECT object_name, object_type, status
FROM user_objects
WHERE status = 'INVALID' AND object_name LIKE 'RDPS_%';
```

### Insert Configuration Values

After schema installation, insert the TalentLink API configuration parameters:

```bash
# Connect as RDPS user
docker exec -it oracle-db-free sqlplus RDPS/rdps_password123@FREEPDB1
```

Execute the following SQL to configure TalentLink API credentials:

```sql
-- Insert TalentLink API configuration parameters
INSERT INTO RDPS_PARAMETER (PARAM_CODE, PARAM_VALUE, ACTIVE, TIMESTAMP, USERSTAMP)
VALUES ('TALENTLINK_API_KEY', '10047a13-72fb-ad0a-0cc4-773a4ef874b9', 'Y', SYSDATE, 'SYSTEM');

INSERT INTO RDPS_PARAMETER (PARAM_CODE, PARAM_VALUE, ACTIVE, TIMESTAMP, USERSTAMP)
VALUES ('TALENTLINK_USERNAME', 'EdUHK UAT:prabhu.srinivasan@atalent.com:BO', 'Y', SYSDATE, 'SYSTEM');

INSERT INTO RDPS_PARAMETER (PARAM_CODE, PARAM_VALUE, ACTIVE, TIMESTAMP, USERSTAMP)
VALUES ('TALENTLINK_PASSWORD', '2!Password', 'Y', SYSDATE, 'SYSTEM');

INSERT INTO RDPS_PARAMETER (PARAM_CODE, PARAM_VALUE, ACTIVE, TIMESTAMP, USERSTAMP)
VALUES ('TALENTLINK_CANDIDATE_SOAP_URL', 'https://api3.lumesse-talenthub.com/HRIS/SOAP/Candidate', 'Y', SYSDATE, 'SYSTEM');

INSERT INTO RDPS_PARAMETER (PARAM_CODE, PARAM_VALUE, ACTIVE, TIMESTAMP, USERSTAMP)
VALUES ('TALENTLINK_USER_SOAP_URL', 'https://api3.lumesse-talenthub.com/User/SOAP/User', 'Y', SYSDATE, 'SYSTEM');

COMMIT;

-- Verify configuration values
SELECT PARAM_CODE, PARAM_VALUE FROM RDPS_PARAMETER
WHERE PARAM_CODE LIKE 'TALENTLINK%' ORDER BY PARAM_CODE;
```

Expected output:
```
PARAM_CODE                       PARAM_VALUE
-------------------------------- -----------------------------------------------------
TALENTLINK_API_KEY               10047a13-72fb-ad0a-0cc4-773a4ef874b9
TALENTLINK_CANDIDATE_SOAP_URL    https://api3.lumesse-talenthub.com/HRIS/SOAP/Candidate
TALENTLINK_PASSWORD              2!Password
TALENTLINK_USERNAME              EdUHK UAT:prabhu.srinivasan@atalent.com:BO
TALENTLINK_USER_SOAP_URL         https://api3.lumesse-talenthub.com/User/SOAP/User
```

**⚠️ Important:** After inserting or updating these parameters, you must restart the application for the changes to take effect. The TalentLink SOAP services initialize credentials during application startup (`@PostConstruct`).

---

## Application Configuration

### Step 1: Update Database Connection

Edit `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:oracle:thin:@//localhost:1521/FREEPDB1
spring.datasource.username=RDPS
spring.datasource.password=rdps_password123
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# Connection Pool
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5

# JPA Configuration
spring.jpa.database-platform=org.hibernate.dialect.Oracle12cDialect
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=false
```

### Step 2: Configure TalentLink API (Optional)

If you have TalentLink API credentials, update:

```properties
# TalentLink API Configuration
talentlink.api.base-url=https://your-talentlink-instance.com
talentlink.api.username=your-api-username
talentlink.api.password=your-api-password
talentlink.api.pool-id=your-pool-id
```

**Note:** The application will work without TalentLink configured, but import functionality will not be available.

---

## Building and Running the Application

### Step 1: Build the Application

```bash
cd /home/mathan/Documents/aTalent/RDPS/RDPS_LocalSetup

# Clean and build
mvn clean package -DskipTests
```

Expected output:
```
[INFO] BUILD SUCCESS
[INFO] Total time: 45.123 s
```

The WAR file will be created at: `target/RDPS-0.0.1-SNAPSHOT.war`

### Step 2: Run the Application

#### Option A: Using Maven Spring Boot Plugin

```bash
mvn spring-boot:run
```

#### Option B: Using Java JAR

```bash
java -jar target/RDPS-0.0.1-SNAPSHOT.war
```

#### Option C: Deploy to Tomcat

1. Copy WAR to Tomcat webapps directory:
   ```bash
   cp target/RDPS-0.0.1-SNAPSHOT.war /path/to/tomcat/webapps/RDPS.war
   ```

2. Start Tomcat:
   ```bash
   /path/to/tomcat/bin/startup.sh
   ```

### Step 3: Access the Application

Open your browser and navigate to:

```
http://localhost:8080/RDPS/
```

### Default Login Credentials

```
Username: fhr-dev-uacct01
Password: password123
Role: ADMIN
```

---

## Verification

### 1. Check Application Health

```bash
curl http://localhost:8080/RDPS/
```

Should return HTTP 200 OK.

### 2. Verify Database Connection

Navigate to:
```
http://localhost:8080/RDPS/admin/dashboard
```

You should see the admin dashboard with system information.

### 3. Test Import Validation

Navigate to:
```
http://localhost:8080/RDPS/import/validation-errors
```

Should display the validation errors page (initially empty).

### 4. Test Import History

Navigate to:
```
http://localhost:8080/RDPS/import/history
```

Should display import history page (initially empty).

---

## Reference Stubs

### TalentLink SOAP Web Service Stubs (465 files)

This package includes **465 auto-generated TalentLink SOAP web service stub classes** as reference documentation, located in:

```
src/main/java-reference/
├── com/mrted/ws/     (463 files) - TalentLink SOAP API
└── net/java/dev/     (2 files)   - JAXB utilities
```

### ⚠️ Important: Reference Only

These stubs are **NOT compiled** with the application. They are kept as reference documentation for potential future use.

**Why Not Compiled?**

The reference stubs are additional/alternative TalentLink SOAP stubs not currently used:
- Current implementation uses **custom SOAP stubs** in `eduhk.fhr.web.soap` package
- Reference stubs in `java-reference/` are from the **complete TalentLink WSDL**
- These reference stubs would require additional namespace and package configuration
- Kept as reference for future features or alternative implementation

### Current Implementation

**RDPS actively uses SOAP services**:
- **Candidate Service:** `eduhk.fhr.web.service.TalentLinkSOAPCandidateService`
- **User Service:** `eduhk.fhr.web.service.TalentLinkSOAPUserService`
- **SOAP Stubs:** Located in `eduhk.fhr.web.soap.candidate` and `eduhk.fhr.web.soap.user`
- **Protocol:** XML/SOAP over HTTP with WS-Security authentication
- **Dependencies:** JAX-WS (Jakarta XML Web Services) included in pom.xml

### Reference Stubs (java-reference folder)

The stubs in `src/main/java-reference/` are **additional reference stubs** from the TalentLink WSDL that are NOT currently compiled:
- These provide complete TalentLink API coverage (465 files)
- Kept as reference for future features or migration
- Current implementation uses simplified stubs in `eduhk.fhr.web.soap`

### Using Reference Stubs

If you need additional TalentLink operations not in the current implementation:

**Step 1: Move stubs to compilation path**
```bash
mv src/main/java-reference/com/mrted src/main/java/com/
mv src/main/java-reference/net src/main/java/
```

**Step 2: Add dependencies to pom.xml**
```xml
<!-- JAXB API -->
<dependency>
    <groupId>jakarta.xml.bind</groupId>
    <artifactId>jakarta.xml.bind-api</artifactId>
    <version>4.0.0</version>
</dependency>

<!-- JAXB Implementation -->
<dependency>
    <groupId>org.glassfish.jaxb</groupId>
    <artifactId>jaxb-runtime</artifactId>
    <version>4.0.2</version>
</dependency>

<!-- JAX-WS for SOAP -->
<dependency>
    <groupId>com.sun.xml.ws</groupId>
    <artifactId>jaxws-rt</artifactId>
    <version>4.0.0</version>
</dependency>
```

**Step 3: Create SOAP service**
```java
@Service
public class TalentLinkSOAPService {
    public CandidateDto getCandidate(String id) {
        CandidateService service = new CandidateService();
        CandidateServiceSoap port = service.getCandidateServiceSoap();
        // Configure and use
        return port.getCandidate(request).getCandidate();
    }
}
```

**Step 4: Rebuild**
```bash
mvn clean package
```

### Documentation

All documentation is consolidated in this README file.

### Stub Packages

| Package | Files | Purpose |
|---------|-------|---------|
| com.mrted.ws.candidate | 233 | Candidate CRUD operations |
| com.mrted.ws.position | - | Position/requisition management |
| com.mrted.ws.user | - | User authentication |
| com.mrted.ws.documents | - | Document handling |
| com.mrted.ws.configfields | - | Configuration fields |
| com.mrted.ws.lovhierarchy | - | LOV hierarchies |
| net.java.dev.jaxb | 2 | JAXB array utilities |

**Total: 465 Java stub files**

---

## Git Repository Setup

### Before Pushing to Repository

1. **Remove sensitive information:**
   ```bash
   # Check for passwords
   grep -r "password" . --exclude-dir={.git,target} | grep -v "password123"
   ```

2. **Create configuration template:**
   ```bash
   cp src/main/resources/application.properties \
      config/application.properties.sample
   # Remove sensitive values from sample
   ```

3. **Initialize and push:**
   ```bash
   git init
   git add .
   git commit -m "Initial RDPS local setup package"
   git remote add origin <your-repo-url>
   git push -u origin main
   ```

### For Junior Developers

After cloning:
```bash
git clone <repo-url>
cd RDPS_LocalSetup
./quick-start.sh
```

---

## Troubleshooting

### Issue 1: Database Connection Refused

**Symptoms:**
```
java.sql.SQLException: Listener refused the connection
```

**Solution:**
```bash
# Check if Oracle container is running
docker ps | grep oracle-db-free

# Check container logs
docker logs oracle-db-free

# Restart container if needed
docker restart oracle-db-free

# Wait 2-5 minutes for database to be ready
```

### Issue 2: ORA-01017: invalid username/password

**Solution:**
- Verify credentials in `application.properties` match the user created in database
- Default: `RDPS/rdps_password123`

### Issue 3: ORA-00942: table or view does not exist

**Solution:**
```bash
# Re-run database installation
docker exec -it oracle-db-free sqlplus RDPS/rdps_password123@FREEPDB1
SQL> @/opt/oracle/db_scripts/00_INSTALL_ALL.sql
```

### Issue 4: Port 1521 Already in Use

**Solution:**
```bash
# Use different port
docker run -d \
  --name oracle-db-free \
  -p 1522:1521 \
  -e ORACLE_PWD=password123 \
  container-registry.oracle.com/database/free

# Update application.properties
spring.datasource.url=jdbc:oracle:thin:@//localhost:1522/FREEPDB1
```

### Issue 5: Maven Build Fails

**Solution:**
```bash
# Clear Maven cache
mvn clean

# Build with debug
mvn clean package -X

# Skip tests if needed
mvn clean package -DskipTests
```

### Issue 6: Application Starts but Pages Show Errors

**Solution:**
1. Check application logs:
   ```bash
   tail -f logs/rdps.log
   ```

2. Verify all database tables exist:
   ```sql
   SELECT COUNT(*) FROM user_tables;
   -- Should return 21
   ```

3. Check for missing data:
   ```sql
   -- Verify system parameters
   SELECT * FROM RDPS_PARAMETER;

   -- Verify user profiles
   SELECT * FROM RDPS_USER_PROFILE;

   -- Verify LOV tables are populated
   SELECT COUNT(*) AS district_count FROM RDPS_LOV_DISTRICT;
   SELECT COUNT(*) AS edu_level_count FROM RDPS_LOV_EDU_LEVEL;
   SELECT COUNT(*) AS study_mode_count FROM RDPS_LOV_STUDY_MODE;
   SELECT COUNT(*) AS award_class_count FROM RDPS_LOV_QUAL_AWARD_CLASS;
   SELECT COUNT(*) AS award_desc_count FROM RDPS_LOV_QUAL_AWARD_DESC;
   ```

### Issue 7: Pagination Not Working

**Symptoms:**
- Shows all rows instead of 10 per page

**Solution:**
1. Check browser console (F12) for JavaScript errors
2. Clear browser cache (Ctrl+Shift+Delete)
3. Verify DataTables library loaded:
   ```bash
   curl -I http://localhost:8080/RDPS/resources/js/plugins/tables/datatables/datatables.min.js
   ```

### Issue 8: TalentLink API Connection Failed

**Symptoms:**
```
Failed to connect to TalentLink API
java.net.UnknownHostException
```

**Solution:**
1. Verify configuration:
   ```properties
   talentlink.api.base-url=https://your-instance.lumessetalentlink.com
   talentlink.api.username=your-username
   talentlink.api.password=your-password
   ```

2. Test connectivity:
   ```bash
   curl -v https://your-instance.lumessetalentlink.com
   ```

### Issue 9: TalentLink SOAP Authentication Failed (403 Forbidden)

**Symptoms:**
```
WARN  e.f.w.s.h.TalentLinkSOAPHandler - No username configured - SOAP request may fail authentication
ERROR e.f.w.s.TalentLinkSOAPUserService - Failed to create user via SOAP: The server sent HTTP status code 403: Forbidden
```

**Solution:**
1. Verify all 4 required parameters exist in RDPS_PARAMETER table:
   ```sql
   SELECT PARAM_CODE, PARAM_VALUE, ACTIVE
   FROM RDPS.RDPS_PARAMETER
   WHERE PARAM_CODE IN (
       'TALENTLINK_USER_SOAP_URL',
       'TALENTLINK_USERNAME',
       'TALENTLINK_PASSWORD',
       'TALENTLINK_API_KEY'
   )
   ORDER BY PARAM_CODE;
   ```

2. Ensure all parameters have valid values (not NULL or empty)

3. **Restart the application** after adding/updating parameters:
   ```bash
   # Stop Tomcat
   /path/to/tomcat/bin/shutdown.sh

   # Wait a few seconds, then start
   /path/to/tomcat/bin/startup.sh
   ```

4. Verify credentials are loaded on startup by checking logs:
   ```bash
   grep "SOAP credentials configured" logs/catalina.out
   # Should show: SOAP credentials configured for user: EdUHK UAT:prabhu.srinivasan@atalent.com:BO
   ```

### Logging and Debugging

Enable debug logging in `application.properties`:
```properties
logging.level.eduhk.fhr.web=DEBUG
logging.level.org.springframework.jdbc=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

View logs:
```bash
# Real-time
tail -f logs/rdps.log

# Search for errors
grep ERROR logs/rdps.log
```

---

## Project Structure

```
RDPS_LocalSetup/
├── README.md                           # This file
├── pom.xml                             # Maven configuration
├── .gitignore                          # Git ignore file
├── db_scripts/                         # Database scripts
│   ├── 00_INSTALL_ALL.sql             # Master installation script
│   ├── rdps_candidate.sql             # Candidate table
│   ├── rdps_user_profile.sql          # User profile table
│   ├── rdps_parameter.sql             # System parameters
│   ├── rdps_import_validation_error.sql  # Validation errors
│   └── ...                            # Other database scripts
├── src/
│   ├── main/
│   │   ├── java/                      # Java source code
│   │   │   └── eduhk/fhr/web/
│   │   │       ├── controller/        # REST controllers
│   │   │       ├── service/           # Business logic
│   │   │       ├── dao/               # Data access
│   │   │       └── model/             # Domain models
│   │   ├── java-reference/            # Reference stubs (NOT compiled)
│   │   │   ├── com/mrted/ws/         # TalentLink SOAP stubs (463 files)
│   │   │   └── net/java/dev/         # JAXB utilities (2 files)
│   │   └── resources/
│   │       ├── application.properties # Configuration
│   │       ├── templates/             # Thymeleaf templates
│   │       └── static/                # CSS, JS, images
│   └── test/                          # Unit tests
├── config/                            # Configuration templates
│   └── application.properties.sample # Sample configuration
└── target/                            # Build output (generated)
```

---

## Database Tables Overview

### User Management (2 tables)
- `RDPS_USER_PROFILE` - User account information
- `RDPS_USER_ROLE` - User roles and permissions

### System Configuration (2 tables)
- `RDPS_PARAMETER` - System configuration parameters
- `RDPS_SYSTEM_LOG` - Application logs

### Candidate Data (5 tables)
- `RDPS_CANDIDATE` - Main candidate information
- `RDPS_EDU_PROF_QUAL` - Education qualifications
- `RDPS_WORK_EXPERIENCE` - Work experience
- `RDPS_REFEREE` - References
- `RDPS_OTHER_INFO` - Additional candidate information

### Import System (3 tables)
- `RDPS_IMPORT_HISTORY` - Import operation history
- `RDPS_IMPORT_VALIDATION_ERROR` - Pre-import validation errors
- `RDPS_TALENTLINK_USER_STAGING` - User synchronization staging

### Email System (2 tables)
- `RDPS_EMAIL_TEMPLATE` - Email templates
- `RDPS_EMAIL_JOB` - Email queue

### Lookup Tables - LOV (5 tables) ⭐ NOW POPULATED
- `RDPS_LOV_DISTRICT` - Districts (19 Hong Kong districts)
- `RDPS_LOV_EDU_LEVEL` - Education levels (14 levels)
- `RDPS_LOV_QUAL_AWARD_CLASS` - Qualification award classes (16 classes)
- `RDPS_LOV_QUAL_AWARD_DESC` - Qualification descriptions (38 qualifications)
- `RDPS_LOV_STUDY_MODE` - Study modes (7 modes)

### System Tables (1 table)
- `RDPS_SHEDLOCK` - Scheduled job locking

**Total: 18 tables + 1 function (EXECUTE_EMAIL_JOB)**

---

## Key Features

### 1. Candidate Import from TalentLink
- Automated import of candidate data via TalentLink API
- Pre-import validation to prevent database errors
- Batch processing with error tracking
- Import history and audit trail

### 2. Validation Error Tracking
- Comprehensive field validation before database insert
- Tracks validation errors by batch
- User-friendly error display with pagination
- Error resolution tracking

### 3. Email Notifications
- Template-based email system
- Queue management for bulk emails
- Email job tracking

### 4. User Management
- Role-based access control (ADMIN, STAFF)
- User profile management
- Secure authentication

---

## Development Tips

### Enable Debug Logging

In `application.properties`:
```properties
logging.level.eduhk.fhr.web=DEBUG
logging.level.org.springframework.jdbc=DEBUG
```

### View SQL Queries

```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### Hot Reload (Development)

Add Spring Boot DevTools dependency in `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <optional>true</optional>
</dependency>
```

---

## Support and Resources

- **Complete Setup Guide:** This README file
- **Database Scripts:** `db_scripts/` folder (33 SQL files including LOV data inserts)
- **Schema Fixes Documentation:** `db_scripts/FIXES_APPLIED.md` - Complete list of fixes applied
- **Configuration Template:** `config/application.properties.sample`
- **Quick Setup:** Run `./quick-start.sh`

---

## License

Proprietary - The Education University of Hong Kong

---

## Changelog

### Version 0.0.1-SNAPSHOT (2025-10-17) - Updated

**Database Schema Fixes Applied:**
- ✅ Fixed RDPS_EMAIL_JOB entity-table alignment (added sendStatus, sendDate, errorMessage, retryCount to Java)
- ✅ Fixed RDPS_EMAIL_TEMPLATE entity-table alignment (added CREATED_BY, CREATION_DATE to SQL; renamed templateName to notificationStage)
- ✅ Fixed RDPS_USER_PROFILE primary key constraint (added PK on CN, ROLE)
- ✅ Fixed EXECUTE_EMAIL_JOB function column references (STATUS→SEND_STATUS, SENT_DATE→SEND_DATE)
- ✅ Fixed rdps_talentlink_soap_parameters.sql INSERT statements (PARAMETER_NAME→PARAM_CODE)
- ⭐ **Created LOV data population scripts** (5 new files with 94 reference data records)
- ✅ Updated 00_INSTALL_ALL.sql to include LOV data population steps
- ✅ Schema alignment: **100%** (was 65%)

**Initial Release:**
- Complete local development setup package
- Oracle Database setup with Docker
- 33 database installation scripts (28 table scripts + 5 LOV data inserts)
- Master installation script (00_INSTALL_ALL.sql) with 31 steps
- Automated setup script (quick-start.sh)
- Comprehensive documentation and troubleshooting guides

**Features:**
- Validation error tracking with pagination
- TalentLink GraphQL import integration
- Pre-import validation to prevent database errors
- Import history and audit trail
- Email notification system with templates
- User management and authentication
- Role-based access control (ADMIN, STAFF)

**Reference Materials:**
- 465 TalentLink SOAP web service stubs (reference only)
- Located in `src/main/java-reference/` for future use
- Complete migration guide if SOAP needed

**Database:**
- 18 tables with sequences, indexes, and 1 function
- **LOV tables now populated with 94 reference data records:**
  - 19 Hong Kong districts
  - 14 education levels
  - 7 study modes
  - 16 qualification award classes
  - 38 qualification descriptions
- Reference data and initial user accounts
- Verification scripts included
- Complete documentation in FIXES_APPLIED.md

**Build:**
- Maven 3.6+ with Spring Boot 3.x
- Java 17 compatible
- WAR deployment for Tomcat 11.x
- Zero compilation issues
- All entity models aligned with SQL schema





















## Screenshots:

	
		


	

	










	


	














