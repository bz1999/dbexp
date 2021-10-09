package dbexp.framework.experiment;

import java.util.Properties;

import dbexp.framework.configuration.ConnectionConfiguration;
import dbexp.framework.datacollection.DataCollector;

public abstract class AbstractExperiment {
	
	protected static ConnectionConfiguration mConnectionConfiguration;
	
	public AbstractExperiment(
			ConnectionConfiguration aConnectionConfig) {
		
		mConnectionConfiguration = aConnectionConfig;	
		
	}
	
	public abstract String getName();
	
	public abstract void setUp();
	
	public abstract void setUp(Properties runtimeProperties);
	
	public abstract void tearDown();
	
	public abstract DataCollector getDataCollector();
	
	
}
