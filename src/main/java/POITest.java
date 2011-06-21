import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.Test;

public class POITest {

	@Test
	public void testXls() throws InvalidFormatException, IOException {
		InputStream inp = new FileInputStream("/home/feng/Downloads/xls.xls");
		Workbook wb = WorkbookFactory.create(inp);

		int num = 0;

		for (int i = 0; i < wb.getNumberOfSheets(); i++) {
			Sheet sheet = wb.getSheetAt(0);
			for (Row row : sheet) {
				for (Cell cell : row) {
					++num;
				}
			}
		}
		System.out.println(num);
	}

	@Test
	public void testXlsx() throws InvalidFormatException, IOException {
		InputStream inp = new FileInputStream("/home/feng/Downloads/xlsx.xlsx");
		Workbook wb = WorkbookFactory.create(inp);
		int num = 0;
		for (int i = 0; i < wb.getNumberOfSheets(); i++) {
			Sheet sheet = wb.getSheetAt(0);
			for (Row row : sheet) {
				for (Cell cell : row) {
					++num;
				}
			}
		}
		System.out.println(num);
	}

	@Test
	public void testXlsx2() throws InvalidFormatException, IOException {
		InputStream inp = new FileInputStream("/home/feng/Downloads/xlsx.xlsx");
		Workbook wb = WorkbookFactory.create(inp);
		int num = 0;
		for (int i = 0; i < wb.getNumberOfSheets(); i++) {
			Sheet sheet = wb.getSheetAt(0);
			for (Row row : sheet) {
				for (Cell cell : row) {
					++num;
				}
			}
		}
		System.out.println(num);
	}
	
	public static void main(String[] args) throws InvalidFormatException, IOException {
		for (int i = 0; i < 50; i++) {
			new POITest().testXlsx2();
		}
	}
}
