package dbexp.framework.datacollection.excel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExcelTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		ExcelWorkbook xls = new ExcelWorkbook();
		xls.addSheet("Test");
		xls.setTableTitle("Test", "Throughput");
		xls.addColumn("Test", "Run");
		xls.addColumn("Test", "MPL");
		xls.addColumn("Test", "SI");
		xls.addColumn("Test", "SSI");
		xls.addColumn("Test", "S2PL");
		xls.saveToFile("t:\\test.xls");
		xls.addSheet("2", "2");
		xls.addColumn("2", "new");
		List<Integer> values = new ArrayList<Integer>();
		values.add(1);
		values.add(10);
		values.add(100);
		xls.appendColumnData("Test", "mpl", 2, 3, 4, 1.4, "ABC", false, new Date(), "Last");
		xls.appendRowData("test", 1, "two", 3.3, new Date(), true, "LAST");
		xls.appendColumnData("Test", "Si", values);
		xls.appendRowData("test", 1, "two", 3.3, new Date(), true, "LAST");
		values.add(2, 222);
		xls.appendColumnData("Test", "Si", values);
		xls.appendRowData("Test", values);
		xls.saveToFile("t:\\test2.xls");
	}

}
