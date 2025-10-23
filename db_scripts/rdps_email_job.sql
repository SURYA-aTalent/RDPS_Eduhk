-- RDPS_EMAIL_JOB table for email queue management
-- Updated to match current schema with SEND_STATUS instead of EMAIL_SENT

DROP TABLE RDPS.RDPS_EMAIL_JOB CASCADE CONSTRAINTS;

DROP SEQUENCE RDPS.RDPS_EMAIL_JOB_SEQ;

CREATE TABLE RDPS.RDPS_EMAIL_JOB
(
  EMAIL_ID       NUMBER                             NOT NULL,
  RECIPIENT      VARCHAR2(500 BYTE)                 NOT NULL,
  CC_TO          VARCHAR2(500 BYTE),
  BCC_TO         VARCHAR2(500 BYTE),
  EMAIL_SUBJECT  VARCHAR2(500 BYTE)                 NOT NULL,
  EMAIL_BODY     CLOB                               NOT NULL,
  SEND_STATUS    VARCHAR2(20 BYTE)                  DEFAULT 'PENDING',
  SEND_DATE      DATE,
  ERROR_MESSAGE  VARCHAR2(4000 BYTE),
  RETRY_COUNT    NUMBER                             DEFAULT 0,
  CREATED_BY     VARCHAR2(32 BYTE)                  DEFAULT USER NOT NULL,
  CREATION_DATE  DATE                               DEFAULT SYSDATE NOT NULL,
  USERSTAMP      VARCHAR2(32 BYTE)                  DEFAULT USER NOT NULL,
  TIMESTAMP      DATE                               DEFAULT SYSDATE NOT NULL,
  CONSTRAINT PK_RDPS_EMAIL_JOB PRIMARY KEY (EMAIL_ID)
)
TABLESPACE USERS
PCTFREE    10
INITRANS   1
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           )
LOGGING
NOCOMPRESS
NOCACHE;

-- Create sequence for EMAIL_ID
CREATE SEQUENCE RDPS.RDPS_EMAIL_JOB_SEQ
  START WITH 1
  INCREMENT BY 1
  NOCACHE
  NOCYCLE;

-- Create trigger for auto-incrementing EMAIL_ID (alternative to sequence)
-- Note: The EmailDao uses MAX(email_id)+1, so this trigger is optional
CREATE OR REPLACE TRIGGER RDPS.RDPS_EMAIL_JOB_AUTO_PK
    BEFORE INSERT
    ON RDPS.RDPS_EMAIL_JOB
    FOR EACH ROW
DECLARE
    v_next_id NUMBER;
BEGIN
    IF :NEW.email_id IS NULL THEN
        SELECT NVL(MAX(email_id), 0)+1
        INTO v_next_id
        FROM RDPS.RDPS_EMAIL_JOB;

        :NEW.email_id := v_next_id;
    END IF;
END;
/

COMMIT;
