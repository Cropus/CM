package Server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.*;

public class ClientThreadContext implements Runnable {
	private final int currentID = 0;
	private final String driver = "com.mysql.jdbc.Driver";
	private final String url = "jdbc:mysql://localhost:9999/cm";
	private final String login = "root";
	private final String password = "root";
	private Socket clientSocket;
	private InputStream input = null;
	private OutputStream output = null;
	public ClientThreadContext(Socket socket) {
		clientSocket = socket;
	}

	@Override
	public void run() {
		try {
			input = clientSocket.getInputStream();
			output = clientSocket.getOutputStream();
		} catch (IOException e) {
//TODO log
		}

		byte[] buffer = new byte[4096];
		while (true) {
			try {
				int letters = input.read(buffer);
				if (letters > 0) {
					String message = new String(buffer, 0, letters);
					sendData(message.getBytes());
					System.out.println(message);


					Class.forName(driver);
					Connection connection = DriverManager.getConnection(url, login, password);
					String newMessage = "INSERT INTO messages (text, `from`, `to`) VALUES (?, ?, ?)";
					PreparedStatement preparedStmt = connection.prepareStatement(newMessage);
					preparedStmt.setString(1, message);
					preparedStmt.setInt(2, currentID);
					preparedStmt.setInt(3, currentID);
					preparedStmt.execute();
				} else {
//TODO log
					clientSocket.close();
					break;
				}
			} catch (IOException | SQLException | ClassNotFoundException e) {
//TODO log
			}
		}
	}

	public void sendData(byte[] message) {
		try {
			output.write(message);
			output.flush();
		} catch (IOException e) {
//TODO log
		}
	}
}
