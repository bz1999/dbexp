package dbexp.framework.experiment;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;

import dbexp.framework.configuration.AbstractExperimentConfiguration;
import dbexp.framework.configuration.ConnectionConfiguration;
import dbexp.framework.configuration.FrameworkConfiguration;
import dbexp.framework.datacollection.DataCollector;
import dbexp.framework.datacollection.DataRecorder;
import dbexp.framework.datacollection.ExcelDataRecorder;
import dbexp.framework.datacollection.RecorderException;
import dbexp.framework.datacollection.Titles;
import dbexp.framework.datastatistics.AbstractDataStatistics;
import dbexp.framework.progressmonitor.LightWeightLogger;
import dbexp.framework.progressmonitor.Logger;

public class ExperimentDriver {

	private static final Logger LOG = new LightWeightLogger(ExperimentDriver.class.getName());
	private AbstractClient[] mThreads;
	private final AbstractExperimentConfiguration mExperimentConfiguration;
	private final ConnectionConfiguration mConnectionConfiguration;
	private final FrameworkConfiguration mFrameworkConfiguration;
	private AbstractExperiment mAnExperiment;
	private DataRecorder mDataRecorder;
	private DataCollector mRuntimePropertiesDC;
	private DataCollector mAllThreadsDC;
	
	private AbstractDataStatistics mDataStatistics;
	
	/**
	 * must be a subclass of AbstractExperiment
	 */
	private final Class<? extends AbstractExperiment> mExperimentClassType;
	/**
	 * must be a subclass of AbstractClient
	 */
	private final Class<? extends AbstractClient> mClientClassType;
	
	private int mTotalExperiment;

	public ExperimentDriver(Class<? extends AbstractExperiment> anExperimentClassType,
			Class<? extends AbstractClient> aClientClassType,
			AbstractExperimentConfiguration anExperimentConfiguration,
			ConnectionConfiguration aConnectionConfiguration, FrameworkConfiguration aFrameworkConfiguration) {
		
		 mExperimentClassType = anExperimentClassType;
		 mClientClassType = aClientClassType;
		 mExperimentConfiguration = anExperimentConfiguration;
		 mConnectionConfiguration = aConnectionConfiguration;
		 mFrameworkConfiguration = aFrameworkConfiguration;

	}
	
	/**
	 * Create an instance of the Experiment class specified in the experiment configuration file
	 * @throws ExperimentException
	 */
	private void constructExperiment() throws ExperimentException {
		Constructor<? extends AbstractExperiment> experimentConstructor;
		try {
			experimentConstructor = mExperimentClassType.getConstructor(
					 new Class[] {AbstractExperimentConfiguration.class, ConnectionConfiguration.class});
		} catch (SecurityException e) {
			throw new ExperimentException(e);
		} catch (NoSuchMethodException e) {
			throw new ExperimentException(e);
		}
		
		try {      
			mAnExperiment = experimentConstructor.newInstance(new Object[] {mExperimentConfiguration, mConnectionConfiguration});		
		 } catch (IllegalArgumentException e) {
			 throw new ExperimentException(e);
		 } catch (InstantiationException e) {
			throw new ExperimentException(e);
		 } catch (IllegalAccessException e) {
			throw new ExperimentException(e);
		 } catch (InvocationTargetException e) {
			throw new ExperimentException(e);
		 }
	}
	
	private void destroyExperiment() {
		mAnExperiment = null;
	}
	
	/**
	 * Create mpl number of client classes as specified in the experiment configuration file
	 * @throws ExperimentException
	 */
	private void constructClients(int amount) throws ExperimentException {
		Constructor<? extends AbstractClient> clientConstructor;
		try {
			clientConstructor = mClientClassType.getConstructor(
					 new Class[] {AbstractExperimentConfiguration.class, ConnectionConfiguration.class, int.class});
		} catch (SecurityException e) {
			throw new ExperimentException(e);
		} catch (NoSuchMethodException e) {
			throw new ExperimentException(e);
		}
		
		try {
			mThreads = (AbstractClient[]) Array.newInstance(mClientClassType, amount);
			for (int i = 0; i < amount; i++) {
				mThreads[i] = clientConstructor.newInstance(new Object[] {mExperimentConfiguration, mConnectionConfiguration, i});		
		    }
		} 
		catch (IllegalArgumentException e) {
			throw new ExperimentException(e);
		}
		catch (InstantiationException e) {
			throw new ExperimentException(e);
		}
		catch (IllegalAccessException e) {
			throw new ExperimentException(e);
		}
		catch (InvocationTargetException e) {
			throw new ExperimentException(e);
		}
	}
	
	private void destroyClients(int amount) {
		for (int i = 0; i < amount; i++) {
            mThreads[i] = null;
        }
	}
	
	private void executeClients(int expNo, int run, int mpl, Properties properties) throws InterruptedException, ExperimentException, RecorderException {
		
		LOG.info("Setup " + mpl + " clients.");
		
        AbstractClient.state = BenchmarkState.WAIT;
		
        //*************SET UP***********************
        for (int i = 0; i < mpl; i++) {
        	
        	if (properties != null)
        		mThreads[i].setUp(properties);
        	else
        		mThreads[i].setUp(); 
        	
        }		
		
        //*************RUN EXPERIMENTS**************
        for (int i = 0; i < mpl; i++) {
        	mThreads[i].start();
        }
                
        //Experiment threads start WARMUP (do transactions but NOT record results)
        if (mExperimentConfiguration.getWarmupTime() > 0)
        	LOG.info(String.format("Experiment warming up (%.2f Sec) ...", mExperimentConfiguration.getWarmupTime() / 1000.00));
		AbstractClient.state = BenchmarkState.WARMUP;
        Thread.sleep(mExperimentConfiguration.getWarmupTime());
  
        //Experiment threads start MEASURING (do transactions and record RESULTS)
		LOG.info(String.format("Experiment measuring (%.2f Sec) ...", mExperimentConfiguration.getMeasurementTime() / 1000.00));
        AbstractClient.state = BenchmarkState.MEASURING;
        Thread.sleep(mExperimentConfiguration.getMeasurementTime());
        
        AbstractClient.state = BenchmarkState.FINISHED;

        
        //**************JOIN METHOD******************
        for (int i = 0; i < mpl; i++) {
            mThreads[i].join();
        }
        
 
        //**************CLOSING CONNECTIONS etc.******   
        for (int i = 0; i < mpl; i++) {
            mThreads[i].tearDown();
        }
        
        //**************DATA COLLECTION***************
        //DataCollector mAllThreadsDC = new DataCollector(ALL_TRHEADS);
        
        for (int i = 0; i < mpl; i++) {   
        	//insert run and MPL number
        	DataCollector dc = mThreads[i].getDataCollector();
        	dc.add(Titles.EXP_NO, Titles.EXP_NO_VALUE_PREFIX + expNo);
    		dc.add(Titles.RUN_NO, run + 1);
    		dc.add(Titles.MEASUREMENT_TIME_MS, mExperimentConfiguration.getMeasurementTime());
    		dc.add(Titles.CLIENT_ID, i + 1);

    		if (properties != null) {
    			Object[] keys = properties.keySet().toArray();
    			for (Object key: keys) {
    				dc.add(key.toString(), properties.get(key));
    			}
    		}
    
            //record the data that was collected
            mAllThreadsDC.splice(dc);
        }
        
	}
	
	private void executeExperiment(int expNo, int run, int mpl, Properties properties) 
		throws InterruptedException, ExperimentException, RecorderException {
		
		constructExperiment();
		
		//construct maximum client amount
		constructClients(mpl);
		
		long experimentRunStart = System.currentTimeMillis();
		
		if (properties != null)
			mAnExperiment.setUp(properties);
		else
			mAnExperiment.setUp();
		
		long startTime = System.currentTimeMillis();
		
		//warm up then run each clients with the amount of mpl
		executeClients(expNo, run, mpl, properties);
		
		long endTime = System.currentTimeMillis();
		
		mAnExperiment.tearDown();

		//record the data that was collected
		DataCollector dc = mAnExperiment.getDataCollector();
		dc.add(Titles.EXP_NO, Titles.EXP_NO_VALUE_PREFIX + expNo);
		dc.add(Titles.RUN_NO, run + 1);
		dc.add(Titles.MEASUREMENT_TIME_MS, mExperimentConfiguration.getMeasurementTime());	
		
		dc.add(Titles.WALL_CLOCK_MS, (endTime - startTime));
		
		mDataRecorder.recordData(dc);
		
		long runtime = (System.currentTimeMillis() - experimentRunStart) / 1000;
		
		LOG.info(MessageFormat.format("Completed run {0,number} mpl {4,number}. Cumulative running time {1,number}:{2,number,00}:{3,number,00}",
				new Object[] { run + 1, (int)(runtime / 3600),(int)((runtime % 3600) / 60), (int)(runtime % 60), mpl}));
		
		destroyExperiment();
		destroyClients(mpl);
		
		//garbage collection for minimise the effect of memory resource
		Runtime.getRuntime().gc();
        //*************WAIT (DO NOTHING)*************
        if ((mExperimentConfiguration.getWaitTime() > 0) && (expNo < mTotalExperiment)) {
        	LOG.info(String.format("Experiment waiting (%.2f Sec) ...", mExperimentConfiguration.getWaitTime() / 1000.00));
        	Thread.sleep(mExperimentConfiguration.getWaitTime());
        }

	}

	/**
	 * Execute the experiment for the number of runs specified in the configuration file.
	 * Start by initialising the data recording and maintain the data recording for each run
	 * @throws InterruptedException
	 * @throws ExperimentException
	 * @throws RecorderException
	 * @throws ClassNotFoundException 
	 */
	public void executeExperiments() throws InterruptedException, ExperimentException, RecorderException {

		// create instance of DataRecorder
		mDataRecorder = createDataRecorderInstance();
		mDataStatistics = createDataStatisticsInstance();
		
		// set recorder name, used for file naming etc.
		mDataRecorder.setName(mExperimentClassType.getSimpleName());
		
		//capture the experiment configuration
		mDataRecorder.recordExperiment(mFrameworkConfiguration, mExperimentConfiguration);
        
		//capture the database configuration
		mDataRecorder.recordConnection(mConnectionConfiguration);		
		
		List<Integer> mpls = mExperimentConfiguration.getMPLs();
		
		// numbers of experiment
		int expType = mpls.size();
		int runAmount = mExperimentConfiguration.getNumberOfRuns();
		int profileAmount = mExperimentConfiguration.getComparatorMatrix().size();
		
		if (profileAmount > 0) {
			expType *= profileAmount;
		}
		
		int expTotal = runAmount * expType;
		
		mTotalExperiment = expTotal;
		
		if (expTotal > 0) {
			long eachExpTime = mExperimentConfiguration.getWaitTime() + mExperimentConfiguration.getWarmupTime() + mExperimentConfiguration.getMeasurementTime();
			//System.out.println("Total experiments: " + expTotal + "\nEstamited time: more than " + expTotal * ((double)(eachExpTime) / 1000)+ " Sec");
			System.out.printf("\nTotal experiments: %d\nEstamited time: more than %.2f Seconds\n", expTotal, expTotal * ((double)(eachExpTime) / 1000));
		}
		else
			System.out.println("No experiments required");
		
		//record runtime properties of each experiment
		mRuntimePropertiesDC = new DataCollector(Titles.RUNTIME_PROPERTIES);
		//record raw data
		mAllThreadsDC = new DataCollector(Titles.ALL_TRHEADS);
		
		int expNo = 1;
		int expNoWithRun = 1;
		// different mpl loop
		for (Integer mpl: mpls) {

			// not properties set
			if (profileAmount < 1) {
				for (int run = 0; run < runAmount; run++) {
					System.out.println("\n[Experiment " + expNo + ": Run " + (run + 1) + "]");
					System.out.println(Titles.MPL + "=" + mpl);
					executeExperiment(expNo, run, mpl, null);
				}
				// record each runtime properties with MPL
				mRuntimePropertiesDC.add(Titles.EXP_NO, Titles.EXP_NO_VALUE_PREFIX + expNo);
				mRuntimePropertiesDC.add(Titles.MPL, mpl);
				mRuntimePropertiesDC.add(Titles.MEASUREMENT_TIME_MS, mExperimentConfiguration.getMeasurementTime());
				expNo++;
				
				mDataRecorder.recordData(mRuntimePropertiesDC);			
				// record raw data
				mDataRecorder.recordData(mAllThreadsDC);
				
				mDataRecorder.save();
				continue;
			}
			
			// different properites loop
			else for (int p = 0; p < profileAmount; p++) {
				
				// executes each experiment
				Properties runtimeProperties = mExperimentConfiguration.getComparatorMatrix().get(p);
				
				// multiple runs using same properties 
				for (int run = 0; run < runAmount; run++) {
					String prompt = "\n[Experiment " + expNoWithRun  + " of " + expTotal + ": Runtime Property Setting " + expNo;
					prompt = (runAmount > 1)? prompt + ", Run " + (run + 1) + "]" : prompt + "]";
					System.out.println(prompt);
					System.out.println(Titles.MPL + "=" + mpl);
					executeExperiment(expNo, run, mpl, runtimeProperties);
					expNoWithRun++;
				}
				
				// record each runtime properties with MPL
				mRuntimePropertiesDC.add(Titles.EXP_NO, Titles.EXP_NO_VALUE_PREFIX + expNo);
				mRuntimePropertiesDC.add(Titles.MPL, mpl);
				mRuntimePropertiesDC.add(Titles.RUN_NUM, mExperimentConfiguration.getNumberOfRuns());
				mRuntimePropertiesDC.add(Titles.MEASUREMENT_TIME_MS, mExperimentConfiguration.getMeasurementTime());
				Object[] keys = runtimeProperties.keySet().toArray();
				for (Object key: keys) {
					mRuntimePropertiesDC.add(key.toString(), runtimeProperties.get(key));
				}	
				
				mDataRecorder.recordData(mRuntimePropertiesDC);
				
				// record raw data
				mDataRecorder.recordData(mAllThreadsDC);
				
				// record statistics for current experiment
				if (mDataStatistics != null) {
					List<DataCollector> statResults = mDataStatistics.getResults(mRuntimePropertiesDC, mAllThreadsDC);
					for (int r = 0; r < statResults.size(); r++)
						mDataRecorder.recordData(statResults.get(r));
				}
				
				mDataRecorder.save();
				mAllThreadsDC.clear();
				mRuntimePropertiesDC.clear();
				
				expNo++;
			}	
		}

		System.out.println("\nAll experiment done.\n");
	}
	
	private DataRecorder createDataRecorderInstance() {
		
		// create instance of DataRecorder
		DataRecorder dr = null;
		String dataRecorder = mExperimentConfiguration.getDataRecorderClassName();
		if (dataRecorder.equals("")) 
			//record data in default format
			dr = new ExcelDataRecorder();
		else 
			try {
				dr = (DataRecorder) Class.forName(dataRecorder).newInstance();
			} catch (Exception e) {
				LOG.error(e);
			}
		return dr;
	}
	
	private AbstractDataStatistics createDataStatisticsInstance(){
		
		AbstractDataStatistics ds = null;
		
		String className = mExperimentConfiguration.getDataStatisticsClassName();

		Class<? extends AbstractDataStatistics> statisticsClassType = null;
		try {
			statisticsClassType = (Class<? extends AbstractDataStatistics>) Class.forName(className);
		} catch (ClassNotFoundException e) {
			LOG.debug(e.getMessage());
		}
		
		Constructor<? extends AbstractDataStatistics> statisticsConstructor = null;
		try {
			statisticsConstructor = statisticsClassType.getConstructor(
				new Class[] {AbstractExperimentConfiguration.class});
		} catch (Exception e) {
			LOG.debug(e.getMessage());
		}

		if (statisticsConstructor != null)
		try {      
			ds = statisticsConstructor.newInstance(new Object[] {mExperimentConfiguration});
		} catch (Exception e) {
			LOG.debug(e.getMessage());
		}
		return ds;
	}
}
