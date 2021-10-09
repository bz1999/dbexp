package dbexp.framework.experiment;

public class ExperimentException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2350968949378544064L;

	public ExperimentException() {
	}

	public ExperimentException(String someMessage) {
		super(someMessage);
	}

	public ExperimentException(Throwable someCause) {
		super(someCause);
	}

	public ExperimentException(String someMessage, Throwable someCause) {
		super(someMessage, someCause);
	}

}
