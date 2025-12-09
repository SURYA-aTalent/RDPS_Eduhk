-- Add TALENTLINK_DOCUMENT_SOAP_URL parameter
-- This parameter is required for the TalentLinkSOAPDocumentService to access
-- application-specific documents containing education, work experience, and referee data

-- Check if parameter already exists and delete it
DELETE FROM RDPS_PARAMETER
WHERE PARAM_CODE = 'TALENTLINK_DOCUMENT_SOAP_URL';

-- Insert the new parameter
INSERT INTO RDPS_PARAMETER (PARAM_CODE, PARAM_VALUE, ACTIVE, TIMESTAMP, USERSTAMP)
VALUES (
    'TALENTLINK_DOCUMENT_SOAP_URL',
    'https://api3.lumesse-talenthub.com/HRIS/SOAP/Document',
    'Y',
    SYSTIMESTAMP,
    'SYSTEM'
);

COMMIT;

-- Verify the parameter was added
SELECT PARAM_CODE, PARAM_VALUE, ACTIVE, TIMESTAMP
FROM RDPS_PARAMETER
WHERE PARAM_CODE = 'TALENTLINK_DOCUMENT_SOAP_URL';
