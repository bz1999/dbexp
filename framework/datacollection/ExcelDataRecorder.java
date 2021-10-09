package dbexp.framework.datacollection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import dbexp.framework.configuration.AbstractExperimentConfiguration;
import dbexp.framework.configuration.ConnectionConfiguration;
import dbexp.framework.configuration.FrameworkConfiguration;
import dbexp.framework.datacollection.excel.ExcelWorkbook;

public class ExcelDataRecorder extends ExcelWorkbook implements DataRecorder {
	
	private String mFileName;
	private static final String TABLE_OF_CONTENTS = "SHEET LIST";
	private static final String SHEET_ID = "NO.";
	private static final String SHEET_NAME = "SHEET NAME";
	
	private List<String> existingReportSheets;
	
	public ExcelDataRecorder() {
		mFileName = FileNameGenerator.newName("") + ".xls";
		
		addSheet(TABLE_OF_CONTENTS);
		setTableTitle(TABLE_OF_CONTENTS, TABLE_OF_CONTENTS);
		addColumn(TABLE_OF_CONTENTS, SHEET_ID);
		addColumn(TABLE_OF_CONTENTS, SHEET_NAME);
		
		existingReportSheets = new ArrayList<String>();
	}

	private void addToTableOfContents(String sheetName) {
		if (!existingReportSheets.contains(sheetName)){
			existingReportSheets.add(sheetName);
			appendTableOfContent(TABLE_OF_CONTENTS, SHEET_ID, existingReportSheets.size(), sheetName);
			
			appendColumnData(TABLE_OF_CONTENTS, SHEET_NAME, sheetName);
		}
		
		freezeHeader(TABLE_OF_CONTENTS);
		autoColumnWidth(TABLE_OF_CONTENTS);
	}
	
	@Override
	public void recordAllData(List<DataCollector> someDataCollectors) throws RecorderException {
		
	}

	@Override
	public void recordConnection(
			ConnectionConfiguration aConnectionConfiguration)
			throws RecorderException {
		
		String sheetName = "Connection Configuration";
		addSheet(sheetName);

		setTableTitle(sheetName, "Connection Configuration");
		addColumn(sheetName, "Key");
		addColumn(sheetName, "Value");
		
		Properties props = aConnectionConfiguration.toProperties();
		//props.putAll(anExperimentConfiguration.toProperties());
		
		Object[] keys = props.keySet().toArray();
		Arrays.sort(keys);
		for (Object o: keys) {
			String key = o.toString();
			String value = props.get(key).toString();
			try {
				appendRowData(sheetName, key, Double.valueOf(value));
			}
			catch(Exception e) {
				appendRowData(sheetName, key, value);
			}
		}
		freezeHeader(sheetName);
		autoColumnWidth(sheetName);
		
		addToTableOfContents(sheetName);
	}

	@Override
	public void recordExperiment(
			FrameworkConfiguration aFrameworkConfiguration,
			AbstractExperimentConfiguration anExperimentConfiguration)
			throws RecorderException {
		String sheetName = "Experiment Configuration";
		addSheet(sheetName);
		setTableTitle(sheetName, "Experiment Configuration");
		addColumn(sheetName, "Key");
		addColumn(sheetName, "Value");
		
		Properties props = aFrameworkConfiguration.toProperties();
		props.putAll(anExperimentConfiguration.toProperties());
		
		Object[] keys = props.keySet().toArray();
		Arrays.sort(keys);
		for (Object o: keys) {
			String key = o.toString();
			String value = props.get(key).toString();
			try {
				appendRowData(sheetName, key, Double.valueOf(value));
			}
			catch(Exception e) {
				appendRowData(sheetName, key, value);
			}
		}
		
		freezeHeader(sheetName);
		autoColumnWidth(sheetName);
		
		addToTableOfContents(sheetName);
	}

	@Override
	public void recordData(DataCollector aDataCollector)
			throws RecorderException {

		//LOG.info("Saving \"" + aDataCollector.getName() + "\" data...");
		String sheetName = aDataCollector.getName();
		
		addSheet(sheetName);
		setTableTitle(sheetName, sheetName);
		
		Object[] headers = aDataCollector.getDataNames().toArray();
		appendOrderedHeader(sheetName, headers, Titles.EXP_NO);
		appendOrderedHeader(sheetName, headers, Titles.MPL);
		appendOrderedHeader(sheetName, headers, Titles.RUN_NUM);
		appendOrderedHeader(sheetName, headers, Titles.RUN_NO);
		appendOrderedHeader(sheetName, headers, Titles.CLIENT_ID);
		appendOrderedHeader(sheetName, headers, Titles.MEASUREMENT_TIME_MS);
		appendOrderedHeader(sheetName, headers, Titles.TRANS_ATTEMPTS);
		appendOrderedHeader(sheetName, headers, Titles.TRANS_COMMITS);
		appendOrderedHeader(sheetName, headers, Titles.TRANS_ERROR_RATE);
		appendOrderedHeader(sheetName, headers, Titles.TRANS_ERRORS);
		appendOrderedHeader(sheetName, headers, Titles.TRANS_ERROR_DEADLOCK);
		appendOrderedHeader(sheetName, headers, Titles.TRANS_ERROR_CONFLICT);
		appendOrderedHeader(sheetName, headers, Titles.TRANS_ERROR_UNSAFE);
		appendOrderedHeader(sheetName, headers, Titles.TRANS_ERROR_COMMIT);
		appendOrderedHeader(sheetName, headers, Titles.TRANS_ERROR_OTHER);

		String dataFormat;
		for (Object header: headers) {
			addColumn(sheetName, header.toString());
			List<Object> data = aDataCollector.getDataValues(header.toString());
			if (header.toString().contains(Titles.PERCENTAGE_SYMBOL))
				dataFormat = "0.00%";
			else
				dataFormat = "";
			appendColumnData(sheetName, header.toString(), data, dataFormat);
		}
		
		// Set links will significantly increase excel file size
		//setColumnHyperlinks(ExperimentDriver.EXP_NO, ExperimentDriver.RUNTIME_PROPERTIES, ExperimentDriver.EXP_NO);
		
		autoColumnWidth(sheetName);
		freezeHeader(sheetName);
		
		addToTableOfContents(sheetName);
		//LOG.info("done!");
	}

	@Override
	public void save() {
		
		saveToFile(mFileName);
		
	}

	@Override
	public void setName(String aName) {
		
		mFileName = FileNameGenerator.newName(aName) + ".xls";
		
	}
	
	private void appendOrderedHeader(String sheetName, Object[] headers, String header) {
		for (Object o: headers) {
			if (o.toString().equalsIgnoreCase(header)) {
				addColumn(sheetName, header);
			}
		}
	}

}
