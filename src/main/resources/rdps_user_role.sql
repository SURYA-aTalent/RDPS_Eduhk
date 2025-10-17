DROP TABLE RDPS.RDPS_USER_ROLE CASCADE CONSTRAINTS;

CREATE TABLE RDPS.RDPS_USER_ROLE
(
  ROLE       VARCHAR2(100 CHAR),
  FCN        VARCHAR2(100 CHAR),
  ACTIVE     VARCHAR2(1 CHAR)                   DEFAULT 'Y'                   NOT NULL,
  TIMESTAMP  DATE                               DEFAULT SYSDATE,
  USERSTAMP  VARCHAR2(100 CHAR)                 DEFAULT USER
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
