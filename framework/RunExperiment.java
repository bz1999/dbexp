/**
 * 
 */
package dbexp.framework;

import dbexp.framework.configuration.ConfigurationException;
import dbexp.framework.configuration.Configurator;
import dbexp.framework.configuration.ConnectionConfiguration;
import dbexp.framework.configuration.AbstractExperimentConfiguration;
import dbexp.framework.configuration.FrameworkConfiguration;
import dbexp.framework.datacollection.RecorderException;
import dbexp.framework.experiment.ExperimentDriver;
import dbexp.framework.experiment.ExperimentException;
import dbexp.framework.progressmonitor.LightWeightLogger;
import dbexp.framework.progressmonitor.Logger;

/**
 * @author Shirley Goldrei
 *
 */
public class RunExperiment {
	private static Logger LOG = new LightWeightLogger(RunExperiment.class.getName());
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//configure the experiments
		ExperimentDriver experimentDriver = null;
		try {
			AbstractExperimentConfiguration experimentConfiguration = Configurator.getConfiguration(args);

			ConnectionConfiguration connectionConfiguration = ConnectionConfiguration.getInstance(experimentConfiguration.getConnectionName());
			
			FrameworkConfiguration frameworkConfiguration = FrameworkConfiguration.getInstance();
			
			experimentDriver = new ExperimentDriver(
					Configurator.getExperimentClass(experimentConfiguration), 
					Configurator.getClientClass(experimentConfiguration), 
					experimentConfiguration, 
					connectionConfiguration,
					frameworkConfiguration);
		} catch (ConfigurationException e1) {
			LOG.error(e1);
			System.exit(-1);
		}
		
		try {
			experimentDriver.executeExperiments();
		} catch (InterruptedException e) {
			LOG.error(e);
			LOG.error(e.getStackTrace().toString());
		} catch (ExperimentException e) {
			LOG.error(e);
			LOG.error(e.getStackTrace().toString());
		} catch (RecorderException e) {
			LOG.error(e);
			LOG.error(e.getStackTrace().toString());
		}
	}

}
