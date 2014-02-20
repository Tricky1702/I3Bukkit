package uk.org.rockthehalo.intermud3;

public class I3Exception extends Exception {
	private static final long serialVersionUID = 1L;

	public I3Exception() {
		super();
	}

	public I3Exception(String message) {
		super(message);
	}

	public I3Exception(Throwable cause) {
		super(cause);
	}

	public I3Exception(String message, Throwable cause) {
		super(message, cause);
	}

	public I3Exception(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
