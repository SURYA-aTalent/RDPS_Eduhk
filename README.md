# RDPS Local Development Setup Guide

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Automated Database Setup (Recommended)](#automated-database-setup-recommended)
3. [Oracle Database Setup (Manual)](#oracle-database-setup-manual)
4. [Database Schema Installation](#database-schema-installation)
5. [Database Export and Import](#database-export-and-import)
6. [Application Configuration](#application-configuration)
7. [Building and Running the Application](#building-and-running-the-application)
8. [Verification](#verification)
9. [Reference Stubs](#reference-stubs)
10. [Git Repository Setup](#git-repository-setup)
11. [Troubleshooting](#troubleshooting)

---

## 🎯 Quick Start

**For experienced developers - fastest path to get running:**

### Option 1: Automated Setup (Recommended - 5 minutes)

```bash
# Clone or navigate to project
cd /path/to/eduhk_rdps

# Run automated database setup
./setup_db.sh

# Build and run application
mvn clean package
mvn spring-boot:run

# Access at http://localhost:8080/RDPS
```

**✨ That's it!** The `setup_db.sh` script handles Docker, database, and schema automatically.

### Option 2: Manual Setup (20 minutes)

For detailed step-by-step instructions, see [Automated Database Setup](#automated-database-setup-recommended) or [Manual Oracle Database Setup](#oracle-database-setup-manual) below.

**Last Updated:** 2025-10-23
**Version:** 0.0.1-SNAPSHOT

> ⭐ **IMPORTANT UPDATE (2025-10-23):** Database export/import functionality added! Complete Docker-based setup guide with tested export/import process. All commands verified to work correctly.

> ⭐ **PREVIOUS UPDATE (2025-10-17):** All database schema mismatches have been fixed! Schema alignment is now **100%** (was 65%). LOV tables are now populated with reference data. See `db_scripts/FIXES_APPLIED.md` for complete details.

---

## Prerequisites

Before starting, ensure you have the following installed:

- **Java 21**
  ```bash
  java -version
  # Should show: java version "21" or higher
  ```

- **Apache Maven 3.8+**
  ```bash
  mvn -version
  # Should show: Apache Maven 3.8.0 or higher
  ```

- **Docker** (for Oracle Database)
  ```bash
  docker --version
  # Should show: Docker version 20.0 or higher
  ```

- **Git** (optional, for version control)

**No Oracle Client Required!** All database operations use Docker commands.

---

## 🔧 Automated Database Setup (Recommended)

**⭐ NEW: Automated setup scripts for faster development environment setup!**

The project includes two powerful automation scripts that handle all database setup and cleanup operations:

### Setup Script: `setup_db.sh`

**Complete automated database setup in ~5 minutes!**

#### Features

✅ **Automated Docker Setup:**
- Checks Docker installation
- Pulls Oracle Free image automatically
- Creates and starts Oracle container
- Waits for database to be ready

✅ **RDPS User Configuration:**
- Creates RDPS user with proper privileges
- Handles existing user scenarios
- Configurable credentials

✅ **Schema Installation:**
- Installs all 20 database tables
- Creates sequences and indexes
- Populates LOV tables with reference data (104 records)
- Inserts system parameters and user accounts

✅ **Verification:**
- Validates table count (expects 20)
- Displays installation summary
- Shows error details if issues occur

#### Usage

**Interactive Mode (Recommended for first-time setup):**
```bash
./setup_db.sh
```

The script will guide you through:
1. Docker container setup
2. User creation (or skip if exists)
3. Data import options (fresh install vs. keep existing)

**Command-Line Options:**
```bash
./setup_db.sh --help          # Show all options
./setup_db.sh --skip-docker   # Skip container setup (if already running)
./setup_db.sh --fresh         # Fresh installation (drop existing data)
```

#### Example Output

```
============================================================================
Step 1: Oracle Database Container Setup
============================================================================

✓ Docker is installed
✓ Container 'oracle-db-free' is already running

============================================================================
Step 2: Creating RDPS User
============================================================================

✓ RDPS user created successfully

============================================================================
Step 3: Creating Database Schema
============================================================================

ℹ Copying database scripts to container...
✓ Expected 20 tables and found 20

Database setup completed successfully!
```

#### What Gets Installed

| Component | Count | Details |
|-----------|-------|---------|
| **Tables** | 20 | Candidate, LOV, System, Import tracking |
| **Sequences** | 5 | Auto-increment for primary keys |
| **Functions** | 1 | EXECUTE_EMAIL_JOB |
| **LOV Data** | 104 records | Districts (19), Education levels (14), Awards (51), Study modes (7), etc. |
| **Parameters** | 11 | TalentLink API config, system settings |
| **Users** | 2 | fhr-dev-uacct01 (admin), test accounts |

---

### Cleanup Script: `remove_db.sh`

**Safe database cleanup with multiple options**

#### Features

✅ **Flexible Cleanup Options:**
1. **Drop Tables Only** - Remove all tables, keep container and user
2. **Drop RDPS User** - Remove user and all tables, keep container
3. **Remove Container** - Complete container removal
4. **Complete Cleanup** - Remove container + Oracle image

✅ **Safety Features:**
- Interactive confirmations for destructive operations
- Status display before any action
- Clear warnings about data loss

✅ **Status Monitoring:**
- Shows container status
- Displays RDPS user status
- Shows table count and parameters

#### Usage

**Interactive Mode:**
```bash
./remove_db.sh
```

**Menu Options:**
```
1. Drop All Tables Only (keep container & user)
2. Drop RDPS User (drops tables + user, keep container)
3. Remove Docker Container (complete removal)
4. Complete Cleanup (remove container + image)
5. Show Status
6. Exit
```

**Command-Line Mode:**
```bash
./remove_db.sh --tables      # Drop tables only
./remove_db.sh --user        # Drop RDPS user
./remove_db.sh --container   # Remove container
./remove_db.sh --all         # Complete cleanup
./remove_db.sh --status      # Show status
./remove_db.sh --help        # Show help
```

#### Example: Quick Reset

To reset your database and start fresh:
```bash
# Option 1: Drop tables and reinstall
./remove_db.sh --tables
./setup_db.sh --skip-docker

# Option 2: Drop user and reinstall
./remove_db.sh --user
./setup_db.sh --skip-docker
```

#### Safety Confirmations

All destructive operations require explicit confirmation:
```bash
$ ./remove_db.sh --all

⚠ This will:
  1. Remove Docker container
  2. Remove Oracle Free Docker image
  3. Delete ALL data permanently

Are you ABSOLUTELY sure? (yes/no): yes
```

---

### Quick Setup Workflow

**For first-time setup:**
```bash
# 1. Run automated setup
./setup_db.sh

# 2. Build and run application
mvn clean package
mvn spring-boot:run

# 3. Access application
# http://localhost:8080/RDPS
```

**For reset/cleanup:**
```bash
# Quick table reset
./remove_db.sh --tables
./setup_db.sh --skip-docker
```

---

### Configuration

Both scripts use these default values (configurable in script):

```bash
CONTAINER_NAME="oracle-db-free"
ORACLE_PWD="password123"        # SYS/SYSTEM password
RDPS_USER="RDPS"
RDPS_PWD="rdps_password123"
SERVICE_NAME="FREEPDB1"
```

To change credentials, edit the variables at the top of each script.

---

### Troubleshooting Scripts

**Script fails with "Docker not found":**
- Install Docker: https://docs.docker.com/get-docker/

**Container won't start:**
```bash
docker logs oracle-db-free  # Check error logs
docker rm oracle-db-free    # Remove container
./setup_db.sh               # Try again
```

**Tables not created (count shows 0):**
```bash
./remove_db.sh --tables     # Clean slate
./setup_db.sh --skip-docker # Reinstall schema
```

**Want to see what the script does:**
```bash
bash -x ./setup_db.sh       # Run with debug output
```

---

## Oracle Database Setup (Manual)

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

### Using Docker (Recommended)

```bash
# Navigate to the project directory
cd /path/to/eduhk_rdps

# Copy all database scripts into the container
docker cp db_scripts oracle-db-free:/tmp/db_scripts/

# Run the master installation script
docker exec oracle-db-free bash -c "cd /tmp/db_scripts && sqlplus RDPS/rdps_password123@FREEPDB1 @00_INSTALL_ALL.sql"
```

The installation script will:
1. Create all 20 tables
2. Create sequences and indexes
3. Insert reference data (LOV tables - 104 records)
4. Insert default system parameters
5. Create user accounts
6. Set up email templates

### Verify Installation

```bash
# Check installation status
docker exec oracle-db-free bash -c "cat > /tmp/verify.sql << 'SQL'
SET PAGESIZE 50
COLUMN TABLE_NAME FORMAT A30
COLUMN ROW_COUNT FORMAT 999,999

SELECT 'RDPS_PARAMETER' AS TABLE_NAME, COUNT(*) AS ROW_COUNT FROM RDPS_PARAMETER
UNION ALL
SELECT 'RDPS_USER_PROFILE', COUNT(*) FROM RDPS_USER_PROFILE
UNION ALL
SELECT 'RDPS_LOV_DISTRICT', COUNT(*) FROM RDPS_LOV_DISTRICT
UNION ALL
SELECT 'RDPS_LOV_EDU_LEVEL', COUNT(*) FROM RDPS_LOV_EDU_LEVEL
UNION ALL
SELECT 'RDPS_LOV_QUAL_AWARD_CLASS', COUNT(*) FROM RDPS_LOV_QUAL_AWARD_CLASS
UNION ALL
SELECT 'RDPS_LOV_QUAL_AWARD_DESC', COUNT(*) FROM RDPS_LOV_QUAL_AWARD_DESC
UNION ALL
SELECT 'RDPS_LOV_STUDY_MODE', COUNT(*) FROM RDPS_LOV_STUDY_MODE
UNION ALL
SELECT 'Total Tables', COUNT(*) FROM USER_TABLES
ORDER BY 1;
EXIT;
SQL
sqlplus -s RDPS/rdps_password123@FREEPDB1 @/tmp/verify.sql"
```

Expected output:
```
TABLE_NAME                     ROW_COUNT
------------------------------ ---------
RDPS_LOV_DISTRICT                     19
RDPS_LOV_EDU_LEVEL                    14
RDPS_LOV_QUAL_AWARD_CLASS             16
RDPS_LOV_QUAL_AWARD_DESC              35
RDPS_LOV_STUDY_MODE                    7
RDPS_PARAMETER                        11
RDPS_USER_PROFILE                      2
Total Tables                          20
```

### Verify TalentLink Configuration

```bash
docker exec oracle-db-free bash -c "echo 'SELECT PARAM_CODE, PARAM_VALUE FROM RDPS_PARAMETER WHERE PARAM_CODE LIKE '\''TALENTLINK%'\'' ORDER BY PARAM_CODE;' | sqlplus -s RDPS/rdps_password123@FREEPDB1"
```

Expected output shows 6 TalentLink parameters:
- TALENTLINK_API_KEY
- TALENTLINK_API_URL
- TALENTLINK_CANDIDATE_SOAP_URL
- TALENTLINK_PASSWORD
- TALENTLINK_USERNAME
- TALENTLINK_USER_SOAP_URL

**⚠️ Important:** After schema installation, restart the application to load the TalentLink credentials. The SOAP services initialize credentials during application startup (`@PostConstruct`).

---

## Database Export and Import

This section explains how to export your current database data and import it on a new system for development environment setup.

### Exporting Database Data

Export your current database configuration and reference data to share with other developers:

```bash
# Navigate to db_scripts directory
cd db_scripts

# Run the export script
./DOCKER_EXPORT.sh
```

This creates `rdps_export.sql` containing:
- System parameters (RDPS_PARAMETER) - 11 rows
- User accounts (RDPS_USER_PROFILE) - 2 users
- All LOV reference data - 91 rows total

**What gets exported:**
- ✅ TalentLink API credentials
- ✅ System configuration
- ✅ User accounts and roles
- ✅ District lookup values (19 districts)
- ✅ Education level values (14 levels)
- ✅ Qualification classes (16 classes)
- ✅ Qualification descriptions (35 descriptions)
- ✅ Study modes (7 modes)

**Export file:** `rdps_export.sql` (~3KB)

### Importing Database Data

On a new system, after installing the schema:

```bash
# Copy export file to new system
# Then copy it into the Docker container
docker cp rdps_export.sql oracle-db-free:/tmp/

# Import the data
docker exec oracle-db-free sqlplus RDPS/rdps_password123@FREEPDB1 <<'EOF'
@/tmp/rdps_export.sql
EXIT;
EOF
```

### Verify Import

```bash
# Check data was imported correctly
docker exec oracle-db-free bash -c "cat > /tmp/verify_import.sql << 'SQL'
SELECT 'RDPS_PARAMETER' AS TABLE_NAME, COUNT(*) AS ROW_COUNT FROM RDPS_PARAMETER
UNION ALL
SELECT 'RDPS_USER_PROFILE', COUNT(*) FROM RDPS_USER_PROFILE
UNION ALL
SELECT 'RDPS_LOV_DISTRICT', COUNT(*) FROM RDPS_LOV_DISTRICT
UNION ALL
SELECT 'Total LOV Records', COUNT(*) FROM (
  SELECT 1 FROM RDPS_LOV_DISTRICT
  UNION ALL SELECT 1 FROM RDPS_LOV_EDU_LEVEL
  UNION ALL SELECT 1 FROM RDPS_LOV_QUAL_AWARD_CLASS
  UNION ALL SELECT 1 FROM RDPS_LOV_QUAL_AWARD_DESC
  UNION ALL SELECT 1 FROM RDPS_LOV_STUDY_MODE
);
EXIT;
SQL
sqlplus -s RDPS/rdps_password123@FREEPDB1 @/tmp/verify_import.sql"
```

Expected output:
```
TABLE_NAME            ROW_COUNT
--------------------- ---------
RDPS_PARAMETER               11
RDPS_USER_PROFILE             2
RDPS_LOV_DISTRICT            19
Total LOV Records           104
```

### Complete Setup for New Developer

**Step-by-step for a new team member:**

**Option 1: Automated (Recommended):**
1. **Run setup script:** `./setup_db.sh`
2. **Build & Run:** See [Building and Running](#building-and-running-the-application)

**Time:** ~5 minutes

**Option 2: Manual:**
1. **Set up Oracle Database** (see [Automated Database Setup](#automated-database-setup-recommended) or [Manual Setup](#oracle-database-setup-manual))
2. **Install Schema:** Run `00_INSTALL_ALL.sql`
3. **Import Data:** Run `rdps_export.sql` (provided by team)
4. **Verify:** Check table counts
5. **Build & Run:** See [Building and Running](#building-and-running-the-application)

**Time:** ~15-20 minutes

### Testing Export/Import Process

To test the export/import process works correctly:

```bash
# 1. Export current data
cd db_scripts
./DOCKER_EXPORT.sh

# 2. Backup current database (optional)
docker exec oracle-db-free bash -c "exp RDPS/rdps_password123@FREEPDB1 FILE=/tmp/rdps_backup.dmp OWNER=RDPS"

# 3. Drop all tables to test clean install
docker exec oracle-db-free bash -c "cat > /tmp/drop_all.sql << 'SQL'
BEGIN
  FOR t IN (SELECT table_name FROM user_tables) LOOP
    EXECUTE IMMEDIATE 'DROP TABLE ' || t.table_name || ' CASCADE CONSTRAINTS';
  END LOOP;
END;
/
EXIT;
SQL
sqlplus RDPS/rdps_password123@FREEPDB1 @/tmp/drop_all.sql"

# 4. Reinstall schema
docker cp db_scripts oracle-db-free:/tmp/db_scripts/
docker exec oracle-db-free bash -c "cd /tmp/db_scripts && sqlplus RDPS/rdps_password123@FREEPDB1 @00_INSTALL_ALL.sql"

# 5. Import data
docker cp rdps_export.sql oracle-db-free:/tmp/
docker exec oracle-db-free sqlplus RDPS/rdps_password123@FREEPDB1 @/tmp/rdps_export.sql

# 6. Verify everything works
docker exec oracle-db-free bash -c "echo 'SELECT COUNT(*) FROM RDPS_PARAMETER;' | sqlplus -s RDPS/rdps_password123@FREEPDB1"
```

This validates the complete export/import cycle works correctly.

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

### Version 0.0.1-SNAPSHOT (2025-10-23) - Database Export/Import

**New Features:**
- ✅ **Database Export/Import System** - Complete Docker-based export and import functionality
- ✅ **DOCKER_EXPORT.sh** - Automated export script for database configuration and reference data
- ✅ **export_all.sql** - SQL-based export for all configuration tables
- ✅ **Complete test cycle** - Export → Drop → Reinstall → Import process fully tested and verified
- ✅ **Updated README** - Step-by-step instructions for database export/import
- ✅ **Dev environment setup** - Streamlined process for new developers (~15-20 minutes)

**Documentation Updates:**
- Updated README with tested Docker commands
- Added Database Export and Import section
- Verified all commands work correctly
- Added testing section for validating export/import process

**Verified:**
- Schema installation: ✓ Creates 20 tables
- Data export: ✓ Exports 104 configuration records
- Data import: ✓ Imports all data correctly
- Complete cycle: ✓ Drop → Reinstall → Import tested successfully

### Version 0.0.1-SNAPSHOT (2025-10-17) - Schema Fixes

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

	
		


	

	










	


	














