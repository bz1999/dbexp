package dbexp.framework.progressmonitor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import dbexp.framework.configuration.ConfigurationException;

/**
 * @author Shirley Goldrei
 *
 */
public class LightWeightLogger implements Logger {
	private static final String propertiesFileName = "config/logging.properties";
	
	private final LogLevel mLogLevel;
	private final String mName;


	
	public LightWeightLogger( String aName ) {
		mName = aName;
		mLogLevel = setLogLevel();
	}
	
	private LogLevel setLogLevel() {
		Properties loggingProperties = 	new Properties();		
		try {
			try {
				loggingProperties.load(new FileInputStream(propertiesFileName));
			} catch (FileNotFoundException e) {
				throw new ConfigurationException("Could not find settings file "+ propertiesFileName);
			} catch (IOException e) {
				throw new ConfigurationException("Error reading settings file "+ propertiesFileName);
			}
		} catch (ConfigurationException e) {
			System.out.println("ERROR " + mName + ": Could not initialise LightWeight Logging. Logging has been disabled");
			e.printStackTrace();
			return LogLevel.OFF;
		}
		return LogLevel.valueOf(loggingProperties.getProperty("loglevel"));
	}
	
	private boolean checkLevel(LogLevel someError) {
		return (mLogLevel.compareTo(someError) >= 0);
	}
	
	public void error(String aMessage) {
		if (checkLevel(LogLevel.ERROR)) {
		 System.out.println("ERROR " + mName + ": " + aMessage);
		}
	}

	public void error(Exception someE) {
		if (checkLevel(LogLevel.ERROR)) {
			System.out.println("ERROR " +mName);	
			someE.printStackTrace(System.out);
		}
	}

	public void debug(String aMessage) {
		if (checkLevel(LogLevel.DEBUG)) {
			System.out.println("DEBUG " + mName + ": " + aMessage);
		}
	}

	public void debug(Exception someE) {
		if (checkLevel(LogLevel.DEBUG)) {
			System.out.println("DEBUG " +mName );	
			someE.printStackTrace(System.out);
		}
	}

	public void info(String aMessage) {
		if (checkLevel(LogLevel.INFO)) {
			System.out.println("INFO " + mName + ": " + aMessage);
		}
	}

	public void info(Exception someE) {
		if (checkLevel(LogLevel.INFO)) {
			System.out.println("INFO " +mName );	
			someE.printStackTrace(System.out);
		}
	}
	
}
