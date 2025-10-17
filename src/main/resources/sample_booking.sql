DROP TABLE RDPS.SAMPLE_BOOKING CASCADE CONSTRAINTS;

CREATE TABLE RDPS.SAMPLE_BOOKING
(
  BOOKING_ID              INTEGER,
  STAFF_ID                VARCHAR2(8 CHAR)      NOT NULL,
  STAFF_NAME              VARCHAR2(1000 CHAR),
  EMAIL_ADDRESS           VARCHAR2(1000 CHAR),
  ACK_EMAIL_SENT_DATE     DATE,
  CANCEL_EMAIL_SENT_DATE  DATE,
  CLIENT_IP               VARCHAR2(100 CHAR),
  CLIENT_INFO             VARCHAR2(1000 CHAR),
  ACTIVE                  INTEGER               DEFAULT 1                     NOT NULL,
  CREATED_BY              VARCHAR2(100 CHAR)    DEFAULT USER,
  CREATION_DATE           DATE                  DEFAULT SYSDATE,
  USERSTAMP               VARCHAR2(100 CHAR)    DEFAULT USER,
  TIMESTAMP               DATE                  DEFAULT SYSDATE
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


CREATE UNIQUE INDEX RDPS.SAMPLE_BOOKING_PK ON RDPS.SAMPLE_BOOKING
(BOOKING_ID)
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

ALTER TABLE RDPS.SAMPLE_BOOKING ADD (
  CONSTRAINT SAMPLE_BOOKING_PK
  PRIMARY KEY
  (BOOKING_ID)
  USING INDEX RDPS.SAMPLE_BOOKING_PK
  ENABLE VALIDATE);


CREATE OR REPLACE TRIGGER rdps.sample_booking_auto_pk
    BEFORE INSERT
    ON rdps.sample_booking
    FOR EACH ROW
DECLARE
    v_next_id INTEGER;
BEGIN
    IF :NEW.booking_id IS NULL THEN
        SELECT NVL(MAX(booking_id), 0)+1
        INTO v_next_id
        FROM sample_booking;
        
        :NEW.booking_id := v_next_id;
    END IF;
END;

--All booking view
/
