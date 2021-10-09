package dbexp.framework.configuration;

public class ConfigurationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6932409358327038731L;

	public ConfigurationException() {
	}

	public ConfigurationException(String someMessage) {
		super(someMessage);
	}

	public ConfigurationException(Throwable someCause) {
		super(someCause);
	}

	public ConfigurationException(String someMessage, Throwable someCause) {
		super(someMessage, someCause);
	}

}
