package dbexp.framework.db;

/**
 * @author Shirley Goldrei
 *
 */
public class ConnectionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1785872231083429270L;

	public ConnectionException() {
	}

	public ConnectionException(String someMessage) {
		super(someMessage);
	}

	public ConnectionException(Throwable someCause) {
		super(someCause);
	}

	public ConnectionException(String someMessage, Throwable someCause) {
		super(someMessage, someCause);
	}

}
