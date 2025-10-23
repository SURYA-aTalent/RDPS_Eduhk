#!/bin/bash
################################################################################
# Export RDPS Database using Docker
# Output: rdps_export.sql
################################################################################

echo "Exporting RDPS database..."

docker exec oracle-db-free bash << 'DOCKERSCRIPT'
cat > /tmp/export.sql << 'SQL'
SET PAGESIZE 0
SET LINESIZE 32767
SET FEEDBACK OFF
SET HEADING OFF
SPOOL /tmp/rdps_export.sql

PROMPT -- ============================================================================
PROMPT -- RDPS Database Export
PROMPT -- ============================================================================
PROMPT

-- Export all tables
SELECT 'INSERT INTO RDPS_PARAMETER VALUES (''' || PARAM_CODE || ''',''' ||
       REPLACE(PARAM_VALUE,'''','''''') || ''',''' || ACTIVE || ''',SYSDATE,''' || USERSTAMP || ''');'
FROM RDPS_PARAMETER;
PROMPT COMMIT;
PROMPT

SELECT 'INSERT INTO RDPS_USER_PROFILE (CN,ROLE,EFFECTIVE_DATE_FROM,EFFECTIVE_DATE_TO,ACTIVE,TIMESTAMP,USERSTAMP) VALUES (''' || CN || ''',''' || ROLE || ''',' ||
       CASE WHEN EFFECTIVE_DATE_FROM IS NULL THEN 'NULL' ELSE 'TO_DATE(''' || TO_CHAR(EFFECTIVE_DATE_FROM,'YYYY-MM-DD') || ''',''YYYY-MM-DD'')' END || ',' ||
       CASE WHEN EFFECTIVE_DATE_TO IS NULL THEN 'NULL' ELSE 'TO_DATE(''' || TO_CHAR(EFFECTIVE_DATE_TO,'YYYY-MM-DD') || ''',''YYYY-MM-DD'')' END || ',''' ||
       ACTIVE || ''',SYSDATE,''' || USERSTAMP || ''');'
FROM RDPS_USER_PROFILE;
PROMPT COMMIT;
PROMPT

SELECT 'INSERT INTO RDPS_LOV_DISTRICT VALUES (''' || CODE || ''',''' ||
       REPLACE(DISTRICT,'''','''''') || ''',''' || ACTIVE || ''',SYSDATE,''' || USERSTAMP || ''');'
FROM RDPS_LOV_DISTRICT WHERE ROWNUM <= (SELECT COUNT(*) FROM RDPS_LOV_DISTRICT);
PROMPT COMMIT;
PROMPT

SELECT 'INSERT INTO RDPS_LOV_EDU_LEVEL VALUES (''' || CODE || ''',''' ||
       REPLACE(EDU_LEVEL,'''','''''') || ''',''' || ACTIVE || ''',SYSDATE,''' || USERSTAMP || ''');'
FROM RDPS_LOV_EDU_LEVEL WHERE ROWNUM <= (SELECT COUNT(*) FROM RDPS_LOV_EDU_LEVEL);
PROMPT COMMIT;
PROMPT

SELECT 'INSERT INTO RDPS_LOV_QUAL_AWARD_CLASS VALUES (''' || CODE || ''',''' ||
       REPLACE(QUAL_AWARD_CLASS,'''','''''') || ''',''' || ACTIVE || ''',SYSDATE,''' || USERSTAMP || ''');'
FROM RDPS_LOV_QUAL_AWARD_CLASS WHERE ROWNUM <= (SELECT COUNT(*) FROM RDPS_LOV_QUAL_AWARD_CLASS);
PROMPT COMMIT;
PROMPT

SELECT 'INSERT INTO RDPS_LOV_QUAL_AWARD_DESC VALUES (''' || CODE || ''',''' ||
       REPLACE(QUAL_AWARD_DESC,'''','''''') || ''',''' || ACTIVE || ''',SYSDATE,''' || USERSTAMP || ''');'
FROM RDPS_LOV_QUAL_AWARD_DESC WHERE ROWNUM <= (SELECT COUNT(*) FROM RDPS_LOV_QUAL_AWARD_DESC);
PROMPT COMMIT;
PROMPT

SELECT 'INSERT INTO RDPS_LOV_STUDY_MODE VALUES (''' || CODE || ''',''' ||
       REPLACE(STUDY_MODE,'''','''''') || ''',''' || ACTIVE || ''',SYSDATE,''' || USERSTAMP || ''');'
FROM RDPS_LOV_STUDY_MODE WHERE ROWNUM <= (SELECT COUNT(*) FROM RDPS_LOV_STUDY_MODE);
PROMPT COMMIT;
PROMPT

SPOOL OFF
EXIT;
SQL

sqlplus -s RDPS/rdps_password123@FREEPDB1 @/tmp/export.sql > /dev/null
DOCKERSCRIPT

# Copy export file from container
docker cp oracle-db-free:/tmp/rdps_export.sql rdps_export.sql

echo ""
echo "✓ Export completed!"
echo "  File: rdps_export.sql"
ls -lh rdps_export.sql
echo ""
echo "To import on another database:"
echo "  docker exec oracle-db-free sqlplus RDPS/password@FREEPDB1 @rdps_export.sql"
