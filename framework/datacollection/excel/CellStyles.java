package dbexp.framework.datacollection.excel;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;

public enum CellStyles {
	
	TABLE_TITLE("Cambria", (short)18, Font.BOLDWEIGHT_BOLD),
	TABLE_HEADER("Calibri", (short)12, Font.BOLDWEIGHT_BOLD),
	TABLE_DATA("Calibri", (short)12, Font.BOLDWEIGHT_NORMAL),
	TABLE_DATA_LINK("Calibri", (short)12, Font.BOLDWEIGHT_NORMAL, IndexedColors.BLUE.getIndex()),
	TABLE_DATA_PERCENTAGE("Calibri", (short)12, Font.BOLDWEIGHT_NORMAL, "0.00%");
	
	private String fontName;
	private short fontHeight;
	private short fontBold;
	private short fontColor = IndexedColors.AUTOMATIC.getIndex();
	private String dataFormat = "";

	CellStyle style;
	
	private boolean checkFont(Font font) {
		return (font.getFontName().equalsIgnoreCase(fontName) &&
				font.getFontHeightInPoints() == fontHeight &&
				font.getBoldweight() == fontBold &&
				font.getColor() == fontColor);
	}
	
	private Font createFont(Workbook wb) {
		Font font = wb.createFont();
		font.setBoldweight(fontBold);
		font.setFontName(fontName);
		font.setFontHeightInPoints(fontHeight);
		font.setColor(fontColor);
	
		return font;
	}
	
	public Font getFont(Workbook wb) {
		Font font = null;
		for (short i = 0; i < wb.getNumberOfFonts(); i++) {
			font = wb.getFontAt(i);
			if (checkFont(font))
				return font;
		}
		return createFont(wb);
	}
	
	private CellStyle createStyle(Workbook wb) {
		style = wb.createCellStyle();
		style.setAlignment(CellStyle.ALIGN_LEFT);
		style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		style.setFont(createFont(wb));
		if (!dataFormat.equals(""))
			style.setDataFormat(wb.createDataFormat().getFormat(dataFormat));
		return style;
	}
	
	public CellStyle getStyle(Workbook wb) {
		CellStyle style = null;
		for (short i = 0; i < wb.getNumCellStyles(); i++) {
			style = wb.getCellStyleAt(i);
			Font font =	wb.getFontAt(style.getFontIndex());
			if (checkFont(font))
				return style;
		}
		
		return createStyle(wb);
	}
	
	private CellStyles(String fname, short fsize, short fbold) {
		fontName = fname;
		fontHeight = fsize;
		fontBold = fbold;
		//fontColor = IndexedColors.AUTOMATIC.getIndex();
	}
	
	private CellStyles(String fname, short fsize, short fbold, String dataFmt) {
		fontName = fname;
		fontHeight = fsize;
		fontBold = fbold;
		dataFormat = dataFmt;
		//fontColor = IndexedColors.AUTOMATIC.getIndex();
	}
	
	private CellStyles(String fname, short fsize, short fbold, short fColor) {
		fontName = fname;
		fontHeight = fsize;
		fontBold = fbold;
		fontColor = fColor;
	}
}
