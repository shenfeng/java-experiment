package h2;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class H2DatabaseTest {

	public static void main(String[] args) throws SQLException {
		Connection con = DriverManager
				.getConnection("jdbc:h2:split:nioMapped:/tmp/test");

		PreparedStatement ps = con
				.prepareStatement("create table test (id IDENTITY primary key, data BINARY)");

		ps.execute();

		ps.close();

		ps = con.prepareStatement("insert into test (id, data) values (?, ?)");
		ps.setInt(1, 10);
		ps.setObject(2, "what".getBytes());
		ps.execute();

		ps = con.prepareStatement("select * from test");
		ResultSet rs = ps.executeQuery();

		while (rs.next()) {
			byte[] d = rs.getBytes("data");
			String s = new String(d);
			System.out.println(s);
		}

	}

}
