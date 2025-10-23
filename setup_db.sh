#!/bin/bash
################################################################################
# RDPS Database Setup Script
# Complete automated setup for RDPS development environment
################################################################################

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
CONTAINER_NAME="oracle-db-free"
ORACLE_PWD="password123"
RDPS_USER="RDPS"
RDPS_PWD="rdps_password123"
SERVICE_NAME="FREEPDB1"

################################################################################
# Helper Functions
################################################################################

print_header() {
    echo -e "\n${BLUE}============================================================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}============================================================================${NC}\n"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

wait_for_database() {
    print_info "Waiting for Oracle database to be ready (this may take 2-5 minutes)..."
    local max_attempts=60
    local attempt=0

    while [ $attempt -lt $max_attempts ]; do
        if docker logs $CONTAINER_NAME 2>&1 | grep -q "DATABASE IS READY TO USE"; then
            print_success "Database is ready!"
            return 0
        fi
        echo -n "."
        sleep 5
        attempt=$((attempt + 1))
    done

    print_error "Database did not start within expected time"
    return 1
}

check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed"
        echo "Please install Docker first: https://docs.docker.com/get-docker/"
        exit 1
    fi
    print_success "Docker is installed"
}

check_container_exists() {
    if docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
        return 0
    fi
    return 1
}

check_container_running() {
    if docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
        return 0
    fi
    return 1
}

################################################################################
# Database Setup Functions
################################################################################

setup_docker_container() {
    print_header "Step 1: Oracle Database Container Setup"

    if check_container_exists; then
        if check_container_running; then
            print_warning "Container '$CONTAINER_NAME' is already running"
            read -p "Do you want to use existing container? (y/n): " use_existing
            if [[ $use_existing =~ ^[Yy]$ ]]; then
                print_success "Using existing container"
                return 0
            else
                print_info "Stopping and removing existing container..."
                docker stop $CONTAINER_NAME 2>/dev/null || true
                docker rm $CONTAINER_NAME 2>/dev/null || true
            fi
        else
            print_warning "Container '$CONTAINER_NAME' exists but is not running"
            read -p "Do you want to remove and recreate it? (y/n): " recreate
            if [[ $recreate =~ ^[Yy]$ ]]; then
                docker rm $CONTAINER_NAME 2>/dev/null || true
            else
                print_info "Starting existing container..."
                docker start $CONTAINER_NAME
                wait_for_database
                return 0
            fi
        fi
    fi

    print_info "Pulling Oracle Database Free image..."
    docker pull container-registry.oracle.com/database/free

    print_info "Creating Oracle Database container..."
    docker run -d \
        --name $CONTAINER_NAME \
        -p 1521:1521 \
        -e ORACLE_PWD=$ORACLE_PWD \
        container-registry.oracle.com/database/free

    wait_for_database
    print_success "Oracle container created and running"
}

create_rdps_user() {
    print_header "Step 2: Creating RDPS User"

    # Check if user already exists
    local user_exists=$(docker exec $CONTAINER_NAME bash -c "echo \"SELECT COUNT(*) FROM dba_users WHERE username='RDPS';\" | sqlplus -s sys/$ORACLE_PWD@$SERVICE_NAME as sysdba" | grep -o '[0-9]*' | head -1)

    if [ "$user_exists" = "1" ]; then
        print_warning "RDPS user already exists"
        read -p "Do you want to drop and recreate RDPS user? (y/n): " recreate_user
        if [[ $recreate_user =~ ^[Yy]$ ]]; then
            print_info "Dropping existing RDPS user..."
            docker exec $CONTAINER_NAME bash -c "sqlplus -s sys/$ORACLE_PWD@$SERVICE_NAME as sysdba <<'EOSQL'
DROP USER RDPS CASCADE;
EXIT;
EOSQL
"
            print_success "Dropped existing RDPS user"
        else
            print_success "Using existing RDPS user"
            return 0
        fi
    fi

    print_info "Creating RDPS user..."
    docker exec $CONTAINER_NAME bash -c "sqlplus -s sys/$ORACLE_PWD@$SERVICE_NAME as sysdba <<'EOSQL'
CREATE USER $RDPS_USER IDENTIFIED BY $RDPS_PWD
  DEFAULT TABLESPACE USERS
  QUOTA UNLIMITED ON USERS;

GRANT CONNECT, RESOURCE, CREATE VIEW, CREATE SEQUENCE TO $RDPS_USER;

EXIT;
EOSQL
"

    print_success "RDPS user created with privileges"
}

create_database_schema() {
    print_header "Step 3: Creating Database Schema"

    # Copy scripts to container
    print_info "Copying database scripts to container..."
    docker cp db_scripts $CONTAINER_NAME:/tmp/

    # Run installation script
    print_info "Creating 20 database tables..."
    docker exec $CONTAINER_NAME bash -c "cd /tmp/db_scripts && sqlplus -s $RDPS_USER/$RDPS_PWD@$SERVICE_NAME @00_INSTALL_ALL.sql" > /tmp/rdps_install.log 2>&1

    # Verify table creation
    local table_count=$(docker exec $CONTAINER_NAME bash -c "echo 'SELECT COUNT(*) FROM USER_TABLES;' | sqlplus -s $RDPS_USER/$RDPS_PWD@$SERVICE_NAME" | grep -o '[0-9]*' | head -1)

    if [ "$table_count" = "20" ]; then
        print_success "Created 20 database tables successfully"
    else
        print_error "Expected 20 tables but found $table_count"
        print_warning "Check /tmp/rdps_install.log for details"
        exit 1
    fi
}

import_from_dump() {
    print_header "Step 4: Importing Data from Dump File"

    # Check if dump file exists
    if [ ! -f "db_scripts/rdps_export.sql" ]; then
        print_error "Dump file 'db_scripts/rdps_export.sql' not found"
        print_info "Please ensure the dump file exists or choose Option 2 (fresh setup)"
        exit 1
    fi

    print_info "Importing data from rdps_export.sql..."
    docker exec $CONTAINER_NAME bash -c "sqlplus -s $RDPS_USER/$RDPS_PWD@$SERVICE_NAME <<'EOSQL'
@/tmp/db_scripts/rdps_export.sql
EXIT;
EOSQL
"

    # Verify import
    local param_count=$(docker exec $CONTAINER_NAME bash -c "echo 'SELECT COUNT(*) FROM RDPS_PARAMETER;' | sqlplus -s $RDPS_USER/$RDPS_PWD@$SERVICE_NAME" | grep -o '[0-9]*' | head -1)

    if [ "$param_count" -ge "11" ]; then
        print_success "Data imported successfully ($param_count parameters)"
    else
        print_error "Expected at least 11 parameters but found $param_count"
        exit 1
    fi
}

insert_fresh_data() {
    print_header "Step 4: Inserting Fresh Configuration Data"

    print_info "Inserting TalentLink configuration and system parameters..."
    docker exec $CONTAINER_NAME bash -c "sqlplus -s $RDPS_USER/$RDPS_PWD@$SERVICE_NAME <<'EOSQL'
-- TalentLink API Configuration
INSERT INTO RDPS_PARAMETER VALUES ('TALENTLINK_API_URL','https://api3.lumesse-talenthub.com','Y',SYSDATE,'SYSTEM');
INSERT INTO RDPS_PARAMETER VALUES ('TALENTLINK_API_KEY','10047a13-72fb-ad0a-0cc4-773a4ef874b9','Y',SYSDATE,'SYSTEM');
INSERT INTO RDPS_PARAMETER VALUES ('TALENTLINK_USERNAME','EdUHK UAT:prabhu.srinivasan@atalent.com:BO','Y',SYSDATE,'SYSTEM');
INSERT INTO RDPS_PARAMETER VALUES ('TALENTLINK_PASSWORD','2!Password','Y',SYSDATE,'SYSTEM');
INSERT INTO RDPS_PARAMETER VALUES ('TALENTLINK_CANDIDATE_SOAP_URL','https://api3.lumesse-talenthub.com/HRIS/SOAP/Candidate','Y',SYSDATE,'SYSTEM');
INSERT INTO RDPS_PARAMETER VALUES ('TALENTLINK_USER_SOAP_URL','https://api3.lumesse-talenthub.com/User/SOAP/User','Y',SYSDATE,'SYSTEM');

-- System Configuration
INSERT INTO RDPS_PARAMETER VALUES ('ENVIRONMENT','DEVELOPMENT','Y',SYSDATE,'SYSTEM');
INSERT INTO RDPS_PARAMETER VALUES ('IMPORT_BATCH_SIZE','100','Y',SYSDATE,'SYSTEM');
INSERT INTO RDPS_PARAMETER VALUES ('LAST_IMPORTED_CANDIDATE_ID','0','Y',SYSDATE,'SYSTEM');
INSERT INTO RDPS_PARAMETER VALUES ('SSO_LOGOUT_URL','https://dapplt.eduhk.hk/logout.jsp','Y',SYSDATE,'SYSTEM');
INSERT INTO RDPS_PARAMETER VALUES ('LAST_IMPORT_TIMESTAMP','2025-01-01 00:00:00','Y',SYSDATE,'SYSTEM');

COMMIT;
EXIT;
EOSQL
"

    # Verify insertion
    local param_count=$(docker exec $CONTAINER_NAME bash -c "echo 'SELECT COUNT(*) FROM RDPS_PARAMETER;' | sqlplus -s $RDPS_USER/$RDPS_PWD@$SERVICE_NAME" | grep -o '[0-9]*' | head -1)

    if [ "$param_count" -ge "11" ]; then
        print_success "Inserted $param_count system parameters"
    else
        print_error "Expected 11 parameters but found $param_count"
        exit 1
    fi
}

verify_setup() {
    print_header "Step 5: Verification"

    print_info "Running verification queries..."
    docker exec $CONTAINER_NAME bash -c "cat > /tmp/verify.sql <<'SQL'
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
SELECT 'Total Tables', COUNT(*) FROM USER_TABLES;
EXIT;
SQL
sqlplus -s $RDPS_USER/$RDPS_PWD@$SERVICE_NAME @/tmp/verify.sql"

    print_success "Verification complete"
}

print_summary() {
    print_header "Setup Complete!"

    echo -e "${GREEN}Database Setup Summary:${NC}"
    echo ""
    echo "  Container Name:  $CONTAINER_NAME"
    echo "  Host:           localhost"
    echo "  Port:           1521"
    echo "  Service:        $SERVICE_NAME"
    echo "  Schema:         $RDPS_USER"
    echo "  Password:       $RDPS_PWD"
    echo ""
    echo -e "${GREEN}Next Steps:${NC}"
    echo "  1. Build application:  mvn clean package -DskipTests"
    echo "  2. Run application:    mvn spring-boot:run"
    echo "  3. Access at:          http://localhost:8080/RDPS/"
    echo "  4. Default user:       fhr-dev-uacct01"
    echo ""
    echo -e "${BLUE}Useful Commands:${NC}"
    echo "  Check database:        docker exec $CONTAINER_NAME sqlplus $RDPS_USER/$RDPS_PWD@$SERVICE_NAME"
    echo "  View logs:            docker logs $CONTAINER_NAME"
    echo "  Stop container:       docker stop $CONTAINER_NAME"
    echo "  Start container:      docker start $CONTAINER_NAME"
    echo ""
}

################################################################################
# Main Script
################################################################################

main() {
    clear
    print_header "RDPS Database Setup - Automated Installation"

    # Check prerequisites
    check_docker

    # Step 1: Docker container setup
    setup_docker_container

    # Step 2: Create RDPS user
    create_rdps_user

    # Step 3: Create database schema
    create_database_schema

    # Step 4: Choose data import method
    print_header "Step 4: Data Import Options"
    echo "Choose data import method:"
    echo "  1. Import from existing dump (db_scripts/rdps_export.sql)"
    echo "  2. Fresh setup with default configuration"
    echo ""
    read -p "Enter your choice (1 or 2): " import_choice

    case $import_choice in
        1)
            import_from_dump
            ;;
        2)
            insert_fresh_data
            ;;
        *)
            print_error "Invalid choice. Exiting."
            exit 1
            ;;
    esac

    # Step 5: Verify setup
    verify_setup

    # Print summary
    print_summary
}

# Run main function
main
