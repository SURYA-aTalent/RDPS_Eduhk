-- ============================================================================
-- RDPS Database Installation Verification Script
-- ============================================================================
-- Run this script to verify all database objects were created successfully
-- ============================================================================

SET LINESIZE 200
SET PAGESIZE 1000
SET FEEDBACK ON
SET ECHO ON

PROMPT ============================================================================
PROMPT RDPS Database Installation Verification
PROMPT ============================================================================

-- Check current schema
PROMPT
PROMPT Current Schema:
SELECT SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') AS current_schema FROM dual;

-- ============================================================================
-- TABLE COUNT
-- ============================================================================
PROMPT
PROMPT ============================================================================
PROMPT Table Count (Expected: 21 tables)
PROMPT ============================================================================
SELECT COUNT(*) as table_count FROM user_tables;

-- ============================================================================
-- LIST ALL TABLES
-- ============================================================================
PROMPT
PROMPT ============================================================================
PROMPT All Tables in RDPS Schema
PROMPT ============================================================================
SELECT table_name, num_rows, last_analyzed
FROM user_tables
ORDER BY table_name;

-- ============================================================================
-- SEQUENCE COUNT
-- ============================================================================
PROMPT
PROMPT ============================================================================
PROMPT Sequence Count
PROMPT ============================================================================
SELECT COUNT(*) as sequence_count FROM user_sequences;

-- List all sequences
SELECT sequence_name, last_number
FROM user_sequences
ORDER BY sequence_name;

-- ============================================================================
-- FUNCTION/PROCEDURE COUNT
-- ============================================================================
PROMPT
PROMPT ============================================================================
PROMPT Functions and Procedures
PROMPT ============================================================================
SELECT object_type, COUNT(*) as count
FROM user_objects
WHERE object_type IN ('FUNCTION', 'PROCEDURE')
GROUP BY object_type;

-- List all functions and procedures
SELECT object_name, object_type, status
FROM user_objects
WHERE object_type IN ('FUNCTION', 'PROCEDURE')
ORDER BY object_type, object_name;

-- ============================================================================
-- INVALID OBJECTS CHECK
-- ============================================================================
PROMPT
PROMPT ============================================================================
PROMPT Invalid Objects (Should be empty)
PROMPT ============================================================================
SELECT object_name, object_type, status
FROM user_objects
WHERE status = 'INVALID'
ORDER BY object_type, object_name;

-- ============================================================================
-- ROW COUNTS FOR KEY TABLES
-- ============================================================================
PROMPT
PROMPT ============================================================================
PROMPT Row Counts for Key Tables
PROMPT ============================================================================

-- User profiles
SELECT 'RDPS_USER_PROFILE' as table_name, COUNT(*) as row_count
FROM RDPS_USER_PROFILE
UNION ALL
SELECT 'RDPS_USER_ROLE', COUNT(*) FROM RDPS_USER_ROLE
UNION ALL
SELECT 'RDPS_PARAMETER', COUNT(*) FROM RDPS_PARAMETER
UNION ALL
SELECT 'RDPS_EMAIL_TEMPLATE', COUNT(*) FROM RDPS_EMAIL_TEMPLATE
UNION ALL
SELECT 'RDPS_CANDIDATE', COUNT(*) FROM RDPS_CANDIDATE
UNION ALL
SELECT 'RDPS_IMPORT_VALIDATION_ERROR', COUNT(*) FROM RDPS_IMPORT_VALIDATION_ERROR;

-- ============================================================================
-- SAMPLE USER DATA
-- ============================================================================
PROMPT
PROMPT ============================================================================
PROMPT Sample User Profiles (for login testing)
PROMPT ============================================================================
SELECT user_id, user_name, email, status
FROM RDPS_USER_PROFILE
ORDER BY user_id;

-- ============================================================================
-- PARAMETER CHECK
-- ============================================================================
PROMPT
PROMPT ============================================================================
PROMPT System Parameters
PROMPT ============================================================================
SELECT param_key, param_value
FROM RDPS_PARAMETER
ORDER BY param_key;

-- ============================================================================
-- TALENTLINK CONFIGURATION
-- ============================================================================
PROMPT
PROMPT ============================================================================
PROMPT TalentLink SOAP Parameters
PROMPT ============================================================================
SELECT param_key, param_value
FROM RDPS_TALENTLINK_SOAP_PARAMETERS
WHERE param_key NOT LIKE '%PASSWORD%'
ORDER BY param_key;

-- ============================================================================
-- CONSTRAINT VERIFICATION
-- ============================================================================
PROMPT
PROMPT ============================================================================
PROMPT Constraint Count
PROMPT ============================================================================
SELECT constraint_type,
       CASE constraint_type
         WHEN 'P' THEN 'Primary Key'
         WHEN 'R' THEN 'Foreign Key'
         WHEN 'U' THEN 'Unique'
         WHEN 'C' THEN 'Check'
       END as constraint_description,
       COUNT(*) as count
FROM user_constraints
GROUP BY constraint_type
ORDER BY constraint_type;

-- ============================================================================
-- INDEX VERIFICATION
-- ============================================================================
PROMPT
PROMPT ============================================================================
PROMPT Index Count
PROMPT ============================================================================
SELECT COUNT(*) as index_count FROM user_indexes;

-- ============================================================================
-- TABLESPACE USAGE
-- ============================================================================
PROMPT
PROMPT ============================================================================
PROMPT Tablespace Usage
PROMPT ============================================================================
SELECT tablespace_name, COUNT(*) as object_count
FROM user_tables
GROUP BY tablespace_name;

-- ============================================================================
-- FINAL SUMMARY
-- ============================================================================
PROMPT
PROMPT ============================================================================
PROMPT Installation Verification Summary
PROMPT ============================================================================

DECLARE
  v_table_count NUMBER;
  v_seq_count NUMBER;
  v_func_count NUMBER;
  v_invalid_count NUMBER;
  v_user_count NUMBER;
  v_param_count NUMBER;

  v_status VARCHAR2(10) := 'PASS';
BEGIN
  -- Count tables
  SELECT COUNT(*) INTO v_table_count FROM user_tables;

  -- Count sequences
  SELECT COUNT(*) INTO v_seq_count FROM user_sequences;

  -- Count functions
  SELECT COUNT(*) INTO v_func_count
  FROM user_objects
  WHERE object_type IN ('FUNCTION', 'PROCEDURE');

  -- Count invalid objects
  SELECT COUNT(*) INTO v_invalid_count
  FROM user_objects
  WHERE status = 'INVALID';

  -- Count users
  SELECT COUNT(*) INTO v_user_count FROM RDPS_USER_PROFILE;

  -- Count parameters
  SELECT COUNT(*) INTO v_param_count FROM RDPS_PARAMETER;

  DBMS_OUTPUT.PUT_LINE('Tables created: ' || v_table_count || ' (Expected: 21+)');
  DBMS_OUTPUT.PUT_LINE('Sequences created: ' || v_seq_count);
  DBMS_OUTPUT.PUT_LINE('Functions/Procedures: ' || v_func_count || ' (Expected: 1+)');
  DBMS_OUTPUT.PUT_LINE('Invalid objects: ' || v_invalid_count || ' (Expected: 0)');
  DBMS_OUTPUT.PUT_LINE('User profiles: ' || v_user_count || ' (Expected: 1+)');
  DBMS_OUTPUT.PUT_LINE('System parameters: ' || v_param_count || ' (Expected: 3+)');

  DBMS_OUTPUT.PUT_LINE('');

  -- Verification checks
  IF v_table_count < 21 THEN
    DBMS_OUTPUT.PUT_LINE('❌ FAIL: Insufficient tables created');
    v_status := 'FAIL';
  END IF;

  IF v_invalid_count > 0 THEN
    DBMS_OUTPUT.PUT_LINE('❌ FAIL: Invalid objects found');
    v_status := 'FAIL';
  END IF;

  IF v_user_count = 0 THEN
    DBMS_OUTPUT.PUT_LINE('❌ FAIL: No user profiles found');
    v_status := 'FAIL';
  END IF;

  IF v_param_count = 0 THEN
    DBMS_OUTPUT.PUT_LINE('❌ FAIL: No system parameters found');
    v_status := 'FAIL';
  END IF;

  DBMS_OUTPUT.PUT_LINE('');

  IF v_status = 'PASS' THEN
    DBMS_OUTPUT.PUT_LINE('✓ VERIFICATION PASSED - Database installation successful!');
  ELSE
    DBMS_OUTPUT.PUT_LINE('✗ VERIFICATION FAILED - Please review errors above');
  END IF;
END;
/

PROMPT
PROMPT ============================================================================
PROMPT End of Verification Report
PROMPT ============================================================================
