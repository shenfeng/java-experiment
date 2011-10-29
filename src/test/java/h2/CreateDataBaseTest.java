package h2;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import org.junit.Test;

import util.RandBytes;
import util.StringUtil;

public class CreateDataBaseTest {

	private static final int QUERY_SIZE = 2000;

	private static final int SIZE = 10000 * 50;

	static final String PATH = "/home/feng/h2/test";
	static final String PATH_SIZE = PATH + ";PAGE_SIZE=8192";

	static final String URL = "jdbc:h2:" + PATH;
	static final String URL_NIO = "jdbc:h2:nio:" + PATH;
	static final String URL_NIOMAPPED = "jdbc:h2:split:nioMapped:" + PATH;

	static final String URL_SIZE = "jdbc:h2:" + PATH_SIZE;
	static final String URL_NIO_SIZE = "jdbc:h2:nio:" + PATH_SIZE;
	static final String URL_NIOMAPPED_SIZE = "jdbc:h2:split:nioMapped:"
			+ PATH_SIZE;

	static final String CREATE = "create table test (id IDENTITY primary key, "
			+ "url varchar, " + "data BINARY)";
	static final String INSERT = "insert into test (url, data) values (?, ?)";

	static final RandBytes rb = new RandBytes();

	static String DB_URL = URL_NIOMAPPED;
	static Random r = new Random();

	public void createAndInsert(String url) throws SQLException {

		File f = new File(PATH + ".h2.db");
		for (File file : f.getParentFile().listFiles()) {
			if (file.getName().startsWith("test.h2")) {
				// System.out.println("delete " + file);
				file.delete();
			}
		}

		Connection con = DriverManager.getConnection(url);
		Statement stat = con.createStatement();
		stat.execute(CREATE);
		stat.close();

		PreparedStatement insert = con.prepareStatement(INSERT);
		for (int i = 0; i < SIZE; ++i) {
			insert.clearParameters();
			insert.setString(1, StringUtil.getRandomString(200));
			insert.setBytes(2, rb.get(r.nextInt(6) + 2));
			insert.execute();
		}

		// select
		long start = System.currentTimeMillis();
		PreparedStatement select = con
				.prepareStatement("select * from test where id = ? ");
		int count = 0;
		for (int i = 0; i < QUERY_SIZE; ++i) {
			select.clearBatch();
			select.setInt(1, r.nextInt(SIZE));
			ResultSet rs = select.executeQuery();
			while (rs.next()) {
				count++;
				byte[] bytes = rs.getBytes("data");
			}
		}
		System.out.printf("%10dms, select %d \n", System.currentTimeMillis()
				- start, count);

		// delete
		start = System.currentTimeMillis();
		PreparedStatement delete = con
				.prepareStatement("delete from test where id = ? ");
		count = 0;
		for (int i = 0; i < QUERY_SIZE; ++i) {
			delete.clearBatch();
			delete.setInt(1, r.nextInt(SIZE));
			if (select.execute()) {
				count++;
			}
		}
		System.out.printf("%10dms, delete %d \n", System.currentTimeMillis()
				- start, count);

		// update
		start = System.currentTimeMillis();
		PreparedStatement update = con
				.prepareStatement("update test set data = ? where id = ?");
		count = 0;
		for (int i = 0; i < QUERY_SIZE; ++i) {
			update.clearBatch();
			update.setBytes(1, rb.get(4));
			update.setInt(2, r.nextInt(SIZE));
			if (select.execute()) {
				count++;
			}
		}
		System.out.printf("%10dms, update %d \n", System.currentTimeMillis()
				- start, count);

		// fail insert
		insert = con
				.prepareStatement("insert into test (id, url, data) values (?, ?, ?)");
		start = System.currentTimeMillis();
		count = 0;
		for (int i = 0; i < QUERY_SIZE; ++i) {
			insert.clearBatch();
			insert.setInt(1, r.nextInt(SIZE));
			insert.setString(2, StringUtil.getRandomString(200));
			insert.setBytes(3, rb.get(4));
			try {
				insert.execute();
			} catch (Exception e) {
				count++;
			}
		}
		System.out.printf("%10dms, fail insert %d \n",
				System.currentTimeMillis() - start, count);

		con.close();
	}

	@Test
	public void testPerf() throws SQLException {
		String[] urls = new String[] { URL, URL_NIO, URL_NIOMAPPED, URL_SIZE,
				URL_NIO_SIZE, URL_NIOMAPPED_SIZE, URL, URL_NIO, URL_NIOMAPPED,
				URL_SIZE, URL_NIO_SIZE, URL_NIOMAPPED_SIZE };
		for (String url : urls) {
			long start = System.currentTimeMillis();
			System.out.println(url);
			createAndInsert(url);
			System.out.printf("%10dms\n", (System.currentTimeMillis() - start));
			System.out.println("-------------------------------------\n");
		}

	}
}
