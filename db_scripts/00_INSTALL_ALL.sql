-- ============================================================================
-- RDPS Database Installation Script
-- ============================================================================
-- This script creates all required database objects for RDPS application
-- Execute this as SYSTEM user or a user with CREATE TABLE, CREATE SEQUENCE privileges
-- ============================================================================

-- Set session parameters
SET SERVEROUTPUT ON;
ALTER SESSION SET CURRENT_SCHEMA = RDPS;

PROMPT ============================================================================
PROMPT Starting RDPS Database Installation
PROMPT ============================================================================

-- ============================================================================
-- STEP 1: Create User Profile Tables
-- ============================================================================
PROMPT
PROMPT [1/18] Creating RDPS_USER_PROFILE table...
@@rdps_user_profile.sql

PROMPT [2/18] Creating RDPS_USER_ROLE table...
@@rdps_user_role.sql

-- ============================================================================
-- STEP 2: Create Parameter Tables
-- ============================================================================
PROMPT [3/18] Creating RDPS_PARAMETER table...
@@rdps_parameter.sql

PROMPT [4/18] Creating RDPS_TALENTLINK_SOAP_PARAMETERS table...
@@rdps_talentlink_soap_parameters.sql

-- ============================================================================
-- STEP 3: Create Lookup Tables (LOV)
-- ============================================================================
PROMPT [5/18] Creating RDPS_LOV_DISTRICT table...
@@rdps_lov_district.sql

PROMPT [6/18] Creating RDPS_LOV_EDU_LEVEL table...
@@rdps_lov_edu_level.sql

PROMPT [7/18] Creating RDPS_LOV_QUAL_AWARD_CLASS table...
@@rdps_lov_qual_award_class.sql

PROMPT [8/18] Creating RDPS_LOV_QUAL_AWARD_DESC table...
@@rdps_lov_qual_award_desc.sql

PROMPT [9/18] Creating RDPS_LOV_STUDY_MODE table...
@@rdps_lov_study_mode.sql

-- ============================================================================
-- STEP 4: Create Main Application Tables
-- ============================================================================
PROMPT [10/18] Creating RDPS_CANDIDATE table...
@@rdps_candidate.sql

PROMPT [11/18] Creating RDPS_EDU_PROF_QUAL table...
@@rdps_edu_prof_qual.sql

PROMPT [12/18] Creating RDPS_WORK_EXPERIENCE table...
@@rdps_work_experience.sql

PROMPT [13/18] Creating RDPS_REFEREE table...
@@rdps_referee.sql

PROMPT [14/18] Creating RDPS_OTHER_INFO table...
@@rdps_other_info.sql

-- ============================================================================
-- STEP 5: Create Email System Tables
-- ============================================================================
PROMPT [15/18] Creating RDPS_EMAIL_TEMPLATE table...
@@rdps_email_template.sql

PROMPT [16/18] Creating RDPS_EMAIL_JOB table...
@@rdps_email_job.sql

-- ============================================================================
-- STEP 6: Create Import System Tables
-- ============================================================================
PROMPT [17/18] Creating RDPS_IMPORT_HISTORY table...
@@rdps_import_history.sql

PROMPT [18/18] Creating RDPS_IMPORT_VALIDATION_ERROR table...
@@rdps_import_validation_error.sql

PROMPT [19/18] Creating RDPS_TALENTLINK_USER_STAGING table...
@@rdps_talentlink_user_staging.sql

-- ============================================================================
-- STEP 7: Create System Tables
-- ============================================================================
PROMPT [20/18] Creating RDPS_SYSTEM_LOG table...
@@rdps_system_log.sql

PROMPT [21/18] Creating RDPS_SHEDLOCK table (for scheduled jobs)...
@@rdps_shedlock.sql

-- ============================================================================
-- STEP 8: Insert Reference Data
-- ============================================================================
PROMPT
PROMPT Inserting reference data...

-- LOV Data
PROMPT [22/31] Inserting district lookup values...
@@rdps_lov_district_insert.sql

PROMPT [23/31] Inserting education level lookup values...
@@rdps_lov_edu_level_insert.sql

PROMPT [24/31] Inserting study mode lookup values...
@@rdps_lov_study_mode_insert.sql

PROMPT [25/31] Inserting qualification award class lookup values...
@@rdps_lov_qual_award_class_insert.sql

PROMPT [26/31] Inserting qualification award description lookup values...
@@rdps_lov_qual_award_desc_insert.sql

-- Application Data
PROMPT [27/31] Inserting user profiles...
@@rdps_user_profile_insert.sql

PROMPT [28/31] Inserting user roles...
@@rdps_user_role_insert.sql

PROMPT [29/31] Inserting system parameters...
@@rdps_parameter_insert.sql

PROMPT [30/31] Inserting email templates...
@@rdps_email_template_insert.sql

-- ============================================================================
-- STEP 9: Create Functions
-- ============================================================================
PROMPT
PROMPT Creating database functions...
PROMPT [31/31] Creating EXECUTE_EMAIL_JOB function...
@@rdps_execute_email_job_function.sql

PROMPT
PROMPT ============================================================================
PROMPT RDPS Database Installation Completed Successfully!
PROMPT ============================================================================
PROMPT
PROMPT Next Steps:
PROMPT 1. Verify all objects were created: SELECT object_name, object_type, status FROM user_objects;
PROMPT 2. Check for any invalid objects: SELECT object_name, object_type FROM user_objects WHERE status = 'INVALID';
PROMPT 3. Update application.properties with your database connection details
PROMPT 4. Start the Spring Boot application
PROMPT
PROMPT ============================================================================
