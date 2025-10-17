package eduhk.fhr.web.exception;

public class InvalidPaymentStatusException extends Exception {

	private static final long serialVersionUID = 1L;

	public InvalidPaymentStatusException() {
		super();
	}

	public InvalidPaymentStatusException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidPaymentStatusException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidPaymentStatusException(String message) {
		super(message);
	}

	public InvalidPaymentStatusException(Throwable cause) {
		super(cause);
	}

}
