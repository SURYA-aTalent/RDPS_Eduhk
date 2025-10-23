#!/bin/bash

# ============================================================================
# RDPS Quick Start Script
# ============================================================================
# This script automates the setup of RDPS in a local development environment
# ============================================================================

set -e

echo "============================================================================"
echo "RDPS Local Development Setup - Quick Start"
echo "============================================================================"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored messages
print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_info() {
    echo -e "${YELLOW}ℹ${NC} $1"
}

# Check prerequisites
echo "Step 1: Checking prerequisites..."
echo "-----------------------------------"

# Check Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    print_success "Java $JAVA_VERSION found"
else
    print_error "Java not found. Please install Java 17 or higher."
    exit 1
fi

# Check Maven
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -n 1 | awk '{print $3}')
    print_success "Maven $MVN_VERSION found"
else
    print_error "Maven not found. Please install Maven 3.6 or higher."
    exit 1
fi

# Check Docker
if command -v docker &> /dev/null; then
    DOCKER_VERSION=$(docker --version | awk '{print $3}' | tr -d ',')
    print_success "Docker $DOCKER_VERSION found"
else
    print_error "Docker not found. Please install Docker."
    exit 1
fi

echo ""
echo "Step 2: Setting up Oracle Database..."
echo "---------------------------------------"

# Check if container already exists
if docker ps -a | grep -q oracle-db-free; then
    print_info "Oracle container already exists"

    if docker ps | grep -q oracle-db-free; then
        print_success "Oracle container is running"
    else
        print_info "Starting existing Oracle container..."
        docker start oracle-db-free
        print_success "Oracle container started"
    fi
else
    print_info "Pulling Oracle Free Docker image..."
    docker pull container-registry.oracle.com/database/free

    print_info "Starting Oracle Database container..."
    docker run -d \
        --name oracle-db-free \
        -p 1521:1521 \
        -e ORACLE_PWD=password123 \
        container-registry.oracle.com/database/free

    print_success "Oracle container created and started"
fi

print_info "Waiting for database to be ready (this may take 2-5 minutes)..."

# Wait for database to be ready
MAX_WAIT=300  # 5 minutes
WAIT_TIME=0
READY=false

while [ $WAIT_TIME -lt $MAX_WAIT ]; do
    if docker logs oracle-db-free 2>&1 | grep -q "DATABASE IS READY TO USE"; then
        READY=true
        break
    fi
    sleep 10
    WAIT_TIME=$((WAIT_TIME + 10))
    echo -n "."
done

echo ""

if [ "$READY" = true ]; then
    print_success "Database is ready!"
else
    print_error "Database did not start within 5 minutes. Please check logs: docker logs oracle-db-free"
    exit 1
fi

echo ""
echo "Step 3: Creating RDPS database user..."
echo "---------------------------------------"

# Create RDPS user
docker exec oracle-db-free bash -c "sqlplus -s sys/password123@FREEPDB1 as sysdba <<'EOSQL' > /tmp/create_user.log 2>&1
SET ECHO OFF
SET FEEDBACK OFF
SET HEADING OFF
SET VERIFY OFF

-- Drop user if exists
DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM dba_users WHERE username = 'RDPS';
    IF v_count > 0 THEN
        EXECUTE IMMEDIATE 'DROP USER RDPS CASCADE';
    END IF;
END;
/

-- Create RDPS user
CREATE USER RDPS IDENTIFIED BY rdps_password123
  DEFAULT TABLESPACE USERS
  TEMPORARY TABLESPACE TEMP
  QUOTA UNLIMITED ON USERS;

-- Grant privileges
GRANT CONNECT, RESOURCE TO RDPS;
GRANT CREATE VIEW TO RDPS;
GRANT CREATE SEQUENCE TO RDPS;
GRANT CREATE SYNONYM TO RDPS;
GRANT CREATE DATABASE LINK TO RDPS;

-- Verify
SELECT 'User RDPS created successfully' FROM dual;

EXIT;
EOSQL
"

if grep -q "created successfully" /tmp/create_user.log; then
    print_success "RDPS user created"
else
    print_info "RDPS user may already exist or was recreated"
fi

echo ""
echo "Step 4: Installing database schema..."
echo "---------------------------------------"

# Copy database scripts to container
print_info "Copying database scripts to container..."
docker cp db_scripts oracle-db-free:/opt/oracle/
print_success "Scripts copied"

# Run installation script
print_info "Executing database installation script..."
docker exec oracle-db-free bash -c "sqlplus -s RDPS/rdps_password123@FREEPDB1 <<'EOSQL' > /tmp/install_db.log 2>&1
SET ECHO OFF
SET FEEDBACK ON
SET HEADING ON

@/opt/oracle/db_scripts/00_INSTALL_ALL.sql

EXIT;
EOSQL
"

# Check if installation was successful
if grep -q "Completed Successfully" /tmp/install_db.log; then
    print_success "Database schema installed"
else
    print_error "Database installation may have errors. Check /tmp/install_db.log"
fi

# Verify table count
TABLE_COUNT=$(docker exec oracle-db-free bash -c "sqlplus -s RDPS/rdps_password123@FREEPDB1 <<'EOSQL'
SET FEEDBACK OFF
SET HEADING OFF
SELECT COUNT(*) FROM user_tables;
EXIT;
EOSQL
" | grep -o '[0-9]*')

if [ "$TABLE_COUNT" -ge 20 ]; then
    print_success "Database verification passed ($TABLE_COUNT tables created)"
else
    print_error "Expected at least 20 tables, found $TABLE_COUNT"
fi

echo ""
echo "Step 5: Building application..."
echo "---------------------------------------"

print_info "Cleaning previous builds..."
mvn clean -q

print_info "Building application (this may take a minute)..."
if mvn package -DskipTests -q; then
    print_success "Application built successfully"
    print_info "WAR file: target/RDPS-0.0.1-SNAPSHOT.war"
else
    print_error "Build failed. Run 'mvn clean package' to see detailed errors."
    exit 1
fi

echo ""
echo "============================================================================"
echo "Setup Complete! "
echo "============================================================================"
echo ""
echo "Next Steps:"
echo ""
echo "1. Start the application:"
echo "   mvn spring-boot:run"
echo ""
echo "2. Access the application:"
echo "   http://localhost:8080/RDPS/"
echo ""
echo "3. Login with:"
echo "   Username: fhr-dev-uacct01"
echo "   Password: password123"
echo ""
echo "4. Useful pages:"
echo "   • Main Page: http://localhost:8080/RDPS/"
echo "   • Import Data: http://localhost:8080/RDPS/import/data"
echo "   • Import History: http://localhost:8080/RDPS/import/history"
echo "   • Validation Errors: http://localhost:8080/RDPS/import/validation-errors"
echo "   • User Sync: http://localhost:8080/RDPS/admin/userSync"
echo ""
echo "Database Connection Details:"
echo "   Host: localhost"
echo "   Port: 1521"
echo "   Service: FREEPDB1"
echo "   User: RDPS"
echo "   Password: rdps_password123"
echo ""
echo "To stop Oracle Database:"
echo "   docker stop oracle-db-free"
echo ""
echo "To start Oracle Database:"
echo "   docker start oracle-db-free"
echo ""
echo "For troubleshooting, see README.md"
echo "============================================================================"
