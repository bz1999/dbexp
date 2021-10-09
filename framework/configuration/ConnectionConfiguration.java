package dbexp.framework.configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import dbexp.framework.db.DBMSType;


/**
 * This class is used to hold the parameters needed to define a db connection
 * @author Shirley Goldrei
 *
 */
public class ConnectionConfiguration {
	//TODO Load this file from the class path?
	private static final String propertiesFileName = "config/databaseConnections.properties";
	private static Properties connectionProperties = null;
	
	public enum Props {
		LABEL ("label"),
		USER_NAME ("userName"),
		PASSWORD ("password"),
		CONNECTION_URL ("connectionURL"),
		DRIVER_NAME ("driverName"),
		DBMS_TYPE ("dbmsType"),
		DATABASE_NAME ("databaseName"),
		COMMENT("comment");
		
		String mPropsKey;
		Props(String key) {
			mPropsKey = key;
		}
		@Override
		public String toString() {
			return mPropsKey;
		}
		
	}

	private String mLabel;
	private String mUserName;
	private String mPassword;
	private String mConnectionURL;
	private String mDriverName;
	private DBMSType mDBMSType;
	private String mDatabaseName;
	private String mComment;

	private ConnectionConfiguration(){}

	
	public synchronized static ConnectionConfiguration getInstance(String label) throws ConfigurationException {

		if (connectionProperties == null) {
			loadDatabaseConnectionConfigurations();
		}	
		
		ConnectionConfiguration config = new ConnectionConfiguration();
		loadInstance(config, label);
		return config;
	}

	private static void loadDatabaseConnectionConfigurations() throws ConfigurationException {
		connectionProperties = new Properties();
		try {
			connectionProperties.load(new FileInputStream(propertiesFileName));
		} catch (FileNotFoundException e) {
			throw new ConfigurationException("Could not find settings file "+ propertiesFileName);
		} catch (IOException e) {
			throw new ConfigurationException("Error reading settings file "+ propertiesFileName);
		}
	}
	
	private static void loadInstance(ConnectionConfiguration config, String label) throws ConfigurationException {
		config.mLabel = label;
		config.mUserName = connectionProperties.getProperty(label + "." + Props.USER_NAME.toString());
		config.mPassword = connectionProperties.getProperty(label + "." + Props.PASSWORD.toString());
		config.mConnectionURL = connectionProperties.getProperty(label + "." + Props.CONNECTION_URL.toString());
		config.mDriverName = connectionProperties.getProperty(label + "." + Props.DRIVER_NAME.toString());
		config.mDBMSType = dbmsTypeFromName(connectionProperties.getProperty(label + "." + Props.DBMS_TYPE.toString()));
		config.mDatabaseName = connectionProperties.getProperty(label + "." + Props.DATABASE_NAME.toString());
		config.mComment = connectionProperties.getProperty(label + "." + Props.COMMENT.toString());
	}

	private static DBMSType dbmsTypeFromName(String someProperty) throws ConfigurationException {
		DBMSType dbms = null;
		for (DBMSType type : DBMSType.values()) {
			if (type.toString().equals(someProperty)) {
				dbms = type;
				return dbms;
			}
		}
		
		if (dbms == null) {
			throw new ConfigurationException("Unknown DBMS Type " + someProperty);
		}
		//should never get here:
		return null;
	}


	public String getLabel() {
		return mLabel;
	}
	
	public String getUserName() {
		return mUserName;
	}


	public String getPassword() {
		return mPassword;
	}


	public String getConnectionURL() {
		return mConnectionURL;
	}


	public String getDriverName() {
		return mDriverName;
	}


	public DBMSType getDBMSType() {
		return mDBMSType;
	}
	
	public String getDatabaseName() {
		return mDatabaseName;
	}

	public void setDatabaseName(String name) {
		mDatabaseName = name;
	}

	public Properties toProperties() {
		Properties properties = new Properties();
		properties.setProperty(Props.CONNECTION_URL.toString(), mConnectionURL);
		properties.setProperty(Props.DBMS_TYPE.toString(), mDBMSType.toString());
		properties.setProperty(Props.DRIVER_NAME.toString(), mDriverName);
		properties.setProperty(Props.LABEL.toString(), mLabel);
		properties.setProperty(Props.PASSWORD.toString(), mPassword);
		properties.setProperty(Props.USER_NAME.toString(), mUserName);
		properties.setProperty(Props.DATABASE_NAME.toString(), mDatabaseName);
		if (mComment != null) properties.setProperty(Props.COMMENT.toString(), mComment);
		
		return properties;
		
	}

}
