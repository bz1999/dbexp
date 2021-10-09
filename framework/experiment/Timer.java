package dbexp.framework.experiment;

/**
 * This class implements a timer for measuring the elapsed time of an experiment,
 * using the most precise mechanisms available.
 * <p>
 * The methods of this class are useful during setting up or tearing
 * down experiments.
 * <p>
 * WARNING: The methods of this should *NOT* be called inside 
 * threaded performance evaluation code (i.e. during the "MEASUREMENT" phase of
 * thread execution) as methods of this class could use synchronization and/or 
 * shared/static objects and also log to i/o.
 * 
 * @author Shirley Goldrei
 *
 */
public class Timer {
	
	/**
	 * Use the return value only for calculating elapsed time with nano-second precision (but not necessarily
	 * nano-second accuracy)
	 * @return the number of nano-seconds from a fixed time either in the past or future (i.e. may be negative)
	 */
	public static long getNanoTime() {
		return System.nanoTime();
	}
}
