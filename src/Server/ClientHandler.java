package Server;

import Units.UID;

import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.util.Iterator;

public class ClientHandler implements Runnable {
	String name;
	private Server server;
	private final int currentID = 0;
	private final String driver = "com.mysql.cj.jdbc.Driver";
	private final String url = "jdbc:mysql://localhost:9999/cm?serverTimezone=UTC";
	private final String login = "root";
	private final String password = "root";
	private Socket clientSocket;
	private BufferedReader input = null;
	private PrintWriter output = null;
	public ClientHandler(Socket socket, Server server) {
		this.server = server;
		clientSocket = socket;

		try {
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			output = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
//TODO log
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				name = input.readLine();


				synchronized(server.clients) {
					Iterator<ClientHandler> iter = server.clients.iterator();
					while(iter.hasNext()) {
						ClientHandler cl = iter.next();
						if (cl != null) {
							cl.output.println(name + " cames now");
						}
					}
				}

				String str = "";
				while (true) {
					str = input.readLine();
					if (str.equals("exit")) break;

					Class.forName(driver);
					Connection connection = DriverManager.getConnection(url, login, password);
					String newMessage = "INSERT INTO messages (ID, text, `from`, `to`) VALUES (?, ?, ?, ?)";
					PreparedStatement preparedStmt = connection.prepareStatement(newMessage);
					preparedStmt.setInt(1, UID.generate());
					preparedStmt.setString(2, str);
					preparedStmt.setInt(3, currentID);
					preparedStmt.setInt(4, currentID);
					preparedStmt.execute();

					synchronized (server.clients) {
						Iterator<ClientHandler> iter = server.clients.iterator();
						while (iter.hasNext()) {
							((ClientHandler) iter.next()).output.println(name + ": " + str);
						}
					}
				}

				synchronized(server.clients) {
					Iterator<ClientHandler> iter = server.clients.iterator();
					while(iter.hasNext()) {
						((ClientHandler) iter.next()).output.println(name + " has left");
					}
				}
			} catch (IOException | NullPointerException e) {
//TODO log
				break;
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
