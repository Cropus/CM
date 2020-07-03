package Units;

import java.sql.*;

public class UID {
	static final String driver = "com.mysql.cj.jdbc.Driver";
	static final String url = "jdbc:mysql://localhost:9999/cm?serverTimezone=UTC";
	static final String login = "root";
	static final String password = "root";
	private static int lastID = 0;
	public UID() {
		try {
			Class.forName(driver);
			Connection connection = DriverManager.getConnection(url, login, password);
			String getUID = "SELECT MAX(LastUID) FROM uid";
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(getUID);
			rs.next();
			lastID = rs.getInt(1);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}

	}
	public static int generate() {
		return lastID++;
	}

	public static void save() {
		try {
			Class.forName(driver);
			Connection connection = DriverManager.getConnection(url, login, password);
			String setUID = "INSERT INTO uid (LastUID) VALUES (?)";
			PreparedStatement prepStmt = connection.prepareStatement(setUID);
			prepStmt.setInt(1, lastID++);
			prepStmt.execute();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}

	}
}
