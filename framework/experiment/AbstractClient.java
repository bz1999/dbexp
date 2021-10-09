package dbexp.framework.experiment;

import java.util.Properties;

import dbexp.framework.configuration.ConnectionConfiguration;
import dbexp.framework.datacollection.DataCollector;

public abstract class AbstractClient extends Thread {
	
	protected static BenchmarkState state = BenchmarkState.WAIT;
	protected ConnectionConfiguration mConnectionConfiguration;
	protected int mThreadID;
	
	public AbstractClient(
			ConnectionConfiguration aConnectionConfig,
			int aThreadID) {
		mConnectionConfiguration = aConnectionConfig;
		mThreadID = aThreadID;		
	}
	
	public abstract void setUp();
	
	public abstract void setUp(Properties runtimeProperties);
	
	public abstract void tearDown();
	
	public void run() {
		runClient();
	}
	
	public abstract DataCollector getDataCollector();
	public abstract void runClient();

}
