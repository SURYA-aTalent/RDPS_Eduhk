#!/bin/bash
################################################################################
# RDPS Database Removal Script
# Removes Docker container and cleans up RDPS database
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
# Removal Functions
################################################################################

drop_all_tables() {
    print_header "Option 1: Drop All RDPS Tables Only"

    if ! check_container_running; then
        print_error "Container '$CONTAINER_NAME' is not running"
        print_info "Cannot drop tables. Please start container first or use Option 2 to remove container."
        return 1
    fi

    print_warning "This will drop all RDPS tables but keep the container running"
    read -p "Are you sure you want to drop all tables? (yes/no): " confirm

    if [ "$confirm" != "yes" ]; then
        print_info "Operation cancelled"
        return 0
    fi

    print_info "Dropping all RDPS tables..."

    # Get list of all tables
    local tables=$(docker exec $CONTAINER_NAME bash -c "echo 'SELECT table_name FROM user_tables;' | sqlplus -s $RDPS_USER/$RDPS_PWD@$SERVICE_NAME" | grep -v "^$" | grep -v "TABLE_NAME" | grep -v "^-" | grep -v "selected")

    if [ -z "$tables" ]; then
        print_warning "No tables found in RDPS schema"
        return 0
    fi

    # Drop each table
    for table in $tables; do
        print_info "Dropping table: $table"
        docker exec $CONTAINER_NAME bash -c "echo 'DROP TABLE $table CASCADE CONSTRAINTS;' | sqlplus -s $RDPS_USER/$RDPS_PWD@$SERVICE_NAME" > /dev/null 2>&1
    done

    # Drop sequences
    local sequences=$(docker exec $CONTAINER_NAME bash -c "echo 'SELECT sequence_name FROM user_sequences;' | sqlplus -s $RDPS_USER/$RDPS_PWD@$SERVICE_NAME" | grep -v "^$" | grep -v "SEQUENCE_NAME" | grep -v "^-" | grep -v "selected")

    for seq in $sequences; do
        print_info "Dropping sequence: $seq"
        docker exec $CONTAINER_NAME bash -c "echo 'DROP SEQUENCE $seq;' | sqlplus -s $RDPS_USER/$RDPS_PWD@$SERVICE_NAME" > /dev/null 2>&1
    done

    # Verify
    local table_count=$(docker exec $CONTAINER_NAME bash -c "echo 'SELECT COUNT(*) FROM USER_TABLES;' | sqlplus -s $RDPS_USER/$RDPS_PWD@$SERVICE_NAME" | grep -o '[0-9]*' | head -1)

    if [ "$table_count" = "0" ]; then
        print_success "All tables dropped successfully"
        print_info "Container is still running. RDPS user exists but has no tables."
        print_info "Run ./setup_db.sh to recreate tables"
    else
        print_error "Some tables remain ($table_count tables)"
    fi
}

drop_rdps_user() {
    print_header "Option 2: Drop RDPS User (Tables + User)"

    if ! check_container_running; then
        print_error "Container '$CONTAINER_NAME' is not running"
        print_info "Cannot drop user. Please start container first or use Option 3 to remove container."
        return 1
    fi

    print_warning "This will drop RDPS user and all associated tables"
    read -p "Are you sure you want to drop RDPS user? (yes/no): " confirm

    if [ "$confirm" != "yes" ]; then
        print_info "Operation cancelled"
        return 0
    fi

    print_info "Dropping RDPS user and all tables..."
    docker exec $CONTAINER_NAME sqlplus -s sys/$ORACLE_PWD@$SERVICE_NAME as sysdba <<EOF
DROP USER $RDPS_USER CASCADE;
EXIT;
EOF

    print_success "RDPS user and all tables dropped"
    print_info "Container is still running. Run ./setup_db.sh to recreate user and tables."
}

remove_container() {
    print_header "Option 3: Remove Docker Container Completely"

    if ! check_container_exists; then
        print_warning "Container '$CONTAINER_NAME' does not exist"
        return 0
    fi

    print_warning "This will PERMANENTLY remove the Oracle Docker container"
    print_warning "All data will be lost!"
    read -p "Are you sure you want to remove the container? (yes/no): " confirm

    if [ "$confirm" != "yes" ]; then
        print_info "Operation cancelled"
        return 0
    fi

    if check_container_running; then
        print_info "Stopping container..."
        docker stop $CONTAINER_NAME
        print_success "Container stopped"
    fi

    print_info "Removing container..."
    docker rm $CONTAINER_NAME
    print_success "Container removed"

    print_info "Container '$CONTAINER_NAME' has been completely removed"
    print_info "Run ./setup_db.sh to recreate everything from scratch"
}

remove_all() {
    print_header "Option 4: Complete Cleanup (Container + Image)"

    print_warning "This will:"
    print_warning "  1. Remove Docker container"
    print_warning "  2. Remove Oracle Free Docker image"
    print_warning "  3. Delete ALL data permanently"
    echo ""
    read -p "Are you ABSOLUTELY sure? (yes/no): " confirm

    if [ "$confirm" != "yes" ]; then
        print_info "Operation cancelled"
        return 0
    fi

    # Remove container
    if check_container_exists; then
        if check_container_running; then
            print_info "Stopping container..."
            docker stop $CONTAINER_NAME
        fi
        print_info "Removing container..."
        docker rm $CONTAINER_NAME
        print_success "Container removed"
    fi

    # Remove image
    print_info "Removing Oracle Free image..."
    docker rmi container-registry.oracle.com/database/free 2>/dev/null || print_warning "Image not found or already removed"

    print_success "Complete cleanup done"
    print_info "Run ./setup_db.sh to start fresh installation"
}

show_status() {
    print_header "Current Status"

    echo -e "${BLUE}Docker Container Status:${NC}"
    if check_container_exists; then
        if check_container_running; then
            echo -e "  Container: ${GREEN}Running${NC}"

            # Check RDPS user
            local user_exists=$(docker exec $CONTAINER_NAME bash -c "echo \"SELECT COUNT(*) FROM dba_users WHERE username='RDPS';\" | sqlplus -s sys/$ORACLE_PWD@$SERVICE_NAME as sysdba 2>/dev/null" | grep -o '[0-9]*' | head -1)

            if [ "$user_exists" = "1" ]; then
                echo -e "  RDPS User: ${GREEN}Exists${NC}"

                # Count tables
                local table_count=$(docker exec $CONTAINER_NAME bash -c "echo 'SELECT COUNT(*) FROM USER_TABLES;' | sqlplus -s $RDPS_USER/$RDPS_PWD@$SERVICE_NAME 2>/dev/null" | grep -o '[0-9]*' | head -1)
                echo -e "  Tables: ${GREEN}$table_count${NC}"

                # Count parameters
                local param_count=$(docker exec $CONTAINER_NAME bash -c "echo 'SELECT COUNT(*) FROM RDPS_PARAMETER;' | sqlplus -s $RDPS_USER/$RDPS_PWD@$SERVICE_NAME 2>/dev/null" | grep -o '[0-9]*' | head -1)
                if [ ! -z "$param_count" ]; then
                    echo -e "  Parameters: ${GREEN}$param_count${NC}"
                fi
            else
                echo -e "  RDPS User: ${RED}Not Found${NC}"
            fi
        else
            echo -e "  Container: ${YELLOW}Stopped${NC}"
        fi
    else
        echo -e "  Container: ${RED}Not Found${NC}"
    fi

    echo ""
}

################################################################################
# Main Menu
################################################################################

show_menu() {
    clear
    print_header "RDPS Database Removal Tool"

    show_status

    echo -e "${BLUE}Removal Options:${NC}"
    echo ""
    echo "  1. Drop All Tables Only (keep container & user)"
    echo "  2. Drop RDPS User (drops tables + user, keep container)"
    echo "  3. Remove Docker Container (complete removal)"
    echo "  4. Complete Cleanup (remove container + image)"
    echo "  5. Show Status"
    echo "  6. Exit"
    echo ""
    read -p "Enter your choice (1-6): " choice

    case $choice in
        1)
            drop_all_tables
            ;;
        2)
            drop_rdps_user
            ;;
        3)
            remove_container
            ;;
        4)
            remove_all
            ;;
        5)
            show_status
            ;;
        6)
            print_info "Exiting..."
            exit 0
            ;;
        *)
            print_error "Invalid choice"
            ;;
    esac

    echo ""
    read -p "Press Enter to continue..."
    show_menu
}

################################################################################
# Main Script
################################################################################

main() {
    # Check if running with arguments
    if [ $# -gt 0 ]; then
        case $1 in
            --tables)
                drop_all_tables
                ;;
            --user)
                drop_rdps_user
                ;;
            --container)
                remove_container
                ;;
            --all)
                remove_all
                ;;
            --status)
                show_status
                ;;
            --help)
                echo "RDPS Database Removal Script"
                echo ""
                echo "Usage: $0 [option]"
                echo ""
                echo "Options:"
                echo "  --tables      Drop all RDPS tables only"
                echo "  --user        Drop RDPS user and tables"
                echo "  --container   Remove Docker container"
                echo "  --all         Complete cleanup (container + image)"
                echo "  --status      Show current status"
                echo "  --help        Show this help"
                echo ""
                echo "Run without arguments for interactive menu"
                ;;
            *)
                print_error "Unknown option: $1"
                echo "Run with --help for usage information"
                exit 1
                ;;
        esac
    else
        # Interactive menu
        show_menu
    fi
}

# Run main function
main "$@"
