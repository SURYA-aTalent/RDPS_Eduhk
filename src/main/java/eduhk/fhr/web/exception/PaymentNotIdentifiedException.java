package eduhk.fhr.web.exception;

public class PaymentNotIdentifiedException extends Exception {

	private static final long serialVersionUID = 1L;

	public PaymentNotIdentifiedException() {
		super();
	}

	public PaymentNotIdentifiedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public PaymentNotIdentifiedException(String message, Throwable cause) {
		super(message, cause);
	}

	public PaymentNotIdentifiedException(String message) {
		super(message);
	}

	public PaymentNotIdentifiedException(Throwable cause) {
		super(cause);
	}

}
