package dbexp.framework.db;

public class UnimplementedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4891547477627815964L;

	public UnimplementedException() {
		super();
	}

	public UnimplementedException(String someMessage, Throwable someCause) {
		super(someMessage, someCause);
	}

	public UnimplementedException(String someMessage) {
		super(someMessage);
	}

	public UnimplementedException(Throwable someCause) {
		super(someCause);
	}

}
