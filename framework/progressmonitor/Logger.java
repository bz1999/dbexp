package dbexp.framework.progressmonitor;

/**
 * @author Shirley Goldrei
 *
 */
public interface Logger {
	public void error( String aMessage );
	public void error( Exception e );
	public void info( String aMessage );
	public void info( Exception e );
	public void debug( String aMessage );
	public void debug( Exception e );
}
