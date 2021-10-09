package dbexp.framework.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import dbexp.framework.experiment.AbstractClient;
import dbexp.framework.experiment.AbstractExperiment;
import dbexp.framework.experiment.Version;


/**
 * @author Shirley Goldrei
 *
 */
public class AbstractExperimentConfiguration {
	
	public static final String KEY_SPLITTER = ".";
	public static final String VALUE_SPLITTER = ",";
	
	protected enum Keys {
		COMMENT("experiment.comment"),
		MULTIPROGRAMMING_LEVEL ("experiment.mpl"),
		MPL_MIN("experiment.mpl.min"),
		MPL_MAX("experiment.mpl.max"),
		MPL_INTERVAL("experiment.mpl.interval"),
		MPL_ENUM("experiment.mpl.enum"),
		MPL_TYPE("experiment.mpl.type"),
		WAIT_TIME("experiment.waitTime"), //wait time (do nothing) milliseconds
		WARMUP_TIME ("experiment.warmupTime"), //ramp-up time milliseconds
		MEASUREMENT_TIME ("experiment.measurementTime"), //experiment time milliseconds
		HOTSPOT_SIZE ("experiment.hotspotSize"),//experiment size dependent
		
		EXPERIMENT_CLASS_NAME ("experiment.experimentClass"),
		CONFIGURATION_CLASS_NAME ("experiment.configurationClassName"),
		CLIENT_CLASS_NAME ("experiment.clientClass"),
		DATA_RECORDER_CLASS_NAME("experiment.dataRecorderClass"),
		DATA_STATISTICS_CLASS_NAME("experiment.dataStatisticsClass"),
		
		CONNECTION("experiment.connectionName"),
		
		NUMBER_OF_RUNS("experiment.numberOfRuns"),
		
		COMPARATORS("experiment.runtimeProperties"),
		
		SQLERROR_STATISTICS_WANTED("experiment.sqlError.doStatistics"),
		SQLERROR_CODES("experiment.sqlError.codes");
		
		String mName;
		Keys(String name) {
			mName = name;
		}
		
		@Override
		public String toString() {
			return mName;
		}
	}

	private String mComment;
	private String mExperimentClassName;
	private String mConfigurationClassName;
	
	private int mMultiprogramingLevel;
	private int mMPLmin;
	private int mMPLmax;
	private int mMPLinterval;
	private String mMPLenumString;
	private int mMPLtype;
	private String mClientClassName;
	private String mDataRecorderClassName;
	private String mDataStatisticsClassName;
	private String mConnectionName;
	private int mNumberOfRuns = 1;
	private int mHotspotSize;
	private long mMeasurementTime = 30000L; //measurement time
	private long mWarmupTime = 30000L; //ramp-up time
	private long mWaitTime = 10000L;
	
	private Map<String, List<String>> mBenchmarkComparators;
	private List<Properties> mComparatorMatrix;
	private List<Integer> mMPLs;
	
	private boolean mStatError;
	private List<Integer> mStatErrorCodes;
	

	private Properties properties;
	
	@SuppressWarnings("unused")
	private AbstractExperimentConfiguration() {

	}
	
	public AbstractExperimentConfiguration(Properties someProperties) {
		
		properties = someProperties;
		// used for looping client by setting each comparative factor within each group
		mBenchmarkComparators = new HashMap<String, List<String>>();
		
		mComparatorMatrix = new ArrayList<Properties>();
		mMPLs = new ArrayList<Integer>();
		mStatErrorCodes = new ArrayList<Integer>();
		
		retrieveProperties();
	}
	
	private void retrieveProperties() {
		mComment = getProperty(Keys.COMMENT);
		mClientClassName = getProperty(Keys.CLIENT_CLASS_NAME);
		mConfigurationClassName = getProperty(Keys.CONFIGURATION_CLASS_NAME);
		mConnectionName = getProperty(Keys.CONNECTION);
		mExperimentClassName = getProperty(Keys.EXPERIMENT_CLASS_NAME);
		mDataRecorderClassName = getProperty(Keys.DATA_RECORDER_CLASS_NAME);
		mDataStatisticsClassName = getProperty(Keys.DATA_STATISTICS_CLASS_NAME);
		
		mHotspotSize = Integer.parseInt(getProperty(Keys.HOTSPOT_SIZE));
		mWaitTime = Long.parseLong(getProperty(Keys.WAIT_TIME));
		mWarmupTime = Long.parseLong(getProperty(Keys.WARMUP_TIME));
		mMeasurementTime = Long.parseLong(getProperty(Keys.MEASUREMENT_TIME));
		mMultiprogramingLevel = Integer.parseInt(getProperty(Keys.MULTIPROGRAMMING_LEVEL));
		mMPLmin = Integer.parseInt(getProperty(Keys.MPL_MIN));
		mMPLmax = Integer.parseInt(getProperty(Keys.MPL_MAX));
		mMPLinterval = Integer.parseInt(getProperty(Keys.MPL_INTERVAL));
		mMPLenumString = getProperty(Keys.MPL_ENUM);
		mMPLtype = Integer.parseInt(getProperty(Keys.MPL_TYPE));
		mNumberOfRuns = Integer.parseInt(getProperty(Keys.NUMBER_OF_RUNS));
		
		String value = getProperty(Keys.SQLERROR_STATISTICS_WANTED);
		if (value != null && (value.equalsIgnoreCase("1") || value.equalsIgnoreCase("YES") || value.equalsIgnoreCase("TRUE")))
			mStatError = true;
		else
			mStatError = false;
		
		retrieveComparators();
		
		retrieveMPLs();
			
		retrieveSqlErrorCodes();
	}
    
	private void retrieveComparators() {
		
		Map<Object, Object[]> comparatorTable = new HashMap<Object, Object[]>();
		
		Enumeration<?> props = properties.propertyNames();
		while (props.hasMoreElements()) {
			String key = (String) props.nextElement();
			String comparator = extractComparator(key);
			// is not a comparator property, dont analyse further
			if (comparator == null)
				continue;
			
			String value = properties.getProperty(key);
			
			Object[] valueList = extractComparatorValues(value);
			
			// this property has not any value, dont process further
			if (valueList != null)
				comparatorTable.put(comparator, valueList);
		}
		
		if (comparatorTable.size() < 1)
			return;
		
		Object[] comparators = comparatorTable.keySet().toArray();
		
		Object[][] values = new Object[comparators.length][];
		int[] count = new int[comparators.length];
		int index = 0;
		
		mComparatorMatrix.clear();
		populateComparator(comparatorTable, comparators, values, count, index);
	}
	
	private String extractComparator(String key) {
		
		String prefix = Keys.COMPARATORS.toString() + KEY_SPLITTER;
		
		if (!key.startsWith(prefix))
			return null;
		
		String comparator = key.substring(prefix.length());
		
		if (comparator.equalsIgnoreCase(""))
			return null;
		else
			return comparator;
	}
	
	private Object[] extractComparatorValues(String value) {
		String[] values = value.split(VALUE_SPLITTER);
		//List<String> valueList = new ArrayList<String>();
		List<Object> valueList = new ArrayList<Object>();
		for (int i = 0; i < values.length; i++) {
			values[i] = values[i].trim();
			if (!values[i].equalsIgnoreCase("")) {
				// attempt to add value as a number
				try {
					valueList.add(Double.parseDouble(values[i]));
				}
				catch(NumberFormatException e) {
					valueList.add(values[i]);
				}
			}
		}
		if (valueList.size() > 0)
			return valueList.toArray();
		else
			return null;
	}
	
	private void populateComparator(Map<Object, Object[]> compartorTable, Object[] comparators, Object[][] values, int[] count, int index) {
		values[index] = compartorTable.get(comparators[index]);
		for (count[index] = 0; count[index] < values[index].length; count[index]++) {
			if (index < comparators.length - 1) {
				populateComparator(compartorTable, comparators, values, count, index + 1);
			}
			else {
				for (count[index] = 0; count[index] < values[index].length; count[index]++) {
					Properties matrix = new Properties();
					for (int i = 0; i < comparators.length; i++) {
						matrix.put(comparators[i], values[i][count[i]]);
						//matrix.setProperty(comparators[i].toString(), values[i][count[i]].toString());
						//System.out.println(comparators[i] + "=" + values[i][count[i]].toString() + " ");
					}
					mComparatorMatrix.add(matrix);
				}
			}
		}

	}
	
	private void retrieveMPLs() {
		
		// create mpl list
		switch(mMPLtype) {
			default:
			case 1:
				mMPLs.add(mMultiprogramingLevel);
				mMPLmin = mMultiprogramingLevel;
				mMPLmax = mMultiprogramingLevel;;
				break;
			case 2:
				int mpl = mMPLmin;
				while (mpl <= mMPLmax) {
					mMPLs.add(mpl);
					mpl = mpl + mMPLinterval;
				}
				break;
			case 3:
				String[] mplEnum = mMPLenumString.split(VALUE_SPLITTER);
				mMPLmin = 0;
				mMPLmax = 0;
				for (int i = 0; i < mplEnum.length; i++) {
					try {
						mMPLs.add(Integer.parseInt(mplEnum[i].trim()));
					}
					catch (NumberFormatException e) {
					}
				}
				Collections.sort(mMPLs);
				if (mMPLs.size() > 0) {
					mMPLmin = mMPLs.get(0);
					mMPLmax = mMPLs.get(mMPLs.size() - 1);
				}
				break;
		}

	}
	
	private void retrieveSqlErrorCodes() {
		if (properties.containsKey(Keys.SQLERROR_CODES.toString())) {
			String[] errors = properties.getProperty(Keys.SQLERROR_CODES.toString()).split(VALUE_SPLITTER);
			for (String error: errors) {
				try {
					Integer code = Integer.valueOf(error.trim());
					if (!mStatErrorCodes.contains(code))
						mStatErrorCodes.add(code);
				}
				catch (NumberFormatException e) {}
			}
		}
	}
	
	public List<Properties> getComparatorMatrix() {
		return mComparatorMatrix;
	}
	
	@SuppressWarnings("unchecked")
	public String getClientVersion() {
		try {
			Class<? extends AbstractClient> c = (Class<? extends AbstractClient>) Class.forName(getClientClassName());
			Version version = c.getAnnotation(Version.class);
			return getVersion(version); 
		} catch (ClassNotFoundException e) {
			return "ERROR: Class Not Found";
		} catch (ClassCastException e2) {
			return "ERROR: Class is not a subclass of AbstractClient";
		}
	}
	
	private String getVersion(Version aVersion) {
		if (aVersion != null) {
			return aVersion.major() + "." + aVersion.minor() + "." + aVersion.build() + ":" + aVersion.comment();
		} else return "No version specified";
	}
	
	@SuppressWarnings("unchecked")
	public String getExperimentVersion() {
		try {
			Class<? extends AbstractExperiment> c = (Class<? extends AbstractExperiment>) Class.forName(getExperimentClassName());
			Version version = c.getAnnotation(Version.class);
			return getVersion(version); 
		} catch (ClassNotFoundException e) {
			return "ERROR: Class Not Found";
		} catch (ClassCastException e2) {
			return "ERROR: Class is not a subclass of AbstractExperiment";
		}
	}

	public static String getExperimentConfigurationClassPropertyKey() {
		return Keys.CONFIGURATION_CLASS_NAME.toString();
	}
	
	public Properties toProperties() {
		return properties;
	}
	
	private String getProperty(Keys key) {
		return properties.getProperty(key.toString());
	}
	
	private void setProperty(Keys key, String value){
		properties.setProperty(key.toString(), value);
	}
	
	private void setProperty(Keys key, int value){
		properties.setProperty(key.toString(), Integer.toString(value));
	}
	
	private void setProperty(Keys key, long value){
		properties.setProperty(key.toString(), Long.toString(value));
	}

// Setters & Getters
	
	public String getComment() {
		return mComment;
	}
	
    public String getExperimentClassName() {
		return mExperimentClassName;
	}
    
	public void setExperimentClassName(String someExperimentClassName) {
		mExperimentClassName = someExperimentClassName;
		setProperty(Keys.EXPERIMENT_CLASS_NAME, mExperimentClassName);
	}
    
	public Integer getMultiprogramingLevel() {
		return mMultiprogramingLevel;
	}
	
	public void setMultiprogramingLevel(int someMultiprogramingLevel) {
		mMultiprogramingLevel = someMultiprogramingLevel;
		setProperty(Keys.MULTIPROGRAMMING_LEVEL, mMultiprogramingLevel);
	}
	
	public List<Integer> getMPLs() {
		return mMPLs;
	}
	
	public Integer getMPLmin() {
		return mMPLmin;
	}

	public Integer getMPLmax() {
		return mMPLmax;
	}

	public Long getWaitTime() {
		return mWaitTime;
	}
	
	public void setWaitTime(long someWaitTime) {
		mWaitTime = someWaitTime;
		setProperty(Keys.WAIT_TIME, mWaitTime);
	}
	
	public Long getWarmupTime() {
		return mWarmupTime;
	}
	
	public void setWarmupTime(long someWarmupTime) {
		mWarmupTime = someWarmupTime;
		setProperty(Keys.WARMUP_TIME, mWarmupTime);
	}
	
	public Long getMeasurementTime() {
		return mMeasurementTime;
	}
	
	public void setMeasurementTime(long someMeasurementTime) {
		mMeasurementTime = someMeasurementTime;
	}
	
	public int getHotspotSize() {
		return mHotspotSize;
	}
	
	public void setHotspotSize(int someHotspotSize) {
		mHotspotSize = someHotspotSize;
		setProperty(Keys.HOTSPOT_SIZE, mHotspotSize);
	}
	
	public void setConnectionName(String connectionName) {
		mConnectionName = connectionName;
		setProperty(Keys.CONNECTION, mConnectionName);
	}
	
	public String getConnectionName() {
		return mConnectionName;
	}
	
	public void setClientClassName(String clientClassName) {
		mClientClassName = clientClassName;
		setProperty(Keys.CLIENT_CLASS_NAME, mClientClassName);
	}
	
	public String getClientClassName() {
		return mClientClassName;
	}
	
	public int getNumberOfRuns() {
		return mNumberOfRuns;
	}
	
	public void setNumberOfRuns(int someNumberOfRuns) {
		mNumberOfRuns = someNumberOfRuns;
	}

	public String getDataRecorderClassName() {
		return mDataRecorderClassName;
	}
	
	public void setDataRecorderClassName(String dataRecorderClassName) {
		mDataRecorderClassName = dataRecorderClassName;
		setProperty(Keys.DATA_RECORDER_CLASS_NAME, mDataRecorderClassName);
	}

	public String getDataStatisticsClassName() {
		return mDataStatisticsClassName;
	}
	
	public void setDataStatisticsClassName(String dataStatisticsClassName) {
		mDataStatisticsClassName = dataStatisticsClassName;
		setProperty(Keys.DATA_STATISTICS_CLASS_NAME, mDataStatisticsClassName);
	}
	
	public Map<String, List<String>> getBenchmarkComparators() {
		return mBenchmarkComparators;
	}
	
	public boolean isErrorStatWanted() {
		return mStatError;
	}
	
	public boolean isErrorCodeIncluded(int code) {
		return mStatErrorCodes.contains(code);
	}
}
