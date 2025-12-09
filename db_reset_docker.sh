#!/bin/bash

# Database connection details
DB_USER="RDPS"
DB_PASS="rdps_password123"
# On macOS, the host machine can be accessed from a docker container using host.docker.internal
DB_CONNECT_STRING="//host.docker.internal:1521/F"

# SQL file to import, as it will be seen from inside the container
SQL_FILE="/work/eduhk_prod_final (1).sql"

# Temporary file for drop statements
DROP_SCRIPT="/work/drop_tables.sql"

# Docker image to use
DOCKER_IMAGE="oracle/instantclient:latest"

# Generate drop table statements
echo "Generating drop table statements..."
docker run --rm -v "$(pwd)":/work ${DOCKER_IMAGE} sqlplus -S ${DB_USER}/${DB_PASS}@${DB_CONNECT_STRING} <<EOF
SET HEADING OFF
SET FEEDBACK OFF
SET PAGESIZE 0
SPOOL ${DROP_SCRIPT}
SELECT 'DROP TABLE "' || table_name || '" CASCADE CONSTRAINTS;' FROM user_tables;
SPOOL OFF
EXIT
EOF

# Execute drop table statements
echo "Dropping tables..."
docker run --rm -v "$(pwd)":/work ${DOCKER_IMAGE} sqlplus -S ${DB_USER}/${DB_PASS}@${DB_CONNECT_STRING} @"${DROP_SCRIPT}"

# Import the SQL file
echo "Importing SQL file..."
docker run --rm -v "$(pwd)":/work ${DOCKER_IMAGE} sqlplus -S ${DB_USER}/${DB_PASS}@${DB_CONNECT_STRING} @"${SQL_FILE}"

echo "Database reset complete."
