package dbexp.framework.configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * This class is used to hold the parameters which provide version information about the framework
 * @author Shirley Goldrei
 *
 */
public class FrameworkConfiguration {
	//TODO Load this file from the class path?
	private static final String propertiesFileName = "config/version.properties";
	private static Properties frameworkProperties = null;
	
	private enum Props {
		MAJOR ("experiment.version.major"),
		MINOR ("experiment.version.minor"),
		BUILD ("experiment.version.build"),
		COMMENT("experiment.version.comment");
		
		String mPropsKey;
		Props(String key) {
			mPropsKey = key;
		}
		@Override
		public String toString() {
			return mPropsKey;
		}
		
	}

	private String mMajor;
	private String mMinor;
	private String mBuild;
	private String mComment;

	private FrameworkConfiguration(){}

	
	public synchronized static FrameworkConfiguration getInstance() throws ConfigurationException {

		if (frameworkProperties == null) {
			loadFrameworkConfigurations();
		}	
		
		FrameworkConfiguration config = new FrameworkConfiguration();
		loadInstance(config);
		return config;
	}

	private static void loadFrameworkConfigurations() throws ConfigurationException {
		frameworkProperties = new Properties();
		try {
			frameworkProperties.load(new FileInputStream(propertiesFileName));
		} catch (FileNotFoundException e) {
			throw new ConfigurationException("Could not find settings file "+ propertiesFileName);
		} catch (IOException e) {
			throw new ConfigurationException("Error reading settings file "+ propertiesFileName);
		}
	}
	
	private static void loadInstance(FrameworkConfiguration config) throws ConfigurationException {
		config.mMajor = frameworkProperties.getProperty(Props.MAJOR.toString());
		config.mMinor = frameworkProperties.getProperty(Props.MINOR.toString());
		config.mBuild = frameworkProperties.getProperty(Props.BUILD.toString());
		config.mComment = frameworkProperties.getProperty(Props.COMMENT.toString());
	}

	public String getMajorVersion() {
		return mMajor;
	}
	
	public String getMinorVersion() {
		return mMinor;
	}


	public String getBuildVersion() {
		return mBuild;
	}

	public Properties toProperties() {
		Properties properties = new Properties();
		properties.setProperty(Props.MAJOR.toString(), mMajor);
		properties.setProperty(Props.MINOR.toString(), mMinor.toString());
		properties.setProperty(Props.BUILD.toString(), mBuild);
		if (mComment!= null) properties.setProperty(Props.COMMENT.toString(), mComment);
		
		return properties;
		
	}

}
