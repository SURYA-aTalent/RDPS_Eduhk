-- Create the EXECUTE_EMAIL_JOB function for sending emails
-- This function is called by the application to process email jobs

CREATE OR REPLACE FUNCTION RDPS.EXECUTE_EMAIL_JOB (
    V_EMAIL_ID_IN IN VARCHAR2,
    V_SEND_DATE_IN IN DATE,
    V_USERSTAMP_IN IN VARCHAR2
) RETURN VARCHAR2
IS
    v_result VARCHAR2(100);
    v_count NUMBER;
BEGIN
    -- Check if email job exists
    SELECT COUNT(*) INTO v_count
    FROM RDPS.RDPS_EMAIL_JOB
    WHERE EMAIL_ID = V_EMAIL_ID_IN;

    IF v_count = 0 THEN
        RETURN 'ERROR: Email job not found';
    END IF;

    -- Update the email job status to 'SENT'
    -- In a real implementation, this would integrate with an email server
    -- For now, we'll just mark it as sent
    UPDATE RDPS.RDPS_EMAIL_JOB
    SET STATUS = 'SENT',
        SENT_DATE = NVL(V_SEND_DATE_IN, SYSDATE),
        UPDATED_BY = V_USERSTAMP_IN,
        UPDATED_DATE = SYSDATE
    WHERE EMAIL_ID = V_EMAIL_ID_IN;

    IF SQL%ROWCOUNT > 0 THEN
        v_result := 'SUCCESS';
    ELSE
        v_result := 'ERROR: Update failed';
    END IF;

    COMMIT;

    RETURN v_result;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RETURN 'ERROR: ' || SQLERRM;
END EXECUTE_EMAIL_JOB;
/
