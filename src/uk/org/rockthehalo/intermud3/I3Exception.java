package uk.org.rockthehalo.intermud3;

public class I3Exception extends Exception {
	private static final long serialVersionUID = 1L;

	public I3Exception() {
		super();
	}

	public I3Exception(final String message) {
		super(message);
	}

	public I3Exception(final Throwable cause) {
		super(cause);
	}

	public I3Exception(final String message, final Throwable cause) {
		super(message, cause);
	}

	public I3Exception(final String message, final Throwable cause, final boolean enableSuppression,
			final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
