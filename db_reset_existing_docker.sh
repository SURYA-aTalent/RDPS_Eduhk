#!/bin/bash

# Docker Container details
CONTAINER_NAME="oracle-db-free"

# Database connection details
DB_USER="RDPS"
DB_PASS="rdps_password123"
DB_CONNECT_STRING="localhost:1521/FREE" # Assuming "FREE" is the service name for Oracle Free

# Local SQL file to import
LOCAL_SQL_FILE="/Users/suryasivakumar/Downloads/eduhk_rdps 2/eduhk_prod_final (1).sql"

# Path to SQL file inside the container
CONTAINER_SQL_FILE="/tmp/eduhk_prod_final.sql"

# Temporary file for drop statements (will be created inside container and removed)
DROP_SCRIPT_CONTAINER="/tmp/drop_tables.sql"

# Generate drop table statements within the container
echo "Generating drop table statements within container ${CONTAINER_NAME}..."
docker exec -i ${CONTAINER_NAME} sqlplus -S ${DB_USER}/${DB_PASS}@${DB_CONNECT_STRING} <<EOF
SET HEADING OFF
SET FEEDBACK OFF
SET PAGESIZE 0
SPOOL ${DROP_SCRIPT_CONTAINER}
SELECT 'DROP TABLE "' || table_name || '" CASCADE CONSTRAINTS;' FROM user_tables;
SPOOL OFF
EXIT
EOF

# Execute drop table statements within the container
echo "Dropping tables within container ${CONTAINER_NAME}..."
docker exec -i ${CONTAINER_NAME} sqlplus -S ${DB_USER}/${DB_PASS}@${DB_CONNECT_STRING} @"${DROP_SCRIPT_CONTAINER}"

# Copy SQL file into the container
echo "Copying SQL file to container ${CONTAINER_NAME}..."
docker cp "${LOCAL_SQL_FILE}" "${CONTAINER_NAME}:${CONTAINER_SQL_FILE}"

# Import the SQL file within the container
echo "Importing SQL file into container ${CONTAINER_NAME}..."
docker exec -i ${CONTAINER_NAME} sqlplus -S ${DB_USER}/${DB_PASS}@${DB_CONNECT_STRING} @"${CONTAINER_SQL_FILE}"

# Clean up temporary files in container
echo "Cleaning up temporary files in container ${CONTAINER_NAME}..."
docker exec ${CONTAINER_NAME} rm "${CONTAINER_SQL_FILE}" "${DROP_SCRIPT_CONTAINER}"

echo "Database reset complete."
