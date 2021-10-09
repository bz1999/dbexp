package dbexp.framework.datacollection.excel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

public class ExcelUtilities {

	/*
	 *  Workbook
	 */
	
	private static String mDataFormat = "";
	
	public static Workbook createFile(String filename) {
		Workbook wb = null;
		try {
			wb = new HSSFWorkbook();
			FileOutputStream fos = new FileOutputStream(filename);
			wb.write(fos);
			fos.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		return wb;
	}
	
	public static Workbook readFile(String filename){
		Workbook wb = null;
		try {
			wb = new HSSFWorkbook(new FileInputStream(filename));
		} catch (FileNotFoundException e) {			
		} catch (IOException e) {			
		}
		return wb;
	}
	
	// if file exists, read it, otherwise create it
	public static Workbook openFile(String filename){
		Workbook wb = readFile(filename);
		if (wb == null)
			wb = createFile(filename);
		return wb;
	}
	
	public static boolean saveToFile(Workbook wb, String filename) {
		
		try {
			FileOutputStream fos = new FileOutputStream(filename);
			wb.write(fos);
			fos.flush();
			fos.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	/*
	 * Sheet
	 */
	
	public static Sheet readSheet(Workbook wb, String sheetname) {
		return wb.getSheet(sheetname);
	}
	
	// if file exists, read it, otherwise create it
	public static Sheet openSheet(Workbook wb, String sheetname) {
		Sheet sheet = wb.getSheet(sheetname);
		if (sheet == null)
			sheet = wb.createSheet(sheetname);
		return sheet;
	}
	
	
	/*
	 * Table
	 */
	
	// Table title
	public static void setTableTitle(Sheet sheet, String title) {
		if (sheet == null)
			return;
		Row row = sheet.getRow(0);
		if (row == null)
			row = sheet.createRow(0);
		
		row.setHeightInPoints(40);
		
		Cell cell = row.getCell(0);
		if (cell == null)
			cell = row.createCell(0);
		
		cell.setCellValue(title);
		
		cell.setCellStyle(CellStyles.TABLE_TITLE.getStyle(sheet.getWorkbook()));
	}
	
	public static void setTableTitle(Workbook wb, String sheetname, String title) {
		setTableTitle(wb.getSheet(sheetname), title);
	}
	
	// Table headers
	public static void appendTableHeader(Workbook wb, String sheetname, String colname) {
		
		if(getColumnIndex(wb, sheetname, colname) > -1)
			return;
		
		Sheet sheet = openSheet(wb, sheetname);
		Row row = sheet.getRow(1);
		Cell cell = null;
		
		if (row == null) {
			row = sheet.createRow(1);
			row.setHeightInPoints(20);
			
			cell = row.createCell(0);
		}
		else {
			int colID = row.getLastCellNum();
			cell = row.getCell(colID);
			if (cell == null)
				cell = row.createCell(colID);

			// merge title 
			CellRangeAddress region = null;
			for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
				region = sheet.getMergedRegion(i);
				if (region.getFirstRow() == 0 && region.getFirstColumn() == 0)
					break;
				else
					region = null;
			}
			
			if (region != null)
				region.setLastColumn(colID);
			else {
				region = new CellRangeAddress(0, 0, 0, colID);
				sheet.addMergedRegion(region);
			}

		}
		
		cell.setCellValue(colname);
		
		cell.setCellStyle(CellStyles.TABLE_HEADER.getStyle(wb));
		
	}
	
	public static void freezeHeader(Workbook wb, String sheetname) {
		Sheet sheet = wb.getSheet(sheetname);
		if (sheet == null)
			return;
		sheet.createFreezePane(0, 2);
	}
	
	// Table data
	private static int getColumnIndex(Workbook wb, String sheetname, String colname) {
		
		Sheet sheet = wb.getSheet(sheetname);
		if (sheet == null)
			return -1;
		
		Row headerRow = sheet.getRow(1);
		if (headerRow == null)
			return -1;
		
		Iterator<Cell> headers = headerRow.cellIterator();
		Cell headerCell = null;
		while (headers.hasNext()) {
			headerCell = headers.next();
			if (headerCell.getStringCellValue().equalsIgnoreCase(colname))
				break;
			else
				headerCell = null;
		}
		
		if (headerCell != null)
			return headerCell.getColumnIndex();
		else
			return -1;
	}

	private static Cell setDataCell(Cell cell, Object value) {
		
		if (value instanceof String)
			cell.setCellValue(value.toString());
		else if (value instanceof Number)
			cell.setCellValue(Double.valueOf(value.toString()));
		else if (value instanceof Boolean)
			cell.setCellValue((Boolean) value);
		else if (value instanceof Date) 
			cell.setCellValue((Date) value);
		else if (value instanceof Calendar)
			cell.setCellValue((Calendar)value);
		else if (value instanceof RichTextString)
			cell.setCellValue((RichTextString) value);
		else
			cell.setCellValue(value.toString());

		Workbook wb = cell.getRow().getSheet().getWorkbook();
		
		CellStyle csTableData = CellStyles.TABLE_DATA.getStyle(wb);
		CellStyle cellStyle = null;
		
		if (mDataFormat.equals("")) {
			cellStyle = csTableData;
		}
		else {
			// find existing style to avoid duplicate
			for (short i = 0; i < wb.getNumCellStyles(); i++) {
				CellStyle cs = wb.getCellStyleAt(i);
				if (mDataFormat.equals(cs.getDataFormatString()) && cs.getFontIndex() ==  csTableData.getFontIndex()) {
					cellStyle = cs;
					break;
				}	
			}
			// not found
			if (cellStyle == null) {
				cellStyle = wb.createCellStyle();
				cellStyle.cloneStyleFrom(csTableData);
				cellStyle.setDataFormat(wb.createDataFormat().getFormat(mDataFormat));
			}
		}
		cell.setCellStyle(cellStyle);
		//System.out.println("*** " +wb.getNumCellStyles());
		return cell;
	}
	
	private static Cell setTableData(Sheet sheet, int rowID, int colID, Object value) {

		Row row = sheet.getRow(rowID);
		if (row == null)
			row = sheet.createRow(rowID);
		
		Cell cell = row.getCell(colID);
		if (cell == null)
			cell = row.createCell(colID);
		
		return setDataCell(cell, value);
	}
	
	public static void setTableData(Workbook wb, String sheetname, int rowID, int colID, Object value) {
		Sheet sheet = openSheet(wb, sheetname);
		setTableData(sheet, rowID, colID, value);
	}

	public static void appendColumnData(Workbook wb, String sheetname, int colID, Object...values) {
		
		if (colID < 0)
			return;
		
		Sheet sheet = openSheet(wb, sheetname);
		int lastRowID = sheet.getLastRowNum();
		
		// find appending position
		int rowID;
		for (rowID = lastRowID; rowID > 1; rowID--) {
			Cell cell = sheet.getRow(rowID).getCell(colID);			
			if (cell != null)
				break;
		}
		rowID++;
		
		// append
		for (int i = 0; i < values.length; i++) {
			setTableData(sheet, rowID + i, colID, values[i]);
		}
	}
	
	
	public static void appendColumnData(Workbook wb, String sheetname, String colname, Object...values) {
		int colID = getColumnIndex(wb, sheetname, colname);
		appendColumnData(wb, sheetname, colID, values);
	}
	
	public static void appendColumnData(Workbook wb, String sheetname, String colname, List<?> values, String dataFormat) {
		int colID = getColumnIndex(wb, sheetname, colname);
		if (colID < 0)
			return;
		
		Sheet sheet = openSheet(wb, sheetname);
		int lastRowID = sheet.getLastRowNum();
		
		// find appending position
		int rowID;
		for (rowID = lastRowID; rowID > 1; rowID--) {
			Cell cell = sheet.getRow(rowID).getCell(colID);			
			if (cell != null)
				break;
		}
		rowID++;
		
		mDataFormat = dataFormat;
		// append
		for (int i = 0; i < values.size(); i++) {
			setTableData(sheet, rowID + i, colID, values.get(i));
		}
		mDataFormat = "";
		
	}
	
	public static void appendColumnData(Workbook wb, String sheetname, String colname, List<?> values) {
		appendColumnData(wb, sheetname, colname, values, "");
	}
	
	public static void appendTableOfContent(Workbook wb, String sheetname, String colname, Object value, String targetSheet){
		int colID = getColumnIndex(wb, sheetname, colname);
		if (colID < 0)
			return;
		
		Sheet sheet = openSheet(wb, sheetname);
		
		int lastRowID = sheet.getLastRowNum();
		
		// find appending position
		int rowID;
		for (rowID = lastRowID; rowID > 1; rowID--) {
			Cell cell = sheet.getRow(rowID).getCell(colID);			
			if (cell != null)
				break;
		}
		rowID++;
		
		Cell cell = setTableData(sheet, rowID, colID, value);
		Hyperlink link = wb.getCreationHelper().createHyperlink(Hyperlink.LINK_DOCUMENT);
		
		CellReference cellRef = new CellReference(targetSheet, 0, 0, false, false);
		
		link.setAddress(cellRef.formatAsString());
		cell.setHyperlink(link);
		cell.setCellStyle(CellStyles.TABLE_DATA_LINK.getStyle(wb));

	}
	
	public static void appendRowData(Workbook wb, String sheetname, Object...values) {
		
		Sheet sheet = openSheet(wb, sheetname);
		int lastRowID = sheet.getLastRowNum();
		if (lastRowID < 1)
			lastRowID = 1;
		Row row = sheet.createRow(lastRowID + 1);
		for (int i = 0; i < values.length; i++) {
			Cell cell = row.createCell(i);
			setDataCell(cell, values[i]);
		}	
	}
	
	public static void appendRowData(Workbook wb, String sheetname, List<?> values) {
		
		Sheet sheet = openSheet(wb, sheetname);
		int lastRowID = sheet.getLastRowNum();
		if (lastRowID < 1)
			lastRowID = 1;
		Row row = sheet.createRow(lastRowID + 1);
		for (int i = 0; i < values.size(); i++) {
			Cell cell = row.createCell(i);
			setDataCell(cell, values.get(i));
		}	
	}
	
	
	/*
	 * Size
	 */
	
	public static void autoColumnWidth(Workbook wb, String sheetname) {

		Sheet sheet = readSheet(wb, sheetname);
		Row header = sheet.getRow(1);
		if (header == null)
			return;
		int colNum = header.getLastCellNum();
		for (int i = 0; i < colNum; i++)
			sheet.autoSizeColumn(i, true);
		
	}
	
	/*
	 * Hyperlink
	 */
	
	public static void setHyperlinks(Workbook wb, String linkColumn, String targetSheet, String targetColumn) {
		for (int i = 0; i < wb.getNumberOfSheets(); i++){
			String linkSheet = wb.getSheetName(i);
			if (!linkSheet.equalsIgnoreCase(targetSheet))
				setHyperlinks(wb, linkSheet, linkColumn, targetSheet, targetColumn);
		}
	}
	
	public static void setHyperlinks(Workbook wb, String linkSheet, String linkColumn, String targetSheet, String targetColumn) {
		
		int colID1 = ExcelUtilities.getColumnIndex(wb, linkSheet, linkColumn);
		if (colID1 < 0)
			return;
		int colID2 = ExcelUtilities.getColumnIndex(wb, targetSheet, targetColumn);
		if (colID2 < 0)
			return;

		Sheet sheet1 = readSheet(wb, linkSheet);
		Sheet sheet2 = readSheet(wb, targetSheet);
		
		int rows1 = sheet1.getLastRowNum() + 1;
		int rows2 = sheet2.getLastRowNum() + 1;
		
		
		for (int i = 2; i < rows1; i++) {
			Cell cell1 = sheet1.getRow(i).getCell(colID1);
			for (int k = 2; k < rows2; k++) {
				Cell cell2 = sheet2.getRow(k).getCell(colID2);

				if (cell1.toString().equalsIgnoreCase(cell2.toString())) {
					
					Hyperlink link = wb.getCreationHelper().createHyperlink(Hyperlink.LINK_DOCUMENT);
					CellReference cell2Ref = new CellReference(targetSheet, cell2.getRowIndex(), cell2.getColumnIndex(), false, false);
					
					link.setAddress(cell2Ref.formatAsString());
					cell1.setHyperlink(link);
					cell1.setCellStyle(CellStyles.TABLE_DATA_LINK.getStyle(wb));
					break;
				}
			}
			
		}
	}
	
}
