package eduhk.fhr.web.service.import_;

// File Path: src/main/java/eduhk/fhr/web/service/import_/ImportNotificationService.java
// Purpose: Send email notifications about import results

import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eduhk.fhr.web.dto.import_.ImportErrorDetail;
import eduhk.fhr.web.dto.import_.ImportResultSummary;
import eduhk.fhr.web.model.Email;
import eduhk.fhr.web.service.EmailService;
import eduhk.fhr.web.service.ParameterService;

/**
 * Import Notification Service
 *
 * Sends email notifications to administrators about import results.
 */
@Service
public class ImportNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(ImportNotificationService.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private ParameterService parameterService;

    /**
     * Send import completion notification email
     *
     * @param summary Import result summary
     */
    public void sendImportNotification(ImportResultSummary summary) {
        logger.info("Sending import notification email");

        try {
            String subject = buildEmailSubject(summary);
            String body = buildEmailBody(summary);
            String recipients = getNotificationRecipients();

            if (recipients == null || recipients.trim().isEmpty()) {
                logger.warn("No notification recipients configured. Skipping email notification.");
                return;
            }

            // Create Email object
            Email email = new Email();
            email.setRecipient(recipients);  // Can be comma-separated list
            email.setEmailSubject(subject);
            email.setEmailBody(body);
            email.setUserstamp("SYSTEM");

            // Send email via EmailService
            String result = emailService.sendEmail(email);
            logger.info("Email notification sent. Result: {}", result);

        } catch (Exception e) {
            logger.error("Error sending import notification email", e);
            // Don't throw exception - email failure shouldn't fail import
        }
    }

    /**
     * Build email subject based on import result
     *
     * @param summary Import result summary
     * @return Email subject string
     */
    private String buildEmailSubject(ImportResultSummary summary) {
        // TODO: Build appropriate subject based on success/failure counts

        if (summary.getFailedImports() == 0) {
            return "[RDPS] Import Completed Successfully";
        } else if (summary.getSuccessfulImports() == 0) {
            return "[RDPS] Import Failed";
        } else {
            return "[RDPS] Import Completed with Errors";
        }
    }

    /**
     * Build email body with import statistics
     *
     * @param summary Import result summary
     * @return Email body string
     */
    private String buildEmailBody(ImportResultSummary summary) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        StringBuilder body = new StringBuilder();
        body.append("<html><body>");
        body.append("<h2>RDPS TalentLink Import Summary</h2>");
        body.append("<table border='1' cellpadding='5' cellspacing='0'>");
        body.append("<tr><td><strong>Import Start Time:</strong></td><td>").append(sdf.format(summary.getImportStartTime())).append("</td></tr>");
        body.append("<tr><td><strong>Import End Time:</strong></td><td>").append(sdf.format(summary.getImportEndTime())).append("</td></tr>");

        // Calculate duration
        // long durationSec = summary.getDurationMillis() / 1000;
        // long minutes = durationSec / 60;
        // long seconds = durationSec % 60;
        // body.append("<tr><td><strong>Duration:</strong></td><td>").append(minutes).append(" min ").append(seconds).append(" sec</td></tr>");

        body.append("<tr><td><strong>Total Candidates Processed:</strong></td><td>").append(summary.getTotalCandidatesProcessed()).append("</td></tr>");
        body.append("<tr><td><strong>Successful Imports:</strong></td><td style='color:green;'><strong>").append(summary.getSuccessfulImports()).append("</strong></td></tr>");
        body.append("<tr><td><strong>Failed Imports:</strong></td><td style='color:red;'><strong>").append(summary.getFailedImports()).append("</strong></td></tr>");

        // if (summary.getLastImportedCandidateId() != null) {
        //     body.append("<tr><td><strong>Last Imported Candidate ID:</strong></td><td>").append(summary.getLastImportedCandidateId()).append("</td></tr>");
        // }

        body.append("</table>");

        // Add error details if any failures occurred
        if (summary.getFailedImports() > 0 && summary.getErrors() != null && !summary.getErrors().isEmpty()) {
            body.append("<h3>Error Details</h3>");
            body.append("<table border='1' cellpadding='5' cellspacing='0'>");
            body.append("<tr><th>Candidate ID</th><th>Requisition</th><th>Error Type</th><th>Error Message</th></tr>");

            int maxErrors = Math.min(10, summary.getErrors().size());  // Show max 10 errors
            for (int i = 0; i < maxErrors; i++) {
                ImportErrorDetail error = summary.getErrors().get(i);
                body.append("<tr>");
                body.append("<td>").append(error.getCandidateId()).append("</td>");
                body.append("<td>").append(error.getRequisitionNumber() != null ? error.getRequisitionNumber() : "N/A").append("</td>");
                body.append("<td>").append(error.getErrorType()).append("</td>");
                body.append("<td>").append(error.getErrorMessage()).append("</td>");
                body.append("</tr>");
            }

            if (summary.getErrors().size() > maxErrors) {
                body.append("<tr><td colspan='4'><em>... and ").append(summary.getErrors().size() - maxErrors).append(" more errors. Check logs for details.</em></td></tr>");
            }

            body.append("</table>");
        }

        body.append("<br/><p><em>This is an automated notification from RDPS Import Service.</em></p>");
        body.append("</body></html>");

        return body.toString();
    }

    /**
     * Get list of email recipients for notifications
     *
     * @return Comma-separated email addresses
     */
    private String getNotificationRecipients() {
        String recipients = parameterService.getParameterValue("ERROR_NOTIFICATION_EMAILS");

        if (recipients == null || recipients.trim().isEmpty()) {
            logger.warn("ERROR_NOTIFICATION_EMAILS parameter not configured");
            return null;
        }

        return recipients;
    }
}
