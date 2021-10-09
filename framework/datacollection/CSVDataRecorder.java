package dbexp.framework.datacollection;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import dbexp.framework.configuration.AbstractExperimentConfiguration;
import dbexp.framework.configuration.ConnectionConfiguration;
import dbexp.framework.configuration.FrameworkConfiguration;

public class CSVDataRecorder implements DataRecorder {
	
	private String mFileBaseName;
	
	private static final String fileNameFormat = "%s" + FileNameGenerator.SEPARATOR + "%s.csv";	
	
	public CSVDataRecorder() {
		mFileBaseName = FileNameGenerator.newName("data");
	}
	
	private String genFileName(String typeName) {
		return String.format(fileNameFormat, mFileBaseName, typeName);
	}
	
	@Override
	public void setName(String aName) {
		mFileBaseName = FileNameGenerator.newName(aName);
	}

	public void recordAllData(List<DataCollector> someDataCollectors) throws RecorderException {
		for (DataCollector dataCollector : someDataCollectors) {
			recordData(dataCollector);
		}
		
	}

	public void recordConnection(
			ConnectionConfiguration aConnectionConfiguration) throws RecorderException {
		Properties properties = aConnectionConfiguration.toProperties();
		PrintStream os;
		try {
			//os = new PrintStream(new FileOutputStream(mBaseFileName + mTimeStamp + "ConnConf" + ".csv"));
			os = new PrintStream(new FileOutputStream(genFileName("ConnConf")));
		} catch (FileNotFoundException e) {
			throw new RecorderException(e);
		}

		Enumeration<?> props = properties.propertyNames();
		while (props.hasMoreElements()) {
			String key = (String) props.nextElement();
			os.printf("\"%s\",\"%s\"\n", key, properties.getProperty(key));
		}
		os.flush();
		os.close();
		
	}

	public void recordData(DataCollector aDataCollector) throws RecorderException {
		PrintStream os;
		try {
			os = new PrintStream(new FileOutputStream(genFileName(aDataCollector.getName()), true));
		} catch (FileNotFoundException e) {
			throw new RecorderException(e);
		}
		for (String dataName : aDataCollector.getDataNames() ) {
			os.printf("\"%s\"", dataName);
			for (Object value : aDataCollector.getDataValues(dataName)) {
				os.printf(",\"%s\"", value);
			}
			os.println();
		}
		os.flush();
		os.close();
	}

	public void recordExperiment(
			FrameworkConfiguration aFrameworkConfiguration,
			AbstractExperimentConfiguration anExperimentConfiguration) throws RecorderException {
		Properties properties = aFrameworkConfiguration.toProperties();
		properties.putAll(anExperimentConfiguration.toProperties());
		PrintStream os;
		try {
			//os = new PrintStream(new FileOutputStream(mBaseFileName + mTimeStamp + "ExpConf" + ".csv"));
			os = new PrintStream(new FileOutputStream(genFileName("ExpConf")));
		} catch (FileNotFoundException e) {
			throw new RecorderException(e);
		}

		Enumeration<?> props = properties.propertyNames();
		while (props.hasMoreElements()) {
			String key = (String) props.nextElement();
			os.printf("\"%s\",\"%s\"\n", key, properties.getProperty(key));
		}
		os.flush();
		os.close();
		
	}

	@Override
	public void save() {
		// TODO Auto-generated method stub
		
	}

}
