package Server;

import Exceptions.LoginExistsException;
import Units.Credential;
import Units.Message;
import Units.UID;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;

public class ClientHandler implements Runnable {
	private ArrayList<Integer> myChats = new ArrayList<>();
	private String name;
	private Server server;
	private int currentID = 0;
	private final String driver = "com.mysql.cj.jdbc.Driver";
	private final String url = "jdbc:mysql://localhost:9999/cm?serverTimezone=UTC";
	private final String login = "root";
	private final String password = "root";
	private SSLSocket clientSocket;
	private ObjectInputStream input = null;
	private ObjectOutputStream output = null;
	private String str = "";
	public ClientHandler(SSLSocket socket, Server server) {
		this.server = server;
		clientSocket = socket;

		try {
			input = new ObjectInputStream(clientSocket.getInputStream());
			output = new ObjectOutputStream(clientSocket.getOutputStream());
		} catch (IOException e) {
//TODO log
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				Object in = input.readObject();
				if (in instanceof Credential) {
					Credential cred = (Credential) in;
					if (!cred.isReg()) {
						signUp(cred);
					} else {
						signIn(cred);
					}
					name = cred.getLogin();
				} else if (in instanceof Message) {
					Message message = (Message) in;
					message.setID(UID.generate());
					message.setFrom(currentID);
//					synchronized (server.clients) {
//						Iterator<ClientHandler> iter = server.clients.iterator();
//						while (iter.hasNext()) {
//							ClientHandler cl = iter.next();
//							if (cl != null && cl.myChats.contains(message.getTo())) {
//								if (cl != this) {
//									cl.output.writeObject(name + " cames now");
//									cl.output.flush();
//								} else {
//									cl.output.writeObject("Welcome, " + name);
//									cl.output.flush();
//								}
//							}
//						}
//					}

					while (true) {
						str = message.getText();
						if (str.equals("exit")) break;

						Class.forName(driver);
						Connection connection = DriverManager.getConnection(url, login, password);
						String newMessage = "INSERT INTO messages (ID, text, `from`, `to`) VALUES (?, ?, ?, ?)";
						PreparedStatement preparedStmt = connection.prepareStatement(newMessage);
						preparedStmt.setInt(1, message.getID());
						preparedStmt.setString(2, str);
						preparedStmt.setInt(3, currentID);
						preparedStmt.setInt(4, message.getTo());
						preparedStmt.execute();

						String connectMessageToChat = "INSERT INTO chatsandmessages (ChatID, MessageID) VALUES (?, ?)";
						PreparedStatement connectMtoC = connection.prepareStatement(connectMessageToChat);
						connectMtoC.setInt(1, message.getTo());
						connectMtoC.setInt(2, message.getID());
						connectMtoC.execute();

						synchronized (server.clients) {
							Iterator<ClientHandler> iter = server.clients.iterator();
							while (iter.hasNext()) {
								ClientHandler cl = iter.next();
								if (cl != null && cl.myChats.contains(message.getTo())) {
									if (cl != this) {
										cl.output.writeObject(name + ": " + str);
										cl.output.flush();
									}
								}
							}
						}

						message = (Message) input.readObject();
						message.setID(UID.generate());
					}

					synchronized (server.clients) {
						Iterator<ClientHandler> iter = server.clients.iterator();
						while (iter.hasNext()) {
							ClientHandler cl = iter.next();
							if (cl != null && cl.myChats.contains(message.getTo())) {
								if (cl != this) {
									cl.output.writeObject(name + " has left");
									cl.output.flush();
								}
							}
						}
					}
				} else throw new IllegalArgumentException();
			} catch (IOException | NullPointerException e) {
//TODO log
				break;
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			}
		}
	}

	void signIn(Credential credential) {
		try {
			Class.forName(driver);
			Connection connection = DriverManager.getConnection(url, login, password);
			String checkCredential = "SELECT * FROM users WHERE Hash = ?";
			PreparedStatement stmt = connection.prepareStatement(checkCredential);
			stmt.setInt(1, credential.getHash());
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				currentID = rs.getInt(1);
				String getChats = "SELECT * FROM chatsandusers WHERE UserID = ?";
				PreparedStatement chatStmt = connection.prepareStatement(getChats);
				chatStmt.setInt(1, currentID);
				ResultSet chats = chatStmt.executeQuery();
				while (chats.next()) {
					myChats.add(chats.getInt(1));
				}
				output.writeObject("You are logged in!!!");
				output.flush();
			} else {
				str = "exit";
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void signUp(Credential credential) {
		try {
			Class.forName(driver);
			Connection connection = DriverManager.getConnection(url, login, password);
			String checkLogin = "SELECT * FROM users WHERE Hash = ?";
			PreparedStatement stmt = connection.prepareStatement(checkLogin);
			stmt.setInt(1, credential.getHash());
			ResultSet rs = stmt.executeQuery();
			if (!rs.next()) {
				String newUser = "INSERT INTO users (ID, Login, Password, Hash) VALUES (?, ?, ?, ?)";
				PreparedStatement userStmt = connection.prepareStatement(newUser);
				currentID = UID.generate();
				userStmt.setInt(1, currentID);
				userStmt.setString(2, credential.getLogin());
				userStmt.setString(3, credential.getPassword());
				userStmt.setInt(4, credential.getHash());
				userStmt.execute();
				String addToGeneral = "INSERT INTO chatsandusers (ChatID, UserID) VALUES (?, ?)";
				PreparedStatement generalStmt = connection.prepareStatement(addToGeneral);
				generalStmt.setInt(1, 0);
				generalStmt.setInt(2, currentID);
				generalStmt.execute();
				myChats.add(0);
				output.writeObject("Registration complete");
				output.flush();
			} else throw new LoginExistsException();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		} catch (LoginExistsException e) {
//TODO log
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
