DROP TABLE RDPS.RDPS_USER_PROFILE CASCADE CONSTRAINTS;

CREATE TABLE RDPS.RDPS_USER_PROFILE
(
  CN                   VARCHAR2(100 CHAR),
  ROLE                 VARCHAR2(100 CHAR),
  EFFECTIVE_DATE_FROM  DATE                     DEFAULT TRUNC(SYSDATE),
  EFFECTIVE_DATE_TO    DATE,
  ACTIVE               VARCHAR2(1 CHAR)         DEFAULT 'Y'                   NOT NULL,
  TIMESTAMP            DATE                     DEFAULT SYSDATE,
  USERSTAMP            VARCHAR2(100 CHAR)       DEFAULT USER
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
