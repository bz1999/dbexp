package dbexp.framework.datacollection;

import java.util.List;

import dbexp.framework.configuration.ConnectionConfiguration;
import dbexp.framework.configuration.AbstractExperimentConfiguration;
import dbexp.framework.configuration.FrameworkConfiguration;

public interface DataRecorder {
	
	//identify recorder
	public void setName(String name);
	
	public void recordConnection(ConnectionConfiguration aConnectionConfiguration) throws RecorderException;
	
	public void recordExperiment(FrameworkConfiguration aFrameworkConfiguration, AbstractExperimentConfiguration anExperimentConfiguration) throws RecorderException;
	
	public void recordData(DataCollector aDataCollector) throws RecorderException;
	
	public void recordAllData(List<DataCollector> someDataCollectors) throws RecorderException;

	public void save();

}
