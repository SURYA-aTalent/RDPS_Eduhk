--------------------------------------------------------
-- Schema Migration Script - Align Local with Production
-- Created: 2025-12-02
-- Purpose: Update local database schema to match production
--------------------------------------------------------

-- 1. Add missing END_DATE column to RDPS_EDU_PROF_QUAL
ALTER TABLE RDPS_EDU_PROF_QUAL ADD END_DATE DATE;

-- 2. Rename columns in RDPS_CANDIDATE to match production
ALTER TABLE RDPS_CANDIDATE RENAME COLUMN PASSPORT_NO TO PASSPORT;
ALTER TABLE RDPS_CANDIDATE RENAME COLUMN ADDRESS_LINE1 TO ADDR_LINE1;
ALTER TABLE RDPS_CANDIDATE RENAME COLUMN ADDRESS_LINE2 TO ADDR_LINE2;
ALTER TABLE RDPS_CANDIDATE RENAME COLUMN PHONE_NUMBER TO PHONE_NO;

COMMIT;

PROMPT Schema migration completed successfully!
