DROP TABLE RDPS.RDPS_TALENTLINK_USER_STAGING CASCADE CONSTRAINTS;

CREATE TABLE RDPS.RDPS_TALENTLINK_USER_STAGING
(
  USER_ID               INTEGER,
  LAST_NAME             VARCHAR2(200 CHAR)        NOT NULL,
  FIRST_NAME            VARCHAR2(200 CHAR)        NOT NULL,
  EMAIL                 VARCHAR2(200 CHAR)        NOT NULL,
  USER_TYPE             VARCHAR2(50 CHAR)         DEFAULT 'Advanced User',
  LOGIN_USERNAME        VARCHAR2(200 CHAR)        NOT NULL,
  PASSWORD              VARCHAR2(200 CHAR),
  ROLE                  VARCHAR2(100 CHAR),
  LANGUAGE              VARCHAR2(50 CHAR)         DEFAULT 'English (UK)',
  WEEK_START_ON         VARCHAR2(20 CHAR)         DEFAULT 'Sunday',
  BRANDING              VARCHAR2(100 CHAR)        DEFAULT 'Company Default',
  TIMEZONE              VARCHAR2(100 CHAR)        DEFAULT 'Asia/Hong Kong',
  STATUS                VARCHAR2(20 CHAR)         DEFAULT 'Active',
  ACTIVATION_TYPE       VARCHAR2(100 CHAR)        DEFAULT 'Activate user now without sending email notification',
  SYNCED_TO_TALENTLINK  VARCHAR2(1 CHAR)          DEFAULT 'N',
  SYNC_DATE             DATE,
  SYNC_LOG              VARCHAR2(4000 CHAR),
  CREATED_BY            VARCHAR2(100 CHAR)        DEFAULT user,
  CREATION_DATE         DATE                      DEFAULT sysdate,
  USERSTAMP             VARCHAR2(100 CHAR)        DEFAULT user,
  TIMESTAMP             DATE                      DEFAULT SYSDATE
)
TABLESPACE RDPS_DATA
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


CREATE UNIQUE INDEX RDPS.RDPS_TALENTLINK_USER_STAGING_PK ON RDPS.RDPS_TALENTLINK_USER_STAGING
(USER_ID)
LOGGING
TABLESPACE RDPS_DATA
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           );

ALTER TABLE RDPS.RDPS_TALENTLINK_USER_STAGING ADD (
  CONSTRAINT RDPS_TALENTLINK_USER_STAGING_PK
  PRIMARY KEY
  (USER_ID)
  USING INDEX RDPS.RDPS_TALENTLINK_USER_STAGING_PK
  ENABLE VALIDATE);


CREATE OR REPLACE TRIGGER RDPS.RDPS_TALENTLINK_USER_STAGING_AUTO_PK
    BEFORE INSERT
    ON RDPS.RDPS_TALENTLINK_USER_STAGING
    FOR EACH ROW
DECLARE
    v_next_id INTEGER;
BEGIN
    IF :NEW.user_id IS NULL THEN
        SELECT NVL(MAX(user_id), 0)+1
        INTO v_next_id
        FROM RDPS_TALENTLINK_USER_STAGING;

        :NEW.user_id := v_next_id;
    END IF;
END;
/

-- Insert sample data
INSERT INTO RDPS.RDPS_TALENTLINK_USER_STAGING (
    LAST_NAME, FIRST_NAME, EMAIL, USER_TYPE, LOGIN_USERNAME, PASSWORD, ROLE,
    LANGUAGE, WEEK_START_ON, BRANDING, TIMEZONE, STATUS, ACTIVATION_TYPE
) VALUES (
    'Chan', 'Tai Man', 'taiman.chan@atalent.com', 'Advanced User',
    'taiman.chan@atalent.com', NULL, NULL,
    'English (UK)', 'Sunday', 'Company Default', 'Asia/Hong Kong',
    'Active', 'Activate user now without sending email notification'
);

INSERT INTO RDPS.RDPS_TALENTLINK_USER_STAGING (
    LAST_NAME, FIRST_NAME, EMAIL, USER_TYPE, LOGIN_USERNAME, PASSWORD, ROLE,
    LANGUAGE, WEEK_START_ON, BRANDING, TIMEZONE, STATUS, ACTIVATION_TYPE
) VALUES (
    'Chan', 'Peter', 'peter.chan@test.com', 'Advanced User',
    'peter.chan@test.com', NULL, NULL,
    'English (UK)', 'Sunday', 'Company Default', 'Asia/Hong Kong',
    'Inactive', 'Activate user now without sending email notification'
);

COMMIT;
