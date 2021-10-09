package dbexp.framework.datacollection;

import java.io.File;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
 * File naming scheme
 * <BaseFileName><DateTimeStamp><DataType>.csv
 */

public class FileNameGenerator {
	public static final String SEPARATOR = ", ";
	private static String outputPath = "output";
	private static String timeStamp = "";
	private static Format DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd" + SEPARATOR + "HH.mm");
	
	static {
		File dir = new File(outputPath);
		if (!dir.exists())
			dir.mkdir();
		
		timeStamp = DATE_FORMATTER.format(new Date());
	}
	
	public static String newName() {
		return outputPath + File.separator + timeStamp;
	}

	public static String newName(String base) {
		File dir = new File(outputPath + File.separator + base);
		if (!dir.exists())
			dir.mkdir();
		return dir + File.separator + base + SEPARATOR + timeStamp;
	}
	
	public static String formatName(String format, Object...args) {
		return String.format(format, args);
	}
	
}
