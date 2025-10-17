package eduhk.fhr.web.api.talentlink;

// File Path: src/main/java/eduhk/fhr/web/api/talentlink/TalentLinkApiException.java
// Purpose: Custom exception for TalentLink API errors

/**
 * TalentLink API Exception
 *
 * Custom exception thrown when TalentLink API calls fail.
 * Wraps HTTP errors, network issues, and API-specific errors.
 */
public class TalentLinkApiException extends Exception {

    private static final long serialVersionUID = 1L;

    private int statusCode;
    private String apiResponse;

    public TalentLinkApiException(String message) {
        super(message);
    }

    public TalentLinkApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public TalentLinkApiException(String message, int statusCode, String apiResponse) {
        super(message);
        this.statusCode = statusCode;
        this.apiResponse = apiResponse;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getApiResponse() {
        return apiResponse;
    }
}
