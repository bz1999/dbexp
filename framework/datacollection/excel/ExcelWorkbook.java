package dbexp.framework.datacollection.excel;

import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class ExcelWorkbook {
	private Workbook mWorkbook;
	
	public ExcelWorkbook() {
		mWorkbook = new HSSFWorkbook();
	}
	
	public ExcelWorkbook(String filename) {
		mWorkbook = ExcelUtilities.openFile(filename);
	}
	
	// file
	public void loadFromFile(String filename) {
		mWorkbook = ExcelUtilities.readFile(filename);
	}
	
	public void saveToFile(String filename) {
		ExcelUtilities.saveToFile(mWorkbook, filename);
	}
	
	// sheet
	public String addSheet(String sheetname) {
		return ExcelUtilities.openSheet(mWorkbook, sheetname).getSheetName();
	}
	
	public void addSheet(String sheetname, String title) {
		Sheet sheet = ExcelUtilities.openSheet(mWorkbook, sheetname);
		ExcelUtilities.setTableTitle(sheet, title);
	}
	
	// table
	public void setTableTitle(String sheetname, String title) {
		ExcelUtilities.setTableTitle(mWorkbook, sheetname, title);
	}
	
	public void freezeHeader(String sheetname) {
		ExcelUtilities.freezeHeader(mWorkbook, sheetname);
	}
	
	public void addColumn(String sheetname, String colname) {
		ExcelUtilities.appendTableHeader(mWorkbook, sheetname, colname);
	}

	public void appendRowData(String sheetname, Object... values) {
		ExcelUtilities.appendRowData(mWorkbook, sheetname, values);
	}
	
	public void appendRowData(String sheetname, List<?> values) {
		ExcelUtilities.appendRowData(mWorkbook, sheetname, values);
	}
	
	public void appendTableOfContent(String sheetname, String colname, Object value, String targetSheet) {
		ExcelUtilities.appendTableOfContent(mWorkbook, sheetname, colname, value, targetSheet);
	}
	
	public void appendColumnData(String sheetname, String colname, Object... values) {
		ExcelUtilities.appendColumnData(mWorkbook, sheetname, colname, values);
	}
	
	public void appendColumnData(String sheetname, String colname, List<?> values) {
		ExcelUtilities.appendColumnData(mWorkbook, sheetname, colname, values);
	}
	
	public void appendColumnData(String sheetname, String colname, List<?> values, String dataFormat) {
		ExcelUtilities.appendColumnData(mWorkbook, sheetname, colname, values, dataFormat);
	}
	
	// size
	public void autoColumnWidth(String sheetname) {
		ExcelUtilities.autoColumnWidth(mWorkbook, sheetname);
	}
	
	public void setColumnHyperlinks(String linkSheet, String linkColumn, String targetSheet, String targetColumn) {
		
		ExcelUtilities.setHyperlinks(mWorkbook, linkSheet, linkColumn, targetSheet, targetColumn);
	}
	
	public void setColumnHyperlinks(String linkColumn, String targetSheet, String targetColumn) {
		
		ExcelUtilities.setHyperlinks(mWorkbook, linkColumn, targetSheet, targetColumn);
	}
	
	public boolean containSheet(String sheetName) {
		int size = mWorkbook.getNumberOfSheets();
		for (int i = 0; i < size; i++)
			if (mWorkbook.getSheetName(i).equalsIgnoreCase(sheetName))
				return true;
		return false;
	}
	
	public int getSheetIndex(String sheetName) {
		return mWorkbook.getSheetIndex(sheetName);
	}
}
