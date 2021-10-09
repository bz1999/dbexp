package dbexp.framework.datastatistics;

import java.util.ArrayList;
import java.util.List;

import dbexp.framework.configuration.AbstractExperimentConfiguration;
import dbexp.framework.datacollection.DataCollector;

public abstract class AbstractDataStatistics {

	protected AbstractExperimentConfiguration mExperimentConfiguration;
	
	protected List<DataCollector> mResultsDC;
	
	public AbstractDataStatistics(AbstractExperimentConfiguration experimentConfiguraion) {
		
		mExperimentConfiguration = experimentConfiguraion;
		
		mResultsDC = new ArrayList<DataCollector>();
		
	}
	
	public abstract void setUp();
	
	public abstract void tearDown();
	
	//abstract void populateResults();

	public abstract List<DataCollector> getResults(DataCollector...sourceData);
	
}
