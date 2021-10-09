package dbexp.framework.datacollection;

public class RecorderException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9001990724784321429L;

	public RecorderException() {
	}

	public RecorderException(String someMessage) {
		super(someMessage);
	}

	public RecorderException(Throwable someCause) {
		super(someCause);
	}

	public RecorderException(String someMessage, Throwable someCause) {
		super(someMessage, someCause);
	}

}
