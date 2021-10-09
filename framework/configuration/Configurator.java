package dbexp.framework.configuration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import dbexp.framework.configuration.AbstractExperimentConfiguration.Keys;
import dbexp.framework.experiment.AbstractClient;
import dbexp.framework.experiment.AbstractExperiment;

import dbexp.framework.progressmonitor.Logger;
import dbexp.framework.progressmonitor.LightWeightLogger;


public class Configurator {
	
	private static Logger LOG = new LightWeightLogger(Configurator.class.getName());
	
	private static AbstractExperimentConfiguration config(String propertiesFileName)  throws ConfigurationException {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(propertiesFileName));
		} catch (FileNotFoundException e) {
			throw new ConfigurationException("Could not find settings file "+ propertiesFileName);
		} catch (IOException e) {
			throw new ConfigurationException("Error reading settings file "+ propertiesFileName);
		}
		
		Class<AbstractExperimentConfiguration> configurationClass = getExperimentConfigurationClass(properties.getProperty(
								AbstractExperimentConfiguration.getExperimentConfigurationClassPropertyKey()));
		return constructExperimentConfiguration(configurationClass, properties);
		
	}
	
	private static AbstractExperimentConfiguration constructExperimentConfiguration(Class<AbstractExperimentConfiguration> configurationClass, Properties properties) throws ConfigurationException {
		Constructor<? extends AbstractExperimentConfiguration> configurationConstructor;
		try {
			configurationConstructor = configurationClass.getConstructor(
					 new Class[] {Properties.class});
		} catch (SecurityException e) {
			throw new ConfigurationException(e);
		} catch (NoSuchMethodException e) {
			throw new ConfigurationException(e);
		}
		
		try {      
			return configurationConstructor.newInstance(new Object[] {properties});		
		 } catch (IllegalArgumentException e) {
			 throw new ConfigurationException(e);
		 } catch (InstantiationException e) {
			throw new ConfigurationException(e);
		 } catch (IllegalAccessException e) {
			throw new ConfigurationException(e);
		 } catch (InvocationTargetException e) {
			throw new ConfigurationException(e);
		 }
	}
	
	public static AbstractExperimentConfiguration getConfiguration(String[]args) throws ConfigurationException {

		if (args.length == 1) {
			return config(args[0]);
		} else {
			throw new ConfigurationException("Usage: java RunExperiment propertiesFileName");
		}

	}
	
	@SuppressWarnings("unchecked")
	public static Class<AbstractExperiment> getExperimentClass(AbstractExperimentConfiguration experimentConfiguration) throws ConfigurationException {
		//convert the concrete experiment and client type names to a class objects
		Class<AbstractExperiment> experimentClass = null;
		try {
			experimentClass = (Class<AbstractExperiment>) Class.forName(experimentConfiguration.getExperimentClassName());
		} catch (ClassNotFoundException e) {
			throw new ConfigurationException(e);
		} 
		return experimentClass;
	}
	
	@SuppressWarnings("unchecked")
	public static Class<AbstractClient> getClientClass(AbstractExperimentConfiguration experimentConfiguration) throws ConfigurationException { 
		Class<AbstractClient> clientClass = null;
		try {
			clientClass = (Class<AbstractClient>) Class.forName(experimentConfiguration.getClientClassName());
		} catch (ClassNotFoundException e) {
			throw new ConfigurationException(e);
		}
		return clientClass;
	}
	
	@SuppressWarnings("unchecked")
	private static Class<AbstractExperimentConfiguration> getExperimentConfigurationClass(String experimentConfigurationClassName) throws ConfigurationException { 
		
		Class<AbstractExperimentConfiguration> configurationClass = null;
		try {
			configurationClass = (Class<AbstractExperimentConfiguration>) Class.forName(experimentConfigurationClassName);
		} catch (ClassNotFoundException e) {
			throw new ConfigurationException(e);
		}
		return configurationClass;
	}
}
